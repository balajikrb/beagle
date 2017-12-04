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
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;

import de.keybird.beagle.api.Profile;
import de.keybird.beagle.api.ProfileState;
import de.keybird.beagle.repository.ProfileRepository;
import io.searchbox.client.JestClient;
import io.searchbox.core.DocumentResult;
import io.searchbox.core.Index;

// Syncs database content with elastic
@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class IndexJob extends AbstractJob<Void> {

    private static final Logger LOG = LoggerFactory.getLogger(IndexJob.class);

    @Autowired
    private JestClient client;

    @Autowired
    private ProfileRepository profileRepository;

    @PostConstruct
    public void init() {
        setDescription("Indexing new files");
    }

    @Override
    // TODO MVR at the moment nothing is indexed
    protected Void executeInternal() throws ExecutionException {
        final List<Profile> importedProfiles = profileRepository.findByState(ProfileState.Imported);
        final AtomicInteger i = new AtomicInteger(0);
        int totalSize = importedProfiles.size();
        updateProgress(i.get(), totalSize);

        importedProfiles.forEach(file -> {
            // Sync with elastic
            final byte[] base64bytes = Base64.getEncoder().encode(file.getPayload());
            final JsonObject json = new JsonObject();
            json.addProperty("data", new String(base64bytes));
            json.addProperty("id", file.getId()); // we add the id to ensure it is referencable
            try {
                Index action = new Index.Builder(json).index("documents").type("profiles").setParameter("pipeline", "attachment").build();
                DocumentResult result = client.execute(action);
                if (!result.isSucceeded()) {
                    LOG.error("Could not index file {}. Reason: ", file.getName(), result.getErrorMessage());
                    file.setErrorMessage(result.getErrorMessage());
                    return;
                }
            } catch (IOException e) {
                LOG.error("Could not index file {}. Reason: ", file.getName(), e.getMessage());
                file.setErrorMessage(e.getMessage());
                return;
            }

            // Mark as indexed
            file.setState(ProfileState.Indexed);
            updateProgress(i.incrementAndGet(), totalSize);
        });

        profileRepository.save(importedProfiles);
        updateProgress(totalSize, totalSize);
        return null;
    }
}
