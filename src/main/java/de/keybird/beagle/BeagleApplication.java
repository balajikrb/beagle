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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

import de.keybird.beagle.repository.DocumentRepository;
import de.keybird.beagle.repository.JobRepository;
import de.keybird.beagle.repository.PageRepository;
import de.keybird.beagle.repository.UserRepository;
import de.keybird.beagle.security.User;
import de.keybird.beagle.security.UserState;

@SpringBootApplication
@EnableScheduling
public class BeagleApplication implements CommandLineRunner {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private JobRepository jobRepository;

	@Autowired
	private DocumentRepository documentRepository;

	@Autowired
	private PageRepository pageRepository;

	public static void main(String[] args) {
		SpringApplication.run(BeagleApplication.class, args);
	}

	@Override
	public void run(String... strings) throws Exception {
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

//		Document document = new Document();
//		document.setFilename("My file name.pdf");
//		document.setPageCount(1);
//
//		Page profile = new Page();
//		profile.setImport(document);
//		profile.setName("01");
//
//		documentRepository.save(document);
//		profileRepository.save(profile);
//
//
//
//		final DetectJobEntity detectJobEntity = new DetectJobEntity();
//		detectJobEntity.setCreatTime(new Date());
//		detectJobEntity.setCompleteTime(new Date());
//		detectJobEntity.setStartDate(new Date());
//
//		final LogItem logManifestItem = new LogItem();
//		logManifestItem.setMessage("XXX");
//		detectJobEntity.addManifestItem(logManifestItem);
//
//		final DocumentItem documentItem = new DocumentItem();
//		documentItem.setDocument(document);
//
//		final PageItem pageItem = new PageItem();
//		pageItem.setPage(profile);
//
//		detectJobEntity.addManifestItem(documentItem);
//		detectJobEntity.addManifestItem(pageItem);
//
//		jobRepository.save(detectJobEntity);
//		jobRepository.save(new ImportJobEntity());
	}

}
