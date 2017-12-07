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

package de.keybird.beagle.rest.model;

import java.util.Date;

import de.keybird.beagle.jobs.JobInfo;
import de.keybird.beagle.jobs.JobState;
import de.keybird.beagle.jobs.Progress;

public class JobInfoDTO implements JobInfo {

    private long id;
    private String description;
    private Date startTime;
    private Date completeTime;
    private String errorMessage;
    private JobState state = JobState.Pending;
    private Progress progress = new Progress();

    public JobInfoDTO() {

    }

    public JobInfoDTO(JobInfo info) {
        if (info == null) return;
        this.id = info.getId();
        this.description = info.getDescription();
        this.startTime = info.getStartTime();
        this.completeTime = info.getCompleteTime();
        this.errorMessage = info.getErrorMessage();
        this.state = info.getState();
        this.progress = info.getProgress() == null ? null : new Progress(info.getProgress());
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getCompleteTime() {
        return completeTime;
    }

    public void setCompleteTime(Date completeTime) {
        this.completeTime = completeTime;
    }

    public JobState getState() {
        return state;
    }

    public void setState(JobState state) {
        this.state = state;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Progress getProgress() {
        return progress;
    }

    public void setProgress(Progress progress) {
        this.progress = progress;
    }

}
