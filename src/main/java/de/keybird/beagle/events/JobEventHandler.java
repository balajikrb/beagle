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

import de.keybird.beagle.api.ImportState;
import de.keybird.beagle.jobs.DetectJob;
import de.keybird.beagle.jobs.ImportJob;
import de.keybird.beagle.jobs.IndexJob;
import de.keybird.beagle.jobs.JobFactory;
import de.keybird.beagle.jobs.JobManager;
import de.keybird.beagle.jobs.JobState;
import de.keybird.beagle.repository.ImportRepository;

@Component
@Scope("singleton")
public class JobEventHandler {

    private static final Logger LOG = LoggerFactory.getLogger(JobEventHandler.class);

    @Autowired
    private JobFactory jobFactory;

    @Autowired
    private JobManager jobManager;

    @Autowired
    private ImportRepository importRepository;

    @Subscribe
    public void onJobStarted(JobStartedEvent event) {
        LOG.info("Job of type {} started", event.getSource().getClass().getSimpleName());
    }

    @Subscribe
    public void onJobFinished(JobFinishedEvent event) {
        LOG.info("Execution of {} completed {}", event.getSource().getClass().getSimpleName(), event.isFailed() ? "with error" : "successful");
        if (event.isFailed()) {
            LOG.error("Reason: {}", event.getException().getMessage(), event.getException());
        }
        if (event.isSuccess()) {
            // Kick of import of documents
            if (event.getSource() instanceof DetectJob) {
                // TODO MVR use service for this?
                importRepository
                        .findByState(ImportState.New)
                        .forEach(theImport -> jobManager.submit(jobFactory.createImportJob(theImport)));
            }
            // After import kick of indexing, when no import job is running anymore
            if (event.getSource() instanceof ImportJob) {
                boolean noImportJobsRunningAnymore = jobManager.getJobs(ImportJob.class, JobState.Pending, JobState.Initializing, JobState.Running).isEmpty();
                if (noImportJobsRunningAnymore && jobManager.getJobs(IndexJob.class).isEmpty()) {
                    jobManager.submit(jobFactory.createIndexJob());
                }
            }
        }
    }

    @Subscribe
    public void onJobSubmitted(JobSubmittedEvent event) {
        LOG.info("Job of type {} submitted", event.getSource().getClass().getSimpleName());
    }

}
