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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import de.keybird.beagle.api.PageState;
import de.keybird.beagle.api.Page;
import de.keybird.beagle.jobs.JobContext;
import de.keybird.beagle.jobs.persistence.SyncJobEntity;
import de.keybird.beagle.repository.PageRepository;

// Sync database with filesystem
@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SyncJobExecution extends AbstractJobExecution<Void, SyncJobEntity> {

    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private JobContext context;

    @PostConstruct
    public String getDescription() {
        return "Archiving indexed pages";
    }

    @Override
    protected Void executeInternal() {
        logItem("Archiving pages ...");
        final Path archivePath = context.getArchivePath();
        final List<Page> archiveList = pageRepository.findByState(PageState.Indexed);
        logItem("Found {} index no of pages to archive", archiveList.size());

        archiveList.forEach(page -> {
            logItem("Archiving page '{}/{}'", page.getName(), page.getName());

            final Path path = archivePath.resolve(page.getName());
            try {
                Files.write(path, page.getPayload());
            } catch (IOException e) {
                logPage(page, PageState.Error, e.getMessage());
            }
        });

        pageRepository.save(archiveList);
        return null;
    }
}
