scout.logout = {

  init: function(opts) {
    var $container,
      defaultOpts = {
        loginUrl: './',
        texts: {
          'ui.logoutSuccessful': 'Good bye!',
          'ui.loginAgain': 'Login again'
        }
      },
      options = $.extend({}, defaultOpts, opts),
      texts = new scout.Texts(options.texts);

    var $box = $('<div>')
      .addClass('box-with-logo centered')
      .html(scout.strings.nl2br(texts.get('ui.logoutSuccessful')))
      .appendTo($('body'));
    var $buttonBar = $('<div>')
      .addClass('button')
      .appendTo($box);
    $('<button>')
      .text(texts.get('ui.loginAgain'))
      .on('click', loginAgain)
      .appendTo($buttonBar);

    $('noscript').remove(); // cleanup DOM

    // ----- Helper functions -----

    function loginAgain() {
      window.location = options.loginUrl;
    }
  }

};
