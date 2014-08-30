"use strict";

if (Textalk === undefined) {
    var Textalk = {};
}
Textalk.TextView = (function() {
    var dlg = null;
    function openEditTextDialog() {
        function onLoadDialog() {
            function ignoreEnter(e) {
                if ( e.which === 13 ) {
                    e.preventDefault();
                }
            }
            $("#ok").on("tap", onOk);
            $("#cancel").on("tap", onCancel);
            
            $("#dialog-title").text(navigator.mozL10n.get("edit-text-title"));
            $("#label-text").text(navigator.mozL10n.get("label-text"));
            $("#cancel").text(navigator.mozL10n.get("cancel"));
            $("#ok").text(navigator.mozL10n.get("ok"));

            $("#input-text").textinput();
            $("#input-text").keypress(ignoreEnter);

            dlg = $("#edit-dialog").popup();
            
            dlg.popup("open", {positionTo: "#write"});
            $("#input-text").val("");
            
        }
        function onOk() {
            var txt = $("#input-text").val();
            dlg.popup("close");                
            dfd.resolve(txt);
        }
        function onCancel() {
            dlg.popup("close");                
            dfd.reject();
        }
        if (dlg === null) {
            $("#popup").load("edit-dialog.html", onLoadDialog);
        } else {
            $("#ok").on("tap", onOk);
            $("#cancel").on("tap", onCancel);
            dlg.popup("open", {positionTo: "#write"});
            $("#input-text").val("");
        }

        var dfd = new $.Deferred();
    
        return dfd.promise();
        
    }
    function speak() {
        Textalk.PopupView.toast("Not implemented yet");
    }
    function history() {
        Textalk.PopupView.toast("Not implemented yet");
    }
    function write() {
        function onOk(text) {
            enter(text);
        }
        openEditTextDialog()
            .done(onOk);
    }
    function enter(text) {
        var currTime = Textalk.StringUtil.formatTime(Date.now());
        var msginfo = "(" + currTime + ")";
        $("#messageList")
            .append($("<li/>")
                .append($("<p/>", {
                    "class": "msg-text",
                    "text": text}
                    ))
                .append($("<div/>",{
                    "class": "msg-info",
                    "text": msginfo}
                    ))
            )
            .listview("refresh");
        
    }
    function onEnter(e) {
        var txt = $("#message-text").val();
        enter(txt);
        $("#message-text").val("");
    }
    function setMessageListHeight() {
        $('#messageList').height(Textalk.ViewUtil.getContentHeight());
    }
    function init() {
        $("#messageList").listview().listview("refresh");

        $("#speak").on("tap", speak);
        $("#write").on("tap", write);
        $("#history").on("tap", history);
        $("#enter").on("tap", onEnter);
     
        setMessageListHeight();
    }

    return {
        init: init
    };
}());