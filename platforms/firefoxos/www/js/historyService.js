(function() {
    'use strict';

    angular.module('textalk')
        .factory('History', history);

    history.$inject = ['$q', 'AppInfo', 'Database'];
    function history (q, AppInfo, Database) {

        var history = {};
        var publicObj = {
            get: get,
            set: set,
            save: save,
            load: load,
            reset: reset
        };
        
        function get(keys) {
            var conf = history;
            for (var i = 0; i < keys.length; i++) {
                if (typeof conf[keys[i]] === 'undefined') {
                    console.log('history get error: ' + keys[i]);
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
            var conf = history;
            for (var i = 0; i < keys.length - 1; i++) {
                conf = conf[keys[i]];
            }
            conf[keys[keys.length - 1]] = value;
        }
        function save(newConfig) {
            var onSuccess = function () {
                dfd.resolve();
            };
            var onError = function(err) {
                dfd.reject(err);
            };
    
            history['debug'] = newConfig['debug'];
    
            var dfd = q.defer();
            Database.put('Config', history)
                .then(onSuccess, onError);
            
            return dfd.promise;
        }
        function load() {
            var onSuccess = function (result) {
                if (typeof result !== 'undefined') {
                    history = result;
                } else {
                    history = historyDefault;
                }
                dfd.resolve();
            };
            var onError = function(err) {
                history = historyDefault;
                dfd.reject(err);
            };
            var dfd = q.defer();
    
            Database.get('Config', AppInfo.name)
                .then(onSuccess, onError);
    
            return dfd.promise;        
        }
        function reset() {
            history = historyDefault;
            return save(history);
        }
        
        return publicObj;
       
    }

        
 })();