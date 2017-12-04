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

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.annotation.PostConstruct;
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

import de.keybird.beagle.api.Import;
import de.keybird.beagle.api.ImportState;
import de.keybird.beagle.api.Profile;
import de.keybird.beagle.api.ProfileState;
import de.keybird.beagle.repository.ImportRepository;
import de.keybird.beagle.repository.ProfileRepository;
import de.keybird.beagle.services.PdfManager;

// Imports files to database
@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ImportJob extends AbstractJob<Void> {

    private static final Logger LOG = LoggerFactory.getLogger(ImportJob.class);

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private ImportRepository importRepository;

    private Import importMe;

    public void setImport(Import theImport) {
        this.importMe = Objects.requireNonNull(theImport);
        setDescription("Importing '" + importMe.getFilename() + "'");
    }

    @PostConstruct
    public void init() {
        setDescription("Importing");
    }

    public Void executeInternal() throws Exception {
        final PDDocument document = PdfManager.load(importMe.getPayload());
        final Splitter splitter = new Splitter();
        final List<PDDocument> splitDocuments = splitter.split(document);

        // Update progress, as we now know how many pages there are
        int index = 0;
        updateProgress(index, splitDocuments.size());

        // TODO MVR this is not deterministic. Figure out why and maybe do it differently
        for (PDDocument splitDocument : splitDocuments) {
            final Profile fileWithPayload = new Profile();

            fileWithPayload.setImport(importMe);
            // Determine payload
            final ByteArrayOutputStream payloadByteStream = new ByteArrayOutputStream();
            splitDocument.save(payloadByteStream);

            // Define Payload
            fileWithPayload.setName(UUID.randomUUID().toString() + ".pdf");
            fileWithPayload.setPayload(payloadByteStream.toByteArray());

            // Calculate checksum
            final HashCode hashCode = Hashing.sha256().hashBytes(fileWithPayload.getPayload());
            fileWithPayload.setChecksum(hashCode.toString());

            // Create thumbnail
            final ByteArrayOutputStream thumbnailByteStream = new ByteArrayOutputStream();
            final BufferedImage bufferedImage = new PDFRenderer(splitDocument).renderImage(0);
            ImageIO.write(bufferedImage, "png", thumbnailByteStream);
            fileWithPayload.setThumbnail(thumbnailByteStream.toByteArray());
            // TODO MVR rescale thumbnail
            // TODO MVr don't import same page twice

            // Mark as imported
            fileWithPayload.setState(ProfileState.Imported);
            updateProgress(++index, splitDocuments.size());
            profileRepository.save(fileWithPayload);
        }
        return null;
    }

    @Override
    protected void onSuccess(Void result) {
        if (importMe != null) {
            importMe.setState(ImportState.Imported);
            importRepository.save(importMe);
        }
    }

    @Override
    protected void onError(Throwable t) {
        if (importMe != null) {
            importMe.setState(ImportState.Error);
            importMe.setErrorMessage(t.getMessage());
            importRepository.save(importMe);
        }
    }
}
