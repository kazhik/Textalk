(function() {
    'use strict';

    angular.module('textalk', ['ionic',
                               'pascalprecht.translate',
                               'textalk.tcp',
                               'textalk.chat',
                               'textalk.database'])
        .config(routes)
        .run(init)
        .filter('datetimestr', datetimestr)
        ;
    
    function datetimestr () {
        function timestampToString (timestamp) {
            var str = new Date(timestamp).toLocaleString(navigator.language);
            console.log(navigator.language + ':' + timestamp + ' --> ' + str);
            return str;
        }
        return timestampToString;
    }
    
    init.$inject = ['$ionicPlatform', '$translate',
                    'AppInfo', 'Database', 'Config', 'Chat', 'Tcp'];
    function init($ionicPlatform, $translate,
                  AppInfo, Database, Config, Chat, Tcp) {

        $ionicPlatform.ready(ionicOnReady);
        
        initDb()
            .then(loadConfig)
            .then(startApp)
            .catch(onError);
        
        function initDb () {
            var dbInfo = {
                name: AppInfo.name,
                version: 6,
                objStore: [
                    {
                        name: "Config",
                        keyPath: "appname"
                    },
                    {
                        name: "History",
                        keyPath: "body",
                        indexes: ['timesUsed']
                    }
                ]
            };
            
            return Database.open(dbInfo);
          
        }
        function onError (err) {
            console.log('failed to open db: ' + err);
        }
        function startApp () {
            console.log('Textalk started: ' + $translate.use());

            var ipaddr = Config.get(['IpAddress', 'myAddr']);
            if (ipaddr) {
                Chat.start(ipaddr, 62321);
            }
        }
        function loadConfig () {
            return Config.load();
        }
        
        function ionicOnReady () {
            // Hide the accessory bar by default
            // (remove this to show the accessory bar above the keyboard for form inputs)
            if (window.cordova && window.cordova.plugins.Keyboard) {
                cordova.plugins.Keyboard.hideKeyboardAccessoryBar(true);
            }
            if (window.StatusBar) {
                // org.apache.cordova.statusbar required
                StatusBar.styleDefault();
            }
        }
    }
    routes.$inject = ['$stateProvider', '$urlRouterProvider', '$compileProvider'];
    function routes($stateProvider, $urlRouterProvider, $compileProvider) {

        $stateProvider
            .state('app', {
                url: '/app',
                abstract: true,
                templateUrl: 'templates/menu.html',
                controller: 'MenuCtrl'
            })
            .state('app.draw', {
                url: '/draw',
                views: {
                    'appContent' :{
                        templateUrl: 'templates/draw.html',
                        controller: 'DrawCtrl'
                    }
                }
            })
            .state('app.write', {
                url: '/write',
                views: {
                    'appContent' :{
                        templateUrl: 'templates/write.html',
                        controller: 'WriteCtrl'
                    }
                }
            })
            .state('app.config', {
                url: '/config',
                views: {
                    'appContent' :{
                        templateUrl: 'templates/config.html',
                        controller: 'ConfigCtrl'
                    }
                }
            })
            ;
        $urlRouterProvider.otherwise('/app/write');

        $compileProvider.aHrefSanitizationWhitelist(/^\s*(https?|ftp|mailto|app):/);
    
    };
   
})();

