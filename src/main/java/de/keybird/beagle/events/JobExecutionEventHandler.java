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

package de.keybird.beagle.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.eventbus.Subscribe;

import de.keybird.beagle.api.DocumentState;
import de.keybird.beagle.jobs.JobExecutionFactory;
import de.keybird.beagle.jobs.JobExecutionManager;
import de.keybird.beagle.jobs.execution.DetectJobExecution;
import de.keybird.beagle.jobs.execution.ImportJobExecution;
import de.keybird.beagle.jobs.execution.IndexJobExecution;
import de.keybird.beagle.repository.DocumentRepository;

@Component
@Scope("singleton")
public class JobExecutionEventHandler {

    private static final Logger LOG = LoggerFactory.getLogger(JobExecutionEventHandler.class);

    @Autowired
    private JobExecutionFactory jobFactory;

    @Autowired
    private JobExecutionManager jobManager;

    @Autowired
    private DocumentRepository documentRepository;

    @Subscribe
    public void onJobStarted(JobExecutionStartedEvent event) {
        LOG.info("Job of type {} started", event.getSource().getClass().getSimpleName());
    }

    @Subscribe
    public void onJobFinished(JobExecutionFinishedEvent event) {
        LOG.info("Execution of {} completed {}", event.getSource().getClass().getSimpleName(), event.isFailed() ? "with error" : "successful");
        if (event.isFailed()) {
            LOG.error("Reason: {}", event.getException().getMessage(), event.getException());
        }
        if (event.isSuccess()) {
            // Kick of import of documents
            if (event.getSource() instanceof DetectJobExecution) {
                // TODO MVR use service for this?
                documentRepository
                        .findByState(DocumentState.New)
                        .forEach(theImport -> jobManager.submit(jobFactory.createImportJob(theImport)));
            }
            // After import kick of indexing, when no import job is running anymore
            if (event.getSource() instanceof ImportJobExecution) {
                boolean noImportJobsRunningAnymore = jobManager.getExecutions(ImportJobExecution.class).isEmpty();
                if (noImportJobsRunningAnymore && jobManager.getExecutions(IndexJobExecution.class).isEmpty()) {
                    jobManager.submit(jobFactory.createIndexJob());
                }
            }
        }
    }

    @Subscribe
    public void onJobSubmitted(JobExecutionSubmittedEvent event) {
        LOG.info("Job of type {} submitted", event.getSource().getClass().getSimpleName());
    }

}
