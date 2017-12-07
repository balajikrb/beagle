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
import java.util.concurrent.ExecutionException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;

import de.keybird.beagle.api.Document;
import de.keybird.beagle.api.DocumentState;
import de.keybird.beagle.jobs.persistence.DetectJobEntity;
import de.keybird.beagle.repository.DocumentRepository;
import de.keybird.beagle.services.PdfManager;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DetectJobExecution extends AbstractJobExecution<Integer, DetectJobEntity> {

    private final Logger logger = LoggerFactory.getLogger(DetectJobExecution.class);

    @Autowired
    private DocumentRepository documentRepository;

    @Override
    public String getDescription() {
        return "Detecting new files";
    }

    @Override
    protected Integer executeInternal() throws ExecutionException {
        try {
            logItem("Reading contents from directory {}", context.getInboxPath());

            Files.list(context.getInboxPath())
                    // TODO MVR also handle jpg, etc.
                    .filter(entry -> {
                        boolean accept = !Files.isDirectory(entry) && entry.toString().toLowerCase().endsWith(".pdf");
                        return accept;
                    })
                    .forEach(entry -> {
                        logItem("Handling file {}", entry.toString());

                        final Document theDocument = new Document();
                        theDocument.setState(DocumentState.New);
                        theDocument.setFilename(entry.getFileName().toString());

                        try {
                            final byte[] payload = Files.readAllBytes(entry);
                            theDocument.setPayload(payload);

                            final HashCode hashCode = Hashing.sha256().hashBytes(payload);
                            theDocument.setChecksum(hashCode.toString());

                            final PDDocument pdfDocument = PdfManager.load(payload);
                            theDocument.setPageCount(pdfDocument.getPages().getCount());

                            logDocument(theDocument, DocumentState.Accepted);
                        } catch (IOException ex) {
                            logDocument(theDocument, DocumentState.Error, ex.getMessage());
                        }
                        documentRepository.save(theDocument);
                        if (theDocument.getPayload() != null) {
                            logItem("Deleting file {}", entry);
                            deleteFile(entry);
                        }
                    });
        } catch (IOException e) {
            logger.error("Error while listing files: {}", e.getMessage(), e);
            throw new ExecutionException(e);
        }
        return 0;
    }

    private void deleteFile(Path p) {
        try {
            Files.delete(p);
        } catch (Exception ex) {
            // swallow it
        }
    }
}