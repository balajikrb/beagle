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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import de.keybird.beagle.api.Profile;
import de.keybird.beagle.api.ProfileState;
import de.keybird.beagle.repository.ProfileRepository;

// Sync database with filesystem
@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SyncJob extends AbstractJob<Void> {

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private JobContext context;

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(context.getInboxPath());
        setDescription("Archiving persisted files");
    }

    @Override
    protected Void executeInternal() {
        final Path archivePath = context.getArchivePath();
        final List<Profile> filesImported = profileRepository.findByState(ProfileState.Indexed);

        filesImported.forEach(file -> {
            final Path path = archivePath.resolve(file.getName());
            try {
                Files.write(path, file.getPayload());
                file.setState(ProfileState.Synced);
            } catch (IOException e) {
                file.setErrorMessage(e.getMessage());
            }
        });

        profileRepository.save(filesImported);
        return null;
    }
}
