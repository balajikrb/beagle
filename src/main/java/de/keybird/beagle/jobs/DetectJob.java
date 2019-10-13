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

import java.util.Objects;

import de.keybird.beagle.api.DocumentSource;
import de.keybird.beagle.jobs.source.InboxFileSystemSource;

public class DetectJob extends Job {

    private final DocumentSource source;

    public DetectJob() {
        this(new InboxFileSystemSource());
    }

    public DetectJob(DocumentSource source) {
        this.source = Objects.requireNonNull(source);
    }

    @Override
    public JobType getType() {
        return JobType.Detect;
    }

    public String getDescription() {
        return "Detecting new files";
    }

    public DocumentSource getDocumentSource() {
        return source;
    }

    @Override
    public <T> T accept(JobVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
