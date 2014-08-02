"use strict";

if (Textalk === undefined) {
    var Textalk = {};
}
Textalk.ConfigView = (function() {
    function onClose() {
        var config = {
            "debug": {
                "log": $("#flip-debug-log").val()
            }
        };
        Textalk.Config.save(config);
    }
    function onOpen() {
        var config = Textalk.Config.get([]);

        $("#flip-debug-log").val(config["debug"]["log"])
            .slider("refresh");
    }
    function init() {
        $("#flip-debug-log").slider();

        $("#close-settings").on("tap", onClose);
        $("#reset-conf").on("tap", reset);
        
        $("#Settings").on("pageshow", onOpen);
        initValue();
    }
    function reset() {
        function onFail(err) {
            console.log(err);
        }
        function onConfirm() {
            Textalk.Config.reset()
                .done(onOpen)
                .fail(onFail);
        }

        Textalk.PopupView.openConfirmDialog(
            navigator.mozL10n.get("reset-conf-title"),
            navigator.mozL10n.get("reset-conf-message"),
            navigator.mozL10n.get("reset"),
            onConfirm);
    }
    function initValue() {
        
        /* doesn't work
        values = ["on", "off"];
        for (i = 0; i < values.length; i++) {
            $('#flip-debug-log').append($('<option>', {
                value: values[i],
                text: navigator.mozL10n.get(values[i])
            }));        
        }
        */
        
    }
    
    return {
        init: init
    };
}());
