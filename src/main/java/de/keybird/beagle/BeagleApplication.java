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

package de.keybird.beagle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import de.keybird.beagle.jobs.execution.JobExecutionContext;

@SpringBootApplication
@EnableScheduling
public class BeagleApplication implements CommandLineRunner {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private JobExecutionContext jobContext;

	public static void main(String[] args) {
		SpringApplication.run(BeagleApplication.class, args);
	}

	@Override
	public void run(String... strings) {
		logger.info("Working directory: {}", jobContext.getWorkingPath());
		logger.info("Inbox directory: {}", jobContext.getInboxPath());
		logger.info("Archive directory: {}", jobContext.getArchivePath());
	}

	@Scheduled(fixedRate = 20000, initialDelay = 5000)
	@ConditionalOnProperty(prefix = "log.memoryUsage", value = "true")
	private void logMemoryUsage() {
		final Runtime runtime = Runtime.getRuntime();
		final long mega = 1024 * 1024;
		logger.info("Max memory: {} MB. Allocated memory: {} MB. Free memory: {} MB",
				runtime.maxMemory() / mega,
				runtime.totalMemory() / mega,
				runtime.freeMemory() / mega);
	}
}
