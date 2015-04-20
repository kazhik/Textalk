(function() {
    'use strict';

    angular.module('textalk')
        .directive('draw', [drawDirective]);
    
    function drawDirective () {
        var canvas;
        var canvasCtx;
        var pathList = [];
        var currentTouch = {};
        var publicObj = {
            template: '<canvas></canvas>',
            restrict: 'E',
            scope: {
                width: '@',
                height: '@',
                lineWidth: '@',
                strokeStyle: '@',
                control: '='  
            },
            link: link
        };
        
        function convertTouchEvent (touch) {
            var rect = canvas.getBoundingClientRect();
            return {
                x: touch.pageX - rect.left,
                y: touch.pageY - rect.top
            };
            
        }
        function storePath() {
            var path = angular.copy(currentTouch.paths);
            pathList.push(path);
            currentTouch.paths.length = 0;
        }
        function pickTouchById (touches, id) {
            var touch;
            for (var i = 0; i < touches.length; i++) {
                if (touches[i].identifier === id) {
                    touch = touches[i];
                    break;
                }
            }
            return touch;
        }

        function onTouchStart (ev) {
            ev.preventDefault();
            var touch = ev.changedTouches[0];
            var pt = convertTouchEvent(touch);
            canvasCtx.beginPath();
            canvasCtx.moveTo(pt.x, pt.y);

            currentTouch.identifier = touch.identifier;
            currentTouch.paths = [pt];
        }
        function onTouchMove (ev) {
            ev.preventDefault();
            
            var touch = pickTouchById(ev.changedTouches, currentTouch.identifier);
            var pt = convertTouchEvent(touch);
            
            canvasCtx.lineTo(pt.x, pt.y);
            canvasCtx.stroke();

            currentTouch.paths.push(pt);
        }
        function onTouchEnd (ev) {
            ev.preventDefault();
            canvasCtx.closePath();
            
            storePath();
        }
        function clear() {
            canvasCtx.clearRect(0, 0, canvas.width, canvas.height);
            pathList.length = 0;
            currentTouch.length = 0;
        }
        function undo() {
            pathList.pop();
            redraw();
        }
        function getDataUrl () {
            return canvas.toDataURL();
        }
        function setDataUrl (dataUrl) {
            var img = new Image();
            img.src = dataUrl;
            img.onload = function () {
                canvasCtx.drawImage(img, 0, 0);
            };
        }
        function redraw() {
            canvasCtx.clearRect(0, 0, canvas.width, canvas.height);
            pathList.forEach(function (path, index) {
                for (var i = 0; i < path.length; i++) {
                    var pt = path[i];
                    if (i === 0) {
                        canvasCtx.beginPath();
                        canvasCtx.moveTo(pt.x, pt.y);
                    } else {
                        canvasCtx.lineTo(pt.x, pt.y);
                        canvasCtx.stroke();
                    }
                    if (i === path.length - 1) {
                        canvasCtx.closePath();
                    }
                }
            });
        }

        function link (scope, element, attrs) {
            if (angular.isUndefined(scope.control)) {
                scope.control = {};
            }
            scope.control.undo = undo;
            scope.control.clear = clear;
            scope.control.getDataUrl = getDataUrl;
            scope.control.setDataUrl = setDataUrl;
            
            canvas = element.find('canvas')[0];
            canvas.width = parseInt(scope.width, 10);
            canvas.height = parseInt(scope.height, 10);
            
            canvasCtx = canvas.getContext('2d');
            canvasCtx.lineWidth = parseInt(scope.lineWidth, 10);
            canvasCtx.strokeStyle = scope.strokeStyle;
            
            canvas.addEventListener('touchstart', onTouchStart);
            canvas.addEventListener('touchmove', onTouchMove);
            canvas.addEventListener('touchend', onTouchEnd);
            
            clear();
        }

        return publicObj;

    }
})();