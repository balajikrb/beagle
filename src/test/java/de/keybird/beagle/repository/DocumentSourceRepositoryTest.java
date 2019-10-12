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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import de.keybird.beagle.BeagleTest;
import de.keybird.beagle.api.DocumentSource;
import de.keybird.beagle.api.sources.InboxFileSystemSource;

@BeagleTest
@RunWith(SpringRunner.class)
public class DocumentSourceRepositoryTest {

    @Autowired
    private DocumentSourceRepository documentSourceRepository;

    @Test
    public void verifyPersist() {
        assertThat(documentSourceRepository.count(), is(0L));
        final DocumentSource documentSourceEntity = new InboxFileSystemSource();
        final DocumentSource saved = documentSourceRepository.save(documentSourceEntity);
        assertThat(saved.getId(), is(not(nullValue())));
        assertThat(documentSourceRepository.count(), is(1L));
    }
}
