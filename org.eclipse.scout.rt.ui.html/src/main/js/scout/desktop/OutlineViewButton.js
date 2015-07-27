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

/**
 * Don't await server response to make it more responsive and offline capable.
 * @override Action.js
 */
scout.OutlineViewButton.prototype.beforeSendDoAction = function() {
  if (this.outline) {
    this.desktop.bringOutlineToFront(this.outline);
  }
};

scout.OutlineViewButton.prototype.onOutlineChanged = function(outline) {
  var oldSelected = this.selected,
    selected = this.outline === outline;
  if (selected !== oldSelected) {
    this.selected = selected;
    this._renderSelected(selected);
    this._firePropertyChange('selected', oldSelected, selected);
  }
};
