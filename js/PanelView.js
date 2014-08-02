"use strict";

if (Textalk === undefined) {
    var Textalk = {};
}
Textalk.PanelView = (function() {
    function init() {
        $("#LeftPanel").panel();
        $("#panel-menu").listview().listview("refresh");

        if (Textalk.Config.get(["debug","log"]) === "on") {
            $("#panel-menu")
                .append($("<li/>")
                .append($("<a/>", {
                    "href": "#Log",
                    "id": "open-log",
                    "text": navigator.mozL10n.get("log")
                    })))
                .listview("refresh");
        }
    }
    
    return {
        init: init
    };
}());