"use strict";

if (Textalk === undefined) {
    var Textalk = {};
}
Textalk.CanvasView = function() {
    var canvasCtx;
    var canvas;
    var headerHeight = 0;
    var pathList = [];
    var currentPath = [];
    
    function onPageShow(e, data) {
        canvas = $('#hwCanvas')[0];
        canvas.width = $(window).width();
        canvas.height = Textalk.ViewUtil.getRealContentHeight();
        
        canvasCtx = canvas.getContext("2d");
        canvasCtx.lineWidth = 2;
        canvasCtx.strokeStyle = "#000";

        headerHeight = Textalk.ViewUtil.getHeaderHeight();
        
        $("#hwCanvas").on("touchstart", onTouchStart);
        $("#hwCanvas").on("touchmove", onTouchMove);
        $("#hwCanvas").on("touchend", onTouchEnd);

    }
    function addCurrentPath(touch) {
        var pt = {
            x: touch.pageX,
            y: touch.pageY
        };
        currentPath.push(pt);
    }
    function movePathToList() {
        var path = $.extend(true, [], currentPath);
        pathList.push(path);
        currentPath.length = 0;
    }
    function onTouchStart(ev) {
        ev.preventDefault();
        var touch = ev.originalEvent.changedTouches[0];
        canvasCtx.beginPath();
        canvasCtx.moveTo(touch.pageX, touch.pageY - headerHeight);

        addCurrentPath(touch);        
    }
    function onTouchMove(ev) {
        ev.preventDefault();
        var touch = ev.originalEvent.changedTouches[0];
        canvasCtx.lineTo(touch.pageX, touch.pageY - headerHeight);
        canvasCtx.stroke();

        addCurrentPath(touch);        
    }
    function onTouchEnd(ev) {
        ev.preventDefault();
        canvasCtx.closePath();
        
        movePathToList();
    }
    function clear() {
        canvasCtx.clearRect(0, 0, canvas.width, canvas.height);
        pathList.length = 0;
        currentPath.length = 0;
    }
    function undo() {
        pathList.pop();
        redraw();
    }
    function redraw() {
        function drawPath(path, index) {
            for (var i = 0; i < path.length; i++) {
                var pt = path[i];
                if (i === 0) {
                    canvasCtx.beginPath();
                    canvasCtx.moveTo(pt.x, pt.y - headerHeight);
                } else {
                    canvasCtx.lineTo(pt.x, pt.y - headerHeight);
                    canvasCtx.stroke();
                }
                if (i === path.length - 1) {
                    canvasCtx.closePath();
                }
            }
            
        }
        canvasCtx.clearRect(0, 0, canvas.width, canvas.height);
        pathList.forEach(drawPath);
    }
    function init() {
        $("#clear-draw").on("tap", clear);
        $("#undo-draw").on("tap", undo);
        
        $("#Draw").on("pageshow", onPageShow);
    }
    
    var publicObj = {};
    
    publicObj.init = function() {
        init();
        pathList.length = 0;
    };
    
    return publicObj;
}();