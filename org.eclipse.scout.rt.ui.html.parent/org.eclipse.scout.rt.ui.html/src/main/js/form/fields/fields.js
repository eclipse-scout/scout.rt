scout.fields = {

  new$TextField: function() {
    return $('<input>')
      .attr('type', 'text')
      .disableSpellcheck();
  },

  new$Glasspane: function() {
    return $.makeDiv('glasspane')
      .on('click mousedown', function(event) {
        // If the user clicks directly on the glasspane, do nothing (focus stays where it is)
        if (event.target === this) {
          $.suppressEvent(event);
        }
      });
  }

};
