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

package de.keybird.beagle.events;

import java.util.Objects;

import de.keybird.beagle.jobs.persistence.JobEntity;
import de.keybird.beagle.jobs.persistence.JobState;

public class JobExecutionStateChangedEvent extends JobExecutionEvent {

    private final JobState oldState;
    private final JobState newState;

    public JobExecutionStateChangedEvent(JobEntity jobEntity, JobState oldState, JobState state) {
        super(jobEntity);
        this.oldState = Objects.requireNonNull(oldState);
        this.newState = Objects.requireNonNull(state);
    }
}
