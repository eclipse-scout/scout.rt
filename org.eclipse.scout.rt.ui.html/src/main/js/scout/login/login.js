var translations = {
  en: {
    Login: 'Login',
    LoginError: 'Login failed'
  },
  de: {
    Login: 'Anmelden',
    LoginError: 'Anmeldung fehlgeschlagen'
  }
};

var language = navigator.language || navigator.userLanguage;
if (translations[language] === undefined) {
  language = 'en';
}
var texts = new scout.Texts(translations[language]);

$(document).ready(function() {
  $('#login-button').text(texts.get('Login'));
  $('#login-box').submit(onLoginFormSubmit);
  $('#login-user').focus();
});

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
