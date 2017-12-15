/*
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

'use strict';

describe('Beagle Tests', function() {
    describe('login page', function() {
        it('should load', function() {
            browser.get(browser.baseUrl);

            // Verify login page
            expect(element(by.name('loginBtn')).getText()).toBe("Login");
            expect(element(by.xpath("//h2")).getText()).toContain("Project Beagle");
        });

        it('login works', function() {
            browser.get(browser.baseUrl);

            // Login
            element(by.id("inputEmail")).clear().sendKeys("test@keybird.de");
            element(by.id("inputPassword")).clear().sendKeys("test");
            element(by.name("loginBtn")).click();

            // Verify logged in
            expect(element(by.xpath("//main/h2")).getText()).toBe("Home");
        });
    });

    describe('Detect Job', function() {
       it('should show progress', function() {
            element(by.id("refreshBtn")).click();
            var jobs = element.all(by.repeater("job in jobs"));
            expect(jobs.count()).toEqual(1);
       }) ;
    });

});