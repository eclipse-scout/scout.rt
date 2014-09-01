// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG


scout.DesktopViewButton = function() {
  scout.DesktopViewButton.parent.call(this);
  this._addAdapterProperties('outline');
};
scout.inherits(scout.DesktopViewButton, scout.ModelAdapter);

scout.DesktopViewButton.prototype._render = function($parent) {
  this.$container = $parent.appendDIV('outline-menu-item');

  var that = this;
  this.$container.on('click', '', function() {
    if (!this.$container.isEnabled()) {
      return;
    }

    //don't await server response to make it more responsive and offline capable
    if (this.outline) {
      this.desktop.changeOutline(that.outline);
    }
    this.session.send('click', that.id);
  }.bind(this));

  this._callSetters();
};

scout.DesktopViewButton.prototype._callSetters = function() {
  this._setEnabled(this.enabled);
  this._setText(this.text);
  this._setIconId(this.iconId);
};

scout.DesktopViewButton.prototype._setEnabled = function(enabled) {
  this.$container.setEnabled(enabled);
};

scout.DesktopViewButton.prototype._setText = function(title) {
  this.$container.text(title);
};

scout.DesktopViewButton.prototype._setIconId = function(iconId) {
  //FIXME implement
};

scout.DesktopViewButton.prototype._setOutline = function(outline) {
  // nop
};

scout.DesktopViewButton.prototype.goOffline = function() {
  scout.DesktopViewButton.parent.prototype.goOffline.call(this);

  //Disable if outline has not been loaded yet
  if (!this.outline) {
    this._setEnabled(false);
  }
};

scout.DesktopViewButton.prototype.goOnline = function() {
  scout.DesktopViewButton.parent.prototype.goOnline.call(this);

  if (this.enabled) {
    this._setEnabled(true);
  }
};

scout.DesktopViewButton.prototype.onModelPropertyChange = function(event) {
  scout.DesktopViewButton.parent.prototype.onModelPropertyChange.call(this, event);

  //Update navigation as well if properties for current outline have changed
  var navigation = this.session.desktop.navigation;
  if (navigation.outline == this.outline) {
    navigation.onOutlinePropertyChange(event);
  }
};
