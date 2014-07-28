"use strict";

if (Textalk === undefined) {
    var Textalk = {};
}
Textalk.PopupView = function() {

    function init() {

        $("#open-about").on("tap", openAboutDialog);
    }

    function openConfirmDialog(txtTitle, txtMessage, txtButton, callback) {
        function onLoad() {

            $("#confirm-title").text(txtTitle);
            $("#confirm-message").text(txtMessage);
            $("#confirm-yes").text(txtButton);
            $("#confirm-no").text(navigator.mozL10n.get("no"));
        
            $("#confirm-yes").on("tap", callback);

            $("#confirm-dialog").popup().popup("open");
        }
        $("#popup").load("confirm-dialog.html", onLoad);
    }
    function openAboutDialog() {
        function onLoad() {
            $("#about-dialog").popup().popup("open");
        }
        
        $("#popup").load("about-dialog.html", onLoad);
    }
    function toast(message) {
        function onFadeOut() {
            $("#toast").popup("destroy");
        }
        function onLoad() {
            $("#toast").text(message);
            $("#toast").popup({ positionTo: "window" }).popup("open");
            $("#toast").fadeOut(3000, onFadeOut);
        }
        console.log(message);
        $("#popup").load("toast.html", onLoad);
    }
    var publicObj = {};
    
    publicObj.init = function() {
        init();
    };
    publicObj.openAboutDialog = function() {
        openAboutDialog();  
    };
    publicObj.openConfirmDialog = function(txtTitle, txtMessage, txtButton, callback) {
        openConfirmDialog(txtTitle, txtMessage, txtButton, callback);
    };
    publicObj.toast = function(message) {
        toast(message);
    };
    
    return publicObj;
}();