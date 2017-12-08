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

import java.nio.file.Path;
import java.nio.file.Paths;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.google.common.eventbus.EventBus;

import de.keybird.beagle.repository.JobRepository;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class JobContext {
    @Value("${working.directory:~/.beagle}")
    private String workingDirectory;

    @Autowired
    private EventBus eventBus;

    @Autowired
    private JobRepository jobRepository;

    private Path workingPath;
    private Path inboxPath;
    private Path archivePath;

    @PostConstruct
    public void init() {
        workingDirectory = workingDirectory.replaceAll("~", System.getProperty("user.home"));
        this.workingPath = Paths.get(workingDirectory);
        this.inboxPath = workingPath.resolve("1_inbox");
        this.archivePath = workingPath.resolve("2_archive"); // TODO MVR rip out
    }

    public Path getInboxPath() {
        return inboxPath;
    }

    public Path getArchivePath() {
        return archivePath;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public JobRepository getJobRepository() {
        return jobRepository;
    }
}
