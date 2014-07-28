"use strict";

if (Textalk === undefined) {
    var Textalk = {};
}
Textalk.LogView = function() {
    function init() {
        $("#logList").listview().listview("refresh");
        $("#delete-log").on("tap", clearLog);

        $("#Log").on("pageshow", onPageShow);
        initLog();
    }
    function onPageShow() {
        $('#logList').height(Textalk.ViewUtil.getContentHeight());
    }
    function initLog() {
        var consolelog = console.log;
        console.log = function (message) {
            var log = {
                timestamp: Date.now(),
                message: message,
            };
            Textalk.Database.add("ConsoleLog", log);

            var logmsg = Textalk.StringUtil.formatTime(log.timestamp) + " " + message;
            $("#logList")
                .append($("<li/>")
                    .append(logmsg))
                .listview("refresh");            

            consolelog.apply(console, arguments);
        };
        
    }

    function clearLog() {
        function onConfirm() {
            Textalk.Database.clear("ConsoleLog");
            $('#logList').children().remove('li');
        }

        Textalk.PopupView.openConfirmDialog(
            navigator.mozL10n.get("clear-log-title"),
            navigator.mozL10n.get("clear-log-message"),
            navigator.mozL10n.get("clear"),
            onConfirm);


    }
    
    var publicObj = {};
    
    publicObj.init = function() {
        init();
    };
    
    return publicObj;
}();