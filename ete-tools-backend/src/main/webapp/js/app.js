'use strict';

var angularAppCore = angular.module('AppCore',
    [
        // provides a simple file upload control
        'angularFileUpload',

        // provides the Angular UI Bootstrap stuff
        'ui.bootstrap',

        'ngRoute',

        // Our modules
       'AppCore.filters', 'AppCore.services', 'AppCore.directives', 'AppCore.controllers'
    ]);

/*
 This is the main View "router"
 */
angularAppCore.config(['$routeProvider', function($routeProvider) {
    $routeProvider.when('/main', {templateUrl: 'templates/main.html', controller: 'CtrlMain'});
    $routeProvider.when('/h2generator', {templateUrl: 'templates/h2generator.html', controller: 'H2GeneratorController'});
    $routeProvider.when('/ete', {templateUrl: 'templates/ete-extractor.html', controller: 'EteController'});
    $routeProvider.otherwise({redirectTo: '/main'});
}])
;