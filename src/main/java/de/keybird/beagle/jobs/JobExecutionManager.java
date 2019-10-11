/**
 * This file is part of Beagle.
 * Copyright (c) 2017 Markus von Rüden.
 *
 * Beagle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beagle is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Beagle. If not, see http://www.gnu.org/licenses/.
 */

package de.keybird.beagle.jobs;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Named;
import javax.inject.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import de.keybird.beagle.events.JobExecutionFinishedEvent;
import de.keybird.beagle.jobs.execution.JobExecution;
import de.keybird.beagle.jobs.execution.JobExecutionInfo;
import de.keybird.beagle.jobs.execution.JobRunner;
import de.keybird.beagle.jobs.persistence.JobEntity;
import de.keybird.beagle.jobs.persistence.JobState;
import de.keybird.beagle.jobs.persistence.JobType;
import de.keybird.beagle.services.JobService;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class JobExecutionManager {

    private static Logger LOG = LoggerFactory.getLogger(JobExecutionManager.class);

    @Autowired
    private EventBus eventBus;

    @Autowired
    private JobService jobService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    @Named("poolSize")
    private int poolSize;

    @Autowired
    private JobExecutionFactory jobExecutionFactory;

    @Autowired
    private Provider<JobRunner> jobRunnerProvider;

    private ExecutorService executorService;

    private final List<JobEntity> pendingJobs = new CopyOnWriteArrayList<>();

    private final List<JobRunner> jobRunnerList = new CopyOnWriteArrayList<>();

    @PostConstruct
    public void init() {
        // Initialize execution Manager
        LOG.info("Pool Size of JobExecutionManager is: {}", poolSize);
        executorService = Executors.newFixedThreadPool(
                poolSize,
                new ThreadFactoryBuilder()
                        .setNameFormat("Job-%d")
                        .setDaemon(false)
                        .build());
    }

    @PreDestroy
    public void shutdown() {
        LOG.info("SHUTTING DOWN.");
        if (!executorService.isShutdown()) {
            executorService.shutdown();
            // TODO MVR should we wait for tasks to finish before dying? We probably should...
        }
    }

    public <T extends JobEntity> CompletableFuture submit(final T jobEntity, final JobExecution<T> jobExecution) {
        return this.submit(jobEntity, () -> jobExecution);
    }

    public <T extends JobEntity> CompletableFuture submit(final T jobEntity) {
        return submit(jobEntity, () -> jobExecutionFactory.getJobExecution(jobEntity));
    }

    // TODO MVR this is not very clean, but we leave it for now
    public <T extends JobEntity> CompletableFuture submit(final T jobEntity, final Supplier<JobExecution<T>> executionSupplier) {
        jobService.save(jobEntity);
        pendingJobs.add(jobEntity);

        // Make the job execution completable
        final CompletableFuture completableFuture = new CompletableFuture();

        // First handle error/success
        completableFuture.handle((result, exception) -> {
            // Send event
            final JobResult jobResult = new JobResult(result, (Throwable) exception);
            eventBus.post(new JobExecutionFinishedEvent(jobEntity, jobResult));
            return jobResult;
        });

        // run it
        completableFuture.runAsync(() -> {
            final JobRunner jobRunner = Objects.requireNonNull(jobRunnerProvider.get());
            final JobExecution jobExecution = Objects.requireNonNull(executionSupplier.get());

            try {
                jobRunnerList.add(jobRunner);
                pendingJobs.remove(jobEntity);

                transactionTemplate.execute((status) -> {
                    jobRunner.execute(jobEntity, jobExecution);
                    return null;
                });
                completableFuture.complete(null);
            } catch (Throwable t) {
                completableFuture.completeExceptionally(t);
            } finally {
                // Remove job from list
                jobRunnerList.remove(jobRunner);
            }
        }, executorService);

        return completableFuture;
    }

    public boolean hasRunningJobs() {
        return !jobRunnerList.isEmpty();
    }

    public List<JobExecutionInfo> getExecutions(JobType... types) {
        final List<JobExecutionInfo> runningInfo = getRunningInfo(types);
        runningInfo.addAll(getPendingInfo());
        return runningInfo;
    }

    public List<JobExecutionInfo> getRunningInfo(JobType... types) {
        if (types == null || types.length == 0) {
            return jobRunnerList.stream().filter(context -> context.getJobEntity() != null).collect(Collectors.toList());
        }
        final List<JobType> typeList = Arrays.asList(types);
        return jobRunnerList.stream().filter(runner -> runner.getJobEntity() != null && typeList.contains(runner.getJobEntity().getType()))
                .filter(execution -> !Lists.newArrayList(JobState.Completed).contains(execution.getJobEntity().getState()))
                .collect(Collectors.toList());
    }

    private List<JobExecutionInfo> getPendingInfo() {
        return pendingJobs.stream().map(job -> new JobExecutionInfo() {
            @Override
            public JobEntity getJobEntity() {
                return job;
            }

            @Override
            public Progress getProgress() {
                final Progress progress = new Progress();
                progress.setIndeterminate(true);
                return progress;
            }
        }).collect(Collectors.toList());
    }


    public void awaitTermination(long timeout, TimeUnit timeUnit) throws InterruptedException {
        executorService.awaitTermination(timeout, timeUnit);
    }
}
