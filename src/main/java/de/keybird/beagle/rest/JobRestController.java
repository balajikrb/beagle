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

package de.keybird.beagle.rest;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import de.keybird.beagle.api.ImportState;
import de.keybird.beagle.jobs.JobFactory;
import de.keybird.beagle.jobs.JobInfo;
import de.keybird.beagle.jobs.JobManager;
import de.keybird.beagle.repository.ImportRepository;

@RestController
@RequestMapping("/jobs")
public class JobRestController {

    @Autowired
    private JobManager jobManager;

    @Autowired
    private JobFactory jobFactory;

    @Autowired
    private ImportRepository importRepository;

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<JobInfoDTO>> showProgress() {
        final List<JobInfo> jobs = jobManager.getJobs();
        if (jobs.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        final List<JobInfoDTO> jobDTOList = jobs.stream().map(jobInfo -> new JobInfoDTO(jobInfo)).collect(Collectors.toList());
        jobDTOList.sort(Comparator.comparing(JobInfoDTO::getId).reversed());
        return new ResponseEntity<>(jobDTOList, HttpStatus.OK);
    }

    @RequestMapping(path="/detect", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity startDetect() {
        if (!jobManager.hasRunningJobs()) {
            jobManager.submit(jobFactory.createDetectJob());
        }
        return ResponseEntity.accepted().build();
    }

    @RequestMapping(path="/import", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity startImport() {
        if (!jobManager.hasRunningJobs()) {
            // TODO MVR use service for this?
            importRepository
                .findByState(ImportState.New)
                .forEach(theImport -> jobManager.submit(jobFactory.createImportJob(theImport)));
        }
        return ResponseEntity.accepted().build();
    }

    @RequestMapping(path="/index", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity startIndex() {
        if (!jobManager.hasRunningJobs()) {
            jobManager.submit(jobFactory.createIndexJob());
        }
        return ResponseEntity.accepted().build();
    }
}
