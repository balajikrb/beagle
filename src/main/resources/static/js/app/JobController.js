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

angular.module('beagleApp')
    .controller('JobController',
        ['$scope', '$state', '$http',  function($scope, $state, $http) {

            $scope.jobs = [];
            $scope.errorMessage = '';

            $http.get("/jobs").then(function(response) {
                console.log("success", response);

                // log level to bootstrap coler
                var colorMapping = {
                    'Info': 'secondary',
                    'Success': 'success',
                    'Warn': 'warning',
                    'Error': 'danger',
                    'Failed': 'danger',
                };

                if (response.data && Array.isArray(response.data)) {
                    $scope.jobs = [];
                    for (var i=0; i<response.data.length; i++) {
                        var entry = response.data[i];
                        var jobInfo = {
                            id: entry.id,
                            startTime: entry.startTime,
                            name: entry.type + " Job",
                            duration: entry.completeTime - entry.startTime,
                            status : entry.errorMessage ? "Failed" : "Success",
                            hasWarnings: false,
                            hasErrors: false,
                            logs: entry.logs.slice(0)
                        };

                        // Apply color
                        jobInfo.color = colorMapping[jobInfo.status];

                        // If job ran successful, some items may caused warnings/errors
                        for (var a=0; a<jobInfo.logs.length; a++) {
                            var logEntry = jobInfo.logs[a];

                            if (logEntry.level === 'Warn') {
                                jobInfo.hasWarnings = true;
                                jobInfo.color = 'warning';
                            }
                            if (logEntry.level === 'Error') {
                                jobInfo.hasErrors = true;
                                jobInfo.hasWarnings = false;
                                jobInfo.color = 'danger';
                            }
                            logEntry.color = colorMapping[logEntry.level];
                        }

                        $scope.jobs.push(jobInfo);
                    }
                    console.log($scope.jobs);
                } else {
                    $scope.errorMessage = "Something went wrong"; // TODO MVR do more of this...
                }
            }, function(response) {
                $scope.errorMessage = "An error occurred: " + response.status + " " + response.data;
            });

        }
        ]);
