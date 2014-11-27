// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

//FIXME CGU vielleicht brauchen wir einen NavigationView.js oder so ähnlich, welches eine Outline.js beinhalten kann aber nicht muss (anstatt view button wrappers)
scout.DesktopViewButton = function() {
  scout.DesktopViewButton.parent.call(this);
  this._addAdapterProperties('outline');
};
scout.inherits(scout.DesktopViewButton, scout.ModelAdapter);

scout.DesktopViewButton.prototype._render = function($parent) {
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
    this.session.send('clicked', that.id);
  }.bind(this));
};

scout.DesktopViewButton.prototype._renderProperties = function() {
  this._renderEnabled(this.enabled);
  this._renderText(this.text);
  this._renderIconId(this.iconId);
};

scout.DesktopViewButton.prototype._renderEnabled = function(enabled) {
  this.$container.setEnabled(enabled);
};

scout.DesktopViewButton.prototype._renderText = function(title) {
  this.$container.text(title);
};

scout.DesktopViewButton.prototype._renderIconId = function(iconId) {
  //FIXME implement
};

scout.DesktopViewButton.prototype._renderOutline = function(outline) {
  // nop
};

scout.DesktopViewButton.prototype._goOffline = function() {
  //Disable if outline has not been loaded yet
  if (!this.outline) {
    this._renderEnabled(false);
  }
};

scout.DesktopViewButton.prototype._goOnline = function() {
  if (this.enabled) {
    this._renderEnabled(true);
  }
};

scout.DesktopViewButton.prototype.onModelPropertyChange = function(event) {
  scout.DesktopViewButton.parent.prototype.onModelPropertyChange.call(this, event);

  //Update navigation as well if properties for current outline have changed
  var navigation = this.session.desktop.navigation;
  if (navigation.outline === this.outline) {
    navigation.onOutlinePropertyChange(event);
  }
};
