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
// TODO MVR implement check of "logged in" to every page. At the moment it is only executed when on login page
    .controller('NewUserModalCtrl', ['$scope', '$http', function($scope, $http) {
        $scope.user = {};

        $scope.update = function() {
            console.log("clicked");
        };

        $scope.submitForm = function(valid) {
            console.log("clicked", $scope.user);
            $scope.passwordsDontMatch = ($scope.user.password !== $scope.user.confirm);
            if (valid && false === $scope.passwordsDontMatch) {
                $http.post("/users", $scope.user).then(function(response) {
                    console.log("SUCCESS", response);
                    $scope.$close();
                }, function(response) {
                    console.log("ERROR", response);
                })
            }
        }

    }])
    .controller('AdminController',
        ['$scope', '$state', '$http', '$uibModal',  function($scope, $state, $http, $uibModal) {

            // Load User data
            $scope.users = [];
            $scope.reload = function() {
                $http.get("/users").then(function (response) {
                    if (response.data) {
                        $scope.users = response.data;
                        console.log($scope.users);
                    } else {
                        $scope.users = [];
                    }
                }, function (response) {
                    // TODO MVR implement error handling
                });
            };

            // Open Add New User Modal
            $scope.openNewUserModal = function (size) {
                console.log("WTF");
                var modalInstance = $uibModal.open({
                    animation: true,
                    ariaLabelledBy: 'modal-title',
                    ariaDescribedBy: 'modal-body',
                    templateUrl: '/partials/modals/new-user.html',
                    controller: 'NewUserModalCtrl',
                    controllerAs: '$ctrl',
                    // appendTo: angular.element('#new-user-modal'),
                    size: size,
                });
                modalInstance.result.then(function() {
                    $scope.reload();
                });
            };

            $scope.reload();
        }
        ]);
