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
    .factory('AuthService',
        ['$rootScope', '$http', '$state', function($rootScope, $http, $state) {
            return {
                authenticated: false,
                profile: {},

                authenticate : function(credentials, callback) {
                    var self = this;
                    var headers = {};
                    if (credentials) {
                        headers['authorization'] = "Basic " + btoa(credentials.email + ":" + credentials.password)
                    }

                    $http.get('user', {headers : headers})
                        .then(
                            function(response) {
                                if (response.data && response.data.id) {
                                    self.profile = response.data;
                                    self.authenticated = true;

                                    // if no image is defined, we assign the user one automatically
                                    if (!self.profile.image) {
                                        var numberOfDummyAvatars = 4;
                                        var avatarIndex = btoa(self.profile.name).charCodeAt(0) % (numberOfDummyAvatars - 1);
                                        avatarIndex++;
                                        self.profile.image = "/img/avatars/avatar" + avatarIndex + ".jpg";
                                    }
                                } else {
                                    self.profile = {};
                                    self.authenticated = false;
                                }
                                callback && callback();
                            },
                            function() {
                                self.authenticated = false;
                                callback && callback();
                                // there is no callback, but authentication failed, this probably means we tried instantiating
                                // the application, but don't have authorization. Forward to login page
                                // TODO MVR there must be a more elegant way of doing authorization, thatn what we are currently doing
                                if (!callback) {
                                    $state.go("login");
                                }
                            }
                        );
                },
                logout: function() {
                    var self = this;
                    var handleLogout = function() {
                        self.authenticated = false;
                        $state.go("login");
                    };
                    $http.post('logout', {}).then(handleLogout);
                },
                isAuthenticated: function() {
                    return this.authenticated === true;
                }
            };
        }]
    );