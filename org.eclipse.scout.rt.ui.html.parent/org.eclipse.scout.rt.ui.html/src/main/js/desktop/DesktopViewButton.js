// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG


scout.DesktopViewButton = function() {
  scout.DesktopViewButton.parent.call(this);
  this._$viewButton;
};
scout.inherits(scout.DesktopViewButton, scout.ModelAdapter);

scout.DesktopViewButton.prototype._render = function($parent) {
  this._$viewButton = $parent.appendDiv(this.id, 'view-item ', this.text);

  this._setIconId(this.iconId);

  var that = this;
  this._$viewButton.on('click', '', function() {
    if (that._$viewButton.isEnabled()) {
      return;
    }

    //don't await server response to make it more responsive and offline capable
    that._$viewButton.selectOne();
    if (that.outline) {
      that.desktop.changeOutline(that.outline);
    }

    that.session.send('click', that.id);
  });
};

scout.DesktopViewButton.prototype._callSetters = function() {
  this._setSelected(this.selected);
  this._setEnabled(this.enabled);
};

scout.DesktopViewButton.prototype._setSelected = function(selected) {
  if (selected) {
    this._$viewButton.selectOne();
  }
  else {
    //Called here because select = false comes after select = true and outlineChanged ...
    this.desktop.linkOutlineAndViewButton();
  }
};

scout.DesktopViewButton.prototype._setEnabled = function(enabled) {
  if (enabled) {
    this._$viewButton.removeAttr('disabled');
  } else {
    this._$viewButton.attr('disabled', 'disabled');
  }
};

scout.DesktopViewButton.prototype._setIconId = function(iconId) {
//  this._$viewButton.attr('data-iconId', iconId); //FIXME CGU how to dynamically set icon?
  if (iconId) {
    this._$viewButton.addClass('view-item-icon');
  }
  else {
    this._$viewButton.removeClass('view-item-icon');
  }
};

scout.DesktopViewButton.prototype._setText = function(text) {
  this._$viewButton.text = text;
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
