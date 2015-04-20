describe('config.js', function() {
    beforeEach(angular.mock.module('textalk'));
    beforeEach(angular.mock.module('textalk.database'));
    /*
    it('load default', angular.mock.inject(function($injector) {
        function onSuccess (configuration) {
            console.log('onSuccess');
            expect(configuration.appname).toEqual('textalk');
        }
        function onError (err) {
            console.log('onError: ' + err);
        }
        function loadConfig () {
            var confService = $injector.get('Config');
            confService.load().then(onSuccess, onError);
        }
        var dbInfo = {
            name: "textalk",
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

        var dbService = $injector.get('Database');
        dbService.open(dbInfo)
            .then(loadConfig)
            .catch(onError);

    }));
    */
});