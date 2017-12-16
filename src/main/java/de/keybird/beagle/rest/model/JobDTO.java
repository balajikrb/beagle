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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import de.keybird.beagle.jobs.persistence.JobState;
import de.keybird.beagle.jobs.persistence.JobType;
import de.keybird.beagle.jobs.persistence.JobEntity;

public class JobDTO {

    private Long id;

    private String errorMessage;

    private JobState state = JobState.Pending;

    private Date startTime;

    private Date createTime = new Date();

    private Date completeTime;

    private List<LogDTO> logs = new ArrayList<>();

    private JobType type;

    public JobDTO() {

    }

    public JobDTO(JobEntity jobEntity) {
        setId(jobEntity.getId());
        setCompleteTime(jobEntity.getCompleteTime());
        setCreateTime(jobEntity.getCreateTime());
        setErrorMessage(jobEntity.getErrorMessage());
        setStartTime(jobEntity.getStartTime());
        setState(jobEntity.getState());
        setType(jobEntity.getType());
        setLogs(jobEntity.getLogs().stream().map(le -> new LogDTO(le)).collect(Collectors.toList()));
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public JobState getState() {
        return state;
    }

    public void setState(JobState state) {
        this.state = state;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getCompleteTime() {
        return completeTime;
    }

    public void setCompleteTime(Date completeTime) {
        this.completeTime = completeTime;
    }

    public List<LogDTO> getLogs() {
        return logs;
    }

    public void setLogs(List<LogDTO> logs) {
        this.logs = logs;
    }

    public JobType getType() {
        return type;
    }

    public void setType(JobType type) {
        this.type = type;
    }
}
