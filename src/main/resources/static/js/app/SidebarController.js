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
    .controller('SidebarController',
        ['$stomp', '$interval', '$scope', '$http', '$state', function($stomp, $interval, $scope, $http, $state) {

            var colorMap = {
                'Pending': 'secondary',
                'Running': 'primary',
                'Completed': 'success',
                'Initializing': 'info',
            };

            // // TODO MVR
            $scope.jobs = [];
            // TODO MVR add authentication
            $stomp.connect('http://localhost:8080/beagle-websocket', {})
                .then(function (frame) {
                    var subscription = $stomp.subscribe('/topic/jobs',
                        function (payload, headers, res) {
                            if (payload && Array.isArray(payload)) {
                                console.log(payload);
                                $scope.jobs = [];
                                for(var i=0; i<payload.length; i++) {
                                    var job = payload[i];
                                    var jobData = {
                                        id: job.id,
                                        startTime: job.startTime,
                                        name: job.description,
                                        description: job.state,
                                        progress: {
                                            indeterminate: job.progress.indeterminate,
                                            current: job.progress.progress,
                                            total: job.progress.totalProgress,
                                        },
                                        state: job.state,
                                        color: job.errorMessage ? 'danger' : colorMap[job.state]
                                    };
                                    if (jobData.progress.indeterminate === false) {
                                        if (jobData.progress.current === 0 && jobData.progress.total === 0) {
                                            jobData.progress.percent = 100;
                                        } else {
                                            jobData.progress.percent = jobData.progress.current / jobData.progress.total * 100;
                                        }
                                    } else {
                                        jobData.progress.percent = 100;
                                    }
                                    $scope.jobs.push(jobData);
                                }
                                $scope.jobs.sort(function(a, b) {
                                    return b.startTime - a.startTime;
                                });
                                $scope.$apply();
                            }
                        });

                });

        }
        ]);