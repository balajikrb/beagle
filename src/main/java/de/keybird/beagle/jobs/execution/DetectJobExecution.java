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

import static de.keybird.beagle.Utils.closeSilently;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;

import de.keybird.beagle.api.Document;
import de.keybird.beagle.api.DocumentSource;
import de.keybird.beagle.api.DocumentState;
import de.keybird.beagle.jobs.DetectJob;
import de.keybird.beagle.jobs.source.DocumentEntry;
import de.keybird.beagle.jobs.LogLevel;
import de.keybird.beagle.repository.DocumentRepository;
import de.keybird.beagle.services.PdfManager;

@Service
@Scope("prototype")
public class DetectJobExecution implements JobExecution<DetectJob> {

    private final Logger logger = LoggerFactory.getLogger(DetectJobExecution.class);

    @Autowired
    private DocumentRepository documentRepository;

    // TODO MVR the file size should be checked and too big files should be rejected
    @Override
    public void execute(JobExecutionContext<DetectJob> context) throws ExecutionException {
        final List<DocumentEntry> entries = getEntries(context);
        for (DocumentEntry entry : entries) {
            try {
                context.logEntry(LogLevel.Info, "Handling document '{}'", entry.toString());
                final Document theDocument = new Document();
                theDocument.setState(DocumentState.New);
                theDocument.setFilename(entry.getName());

                final byte[] payload = entry.getPayload();
                theDocument.setPayload(payload);

                final HashCode hashCode = Hashing.sha256().hashBytes(payload);
                theDocument.setChecksum(hashCode.toString());

                final PDDocument pdfDocument = PdfManager.load(payload);
                theDocument.setPageCount(pdfDocument.getPages().getCount());
                closeSilently(pdfDocument);

                // Ensure it is not already persisted
                if (documentRepository.findByChecksum(hashCode.toString()) != null) {
                    context.logEntry(LogLevel.Warn, "Document '{}' was rejected. Reason: Document already exists.", entry);
                } else if (payload == null || payload.length == 0) {
                    context.logEntry(LogLevel.Warn, "Document '{}' was rejected. Reason: Payload is null or empty.", entry);
                } else {
                    context.logEntry(LogLevel.Success, "Document '{}' was accepted.", entry);
                    documentRepository.save(theDocument);

                    // Cleanup
                    try {
                        entry.delete();
                    } catch (IOException ex) {
                        context.logEntry(LogLevel.Warn, "Document '{}' could not be deleted from source. Reason: {}", ex.getMessage(), ex);
                    }
                }
            } catch (IOException ex) {
                context.logEntry(LogLevel.Error, "Document '{}' was rejected. Reason: {}", entry, ex.getMessage());
            }
        }
    }

    private List<DocumentEntry> getEntries(JobExecutionContext<DetectJob> context) throws ExecutionException {
        try {
            final DocumentSource source = context.getJob().getDocumentSource();
            final List<DocumentEntry> entries = source.getEntries(context);
            return entries;
        } catch (IOException e) {
            logger.error("Error while listing files: {}", e.getMessage(), e);
            throw new ExecutionException(e);
        }
    }
}