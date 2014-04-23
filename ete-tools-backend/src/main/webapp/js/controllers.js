'use strict';

/* Declare the Main controller Module */

var controllers = angular.module('AppCore.controllers', []);

controllers.controller('CtrlMain',['$scope',function($scope) {

    $scope.version = "0.2"

}]);

controllers.controller('EteController',[ '$scope', 'uploadHandler',function($scope, uploadHandler) {

    $scope.outputStyle = "JSON"

    $scope.filesForExtract = [];

    $scope.onFileSelect = function($files) {
        $scope.filesForExtract = $files
    }

    var callbackForData = function(data,error) {
        if (data == null) {
            $scope.resultData = "An error occured: (sorry the error handling is not that great yet) (some errors occur with the XLS conversion)"
        } else {
            if ($scope.outputStyle == "JSON") {
              $scope.resultData = vkbeautify.json(data)
            } else if ($scope.outputStyle == "XML") {
              $scope.resultData = vkbeautify.xml(data);
            }
        }
    }

    $scope.extract = function() {
        uploadHandler.handleUpload($scope.filesForExtract,"/api/ete/extract/" + $scope.outputStyle,{},callbackForData)
    }
}]);



controllers.controller('H2GeneratorController',[ '$scope', 'uploadHandler', function($scope, uploadHandler) {

    $scope.filesToConvert = [];
    $scope.databaseName = 'sample_db';
    $scope.setFormScope = function(form) {
        $scope.uploadForm = form
    }

    $scope.onFileSelect = function(files) {
        $scope.filesToConvert = files
    }

    $scope.convertToH2 = function() {
        console.log("Submit was called")
        $scope.uploadForm.action = "/api/h2generator"
        $scope.uploadForm.submit
        //uploadHandler.handleUpload($scope.filesToConvert,"/api/h2generator",{'DatabaseName' : $scope.databaseName })
    }

}]);