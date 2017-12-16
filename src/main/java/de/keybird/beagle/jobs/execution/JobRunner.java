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

package de.keybird.beagle.jobs.execution;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.keybird.beagle.jobs.persistence.JobEntity;
import de.keybird.beagle.jobs.persistence.JobState;

// TODO MVR es wird jetzt zwar geloggt, welche dokumente usw. abgewiesen wurden, aber ein übergeordneter status für die dokumente, pages fehlt noch
// Das muss noch eingeführt werden, damit die queries funktionieren
public class JobRunner<T extends JobEntity> {

    private static final Logger LOG = LoggerFactory.getLogger(JobRunner.class);

    private JobExecutionContext<T> context;

    private JobExecution execution;

    public JobRunner(JobExecutionContext<T> context, JobExecution<T> execution) {
        this.context = Objects.requireNonNull(context);
        this.execution = Objects.requireNonNull(execution);
    }

    public void execute() {
        if (context.getJobEntity() == null) {
            throw new IllegalStateException("JobRunner was not initialized properly. JobEntity is null. Bailing");
        }

        try {
            context.setState(JobState.Initializing);
            context.start();

            execution.execute(context);

            context.success();
            context.onSuccess();
        } catch (Throwable t) {
            context.error(t);
            context.onError(t);
            // We want to propagate properly without having to add Exception to the signature
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else if (t instanceof Error) {
                throw (Error) t;
            }
            throw new RuntimeException(t);
        } finally {
            context.complete();
        }
    }

    public JobExecutionContext<T> getContext() {
        return context;
    }
}
