scout.fields = {

  new$TextField: function() {
    return $('<input>')
      .attr('type', 'text')
      .attr('autocomplete', 'false') /* use false instead of off, off is currently ignored in chrome, false should work with all major browsers*/
      .disableSpellcheck();
  },

  new$Glasspane: function(uiSessionId) {
    return $.makeDiv('glasspane');
  }
};
