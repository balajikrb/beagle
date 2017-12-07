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

import de.keybird.beagle.jobs.JobResult;
import de.keybird.beagle.jobs.execution.AbstractJobExecution;

public class JobExecutionFinishedEvent extends JobExecutionEvent {
    private final JobResult jobResult;

    public JobExecutionFinishedEvent(AbstractJobExecution job, JobResult jobResult) {
        super(job);
        this.jobResult = Objects.requireNonNull(jobResult);
    }

    public boolean isSuccess() {
        return jobResult.getException() == null;
    }

    public boolean isFailed() {
        return !isSuccess();
    }

    public Throwable getException() {
        return jobResult.getException();
    }

    public <T> T getResult() {
        return (T) jobResult.getResult();
    }
}
