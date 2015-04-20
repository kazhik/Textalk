(function() {
    'use strict';

    angular.module('textalk.nfc', [])
        .factory('Nfc', nfc);

    function nfc () {
        var publicObj = {
            start: start,
            stop: stop
        };
        var peer;
        function fromUTF8(str) {
            if (!str) {
                return null;
            }
            var buf = new Uint8Array(str.length);
            for (var i = 0; i < str.length; i++) {
                buf[i] = str.charCodeAt(i);
            }
            return buf;
        }
        function sendNdef (records) {
            var tnf = 1;
            var type = new Uint8Array(fromUTF8("U"));
            var id = new Uint8Array(fromUTF8("xyz"));
            var payload = new Uint8Array(fromUTF8("Hello, world"));
            var ndefRecords = [new MozNDEFRecord(tnf, type, id, payload)];

            var request = peer.sendNDEF(ndefRecords);
            request.onsuccess = onSuccess;
            request.onerror = onError;
            
            function onSuccess (e) {
                console.log('sendNDEF success');
            }
            function onError (e) {
                console.log('sendNDEF failed: ' + e);
            }
        }
        function onPeerFound (evt) {
            console.log('peer found');
            peer = evt.peer;

            sendNdef();
        }
        function onPeerLost (evt) {
            console.log('peer lost');
        }
        function onPeerReady (evt) {
            console.log('peer ready');
            peer = evt.peer;
        }
        function start () {
            if (!navigator.mozNfc) {
                console.log('mozNfc: ' + navigator.mozNfc);
                return;
            }
            
            navigator.mozNfc.onpeerready = onPeerReady;
            navigator.mozNfc.onpeerfound = onPeerFound;
            navigator.mozNfc.onpeerlost = onPeerLost;
        }
        function stop () {
            navigator.mozNfc.onpeerready = null;
            peer = null;
        }
        
        return publicObj;
       
    }

        
 })();