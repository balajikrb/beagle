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

import de.keybird.beagle.jobs.persistence.JobType;
import de.keybird.beagle.services.JobService;

@Component
@Scope("singleton")
public class JobExecutionEventHandler {

    private static final Logger LOG = LoggerFactory.getLogger(JobExecutionEventHandler.class);

    @Autowired
    private JobService jobService;

    @Subscribe
    public void onJobSubmitted(JobExecutionSubmittedEvent event) {
        LOG.info("Job of type {} submitted", event.getSource().getJobEntity().getType());
    }

    @Subscribe
    public void onJobStarted(JobExecutionStartedEvent event) {
        LOG.info("Job of type {} started", event.getSource().getJobEntity().getType());
    }

    @Subscribe
    public void onJobFinished(JobExecutionFinishedEvent event) {
        LOG.info("Execution of {} job completed {}", event.getSource().getJobEntity().getType(), event.isFailed() ? "with error" : "successful");
        if (event.isFailed()) {
            LOG.error("Reason: {}", event.getException().getMessage(), event.getException());
        }
        if (event.isSuccess()) {
            // Kick of import of documents
            if (event.getSource().getJobEntity().getType() == JobType.Detect) {
                jobService.importDocuments();
            }
            jobService.indexPagesIfNecessary();
        }
    }
}
