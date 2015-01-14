// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.OutlineViewButton = function() {
  scout.OutlineViewButton.parent.call(this);
  this._addAdapterProperties('outline');
};
scout.inherits(scout.OutlineViewButton, scout.Action);

scout.OutlineViewButton.prototype._render = function($parent) {
  this.$container = $parent.appendDiv('outline-menu-item');

  var that = this;
  this.$container.on('click', '', function() {
    if (!this.$container.isEnabled()) {
      return;
    }

    // don't await server response to make it more responsive and offline capable
    if (this.outline) {
      this.desktop.changeOutline(that.outline);
    }
    this.session.send(that.id, 'clicked');
  }.bind(this));
};

scout.OutlineViewButton.prototype._renderOutline = function(outline) {
  // nop
};

scout.OutlineViewButton.prototype._goOffline = function() {
  //Disable if outline has not been loaded yet
  if (!this.outline) {
    this._renderEnabled(false);
  }
};

scout.OutlineViewButton.prototype.onModelPropertyChange = function(event) {
  scout.OutlineViewButton.parent.prototype.onModelPropertyChange.call(this, event);

  //Update navigation as well if properties for current outline have changed
  var navigation = this.session.desktop.navigation;
  if (navigation.outline === this.outline) {
    navigation.onOutlinePropertyChange(event);
  }
};
