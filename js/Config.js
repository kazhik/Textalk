"use strict";

if (Textalk === undefined) {
    var Textalk = {};
}
Textalk.Config = (function() {
    var configDefault = {
        "appname": "Textalk",
        "geolocation": {
            "min" : {
                "accuracy": 50,
                "altAccuracy": 50,
                "timeInterval": 5,
                "distanceInterval": 10
            },
            "autoLap": {
                "on": "off",
                "distance": 1000
            },
            "pace": {
                "type": "average-pace"  
            },
            "distanceUnit": "metre"
        },
        "map": {
            "type": "GoogleMap",
            "url": {
                "GoogleMap": "http://kazhik.github.io/Textalk/map/gmap.html",
                "OpenStreetMap": "http://kazhik.github.io/Textalk/map/omap.html"
            },
            "zoom": 16
        },
        "debug": {
            "log": "on",
            "export": "position"
        }
    };
    function get(keys) {
        var conf = config;
        for (var i = 0; i < keys.length; i++) {
            if (typeof conf[keys[i]] === "undefined") {
                console.log("config get error: " + keys[i]);
                return "";
            }
            conf = conf[keys[i]];
        }
        return conf;
    }
    function set(keys, value) {
        if (keys.length < 1) {
            return;
        }
        var conf = config;
        for (var i = 0; i < keys.length - 1; i++) {
            conf = conf[keys[i]];
        }
        conf[keys[keys.length - 1]] = value;
    }
    function save(newConfig) {
        var onSuccess = function () {
            dfd.resolve();
        };
        var onError = function(err) {
            dfd.reject(err);
        };

        config["geolocation"] = newConfig["geolocation"];
        config["map"] = newConfig["map"];
        config["debug"] = newConfig["debug"];

        var dfd = new $.Deferred();
        Textalk.Database.put("Config", config)
            .done(onSuccess)
            .fail(onError);
        
        return dfd.promise();
    }
    function load() {
        var onSuccess = function (result) {
            if (typeof result !== "undefined") {
                config = result;
            } else {
                config = configDefault;
            }
            dfd.resolve();
        };
        var onError = function(err) {
            config = configDefault;
            dfd.reject(err);
        };
        var dfd = new $.Deferred();

        Textalk.Database.get("Config", "Textalk")
            .done(onSuccess)
            .fail(onError);

        return dfd.promise();        
    }
    function reset() {
        config = configDefault;
        return save(config);
    }
    var config = {};
    
    return {
        get: get,
        set: set,
        save: save,
        load: load,
        reset: reset
    };

    
}());
