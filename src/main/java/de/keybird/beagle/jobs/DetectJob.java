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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.apache.pdfbox.pdmodel.PDDocument;
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
import de.keybird.beagle.repository.ImportRepository;
import de.keybird.beagle.services.PdfManager;

// Detects files in inbox directory
@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DetectJob extends AbstractJob<Integer> {

    private static Logger LOG = LoggerFactory.getLogger(DetectJob.class);

    @Autowired
    private ImportRepository importRepository;

    @PostConstruct
    public void initialize() throws IOException {
        Files.createDirectories(context.getInboxPath());
        setDescription("Detecting new files");
    }

    @Override
    protected Integer executeInternal() throws ExecutionException {
        final List<Import> newFiles = new ArrayList<>();
        try {
            // Determine all PDF files
            final Path inboxPath = context.getInboxPath();
            getPdfFiles(inboxPath)
                .forEach(p -> {
                    final Import theImport = new Import();
                    theImport.setState(ImportState.New);
                    theImport.setFilename(p.getFileName().toString());
                    try {
                        final byte[] payload = Files.readAllBytes(p);
                        theImport.setPayload(payload);

                        final HashCode hashCode = Hashing.sha256().hashBytes(payload);
                        theImport.setChecksum(hashCode.toString());

                        final PDDocument pdfDocument = PdfManager.load(payload);
                        theImport.setPageCount(pdfDocument.getPages().getCount());
                    } catch (IOException ex) {
                        theImport.setState(ImportState.Error);
                        theImport.setErrorMessage(ex.getMessage());
                    }
                    newFiles.add(theImport);
                    if (theImport.getPayload() != null) {
                        deleteFile(p);
                    }
                });
        } catch (IOException e) {
            LOG.error("Error while listing files: {}", e.getMessage(), e);
            throw new ExecutionException(e);
        }
        importRepository.save(newFiles);
        return newFiles.size();
    }

    private void deleteFile(Path p) {
        try {
            Files.delete(p);
        } catch (Exception ex) {
            // swallow it
        }
    }

    // Determine all PDF files
    protected static Stream<Path> getPdfFiles(Path path) throws IOException {
        final Stream<Path> pdfFilesStream = Files.list(path)
                .filter(entry -> {
                    boolean accept = !Files.isDirectory(entry) && entry.toString().toLowerCase().endsWith(".pdf");
                    return accept;
                });
        return pdfFilesStream;
    }
}
