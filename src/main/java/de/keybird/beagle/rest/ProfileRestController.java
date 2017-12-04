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

package de.keybird.beagle.rest;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import de.keybird.beagle.api.ProfileState;
import de.keybird.beagle.api.Profile;
import de.keybird.beagle.repository.ProfileRepository;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;

@RestController
@RequestMapping("/profiles")
public class ProfileRestController {

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private JestClient jestClient;

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Profile>> listFiles() {
        Iterable<Profile> files = profileRepository.findAll();
        if (!files.iterator().hasNext()) {
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        }
        files = StreamSupport.stream(files.spliterator(), false)
                .map(f -> {
                    Profile file = new Profile(f);
                    file.setPayload(null);
                    file.setThumbnail(null);
                    return file;
                })
        .collect(Collectors.toList());
        return new ResponseEntity(files, HttpStatus.OK);
    }

    @RequestMapping(value="count", method=RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity count() {
        final List<Profile> byState = profileRepository.findByState(ProfileState.Indexed);
        byState.addAll(profileRepository.findByState(ProfileState.Synced));
        return new ResponseEntity(byState.size(), HttpStatus.OK);
    }

    @RequestMapping(value="search", method=RequestMethod.GET)
    public ResponseEntity search(@RequestParam("query") String query) throws IOException {
        if (query == null || query.isEmpty()) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        // Build json query
        final JsonObject rootObject = new JsonObject();
        final JsonObject queryObject = new JsonObject();
        final JsonObject matchObject = new JsonObject();
        matchObject.addProperty("attachment.content", query);
        queryObject.add("match", matchObject);
        rootObject.add("query", queryObject);

        // Build search
        final Search search = new Search.Builder(rootObject.toString())
                .addIndex("documents")
                .addType("profiles")
                .addSourceExcludePattern("data")
                .build();
        final SearchResult result = jestClient.execute(search);
        final Set<Profile> files = new HashSet<>();
        if (result.isSucceeded()) {
            final JsonArray hits = result.getJsonObject()
                    .get("hits").getAsJsonObject()
                    .get("hits").getAsJsonArray();
            for (int i=0; i<hits.size();i++) {
                final JsonElement jsonElement = hits.get(i).getAsJsonObject()
                        .get("_source").getAsJsonObject()
                        .get("id");
                if (jsonElement != null) {
                    final long internalId = jsonElement.getAsLong();
                    final Profile file = profileRepository.findOne(internalId);
                    if (file != null) {
                        files.add(file);
                    }
                }
            }
        }
        return new ResponseEntity(files, HttpStatus.OK);
    }
}
