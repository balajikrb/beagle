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

var HtmlScreenshotReporter = require('protractor-jasmine2-screenshot-reporter');

var reporter = new HtmlScreenshotReporter({
    dest: 'target/screenshots',
    filename: 'my-report.html'
});

exports.config = {
    capabilities: {
	directConnect: true,
        browserName: 'chrome',
        chromeOptions: {
            args: ["--headless", "--no-sandbox", "--disable-gpu", "--window-size=1920,1080"]
        }
    },

    // Setup the report before any tests start
    beforeLaunch: function() {
        return new Promise(function(resolve){
            reporter.beforeLaunch(resolve);
        });
    },

    // Prepare each running instance
    onPrepare: function() {
        // Hack the base Url
        if (browser.baseUrl === undefined || browser.baseUrl === '') {
            browser.baseUrl = 'http://localhost:8080';
        }
        console.log("Using baseUrl: " + browser.baseUrl);

        // Add report capabilities
        jasmine.getEnv().addReporter(reporter);
    },

    // Close the report after all tests finish
    afterLaunch: function(exitCode) {
        return new Promise(function(resolve){
            reporter.afterLaunch(resolve.bind(this, exitCode));
        });
    },

    seleniumAddress: 'http://localhost:4444/wd/hub',
    specs: ['**/specs/*.js']
};
