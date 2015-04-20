describe('tcpService.js', function() {
    beforeEach(angular.mock.module('textalk.tcp'));
    it('findHosts', angular.mock.inject(function($injector) {
        var tcp = $injector.get('Tcp');

        function findCallback (address) {
            
        }
        tcp.findHosts('192.168.1.4', 24556, findCallback);
        
    }));
});