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

  init: function() {
    var texts = scout.login.initTexts();
    var $form = $('<form id="login-box" action="auth" method="post">')
      .submit(onLoginFormSubmit)
      .appendTo($('body'));
    var $loginUser = $('<input id="login-user" type="text" autocapitalize="off" autocorrect="off">')
      .placeholder(texts.get('User'))
      .appendTo($form);
    $('<input id="login-password" type="password">')
      .placeholder(texts.get('Password'))
      .appendTo($form);
    $('<div id="login-error">')
      .appendTo($form);
    $('<button id="login-button" type="submit">')
      .text(texts.get('Login'))
      .appendTo($form);

    $loginUser.focus();

    function onLoginFormSubmit(event) {
      // Prevent default submit action
      event.preventDefault();

      var $form = $(this),
        $user = $form.find('#login-user'),
        $password = $form.find('#login-password'),
        $error = $form.find('#login-error'),
        $button = $form.find('#login-button'),
        url = $form.attr('action');

      var data = {
        user: $user.val(),
        password: $password.val()
      };

      $button.setEnabled(false);
      if (scout.device.supportsCssAnimation()) {
        $button.text('')
          .appendDiv('login-button-loading');
      }

      $.post(url, data)
        .done(function(data) {
          $error.hide();
          window.location.reload();
        })
        .fail(function(jqXHR, textStatus, errorThrown) {
          // execute delayed to make sure loading animation is visible, otherwise (if it is very fast), it flickers
          setTimeout(function() {
            $error.text(texts.get('LoginError'));
            $error.show();
            $user.focus();
            $button.setEnabled(true);
            $button.html('');
            $button.text(texts.get('Login'));
          }, 300);
        });
    }
  }
};
