scout.SmartField = function() {
  scout.SmartField.parent.call(this);
  this.options;
};
scout.inherits(scout.SmartField, scout.AbstractSmartField);

scout.AbstractSmartField.prototype._get$Options = function() {
  return this._$popup.children('.options').children(':visible');
};

scout.SmartField.prototype._filterOptionsImpl = function(query) {
  var statusText, match, numVisibleOptions = 0,
    showAll = !query || '*' === query,
    regexp = new RegExp(query, 'i');
  // we cannot use _get$Options here because we also want to select invisible options
  this._$popup.children('.options').children().each(function() {
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
  this._$popup.children('.status').text(this._getStatusText(numVisibleOptions));
};

scout.SmartField.prototype._openPopup = function() {
  var numOptions = this.options.length;
  this._showPopup(Math.min(10, numOptions), this._getStatusText(numOptions));
  this._renderOptions(this.options);
};

