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

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import de.keybird.beagle.api.Import;
import de.keybird.beagle.repository.ImportRepository;

@RestController
@RequestMapping("/imports")
public class ImportRestController {

    @Autowired
    private ImportRepository importRepository;

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Import>> listImports() {
        Iterable<Import> files = importRepository.findAll();
        if (!files.iterator().hasNext()) {
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        }
        files = StreamSupport.stream(files.spliterator(), false)
                .map(f -> {
                    Import theImport = new Import(f);
                    theImport.setPayload(null);
                    return theImport;
                })
                .collect(Collectors.toList());
        return new ResponseEntity(files, HttpStatus.OK);
    }
}
