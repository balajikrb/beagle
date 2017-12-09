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

package de.keybird.beagle;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import org.junit.rules.ExternalResource;

// JUnit rule to add files to a working directory.
// This is required to allow adding files to the inbox directory and
// have JUnit clean it up before/after test execution
public class WorkingDirectory extends ExternalResource {

    private final File workingDirectory;

    public WorkingDirectory() {
        this("target/beagle-home");
    }

    public WorkingDirectory(String directory) {
        Objects.requireNonNull(directory);
        this.workingDirectory = Paths.get(directory).toFile();
    }

    @Override
    protected void before() throws Throwable {
        create();
    }

    @Override
    protected void after() {
        delete();
    }

    private void create() {
        recursiveDelete(workingDirectory);
        workingDirectory.mkdirs();
    }

    private void delete() {
        recursiveDelete(workingDirectory);
    }

    private void recursiveDelete(File file) {
        File[] files = file.listFiles();
        if (files != null) {
            for (File each : files) {
                recursiveDelete(each);
            }
        }
        file.delete();
    }

    // Copies the given path to the working directory. Path must be a file
    public void addFile(Path source) throws IOException {
        if (Files.isDirectory(source)) {
            throw new IllegalArgumentException("Path must be a file but was a directory.");
        }
        Files.copy(source, workingDirectory.toPath().resolve(source.getFileName().toString()));
    }

    public void addFile(URL resource) throws URISyntaxException, IOException {
        Objects.requireNonNull(resource);
        this.addFile(Paths.get(resource.toURI()));
    }
}
