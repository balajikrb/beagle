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

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Provider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.keybird.beagle.api.Import;

@Service
public class JobFactory {

    private static AtomicInteger counter = new AtomicInteger(1);

    @Autowired
    private Provider<ImportJob> importJobProvider;

    @Autowired
    private Provider<IndexJob> indexJobProvider;

    @Autowired
    private Provider<DetectJob> detectJobProvider;

    public DetectJob createDetectJob() {
        return applyId(detectJobProvider.get());
    }

    public ImportJob createImportJob(Import theImport) {
        Objects.requireNonNull(theImport);
        ImportJob importJob = importJobProvider.get();
        importJob.setImport(theImport);
        applyId(importJob);
        return importJob;
    }

    public IndexJob createIndexJob() {
        return applyId(indexJobProvider.get());
    }

    private <T extends AbstractJob> T applyId(T job) {
        job.setId(counter.getAndIncrement());
        return job;
    }

}
