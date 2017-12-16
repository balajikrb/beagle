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

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import javax.transaction.Transactional;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import de.keybird.beagle.TestConfig;
import de.keybird.beagle.WorkingDirectory;
import de.keybird.beagle.jobs.execution.AbstractJobExecution;
import de.keybird.beagle.jobs.execution.JobType;
import de.keybird.beagle.jobs.persistence.DetectJobEntity;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = { "working.directory=" + TestConfig.WORKING_DIRECTORY })
@Transactional
public class JobExecutionManagerTest {

    @Rule
    public WorkingDirectory inboxDirectory = new WorkingDirectory(TestConfig.INBOX_DIRECTORY);

    @Autowired
    private JobExecutionManager jobExecutionManager;

    @Autowired
    private ApplicationContext applicationContext;

    // See https://github.com/Keybird/beagle/issues/1
    @Test(timeout=15000)
    public void verifyHasExecutions() {
        // Verify no execution currently in progress
        assertThat(jobExecutionManager.hasRunningJobs(), is(false));

        // Submit dummy job
        final DummyJobExecution dummyJobExecution = new DummyJobExecution();
        applicationContext.getAutowireCapableBeanFactory().autowireBean(dummyJobExecution);
        jobExecutionManager.submit(dummyJobExecution);

        // Verify execution is in progress
        assertThat(jobExecutionManager.hasRunningJobs(), is(true));
        assertThat(jobExecutionManager.getExecutions(JobType.Detect), Matchers.hasSize(1));

        // Wait until finished
        await().atMost(10, SECONDS).until(() -> !jobExecutionManager.hasRunningJobs());
        assertThat(jobExecutionManager.getExecutions(JobType.Detect), Matchers.hasSize(0));

    }

    // Simulates a long running execution
    private static class DummyJobExecution extends AbstractJobExecution<DetectJobEntity> {

        private DummyJobExecution() {
            setJobEntity(new DetectJobEntity());
        }

        @Override
        protected void executeInternal() throws Exception {
            Thread.sleep(5000);
        }

        @Override
        public String getDescription() {
            return "Long running dummy job";
        }
    }
}




