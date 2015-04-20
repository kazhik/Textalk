(function() {
    'use strict';

    angular.module('textalk.tcp', [])
        .factory('Tcp', tcp);

    tcp.$inject = ['$q'];
    function tcp ($q) {
        var publicObj = {
            setOnDataCallback: setOnDataCallback,
            findHosts: findHosts,
            getConnectedHost: getConnectedHost,
            connect: connect,
            disconnect: disconnect,
            listen: listen,
            send: send,
            broadcast: broadcast,
            close: close
        };
        var serverSocket;
        var clientSocket = {};
        var onDataCallback;
        
        function setOnDataCallback (callback) {
            onDataCallback = callback;
        }
        function getConnectedHost () {
            return Object.keys(clientSocket);
        }
        function onClose (evt) {
            console.log(evt.target.host + ' closed: ' + evt.data);
        }
        function onData (evt) {
            //console.log('received from ' + evt.target.host + ': ' + evt.data);
            if (onDataCallback) {
                onDataCallback(evt.target.host, evt.data);
            }
        }


        function listen (port) {
            function onError (evt) {
                console.log('listen error: ' + evt.data);   
            }
            function onConnect (socket) {
                if (!clientSocket[socket.host]) {
                    socket.ondata = onData;
                    socket.onerror = onSocketError;
                    socket.onclose = onClose;
                    clientSocket[socket.host] = socket;
                }
            }
            serverSocket = navigator.mozTCPSocket.listen(port);
            
            serverSocket.onconnect = onConnect;
            serverSocket.onerror = onError;
            
            console.log('listen: ' + port);
        }
        function send(address, msg) {
            if (!clientSocket[address]) {
                console.log(address + ' undefined: ' + angular.toJson(clientSocket));
                return;
            }
            clientSocket[address].send(angular.toJson(msg));
        }
        function broadcast (msg) {
            angular.forEach(clientSocket, function (element, key) {
                
                console.log(element.host + ': ' + element.readyState);
                if (element.readyState === 'open') {
                    send(element.host, msg);
                }
            });
        }
        function findHosts (myaddr, port, findCallback) {
            var addrArray = myaddr.split('.');
            if (addrArray.length !== 4) {
                // invalid address
                return;
            }
            var address;
            var tried = 0;
            var max = 254;
            // check xxx.xxx.xxx.1 - xxx.xxx.xxx.254
            for (var n = 1; n <= max; n++) {
                address = [addrArray[0], addrArray[1], addrArray[2], n].join('.');
                if (address === myaddr) {
                    continue;
                }
                connect(address, port)
                    .then(findCallback.bind(null, address))
                    .finally(checkComplete);
            }
            function checkComplete () {
                tried++;
                if (tried === max) {
                    console.log(Object.keys(clientSocket).length + ' hosts found.');
                    findCallback(null);
                }
                
            }
        }
        function onSocketError (evt) {
            console.log('socket.error: ' + evt.data.name + ':' + evt.target.host);
        }
        function connect (address, port) {
            var dfd = $q.defer();

            var socket = navigator.mozTCPSocket.open(address, port);
            socket.onopen = onOpen;
            socket.onerror = onError;

            function onOpen (evt) {
                var socket = evt.target;
                socket.ondata = onData;
                socket.onclose = onClose;
                socket.onerror = onSocketError;
                clientSocket[socket.host] = socket;
                console.log('open: ' + socket.host + '; readyState = ' + socket.readyState);
                dfd.resolve(socket.host);
            }
            function onError (evt) {
//                console.log('open error: ' + evt.target.host + '/' + evt.data.name);
                dfd.reject(evt.data.name);
            }
            
            return dfd.promise;
        }
        function disconnect (address) {
            if (clientSocket[address]) {
                clientSocket[address].close();
            }
        }
        function close () {
            angular.forEach(clientSocket, function (element, key) {
                disconnect(key);
            });
        }
        
        return publicObj;
       
    }

        
 })();