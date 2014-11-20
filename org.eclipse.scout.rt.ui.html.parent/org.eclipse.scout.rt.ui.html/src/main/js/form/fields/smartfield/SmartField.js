scout.SmartField = function() {
  scout.SmartField.parent.call(this);
  this.options;
};
scout.inherits(scout.SmartField, scout.AbstractSmartField);

scout.SmartField.prototype._filterOptionsImpl = function(query) {
  var match, numVisibleOptions = 0,
    showAll = !query || '*' === query,
    regexp = new RegExp(query, 'i');
  this._get$Options().each(function() {
    if (showAll) {
      $(this).setVisible(true);
    } else {
      match = $(this).html().match(regexp);
      if (match) { numVisibleOptions++; }
      $.log.debug('regexp='+regexp + ' html='+$(this).html());
      $(this).setVisible(match);
    }
  });
  if (showAll) { numVisibleOptions = this.options.length; }
  this._setStatusText(numVisibleOptions);
};

scout.SmartField.prototype._openPopup = function() {
  var numOptions = this.options.length;
  this._showPopup(Math.min(10, numOptions), numOptions);
  this._renderOptions(this.options);
};

