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

package de.keybird.beagle.db;

import java.util.Date;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import de.keybird.beagle.repository.UserRepository;
import de.keybird.beagle.security.User;
import de.keybird.beagle.security.UserState;

// TODO MVR this is ugly as hell, try to not do this
@Component
@Profile({"test", "dev"})
public class DatabaseInitialization {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        // Create dummy user(s) for now
        final User test = new User();
        test.setEmail("test@keybird.de");
        test.setName("Test User");
        test.setState(UserState.Active);
        test.setRegisterDate(new Date());
        test.setPassword(passwordEncoder.encode("test"));

        final User mvr = new User();
        mvr.setEmail("m.v.rueden@keybird.de");
        mvr.setName("Markus von Rüden");
        mvr.setState(UserState.Active);
        mvr.setRegisterDate(new Date());
        mvr.setPassword(passwordEncoder.encode("test"));

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
