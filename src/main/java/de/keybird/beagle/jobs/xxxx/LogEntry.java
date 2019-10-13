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

package de.keybird.beagle.jobs.xxxx;

import java.util.Date;
import java.util.Objects;

public class LogEntry {
    private Date date = new Date();
    private final LogLevel logLevel;
    private final String message;

    public LogEntry(LogLevel logLevel, String message) {
        this.logLevel = Objects.requireNonNull(logLevel);
        this.message = Objects.requireNonNull(message);
    }

    public Date getDate() {
        return date;
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }

    public String getMessage() {
        return message;
    }
}
