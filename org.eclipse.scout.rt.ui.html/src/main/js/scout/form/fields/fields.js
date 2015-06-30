scout.fields = {

  new$TextField: function() {
    return $('<input>')
      .attr('type', 'text')
      .disableSpellcheck();
  },

  new$Glasspane: function(uiSessionId) {
    var $glassPane = $.makeDiv('glasspane');
//    var keyStrokeAdapter = new scout.GlassPaneKeyStrokeAdapter($glassPane, uiSessionId);
//    scout.keyStrokeManager.installAdapter($glassPane, keyStrokeAdapter);
//    $glassPane.on('remove', function () {
//      scout.keyStrokeManager.uninstallAdapter(keyStrokeAdapter);
//    });
    return $glassPane;
  }

};
