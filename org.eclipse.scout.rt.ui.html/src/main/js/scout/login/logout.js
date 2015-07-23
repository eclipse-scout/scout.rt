scout.logout = {

  initTexts: function() {
    var translations = {
      en: {
        LogoutSuccessful: 'See you soon!',
        Relogin: 'Login again'
      },
      de: {
        LogoutSuccessful: 'Auf Wiedersehen!',
        Relogin: 'Erneut anmelden'
      }
    };

    var language = navigator.language || navigator.userLanguage;
    if (translations[language] === undefined) {
      language = 'en';
    }
    return new scout.Texts(translations[language]);
  },

  init: function(opts) {
    var $container,
      defaultOpts = {
        loginUrl: './'
      },
      texts = scout.logout.initTexts(),
      options = $.extend({}, defaultOpts, opts);

    $container = $('<div id="logout-box">')
      .appendTo($('body'));
    $('<div id="logout-text">')
      .text(texts.get('LogoutSuccessful'))
      .appendTo($container);
    $('<a id="relogin" href="' + options.loginUrl + '">')
      .text(texts.get('Relogin'))
      .appendTo($container);
  }

};
