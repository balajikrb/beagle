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

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

import de.keybird.beagle.jobs.JobContext;
import de.keybird.beagle.repository.UserRepository;
import de.keybird.beagle.security.User;
import de.keybird.beagle.security.UserState;

@SpringBootApplication
@EnableScheduling
public class BeagleApplication implements CommandLineRunner {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private JobContext jobContext;

	public static void main(String[] args) {
		SpringApplication.run(BeagleApplication.class, args);
	}

	@Override
	public void run(String... strings) throws Exception {
		logger.info("Working directory: {}", jobContext.getWorkingPath());
		logger.info("Inbox directory: {}", jobContext.getInboxPath());
		logger.info("Archive directory: {}", jobContext.getArchivePath());


		// Create dummy user(s) for now
		// TODO MVR remove later
		final User test = new User();
		test.setEmail("test@keybird.de");
		test.setName("Test User");
		test.setState(UserState.Active);
		test.setRegisterDate(new Date());
		test.setPassword(passwordEncoder.encode("test"));

		// TODO MVR remove later
		final User mvr = new User();
		mvr.setEmail("m.v.rueden@keybird.de");
		mvr.setName("Markus von Rüden");
		mvr.setState(UserState.Active);
		mvr.setRegisterDate(new Date());
		mvr.setPassword(passwordEncoder.encode("test"));

		// TODO MVR remove later
		final User fs = new User();
		fs.setEmail("f.singer@keybird.de");
		fs.setName("Florian Singer");
		fs.setState(UserState.Active);
		fs.setRegisterDate(new Date());
		fs.setPassword(passwordEncoder.encode("test"));

		userRepository.save(mvr);
		userRepository.save(fs);
		userRepository.save(test);
	}

}
