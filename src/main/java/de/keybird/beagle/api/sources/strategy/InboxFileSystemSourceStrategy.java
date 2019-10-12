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

package de.keybird.beagle.api.sources.strategy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import de.keybird.beagle.jobs.execution.JobExecutionContext;
import de.keybird.beagle.jobs.persistence.JobEntity;
import de.keybird.beagle.jobs.persistence.LogLevel;

public class InboxFileSystemSourceStrategy implements DocumentSourceStrategy {
    @Override
    public List<DocumentEntry> getEntries(JobExecutionContext<? extends JobEntity> context) throws IOException {
        Files.createDirectories(context.getInboxPath());
        context.logEntry(LogLevel.Info,"Reading contents from directory '{}'", context.getInboxPath());

        return Files.list(context.getInboxPath())
                // TODO MVR inform user that only pdf files are loaded (or others are ignored, this is not transparent enough)
                .filter(entry -> !Files.isDirectory(entry) && entry.toString().toLowerCase().endsWith(".pdf"))
                .map(FileSystemDocument::new)
                .collect(Collectors.toList());
    }

    @Override
    public void cleanUp(DocumentEntry entry) {
        if (entry instanceof FileSystemDocument) {
            ((FileSystemDocument) entry).delete();
        }
    }

    static class FileSystemDocument implements DocumentEntry {

        private final Path path;

        protected FileSystemDocument(Path path) {
            this.path = Objects.requireNonNull(path);
            if (Files.isDirectory(path)) {
                throw new IllegalArgumentException("Path '" + path + "' must be a file, but was a directory");
            }
        }

        @Override
        public String getName() {
            return path.getFileName().toString();
        }

        @Override
        public byte[] getPayload() throws IOException {
            return Files.readAllBytes(path);
        }

        void delete() {
            try {
                Files.delete(path);
            } catch (Exception ex) {
                // swallow it
            }
        }
    }
}
