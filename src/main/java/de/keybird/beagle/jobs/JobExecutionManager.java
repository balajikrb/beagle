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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import de.keybird.beagle.events.JobExecutionFinishedEvent;
import de.keybird.beagle.events.JobExecutionStartedEvent;
import de.keybird.beagle.events.JobExecutionSubmittedEvent;
import de.keybird.beagle.jobs.execution.AbstractJobExecution;
import de.keybird.beagle.repository.JobRepository;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class JobExecutionManager {

    private static Logger LOG = LoggerFactory.getLogger(JobExecutionManager.class);

    @Autowired
    private EventBus eventBus;

    @Value("${jobmanager.pool.size:5}")
    private int poolSize;

    @Autowired
    private JobRepository jobRepository;

    private ExecutorService executorService;

    private List<AbstractJobExecution> jobExecutionList = new CopyOnWriteArrayList<>();

    @PostConstruct
    public void init() {
        LOG.info("INIT");
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
        }
    }

    // TODO MVR think about maybe persisting jobs to some kind of table
    public void submit(final AbstractJobExecution jobExecution) {
        jobExecutionList.add(jobExecution);
        eventBus.post(new JobExecutionSubmittedEvent(jobExecution));
        jobRepository.save(jobExecution.getJobEntity());

        // Make the job execution completable
        final CompletableFuture completableFuture = new CompletableFuture();

        // First handle error/success
        completableFuture.handle((result, exception) -> {
            // TODO MVR we don't remove it for now, as we use it to show in the UI
            eventBus.post(new JobExecutionFinishedEvent(jobExecution, new JobResult(result, (Throwable) exception)));
            return result;
        });

        // run it
        completableFuture.runAsync(() -> {
            Thread.currentThread().setName(getClass().getName() + " - " + jobExecution.getJobEntity().getId());
            eventBus.post(new JobExecutionStartedEvent(jobExecution));
            jobExecution.execute();
            completableFuture.complete(null);
        }, executorService)
        .thenRun(() -> jobExecutionList.remove(jobExecution)); // Remove job
    }

    public boolean hasRunningJobs() {
        return !jobExecutionList.isEmpty();
    }

    public List<AbstractJobExecution> getExecutions(Class<? extends AbstractJobExecution>... types) {
        if (types == null || types.length == 0) {
            return new ArrayList<>(jobExecutionList);
        }
        return new ArrayList<>(
                jobExecutionList
                        .stream()
                        .filter(execution -> Arrays.asList(types).stream().filter(t -> t.isAssignableFrom(execution.getClass())).findAny().isPresent())
                        .filter(execution -> !Lists.newArrayList(JobState.Completed).contains(execution.getJobEntity().getState()))
                        .collect(Collectors.toSet())
        );
    }
}
