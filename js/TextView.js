"use strict";

if (Textalk === undefined) {
    var Textalk = {};
}
Textalk.TextView = (function() {
    function openEditTextDialog() {
        function onLoadDialog() {
            function ignoreEnter(e) {
                if ( e.which === 13 ) {
                    e.preventDefault();
                }
            }
            function onOk() {
                var txt = $("#input-text").val();
                console.log("onOk:" + txt);
                dfd.resolve($("#input-text").val());
                
            }
            function onCancel() {
                dfd.reject();
            }
            
            $("#dialog-title").text(navigator.mozL10n.get("edit-text-title"));
            $("#label-text").text(navigator.mozL10n.get("label-text"));
            $("#cancel").text(navigator.mozL10n.get("cancel"));
            $("#ok").text(navigator.mozL10n.get("ok"));
            
            $("#input-text").textinput();
        
            $("#ok").on("tap", onOk);
            $("#cancel").on("tap", onCancel);

            $("#edit-dialog").popup().popup("open");

            $("#input-text").keypress(ignoreEnter);
            
            $("#input-text").val("");
            
        }
        $("#popup").load("edit-dialog.html", onLoadDialog);

        var dfd = new $.Deferred();
    
        return dfd.promise();
        
    }
    function speak() {
        // Voice recognition API doesn't exist now.
    }
    function write() {
        function onOk(text) {
            console.log("text: " + text);
            $("#messageList")
                .append($("<li/>")
                    .append(text))
                .listview("refresh");            
            
        }
        openEditTextDialog()
            .done(onOk);
    }

    function init() {
        $("#messageList").listview().listview("refresh");

        $("#speak").on("tap", speak);
        $("#write").on("tap", write);
     
    }

    return {
        init: init
    };
}());