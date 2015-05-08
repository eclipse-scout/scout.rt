scout.status = {

  animateStatusMessage: function($status, message) {
    if (scout.strings.endsWith(message, '...')) {
      var $elipsis = $('<span>').addClass('elipsis');
      for (var i = 0; i < 3; i++) {
        $elipsis.append($('<span>').text('.').addClass('animate-dot').addClass('delay-' + i));
      }
      message = message.substring(0, message.length - 3);
      $status.empty().text(message).append($elipsis);
    } else {
      $status.text(message);
    }
  }

};
