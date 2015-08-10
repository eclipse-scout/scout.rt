scout.status = {

  Severity: {
    OK: 0x01,
    INFO: 0x100,
    WARNING: 0x10000,
    ERROR: 0x1000000
  },

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
