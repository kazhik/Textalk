"use strict";

if (Textalk === undefined) {
    var Textalk = {};
}
Textalk.ViewUtil = (function() {
    function getHeaderHeight() {
        var headerHeight = 0;
        var header = $.mobile.activePage.find("div[data-role='header']:visible");
        if (header.length > 0) {
            headerHeight = header.outerHeight();
        }
        return headerHeight;
    }
    function getFooterHeight() {
        var footerHeight = 0;
        var footer = $.mobile.activePage.find("div[data-role='footer']:visible");
        if (footer.length > 0) {
            footerHeight = footer.outerHeight()
        }
        return footerHeight;
    }
    function getRealContentHeight() {
        var headerFooterHeight = getHeaderHeight() + getFooterHeight();

        var viewportHeight = $(window).height();
        var contentHeight = viewportHeight - headerFooterHeight;
        
        var content = $.mobile.activePage.find("div[data-role='content']:visible:visible");
        if((content.outerHeight() - headerFooterHeight) <= viewportHeight) {
            contentHeight -= (content.outerHeight() - content.height());
        } 
        return contentHeight;
    }
    function getContentHeight() {
        var headerFooterHeight = getHeaderHeight() + getFooterHeight();

        var viewportHeight = $(window).height();
        var contentHeight = viewportHeight - headerFooterHeight;
        
        return contentHeight;
    }
    
    return {
        getRealContentHeight: getRealContentHeight,
        getContentHeight: getContentHeight,
        getHeaderHeight: getHeaderHeight
    };
    
}());