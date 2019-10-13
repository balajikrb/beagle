/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package de.keybird.beagle.jobs.xxxx;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import com.google.common.collect.Lists;

import de.keybird.beagle.jobs.JobVisitor;
import de.keybird.beagle.jobs.Progress;

public abstract class Job {

    private Long id;
    private String errorMessage;
    private JobState state = JobState.Pending;
    private Date startTime;
    private Date createTime = new Date();
    private Date completeTime;
    private Progress progress = new Progress();

    private List<LogEntry> logs = Lists.newArrayList();

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

    public List<LogEntry> getLogs() {
        return logs;
    }

    public void setCompleteTime(Date completeTime) {
        this.completeTime = completeTime;
    }

    public void updateProgress(int currentProgress, int totalProgress) {
        getProgress().setIndeterminate(false);
        getProgress().setProgress(currentProgress);
        getProgress().setTotalProgress(totalProgress);
//        getEventBus().post(new JobExecutionProgressChangedEvent(jobEntity, oldProgress, getProgress()));
    }

    public void updateProgress(int currentProgress) {
        updateProgress(currentProgress, getProgress().getTotalProgress());
    }

    public Progress getProgress() {
        return progress;
    }

    public void addLog(LogEntry logEntry) {
        logs.add(Objects.requireNonNull(logEntry));
    }

    public abstract JobType getType();

    public abstract String getDescription();

    public abstract <T> T accept(JobVisitor<T> visitor);
}
