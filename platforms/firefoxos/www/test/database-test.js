describe('database.js', function() {
    beforeEach(angular.mock.module('textalk.database'));
    
    it('add, get', angular.mock.inject(function($injector) {
        var dbInfo = {
            name: "textalk",
            version: 1,
            objStore: [
                {
                    name: "History",
                    keyPath: "msgText",
                    indexes: ['timesUsed']
                }
            ]
        };

        var $q = $injector.get('$q');
        var dbService = $injector.get('Database');
        var $rootScope = $injector.get('$rootScope');
        
        var record1 = {
            msgText: 'Hello!',
            timesUsed: 1,
            timestamp: Date.now()
        };
        var record2 = {
            msgText: 'Bye!',
            timesUsed: 2,
            timestamp: Date.now()
        };
        
        dbService.open(dbInfo)
            .then(dbService.add.bind(this, 'History', record1))
            .then(dbService.add.bind(this, 'History', record2))
            .then(checkRecord)
            .catch(onError);
            
        function checkRecord () {
            function onSuccess (data) {
                expect(data.timesUsed).toEqual(2);
            }
            console.log('checkRecord');
            dbService.get('History', 'Bye!')
                .then(onSuccess)
                .catch(onError);
        }
        function onSuccess () {
            console.log('onSuccess');
        }
        function onError (err) {
            console.log('onError: ' + err);
        }

    }));
});