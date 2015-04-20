(function() {
    'use strict';

    angular.module('textalk')
        .factory('Config', configuration);

    configuration.$inject = ['$q', 'AppInfo', 'Database'];
    function configuration (q, AppInfo, Database) {
        var config = {
            appname: AppInfo.name
        };
        var publicObj = {
            get: get,
            set: set,
            save: save,
            load: load
        };
        
        function get(keys) {
            var conf = config;
            for (var i = 0; i < keys.length; i++) {
                if (typeof conf[keys[i]] === 'undefined') {
                    console.log('Failed to get config value: ' + keys);
                    return '';
                }
                conf = conf[keys[i]];
            }
            return conf;
        }
        function set(keys, value) {
            if (keys.length < 1) {
                return;
            }
            var conf = config;
            var key;
            for (var i = 0; i < keys.length - 1; i++) {
                key = keys[i];
                if (!conf[key]) {
                    conf[key] = {};
                }
                conf = conf[key];
            }
            conf[keys[keys.length - 1]] = value;
        }
        function save() {
            var onSuccess = function () {
                dfd.resolve();
            };
            var onError = function(err) {
                dfd.reject(err);
            };
            
            var dfd = q.defer();
            Database.put('Config', config)
                .then(onSuccess, onError);
            
            return dfd.promise;
        }
        function load() {
            var onSuccess = function (result) {
                if (typeof result === 'undefined') {
                    console.log('No config in database');
                } else {
                    config = result;
                }
                dfd.resolve();
            };
            var onError = function(err) {
                console.log('load error: ' + err);
                dfd.reject(err);
            };
            var dfd = q.defer();
    
            Database.get('Config', AppInfo.name)
                .then(onSuccess, onError);
    
            return dfd.promise;        
        }
        
        return publicObj;
       
    }

        
 })();