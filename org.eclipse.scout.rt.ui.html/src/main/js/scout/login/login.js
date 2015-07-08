scout.login = {

  initTexts: function() {
    var translations = {
      en: {
        Login: 'Login',
        LoginError: 'Login failed',
        User: 'User',
        Password: 'Password'
      },
      de: {
        Login: 'Anmelden',
        LoginError: 'Anmeldung fehlgeschlagen',
        User: 'Benutzer',
        Password: 'Passwort'
      }
    };

    var language = navigator.language || navigator.userLanguage;
    if (translations[language] === undefined) {
      language = 'en';
    }
    return new scout.Texts(translations[language]);
  },

  init: function(opts) {
    this.options = opts || {};
    var texts = scout.login.initTexts();
    this.$form = $('<form action="auth" method="post">')
      .submit(onLoginFormSubmit.bind(this))
      .appendTo($('body'));
    this.$container = $('<div id="login-box">')
      .appendTo(this.$form);
    this.$user = $('<input id="login-user" type="text" autocapitalize="off" autocorrect="off">')
      .placeholder(texts.get('User'))
      .appendTo(this.$container);
    this.$password = $('<input id="login-password" type="password">')
      .placeholder(texts.get('Password'))
      .appendTo(this.$container);
    this.$button = $('<button id="login-button" type="submit">')
      .text(texts.get('Login'))
      .appendTo(this.$container);

    this.$user.focus();

    // ----- Helper functions -----

    function onLoginFormSubmit(event) {
      // Prevent default submit action
      event.preventDefault();

      var url = this.$form.attr('action');
      var data = {
        user: this.$user.val(),
        password: this.$password.val()
      };

      this.$button
        .removeClass('login-error')
        .setEnabled(false);
      this.$user.off('input.resetLoginError');
      this.$password.off('input.resetLoginError');
      if (scout.device.supportsCssAnimation()) {
        this.$button
          .html('')
          .append($('<div id="login-button-loading">'));
      }

      $.post(url, data)
        .done(onPostDone.bind(this))
        .fail(onPostFail.bind(this));
    }

    function onPostDone(data) {
      // Calculate target URL
      var url = this.options.redirectUrl;
      if (!url) {
        url = (window.location.href || '').trim();
        // Remove everything after the last '/', e.g. things like 'login.html'.
        // Do nothing string already ends with '/' or when the last '/' belongs to the protocol part.
        var lastSlashPos = url.lastIndexOf('/');
        if (lastSlashPos != (url.length - 1) && lastSlashPos > url.lastIndexOf('://') + 2) {
          url = url.substring(0, lastSlashPos + 1);
        }
        if (url.match(/.*:\/+$/)) {
          // If only the protocol:// remains, calculation failed. Don't use that URL.
          url = null;
        }
      }

      // Go to target URL
      if (url) {
        window.location.href = url;
      }
      else {
        window.location.reload();
      }
    }

    function onPostFail(jqXHR, textStatus, errorThrown) {
      // execute delayed to make sure loading animation is visible, otherwise (if it is very fast), it flickers
      setTimeout(function() {
        this.$button
          .setEnabled(true)
          .html('')
          .text(texts.get('LoginError'))
          .addClass('login-error');
        this.$user.focus();
        this.$user.one('input.resetLoginError', resetButtonText.bind(this));
        this.$password.one('input.resetLoginError', resetButtonText.bind(this));
      }.bind(this), 300);
    }

    function resetButtonText() {
      this.$button
        .text(texts.get('Login'))
        .removeClass('login-error');
    }
  }

};
