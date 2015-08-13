scout.login = {

  // FIXME AWE: pass i18n texts from JSP
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

    var preferredLanguage = sessionStorage.getItem('scout:preferredLanguage');
    var language = preferredLanguage || navigator.language || navigator.userLanguage;
    if (language && !translations[language]) {
      language = language.split(/[-_]/)[0];
    }
    if (!language || !translations[language]) {
      language = 'en';
    }
    return new scout.Texts(translations[language]);
  },

  /**
   * opts:
   * - redirectUrl: URL to redirect to after successful login
   * - prepareRedirectUrl: function(s) that is called on the redirectUrl before opening it
   */
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
        var prepareRedirectUrlFunc = this.options.prepareRedirectUrlFunc || prepareRedirectUrl;
        // Remove login.jsp and everything after it from the URL
        url = prepareRedirectUrlFunc(url);
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

    function prepareRedirectUrl(url) {
      var urlParts = /^([^?#]*)(\?[^#]*)?(#.*)?$/.exec(url || ''); // $1 = baseUrl, $2 = queryPart, $3 = hashPart
      var filteredBaseUrl = urlParts[1]
        .replace(/login.jsp$/, '')
        .replace(/login$/, '')
        .replace(/logout$/, '');
      return filteredBaseUrl + (urlParts[2] ? urlParts[2] : '') + (urlParts[3] ? urlParts[3] : '');
    }

    function resetButtonText() {
      this.$button
        .text(texts.get('Login'))
        .removeClass('login-error');
    }
  }

};
