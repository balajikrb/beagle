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
import java.util.Base64;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;

import de.keybird.beagle.api.Page;
import de.keybird.beagle.api.PageState;
import de.keybird.beagle.jobs.persistence.IndexJobEntity;
import de.keybird.beagle.jobs.persistence.LogLevel;
import de.keybird.beagle.repository.PageRepository;
import io.searchbox.client.JestClient;
import io.searchbox.core.DocumentResult;
import io.searchbox.core.Index;

// Syncs database content with elastic
@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class IndexJobExecution extends AbstractJobExecution<IndexJobEntity> {

    private static final Logger LOG = LoggerFactory.getLogger(IndexJobExecution.class);

    @Autowired
    private JestClient client;

    @Autowired
    private PageRepository pageRepository;

    @Override
    public String getDescription() {
        return "Indexing pages";
    }

    @Override
    protected void executeInternal() {
        final List<Page> importedPages = pageRepository.findByState(PageState.Imported);
        final AtomicInteger index = new AtomicInteger(0);
        int totalSize = importedPages.size();
        updateProgress(index.get(), totalSize);

        importedPages.forEach(page -> {
            logEntry(LogLevel.Info,"Indexing page '{}/{}'...", page.getDocument().getFilename(), page.getPageNumber());

            // Sync with elastic
            final byte[] base64bytes = Base64.getEncoder().encode(page.getPayload());
            final JsonObject json = new JsonObject();
            json.addProperty("data", new String(base64bytes));
            json.addProperty("id", page.getId()); // we add the id to ensure it is referencable
            try {
                final Index action = new Index.Builder(json)
                        .index("documents")
                        .type("pages")
                        .setParameter("pipeline", "attachment")
                        .build();
                final DocumentResult result = client.execute(action);
                if (!result.isSucceeded()) {
                    LOG.error("Could not index file {}. Reason: ", page.getName(), result.getErrorMessage());
                    page.setErrorMessage(result.getErrorMessage());
                    logEntry(LogLevel.Error, "Page {}/{} could not be index. Reason: {}", page.getDocument().getFilename(), page.getPageNumber(), result.getErrorMessage());
                    return;
                }
                // Mark as Success
                logEntry(LogLevel.Success, "Page {}/{} was indexed successfully", page.getDocument().getFilename(), page.getPageNumber());
                page.setState(PageState.Indexed);
                page.setErrorMessage(null);
            } catch (IOException e) {
                LOG.error("Could not index file {}. Reason: ", page.getName(), e.getMessage());
                page.setErrorMessage(e.getMessage());
                logEntry(LogLevel.Error, "Page {}/{} could not be index. Reason: {}", page.getDocument().getFilename(), page.getPageNumber(), e.getMessage());
                return;
            } finally {
                updateProgress(index.incrementAndGet(), totalSize);
            }
        });

        pageRepository.save(importedPages);
        updateProgress(totalSize, totalSize);
    }
}
