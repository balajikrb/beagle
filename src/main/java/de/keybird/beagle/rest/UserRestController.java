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

package de.keybird.beagle.rest;

import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import de.keybird.beagle.repository.UserRepository;
import de.keybird.beagle.security.User;
import de.keybird.beagle.security.UserState;

@RestController
public class UserRestController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @RequestMapping(value="/user", method = RequestMethod.GET)
    public User user(Principal user) {
        final User theUser = userRepository.findByEmail(user.getName(), UserState.Active);
        return theUser;
    }

    @RequestMapping(value="/users", method = RequestMethod.GET)
    public List<User> listUsers() {
        return StreamSupport.stream(userRepository.findAll().spliterator(), false)
            .map(user -> {
                User newUser = new User(user);
                newUser.setPassword(null);
                return newUser;
            })
            .collect(Collectors.toList());

    }

    // TODO MVR verify that user does not already exist
    @RequestMapping(value="/users", method = RequestMethod.POST)
    public ResponseEntity createUser(@RequestBody CreateUserDTO newUser) {
        if (!newUser.getPassword().equals(newUser.getConfirm())) {
            return ResponseEntity.badRequest().build();
        }
        User user = new User();
        user.setRegisterDate(new Date());
        user.setName(newUser.getName());
        user.setPassword(passwordEncoder.encode(newUser.getPassword()));
        user.setEmail(newUser.getEmail());
        user.setState(UserState.Active);
        userRepository.save(user);

        return ResponseEntity.ok().build();
    }
}
