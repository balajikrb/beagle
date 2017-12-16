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
        ['$scope', '$http', '$state', '$stateParams',  function($scope, $http, $state, $stateParams) {

            $scope.count = {
                imported: 0,
                indexed: 0
            };
            $scope.successMessage = '';
            $scope.errorMessage = '';
            $scope.searchResult = undefined;
            $scope.searchQuery = '';
            $scope.selectedItem = undefined;

            $scope.select = function (item) {
                console.log("clicked " + item.id);
                if (item.payload === undefined) {
                    $http.get('/pages/' + item.id, {responseType: 'arraybuffer'})
                        .then(function (data) {
                            // TODO MVR revisit this and find a better solution
                            // Convert response to BASE64 String
                            // We do this, as different other approaches failed:
                            //  - Just use /pages/<id> failed, as sometimes the pdf was not refreshed and an empty page was shown instead
                            //  - Use <embed src={{url}}> did not work, as angular does not want to interpolate src and different solutions for this did not work at all (e.g. $sce.trust*)
                            // This is very hacky, but seems to be the only reliably solution for now.
                            var bytes = data.data;
                            var binary = '';
                            var bytes = new Uint8Array( bytes );
                            var len = bytes.byteLength;
                            for (var i = 0; i < len; i++) {
                                binary += String.fromCharCode( bytes[ i ] );
                            }
                            item.payload = window.btoa( binary );
                            $scope.selectedItem = item;
                        });
                } else {
                    $scope.selectedItem = item;
                }
            };

            $scope.doSearch = function () {
                // ignore empty searches
                if ($scope.searchQuery === null || $scope.searchQuery === undefined || $scope.searchQuery.trim().length == 0) {
                    return;
                }
                $scope.errorMessage = '';
                $http.get("/pages/search", {params: {query: $scope.searchQuery}}).then(function(response) {
                    console.log("success", response);
                    if (response.data && Array.isArray(response.data)) {
                        $scope.searchResult = [];
                        for (var i=0; i<response.data.length; i++) {
                            var page = response.data[i];
                            var item = {
                                id: page.id,
                                name: page.name,
                                state: page.state,
                                pageNumber: page.pageNumber,
                                payload: void 0
                            };
                            console.log(item);
                            $scope.searchResult.push(item);
                        }
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
                $http.get('/pages/count')
                    .then(function(response) {
                        if (response.data) {
                            $scope.count = response.data;
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

            // Count
            $scope.count();

            // Perform Search if invoked
            console.log($stateParams);
            if ($stateParams.query) {
                $scope.searchQuery = $stateParams.query;
                $scope.doSearch();
            }

        }
        ]);
