(function() {
    'use strict';

    angular.module('textalk')
        .controller('MenuCtrl', MenuCtrl)
        ;

    MenuCtrl.$inject = ['$scope', '$ionicPopup', '$ionicModal', 'AppInfo', 'Config'];
    function MenuCtrl ($scope, $ionicPopup, $ionicModal, AppInfo, Config) {
        $scope.openAbout = openAbout;
        $scope.openConfig = openConfig;
        
        function openConfig () {
            var historyConf = Config.get(['History']);
            
            $scope.history = {
                maxCount: {
                    options: [5, 10, 20],
                    selected: historyConf['maxCount'] || 5
                }
            };
            var ipaddr = Config.get(['IpAddress']);
            console.log('ipaddr: ' + angular.toJson(ipaddr));
            $scope.ipAddress = {
                myAddr: ipaddr['myAddr'] || '192.168.1.0'
            };
            $scope.onChanged = function (itemKeys, value) {
                Config.set(itemKeys, value);
                console.log('keys: ' + itemKeys + ' = ' + value);
            };
            $scope.onClose = function () {
                $scope.modal.hide();
                Config.save();
            };

            var option = {
                scope: $scope
            };
            $ionicModal.fromTemplateUrl('templates/config.html', option)
                .then(openModal);
            
            function validateIpAddress (str) {
                var re = /^\d{1,3}.\d{1,3}.\d{1,3}.\d{1,3}$/;
                
                return re.test(str);
            }
            function openModal (modal) {
                $scope.modal = modal;
                $scope.modal.show();
                
            }
        }
        function openAbout () {
            var options = {
                title: AppInfo.name,
                templateUrl: 'templates/about.html',
            };
            $ionicPopup.alert(options);
        }
    }
    
})();

