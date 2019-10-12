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

package de.keybird.beagle.api.sources;

import java.util.Objects;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;

import de.keybird.beagle.api.DocumentSource;
import de.keybird.beagle.api.sources.strategy.ByteDocumentSourceStrategy;
import de.keybird.beagle.api.sources.strategy.DocumentSourceStrategy;

@Entity
@DiscriminatorValue("BYTE")
public class ByteDocumentSource extends DocumentSource {
    @Lob
    @Basic(fetch= FetchType.LAZY)
    private byte[] payload;

    @Column(name="name")
    private String name;

    public ByteDocumentSource() {

    }

    public ByteDocumentSource(String name, byte[] payload) {
        this.name = Objects.requireNonNull(name);
        this.payload = Objects.requireNonNull(payload);
    }

    @Override
    public DocumentSourceStrategy getStrategy() {
        return new ByteDocumentSourceStrategy(() -> payload, () -> name);
    }
}
