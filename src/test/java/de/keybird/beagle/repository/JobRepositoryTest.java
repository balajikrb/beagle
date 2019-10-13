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

package de.keybird.beagle.repository;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import de.keybird.beagle.BeagleTest;
import de.keybird.beagle.jobs.persistence.DetectJobEntity;
import de.keybird.beagle.jobs.persistence.LogEntity;
import de.keybird.beagle.jobs.xxxx.LogLevel;

@BeagleTest
@RunWith(SpringRunner.class)
public class JobRepositoryTest {

    @Autowired
    private JobRepository jobRepository;

    @Test
    public void verifyPersistSimple() {
        final DetectJobEntity jobEntity = new DetectJobEntity();
        final DetectJobEntity save = jobRepository.save(jobEntity);
        assertThat(save.getId(), is(not(nullValue())));
    }

    @Test
    public void verifyPersistWithLogs() {
        final DetectJobEntity jobEntity = new DetectJobEntity();
        final LogEntity logEntity = new LogEntity();
        logEntity.setLevel(LogLevel.Info);
        logEntity.setMessage("Wiu wiu wiu");
        jobEntity.getLogs().add(logEntity);

        final DetectJobEntity save = jobRepository.save(jobEntity);
        assertThat(save.getId(), is(not(nullValue())));
        assertThat(save.getLogs(), hasSize(1));
    }
}