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

package de.keybird.beagle.jobs.execution;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import de.keybird.beagle.jobs.persistence.DetectJobEntity;
import de.keybird.beagle.repository.DocumentRepository;
import de.keybird.beagle.repository.JobRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(
    properties = {
        "working.directory=target/beagle-home"
    }
)
public class DetectJobExecutionTest {

    @Autowired
    private DetectJobExecution detectJobExecution;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private JobRepository jobRepository;

    @Test
    public void verifyJobExecution() {
        Assert.assertEquals(0, documentRepository.count());
        Assert.assertEquals(0, jobRepository.count());

        detectJobExecution.setJobEntity(new DetectJobEntity());
        detectJobExecution.execute();

        Assert.assertEquals(0, documentRepository.count());
        Assert.assertEquals(1, jobRepository.count());
    }
}