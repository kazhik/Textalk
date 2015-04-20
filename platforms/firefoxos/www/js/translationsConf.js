(function() {
    'use strict';

    angular.module('textalk')
        .config(translations);

    translations.$inject = ['$translateProvider', 'AppInfo'];
    function translations ($translateProvider, AppInfo) {
        
        $translateProvider
            .translations('en', {
                'appname': AppInfo.name,
                'about': 'About ' + AppInfo.name,
                'log': 'Log',
                'setting': 'Setting',
                'undo': 'Undo',
                'clear': 'Clear',
                'send': 'Send',
                'submit': 'Submit',
                'history': 'History',
                'ipaddress': 'IP address',
                'myaddr': 'IP address of this device',
                'texthere': 'Tap here to enter text',
                'maxCount': 'Maximum number of list items'
            })
            .translations('ja', {
                'appname': AppInfo.name,
                'about': AppInfo.name + 'について',
                'log': 'ログ',
                'setting': '設定',
                'undo': '元に戻す',
                'clear': '消去',
                'send': '送信',
                'submit': '確定',
                'history': '履歴',
                'ipaddress': 'IPアドレス',
                'myaddr': 'この機器のIPアドレス',
                'texthere': 'ここをタップしてテキストを入力してください',
                'maxCount': 'リストの最大件数'
            })
            .registerAvailableLanguageKeys(['en', 'ja'], {
                'en_US': 'en',
                'en_UK': 'en',
                'ja_JP': 'ja'
            })
            .determinePreferredLanguage();
       
    }

        
 })();