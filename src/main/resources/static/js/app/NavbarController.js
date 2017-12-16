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
    .controller('NavbarController',
        ['$interval', '$scope', '$http', '$state', 'AuthService',  function($interval, $scope, $http, $state, AuthService) {

            $scope.profile = AuthService.profile;
            $scope.newImportsAvailable = false;

            $scope.search = function () {
                console.log("SEARCH");
                if ($scope.searchQuery && $scope.searchQuery.trim().length > 0) {
                    $state.go("search", {query: $scope.searchQuery});
                }
            };

            $scope.logout = function() {
                console.log("LOGOUT");
                AuthService.logout();
            };

            $scope.triggerDetection = function() {
                $http.post("/jobs/detect").then(function(response) {
                    console.log("Trigger DETECTION was successful");
                }, function(error) {
                    console.log(error);
                });
            };

            $scope.triggerImport = function() {
                $http.post("/jobs/import").then(function(response) {
                    console.log("Trigger IMPORT was successful");
                }, function(error) {
                    console.log(error);
                });
            };

            $scope.triggerIndex = function() {
                $http.post("/jobs/index").then(function(response) {
                    console.log("Trigger Index was successful");
                }, function(error) {
                    console.log(error);
                });
            };
        }
        ]);