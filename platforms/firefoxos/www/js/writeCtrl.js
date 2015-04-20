(function() {
    'use strict';

    angular.module('textalk')
        .controller('WriteCtrl', WriteCtrl)
        ;

    WriteCtrl.$inject = ['$scope', '$ionicModal',
                         'Database', 'Config', 'Chat'];
    function WriteCtrl ($scope, $ionicModal,
                        Database, Config, Chat) {
        initEventHandler();
        Chat.setOnTextCallback(onReceiveText);
        
        function onError (err) {
            console.log(err);
        }
        function onReceiveText(address, msg) {
            $scope.msgs.push(msg);
            $scope.$apply();
        }
        function storeMessage(msg) {
            function onGetSuccess (data) {
                if (data) {
                    msg.timesUsed = data.timesUsed + 1;
                } else {
                    msg.timesUsed = 1;
                }
                return Database.put('History', msg);
            }
            
            return Database.get('History', msg.body)
                .then(onGetSuccess)
                .catch(onError);
            
        }
        function onSubmit (msgtext) {
            var msg = {
                body: msgtext,
                timestamp: Date.now()
            };
            $scope.msgs.push(msg);
            storeMessage(msg);
            Chat.broadcastText(msgtext);
            
            $scope.msgtext = '';
        }
        function openHistoryDialog () {
            function onSelect (item) {
                $scope.modal.hide();
                $scope.msgs.push(item);
                Chat.broadcastText(item.body);
                
            }
            function openModal (modal) {
                $scope.modal = modal;
                $scope.modal.show();
                
            }
            function showModal (resultList) {
                var maxCount = Config.get(['history', 'maxCount']);
                $scope.historyList = resultList.slice(0, maxCount);
                $scope.onSelect = onSelect;

                var option = {
                    scope: $scope
                };
                $ionicModal.fromTemplateUrl('history.html', option)
                    .then(openModal);
            }
            Database.getByIndex('History', 'timesUsed', 'prev')
                .then(showModal)
                .catch(onError);    
            
        }
        
        function initEventHandler () {
            $scope.msgs = [];
            $scope.onSubmit = onSubmit;
            $scope.openHistoryDialog = openHistoryDialog;
        }
        
    }
    
})();

