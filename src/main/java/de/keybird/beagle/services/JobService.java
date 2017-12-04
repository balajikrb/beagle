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

package de.keybird.beagle.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.google.common.eventbus.Subscribe;

import de.keybird.beagle.events.JobEvent;
import de.keybird.beagle.jobs.JobInfo;
import de.keybird.beagle.jobs.JobManager;
import de.keybird.beagle.jobs.JobState;
import de.keybird.beagle.rest.JobInfoDTO;

@Service
public class JobService {

    @Autowired
    private SimpMessagingTemplate messageTemplate;

    @Autowired
    private JobManager jobManager;

    @Subscribe
    // TODO MVR this is okay, but one thing is missing -> we have to fetch jobs once.
    // Otherwise, we only see jobs when they are created AFTER we have connected.
    // We may miss ones already kicked off by another user
    public void onJobChange(JobEvent event) throws Exception {
        final List<JobInfo> jobs = jobManager.getJobs();

        // Return all jobs except success ones.
        // They are only returned, if they finished within the last n seconds
        final List<JobInfo> jobData = jobs.stream()
                .filter(job -> {
                    if (job.getState() == JobState.Success) {
                        long completedSinceMs = System.currentTimeMillis() - job.getCompleteTime().getTime();
                        return completedSinceMs <= 8 * 1000;
                    }
                    return true;
                }).map(job -> new JobInfoDTO(job)).collect(Collectors.toList());
        messageTemplate.convertAndSend("/topic/jobs", jobData);
    }
}