// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG


scout.DesktopViewButton = function() {
  scout.DesktopViewButton.parent.call(this);
  this._$viewButton;
};
scout.inherits(scout.DesktopViewButton, scout.ModelAdapter);

scout.DesktopViewButton.prototype._render = function($parent) {
  var state = '';
  if (this.selected) {
    state = 'selected';
  }
  this._$viewButton = $parent.appendDiv(this.id, 'view-item ' + state, this.text);

  this._setIconId(this.iconId);

  var that = this;
  this._$viewButton.on('click', '', function() {
    that._$viewButton.selectOne();
    that.session.send('click', that.id);
  });
};

scout.DesktopViewButton.prototype._setSelected = function(selected) {
  if (selected) {
    this._$viewButton.selectOne();
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

