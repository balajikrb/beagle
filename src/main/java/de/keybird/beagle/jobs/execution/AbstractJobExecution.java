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

import java.util.Date;
import java.util.Objects;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;

import de.keybird.beagle.api.Document;
import de.keybird.beagle.api.DocumentState;
import de.keybird.beagle.api.Page;
import de.keybird.beagle.api.PageState;
import de.keybird.beagle.events.JobExecutionProgressChangedEvent;
import de.keybird.beagle.events.JobExecutionStateChangedEvent;
import de.keybird.beagle.jobs.JobContext;
import de.keybird.beagle.jobs.JobState;
import de.keybird.beagle.jobs.Progress;
import de.keybird.beagle.jobs.persistence.DocumentItem;
import de.keybird.beagle.jobs.persistence.JobEntity;
import de.keybird.beagle.jobs.persistence.LogItem;
import de.keybird.beagle.jobs.persistence.PageItem;

// TODO MVR es wird jetzt zwar geloggt, welche dokumente usw. abgewiesen wurden, aber ein übergeordneter status für die dokumente, pages fehlt noch
// Das muss noch eingeführt werden, damit die queries funktionieren
public abstract class AbstractJobExecution<T, J extends JobEntity> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractJobExecution.class);

    @Autowired
    protected JobContext context;

    private J jobEntity;

    private final Progress progress = new Progress();

    @Transactional
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
        } finally {
            context.getJobRepository().save(jobEntity); // Update job entity
        }
    }

    public void setJobEntity(J jobEntity) {
        this.jobEntity = Objects.requireNonNull(jobEntity);
    }

    public Progress getProgress() {
        return progress;
    }

    public J getJobEntity() {
        return this.jobEntity;
    }

    protected void setState(JobState state) {
        JobState oldState = state;
        jobEntity.setState(state);
        context.getEventBus().post(new JobExecutionStateChangedEvent(this, oldState, state));
    }

    protected void setStartTime(Date startTime) {
        jobEntity.setStartTime(startTime);
    }

    protected void setCompleteTime(Date completeTime) {
        jobEntity.setCompleteTime(completeTime);
    }

    protected void setErrorMessage(String errorMessage) {
        jobEntity.setErrorMessage(errorMessage);
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

    protected void logItem(String message, Object... args) {
        LOG.info(message, args);

        final FormattingTuple formattingTuple = MessageFormatter.arrayFormat(message, args);
        LogItem item = new LogItem();
        item.setMessage(formattingTuple.getMessage());
        jobEntity.addManifestItem(item);
    }

    protected void logDocument(Document document, DocumentState state) {
        logDocument(document, state, null);
    }

    protected void logDocument(Document document, DocumentState state, String message) {
        LOG.info("Document with id {} new state: {}, was: {}.{}", document.getId(), state, document.getState(), message != null ? " Reason: " + message : "");

        document.setState(state);
        document.setErrorMessage(message);

        DocumentItem documentItem = new DocumentItem();
        documentItem.setDocument(document);
        documentItem.setStatus(state, message);
        jobEntity.addManifestItem(documentItem);
    }

    protected void logPage(Page page, PageState state) {
        logPage(page, state, null);
    }

    protected void logPage(Page page, PageState state, String message) {
        LOG.info("Page with id {} new state: {}, was: {}.{}", page.getId(), state, page.getState(), message != null ? " Reason: " + message : "");

        page.setState(state);
        page.setErrorMessage(message);

        PageItem pageItem = new PageItem();
        pageItem.setPage(page);
        pageItem.setStatus(state, message);
        jobEntity.addManifestItem(pageItem);
    }

    protected void updateProgress(int currentProgress, int totalProgress) {
        Progress oldProgress = new Progress(getProgress());
        getProgress().setIndeterminate(false);
        getProgress().setProgress(currentProgress);
        getProgress().setTotalProgress(totalProgress);
        context.getEventBus().post(new JobExecutionProgressChangedEvent(this, oldProgress, getProgress()));
    }

    protected abstract T executeInternal() throws Exception;

    // Callback hook
    protected void onSuccess(T result) {

    }

    // Callback hook
    protected void onError(Throwable t) {

    }

    public abstract String getDescription();
}
