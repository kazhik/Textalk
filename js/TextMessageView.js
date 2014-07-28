"use strict";

if (Textalk === undefined) {
    var Textalk = {};
}
Textalk.TextMessageView = function() {
    function init() {
        $("#messageList").listview().listview("refresh");

     
    }
    
    var publicObj = {};
    
    publicObj.init = function() {
        init();
    };
    
    return publicObj;
}();