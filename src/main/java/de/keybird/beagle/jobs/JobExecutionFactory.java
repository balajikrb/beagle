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

import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Provider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.keybird.beagle.api.Document;
import de.keybird.beagle.jobs.execution.DetectJobExecution;
import de.keybird.beagle.jobs.execution.ImportJobExecution;
import de.keybird.beagle.jobs.execution.IndexJobExecution;
import de.keybird.beagle.jobs.persistence.DetectJobEntity;
import de.keybird.beagle.jobs.persistence.ImportJobEntity;
import de.keybird.beagle.jobs.persistence.IndexJobEntity;

@Service
public class JobExecutionFactory {

    private static AtomicInteger counter = new AtomicInteger(1);

    @Autowired
    private Provider<DetectJobExecution> detectJobExecutionProvider;

    @Autowired
    private Provider<ImportJobExecution> importJobExecutionProvider;

    @Autowired
    private Provider<IndexJobExecution> indexJobExecutionProvider;

    public DetectJobExecution createDetectJob() {
        DetectJobExecution execution = detectJobExecutionProvider.get();
        execution.setJobEntity(new DetectJobEntity());
        return execution;
    }

    public IndexJobExecution createIndexJob() {
        final IndexJobExecution indexJobExecution = indexJobExecutionProvider.get();
        indexJobExecution.setJobEntity(new IndexJobEntity());
        return indexJobExecution;
    }

    public ImportJobExecution createImportJob(Document document) {
        final ImportJobExecution importJobExecution = importJobExecutionProvider.get();
        importJobExecution.setJobEntity(new ImportJobEntity(document));
        return importJobExecution;
    }

}
