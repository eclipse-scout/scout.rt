scout.fields = {

  _swallowEventListener: function(event) {
    event.stopImmediatePropagation();
    return false;
  },

  new$TextField: function() {
    return $('<input>')
      .attr('type', 'text')
      .attr('autocomplete', 'false') /* use false instead of off, off is currently ignored in chrome, false should work with all major browsers*/
      .disableSpellcheck();
  },

  new$Glasspane: function(uiSessionId) {
    return $.makeDiv('glasspane')
      .on('keydown', this._swallowEventListener)
      .on('mousedown', this._swallowEventListener);
  }
};
