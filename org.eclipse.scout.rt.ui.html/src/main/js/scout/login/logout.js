scout.logout = {

  // FIXME AWE: pretty graphics for unsupported / logout?

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
      texts = new scout.Texts(options.texts),
      $box = $('<div>')
        .addClass('box-with-logo')
        .text(texts.get('ui.logoutSuccessful'))
        .appendTo($('body')),
      $buttonBar = $('<div>')
        .addClass('button')
        .appendTo($box);
      $('<button>')
        .text(texts.get('ui.loginAgain'))
        .click(function() {
          window.location = options.loginUrl;
        })
        .appendTo($buttonBar);
  }

};
