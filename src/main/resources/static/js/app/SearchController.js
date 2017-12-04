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
    .controller('SearchController',
        ['$scope', '$http', '$state',  function($scope, $http, $state) {

            $scope.documentCount = 0;
            $scope.preview = undefined;
            $scope.successMessage = '';
            $scope.errorMessage = '';
            $scope.searchResult = undefined;
            $scope.searchQuery = '';
            $scope.previewData = undefined;

            $scope.showPdf = function (selectedItem) {
                console.log("clicked");
                $scope.previewData = selectedItem.payload;
            };

            $scope.doSearch = function () {
                // ignore empty searches
                if ($scope.searchQuery.trim().length == 0) {
                    return;
                }
                $scope.errorMessage = '';
                $http.get("/profiles/search", {params: {query: $scope.searchQuery}}).then(function(response) {
                        console.log("success", response);

                        if (response.data) {
                            $scope.searchResult = response.data;
                        } else {
                            $scope.searchResult = undefined;
                            $scope.errorMessage = "Something went wrong"; // TODO MVR do more of this...
                        }
                    }, function(response) {
                        console.log("error", response);
                        $scope.errorMessage = "An error occurred: " + response.status + " " + response.data;
                    });
            };

            $scope.count = function () {
                $http.get('/profiles/count')
                    .then(function(response) {
                        if (response.data) {
                            $scope.documentCount = response.data;
                        }
                        console.log(response);
                    },
                    function(error) {
                        console.log(error);
                        $scope.errorMessage = error;
                    });
            };

            $scope.logout = function() {
                $state.go('login')
            };

            $scope.count();
        }
        ]);
