"use strict";

if (Textalk === undefined) {
    var Textalk = {};
}
Textalk.PanelView = function() {
    function init() {
        $("#LeftPanel").panel();
        $("#panel-menu").listview().listview("refresh");


    }
    
    var publicObj = {};
    
    publicObj.init = function() {
        init();
    };
    
    return publicObj;
}();