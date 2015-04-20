(function() {
    'use strict';

    angular.module('textalk')
        .controller('DrawCtrl', DrawCtrl)
        ;

    DrawCtrl.$inject = ['$scope'];
    function DrawCtrl ($scope) {
        init();
        
        function undo () {
            $scope.CanvasControl.undo();
        }
        function clear () {
            $scope.CanvasControl.clear();
        }
        function send () {
            var dataUrl = $scope.CanvasControl.getDataUrl();
            console.log('dataUrl: ' + dataUrl);
        }
        function getHeaderHeight () {
            var header = angular.element( document.querySelector( 'ion-nav-bar' ) );
    
            return header.prop('offsetHeight');
        }
        function getToolbarHeight () {
            var toolbar = angular.element( document.querySelector( 'ion-header-bar' ) );
            
            return toolbar.prop('offsetHeight');
        }
        
        function calculateContentHeight () {
            
            return window.innerHeight - getHeaderHeight() - getToolbarHeight();
        }
        function init () {
            $scope.CanvasControl = {};
            $scope.undo = undo;
            $scope.clear = clear;
            $scope.send = send;
            
            $scope.canvas = {
              height: calculateContentHeight(),
              width: window.innerWidth,
            };
            
        }
    }

    
})();

