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
    .controller('PageController',
        ['$scope', '$state', '$http', 'AuthService',  function($scope, $state, $http, AuthService) {

            $scope.pages = [];
            $scope.errorMessage = '';

            $http.get("/pages").then(function(response) {
                console.log("success", response);

                if (response.data) {
                    $scope.pages = response.data;
                } else {
                    $scope.errorMessage = "Something went wrong"; // TODO MVR do more of this...
                }
            }, function(response) {
                $scope.errorMessage = "An error occurred: " + response.status + " " + response.data;
            });
        }
        ]);
