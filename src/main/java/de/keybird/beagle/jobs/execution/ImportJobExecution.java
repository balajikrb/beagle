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

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
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
import de.keybird.beagle.api.Page;
import de.keybird.beagle.api.PageState;
import de.keybird.beagle.jobs.persistence.ImportJobEntity;
import de.keybird.beagle.jobs.persistence.LogLevel;
import de.keybird.beagle.repository.DocumentRepository;
import de.keybird.beagle.repository.PageRepository;
import de.keybird.beagle.services.PdfManager;

// Imports files to database
@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ImportJobExecution extends AbstractJobExecution<ImportJobEntity> {

    private final Logger logger = LoggerFactory.getLogger(ImportJobExecution.class);

    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Override
    public String getDescription() {
        return String.format("Importing '%s'", getDocument().getFilename());
    }

    @Override
    public void executeInternal() throws Exception {
        logEntry(LogLevel.Info, "Importing '{}'", getDocument().getFilename());

        final Document importDocument = getDocument();
        final PDDocument pdfDocument = PdfManager.load(importDocument.getPayload());
        final Splitter splitter = new Splitter();
        final List<PDDocument> splitDocuments = splitter.split(pdfDocument);

        // Update progress, as we now know how many pages there are
        int index = 0;
        updateProgress(index, splitDocuments.size());

        // TODO MVR this is not deterministic. Figure out why and maybe do it differently
        for (PDDocument splitDocument : splitDocuments) {
            final Page page = new Page();
            page.setDocument(importDocument);
            page.setPageNumber(index + 1);

            try {
                // Determine payload
                final ByteArrayOutputStream payloadByteStream = new ByteArrayOutputStream();
                splitDocument.save(payloadByteStream);

                // Define Payload
                page.setName(UUID.randomUUID().toString() + ".pdf");
                page.setPayload(payloadByteStream.toByteArray());

                // Calculate checksum
                final HashCode hashCode = Hashing.sha256().hashBytes(page.getPayload());
                page.setChecksum(hashCode.toString());

                // Create thumbnail
                final ByteArrayOutputStream thumbnailByteStream = new ByteArrayOutputStream();
                final BufferedImage bufferedImage = new PDFRenderer(splitDocument).renderImage(0);
                ImageIO.write(bufferedImage, "png", thumbnailByteStream);
                page.setThumbnail(thumbnailByteStream.toByteArray());
                // TODO MVR rescale thumbnail
                // TODO MVR don't import same page twice

                // Mark as imported
                page.setState(PageState.Imported);
                page.setErrorMessage(null);
                logEntry(LogLevel.Info.Success, "Page {} was imported successful", index + 1);
                updateProgress(++index, splitDocuments.size());
                pageRepository.save(page);
            } catch (Exception ex) {
                logger.error("Error while importing page", ex);
                page.setErrorMessage(ex.getMessage());
            }
        }
    }

    @Override
    protected void onSuccess() {
        // TODO MVR figure out if partially imported
        getDocument().setState(DocumentState.Imported);
        getDocument().setErrorMessage(null);
        documentRepository.save(getDocument());
    }

    @Override
    protected void onError(Throwable t) {
        getDocument().setErrorMessage(t.getMessage());
        documentRepository.save(getDocument());
    }

    private Document getDocument() {
        return getJobEntity().getDocument();
    }
}
