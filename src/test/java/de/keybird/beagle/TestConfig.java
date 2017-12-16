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

import java.net.URL;

public interface TestConfig {
    String WORKING_DIRECTORY = "target/beagle-home";
    String INBOX_DIRECTORY = String.format("%s/1_inbox", WORKING_DIRECTORY);

    String BEAGLE_DE_PDF_NAME = "Beagle_(Hunderasse).pdf";
    URL BEAGLE_DE_PDF_URL = TestConfig.class.getResource("/" + BEAGLE_DE_PDF_NAME);

    String BEAGLE_EN_PDF_NAME = "Beagle.pdf";
    URL BEAGLE_EN_PDF_URL = TestConfig.class.getResource("/" + BEAGLE_EN_PDF_NAME);

}
