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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Named;

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
import de.keybird.beagle.events.JobExecutionStartedEvent;
import de.keybird.beagle.events.JobExecutionSubmittedEvent;
import de.keybird.beagle.jobs.execution.JobExecutionContext;
import de.keybird.beagle.jobs.execution.JobRunner;
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

    private ExecutorService executorService;

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

    public CompletableFuture submit(final JobRunner jobRunner) {
        jobRunnerList.add(jobRunner);
        eventBus.post(new JobExecutionSubmittedEvent(jobRunner.getContext()));
        jobService.save(jobRunner.getContext().getJobEntity());

        // Make the job execution completable
        final CompletableFuture completableFuture = new CompletableFuture();

        // First handle error/success
        completableFuture.handle((result, exception) -> {
            try {
                // Send event
                final JobResult jobResult = new JobResult(result, (Throwable) exception);
                eventBus.post(new JobExecutionFinishedEvent(jobRunner.getContext(), jobResult));
                return jobResult;
            } finally {
                // Remove job from list
                jobRunnerList.remove(jobRunner);
            }
        });

        // run it
        completableFuture.runAsync(() -> {
            try {
                Thread.currentThread().setName(getClass().getName() + " - " + jobRunner.getContext().getJobEntity().getId());
                eventBus.post(new JobExecutionStartedEvent(jobRunner.getContext()));
                transactionTemplate.execute((status) -> {
                    jobRunner.execute();
                    return null;
                });
                completableFuture.complete(null);
            } catch (Throwable t) {
                completableFuture.completeExceptionally(t);
            }
        }, executorService);

        return completableFuture;
    }

    public boolean hasRunningJobs() {
        return !jobRunnerList.isEmpty();
    }

    public List<JobExecutionContext> getExecutions(JobType... types) {
        if (types == null || types.length == 0) {
            return jobRunnerList.stream().map(runner -> runner.getContext()).collect(Collectors.toList());
        }
        final List<JobType> typeList = Arrays.asList(types);
        return jobRunnerList.stream().filter(runner -> typeList.contains(runner.getContext().getJobEntity().getType()))
                .filter(execution -> !Lists.newArrayList(JobState.Completed).contains(execution.getContext().getJobEntity().getState()))
                .map(execution -> execution.getContext())
                .collect(Collectors.toList());
    }

    public void awaitTermination(long timeout, TimeUnit timeUnit) throws InterruptedException {
        executorService.awaitTermination(timeout, timeUnit);
    }
}
