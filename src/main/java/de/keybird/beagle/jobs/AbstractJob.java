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

import java.util.Date;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import de.keybird.beagle.events.JobProgressChangedEvent;
import de.keybird.beagle.events.JobStateChangedEvent;

public abstract class AbstractJob<T> implements Job<T> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractJob.class);

    @Autowired
    protected JobContext context;

    private int id;
    private JobState state = JobState.Pending;
    private String description;
    private final Date createTime = new Date();
    private Date startTime;
    private Date completeTime;
    private String errorMessage;
    private final Progress progress = new Progress();

    public void setId(int id) {
        this.id = id;
    }

    @Transactional
    @Override
    public T execute() {
        setState(JobState.Initializing);
        start();
        try {
            T result = executeInternal();
            complete(result);
            onSuccess(result);
            return result;
        } catch (Throwable t) {
            error(t);
            onError(t);
            // We want to propagate properly without having to add Exception to the signature
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else if (t instanceof Error) {
                throw (Error) t;
            }
            throw new RuntimeException(t);
        }
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public JobState getState() {
        return state;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public Date getCreateTime() {
        return createTime;
    }

    @Override
    public Date getStartTime() {
        return startTime;
    }

    @Override
    public Date getCompleteTime() {
        return completeTime;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public Progress getProgress() {
        return progress;
    }

    protected void setState(JobState state) {
        JobState oldState = state;
        this.state = state;
        context.getEventBus().post(new JobStateChangedEvent(this, oldState, state));
    }

    protected void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    protected void setCompleteTime(Date completeTime) {
        this.completeTime = completeTime;
    }

    protected void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    protected void start() {
        setStartTime(new Date());
        setState(JobState.Running);
    }

    protected void complete(T result) {
        setCompleteTime(new Date());
        setState(de.keybird.beagle.jobs.JobState.Success);
    }

    protected void error(Throwable t) {
        setErrorMessage(t.getMessage());
        setState(de.keybird.beagle.jobs.JobState.Error);
    }

    protected void updateProgress(int currentProgress, int totalProgress) {
        Progress oldProgress = new Progress(getProgress());
        getProgress().setIndeterminate(false);
        getProgress().setProgress(currentProgress);
        getProgress().setTotalProgress(totalProgress);
        context.getEventBus().post(new JobProgressChangedEvent(this, oldProgress, getProgress()));
    }

    protected void setDescription(String description) {
        this.description = description;
    }

    protected abstract T executeInternal() throws Exception;

    // Callback hook
    protected void onSuccess(T result) {

    }

    // Callback hook
    protected void onError(Throwable t) {

    }
}
