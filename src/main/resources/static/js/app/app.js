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
var app = angular.module('beagleApp', ['ui.router', 'ui.bootstrap', 'ngStomp']);

app.constant('urls', {
});

app.config(['$stateProvider', '$urlRouterProvider', '$httpProvider',
    function($stateProvider, $urlRouterProvider, $httpProvider) {

        // Required by spring security to not sent authentication headers back to client (prevents browser's credentials dialog)
        $httpProvider.defaults.headers.common["X-Requested-With"] = 'XMLHttpRequest';

        $stateProvider
            .state('home', {
                templateUrl: 'views/home.html',
                url: '/home'
            })
            .state('login', {
                templateUrl: 'views/login.html',
                controller: 'LoginController',
                url: '/login'
            })
            .state('imports', {
                templateUrl: 'views/imports.html',
                controller: 'ImportController',
                url: '/imports'
            })
            .state('pages', {
                templateUrl: 'views/pages.html',
                controller: 'PageController',
                url: '/documents'
            })
            .state('jobs', {
                templateUrl: 'views/jobs.html',
                controller: 'JobController',
                url: '/jobs'
            })
            .state('search', {
                templateUrl: '/views/search.html',
                controller: 'SearchController',
                url: '/search'
            })
            .state('profile', {
                templateUrl: '/views/profile.html',
                controller: 'ProfileController',
                url: '/profile'
            })
            .state('admin', {
                templateUrl: 'views/admin.html',
                controller: 'AdminController',
                url: '/admin'
            });
        $urlRouterProvider.otherwise('/login');
    }]);

app.run(['AuthService', function(AuthService) {
    AuthService.authenticate();
}]);