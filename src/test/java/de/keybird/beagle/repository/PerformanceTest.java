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
package de.keybird.beagle.repository;


import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import javax.persistence.EntityManager;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;

import com.google.common.hash.Hashing;
import com.google.common.io.ByteStreams;

import de.keybird.beagle.BeagleTest;
import de.keybird.beagle.api.Document;
import de.keybird.beagle.api.DocumentState;
import de.keybird.beagle.api.Page;
import de.keybird.beagle.api.PageState;
import de.keybird.beagle.jobs.persistence.DetectJobEntity;
import de.keybird.beagle.jobs.persistence.JobState;
import de.keybird.beagle.jobs.persistence.LogEntity;
import de.keybird.beagle.jobs.persistence.LogLevel;

// See https://github.com/keybird/beagle/issues/5
@RunWith(SpringRunner.class)
@BeagleTest
public class PerformanceTest {

    private static final long EXPECTED_RUNTIME = 300; // ms

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private TransactionOperations transactionOperations;

    @Autowired
    private EntityManager entityManager;

    @After
    public void tearDown() {
        entityManager.clear();
    }

    @Test
    public void verifyDocumentRepositorySpeed() throws IOException {
        final byte[] payload = getPayload();
        final String hash = getHash(payload);

        transactionOperations.execute((TransactionCallback<Void>) transactionStatus -> {
            for (int i=0; i<100; i++) {
                documentRepository.save(createDocument(payload, hash));
            }
            return null;
        });

        long start = System.currentTimeMillis();
        transactionOperations.execute(transactionStatus -> documentRepository.findAll());
        long time = System.currentTimeMillis() - start;
        failIfTookLongerThan(EXPECTED_RUNTIME, time);
    }

    @Test
    public void verifyJobRepositorySpeed() {
        transactionOperations.execute((TransactionCallback<Void>) transactionStatus -> {
            for (int i = 0; i < 10; i++) {
                final DetectJobEntity jobEntity = new DetectJobEntity();
                jobEntity.setCreateTime(new Date());
                jobEntity.setStartTime(new Date());
                jobEntity.setCompleteTime(new Date());
                jobEntity.setState(JobState.Completed);
                for (int a = 0; a < 100; a++) {
                    final LogEntity logEntity = new LogEntity();
                    logEntity.setMessage("Dummy entry " + a);
                    logEntity.setLevel(LogLevel.Info);
                    logEntity.setDate(new Date());
                    jobEntity.addLogEntry(logEntity);
                }
                jobRepository.save(jobEntity);
            }
            return null;
        });

        long start = System.currentTimeMillis();
        transactionOperations.execute(transactionStatus -> {
            jobRepository.findAll().forEach(job -> job.getLogs().forEach(l -> l.getDate()));
            return null;
        });
        failIfTookLongerThan(EXPECTED_RUNTIME, System.currentTimeMillis() - start);
    }

    // We set a timeout to ensure the tests fails
    @Test
    public void verifyPageRepositorySpeed() throws IOException {
        final Document document = createDocument();

        transactionOperations.execute((TransactionCallback<Void>) transactionStatus -> {
            documentRepository.save(document);

            for (int i=0; i<250; i++) {
                Page page = new Page();
                page.setDocument(document);
                page.setState(PageState.Imported);
                page.setPageNumber(i);
                page.setPayload(document.getPayload());
                page.setChecksum(document.getChecksum());
                page.setThumbnail(document.getPayload());
                page.setName("Beagle.pdf#" + i);
                pageRepository.save(page);
            }
            return null;
        });

        long start = System.currentTimeMillis();
        transactionOperations.execute(transactionStatus -> pageRepository.findAll());
        long time = System.currentTimeMillis() - start;
        failIfTookLongerThan(EXPECTED_RUNTIME, time);
    }

    private static Document createDocument() throws IOException {
        return createDocument(getPayload());
    }

    private static Document createDocument(byte[] payload) {
        return createDocument(payload, getHash(payload));
    }

    private static Document createDocument(byte[] payload, String hash) {
        Document document = new Document();
        document.setState(DocumentState.Imported);
        document.setPageCount(15);
        document.setPayload(payload);
        document.setChecksum(hash);
        document.setImportDate(new Date());
        document.setFilename("Beagle.pdf");
        return document;
    }

    private static String getHash(byte[] payload) {
        return Hashing.sha256().hashBytes(payload).toString();
    }

    private static byte[] getPayload() throws IOException {
        final InputStream inputStream = PerformanceTest.class.getResourceAsStream("/Beagle.pdf");
        final byte[] bytes = new byte[inputStream.available()];
        ByteStreams.readFully(inputStream, bytes);
        return bytes;
    }

    private static void failIfTookLongerThan(long expectedTime, long actualTime) {
        if (actualTime >= expectedTime) {
            Assert.fail("Test took too long. Expected: " + expectedTime + " ms but was " + actualTime + " ms)");
        }
    }
}