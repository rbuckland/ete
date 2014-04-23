'use strict';

/* Services */

var services = angular.module('AppCore.services', []);

/**
 * A Factory to create a new file upload handler
 */
services.factory('uploadHandler', ['$upload', function($upload) {

    var uploadHandler = {};

    uploadHandler.handleUpload = function(filesForExtract,postUrl,headerValueMap,callback) {


        //$files: an array of files selected, each file has name, size, and type.
        for (var i = 0; i < filesForExtract.length; i++) {
            var file = filesForExtract[i];

            var headers = {'Upload-File-Name': file.name, 'Upload-File-Size' : file.size }

            for (i in headerValueMap) {
                headers[i] = headerValueMap[i]
            }

            var upload = $upload.upload({
                url: postUrl,
                method: "POST",
                // headers: {'headerKey': 'headerValue'},
                headers: headers,
                // withCredentials: true,
                file: file
                // file: $files, //upload multiple files, this feature only works in HTML5 FromData browsers
                /* set file formData name for 'Content-Desposition' header. Default: 'file' */
                //fileFormDataName: myFile, //OR for HTML5 multiple upload only a list: ['name1', 'name2', ...]
                /* customize how data is added to formData. See #40#issuecomment-28612000 for example */
                //formDataAppender: function(formData, key, val){} //#40#issuecomment-28612000
            }).progress(function(evt) {
                    console.log('percent: ' + parseInt(100.0 * evt.loaded / evt.total));
            }).success(function(data, status, headers, config) {
                    console.log("data returned: " + data.length)
                    // file is uploaded successfully
                    callback(data)
                    // $scope.filesAdded[$scope.filesAdded.length] = data
            }).error(function(error){
                    callback(null,error)
                    console.log(error)
            })
            //.then(success, error, progress);
        }
    };

    return uploadHandler;

}]);