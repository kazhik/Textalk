(function() {
    'use strict';

    angular.module('textalk.chat', ['textalk.tcp'])
        .factory('Chat', chat);

    chat.$inject = ['Tcp'];
    function chat (Tcp) {
        var publicObj = {
            start: start,
            stop: stop,
            setOnTextCallback: setOnTextCallback,
            setOnDataUrlCallback: setOnDataUrlCallback,
            sendText: sendText,
            sendDataUrl: sendDataUrl,
            broadcastText: broadcastText,
            broadcastDataUrl: broadcastDataUrl
        };
        var onTextCallback;
        var onDataUrlCallback;
        
        function setOnTextCallback (callback) {
            onTextCallback = callback;
        }
        function setOnDataUrlCallback (callback) {
            onDataUrlCallback = callback;
        }
        function send (address, type, body) {
            var msg = {
                timestamp: Date.now(),
                type: type,
                body: body
            };
            Tcp.send(address, msg);
        }
        function sendText (address, text) {
            send(address, 'text', text);
        }
        function sendDataUrl (address, dataUrl) {
            send(address, 'dataUrl', dataUrl);
        }
        function broadcast (type, body) {
            var msg = {
                timestamp: Date.now(),
                type: type,
                body: body
            };
            Tcp.broadcast(msg);
        }
        function broadcastText (text) {
            console.log('broadcastText: ' + text);
            broadcast('text', text);
        }
        function broadcastDataUrl (dataUrl) {
            broadcast('dataUrl', dataUrl);
        }
        function start (myaddr, port) {
            Tcp.listen(port);
            Tcp.setOnDataCallback(onData);
            Tcp.findHosts(myaddr, port, findCallback);
            
        }
        function stop () {
            Tcp.close();
        }
        
        function onData(address, data) {
            var msg = angular.fromJson(data);
            if (msg.type === 'text' && onTextCallback) {
                onTextCallback(address, msg);
            } else if (msg.type === 'dataUrl' && onDataUrlCallback) {
                onDataUrlCallback(address, msg);
            } else {
                console.log('Discarding: ' + data);
            }
        }
        function findCallback(address) {
            if (address) {
                console.log('connected: ' + address);
            } else {
                console.lod('findHosts finished');
            }
        }
        return publicObj;
       
    }

        
 })();