/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package de.keybird.beagle.jobs.xxxx;

import java.util.Objects;

import de.keybird.beagle.api.Document;
import de.keybird.beagle.jobs.JobVisitor;

public class ImportJob extends Job {

    private final Document document;

    public ImportJob(Document theDocument) {
        this.document = Objects.requireNonNull(theDocument);
    }

    @Override
    public JobType getType() {
        return JobType.Import;
    }

    @Override
    public String getDescription() {
        return String.format("Importing '%s'", getDocument().getFilename());
    }

    @Override
    public <T> T accept(JobVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public Document getDocument() {
        return document;
    }
}
