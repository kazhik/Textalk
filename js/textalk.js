"use strict";

if (typeof Textalk === "undefined") {
    var Textalk = {};
}

Textalk.start = function() {
    function initUI() {
        
        Textalk.PopupView.init();        
        
        // Panel
        Textalk.ConfigView.init();
        Textalk.PanelView.init();

        // Main Tabs
        Textalk.CanvasView.init();
        Textalk.TextMessageView.init();

    }
    function localize() {
        function onLocalized() {
            dfd.resolve();    
            
        }
        var dfd = new $.Deferred();
    
        navigator.mozL10n.ready(onLocalized);
            
        return dfd.promise();
        
    }
   function startApp() {
        // initialize console.log first
        if (Textalk.Config.get(["debug", "log"]) === "on") {
            Textalk.LogView.init();
        }

        initUI();
        
        console.log("Textalk started");
    
    }
    function onFail(e) {
        console.log(e);
    }
    
    var dbInfo = {
        name: "Textalk",
        version: 1,
        objStore: [
            {
                name: "Config",
                keyPath: "appname"
            },
            {
                name: "ConsoleLog",
                keyPath: null
            }
        ]
    };
    
    Textalk.Database.open(dbInfo)
        .then(localize)
        .then(Textalk.Config.load)
        .done(startApp)
        .fail(onFail);


};
$(document).on("pagecreate", "#Text", Textalk.start);



