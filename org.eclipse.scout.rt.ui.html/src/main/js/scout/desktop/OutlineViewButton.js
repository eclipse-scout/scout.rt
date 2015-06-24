// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.OutlineViewButton = function() {
  scout.OutlineViewButton.parent.call(this);
  this._addAdapterProperties('outline');
};
scout.inherits(scout.OutlineViewButton, scout.ViewButton);

scout.OutlineViewButton.prototype._renderOutline = function(outline) {
  // NOP
};

scout.OutlineViewButton.prototype._goOffline = function() {
  // Disable if outline has not been loaded yet
  if (!this.outline) {
    this._renderEnabled(false);
  }
};

scout.OutlineViewButton.prototype.onModelPropertyChange = function(event) {
  scout.OutlineViewButton.parent.prototype.onModelPropertyChange.call(this, event);
  // Update navigation as well if properties for current outline have changed
  var navigation = this.session.desktop.navigation;
  if (navigation.outline === this.outline) {
    navigation.onOutlinePropertyChange(event);
  }
};

/**
 * Don't await server response to make it more responsive and offline capable.
 * @override Action.js
 */
scout.OutlineViewButton.prototype.beforeSendDoAction = function() {
  if (this.outline) {
    this.desktop.changeOutline(this.outline);
  }
};
