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

package de.keybird.beagle.jobs.persistence;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import de.keybird.beagle.jobs.JobState;
import de.keybird.beagle.jobs.execution.JobType;
import de.keybird.beagle.jobs.persistence.status.JobStatus;

@Entity
@Table(name="jobs")
@Inheritance(strategy= InheritanceType.SINGLE_TABLE)
public abstract class JobEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Embedded
    private JobStatus status = new JobStatus(JobState.Pending);

    @Temporal(TemporalType.TIMESTAMP)
    private Date startTime;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime = new Date();

    @Temporal(TemporalType.TIMESTAMP)
    private Date completeTime;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JobItem> manifestItems = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public List<JobItem> getManifestItems() {
        return manifestItems;
    }

    public void setManifestItems(List<JobItem> manifestItems) {
        this.manifestItems = manifestItems;
    }

    public void addManifestItem(JobItem item) {
        manifestItems.add(item);
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public void setState(JobState state) {
        status.setState(state);
    }

    public void setErrorMessage(String errorMessage) {
        status.setErrorMessage(errorMessage);
    }

    public abstract JobType getType();
}
