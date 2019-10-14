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

package de.keybird.beagle.elastic;

import java.io.IOException;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;

import io.searchbox.action.Ingest;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;

@Service
public class AttachmentPipelineInitializer {

    private final Logger LOG = LoggerFactory.getLogger(AttachmentPipelineInitializer.class);

    @Autowired
    private JestClient jestClient;

    private final JSONObject config;

    public AttachmentPipelineInitializer() throws IOException {
        final JSONTokener tokener = new JSONTokener(getClass().getResourceAsStream("/elastic/pipeline-attachment-config.json"));
        this.config = new JSONObject(tokener);
    }

    public void initialize() throws IOException {
        if (!isInitialized()) {
            doInitialize();
            if (!isInitialized()) {
                throw new IOException("Not initialized properly");
            }
        }
    }

    public boolean isInitialized() throws IOException {
        final Ingest ingest = new Ingest.PipelineBuilder().build();
        final JestResult result = jestClient.execute(ingest);
        final JsonObject received = result.getJsonObject();
        if (received.has("attachment")) {
            final JSONTokener tokener = new JSONTokener(received.getAsJsonObject("attachment").toString());
            final JSONObject jsonReceived = new JSONObject(tokener);
            return config.toString().equals(jsonReceived.toString());
        }
        return false;
    }

    public void doInitialize() throws IOException {
        final Ingest ingest = new Ingest.PipelineBuilder(config.toString())
                .withMethod("PUT")
                .withName("attachment")
                .build();
        final JestResult result = jestClient.execute(ingest);
        if (!result.isSucceeded()) {
            // TODO MVR extract message from json
            throw new IllegalStateException("Pipeline was not properly initialized: " + result.getErrorMessage());
        }
    }
}
