scout.SmartFieldRemote = function() {
  scout.SmartFieldRemote.parent.call(this);
  this._delayed;
};
scout.inherits(scout.SmartFieldRemote, scout.AbstractSmartField);

scout.SmartFieldRemote.prototype._filterOptionsImpl = function(query) {
  clearTimeout(this._delayed);
  this._delayed = setTimeout(function() {
    this._loadOptions(query);
  }.bind(this), 250);
};

scout.SmartFieldRemote.prototype._loadOptions = function(query) {
  $.log.debug('load options from server. query=' + query);
  this.session.send('loadOptions', this.id, {'query':query});
};

scout.SmartFieldRemote.prototype._onOptionsLoaded = function(options) {
  $.log.debug('options loaded: ' + options.length);
  this._$popup.children('.options').empty();

  // adjust size of popup to loaded options (cannot know in advance)
  var oldBounds = scout.HtmlComponent.getBounds(this._$popup),
    popupHeight = Math.min(10, options.length) * 19 + 19 + 3,
    newBounds = new scout.Rectangle(oldBounds.x, oldBounds.y, oldBounds.width, popupHeight);
  scout.HtmlComponent.setBounds(this._$popup, newBounds);
  scout.HtmlComponent.setSize(this._$popup.children('.options'), newBounds.width - 4, popupHeight - 19 - 3);

  this._renderOptions(options);
  this._updateStatusText(options.length);
};

scout.SmartFieldRemote.prototype._openPopup = function() {
  this._showPopup(1, 'Lade Optionen...');
  this._loadOptions('*');
};

scout.SmartFieldRemote.prototype._updateStatusText = function(numOptions) {
  this._$popup.children('.status').text(this._getStatusText(numOptions));
};

scout.SmartFieldRemote.prototype.onModelAction = function(event) {
  if (event.type == 'optionsLoaded') {
    this._onOptionsLoaded(event.options);
  } else {
    scout.SmartFieldRemote.parent.prototype.onModelAction.call(this, event);
  }
};

