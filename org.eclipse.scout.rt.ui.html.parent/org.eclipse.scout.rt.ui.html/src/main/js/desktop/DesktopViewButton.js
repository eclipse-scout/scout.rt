// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG


scout.DesktopViewButton = function() {
  this._$viewButton;
};
scout.inherits(scout.DesktopViewButton, scout.ModelAdapter);

scout.DesktopViewButton.prototype._render = function($parent) {
  var state = '';
  if (this.model.selected) {
    state = 'selected';
  }
  this._$viewButton = $parent.appendDiv(this.model.id, 'view-item ' + state, this.model.text);

  this._setIconId(this.model.iconId);

  var that = this;
  this._$viewButton.on('click', '', function() {
    that._$viewButton.selectOne();
    that.session.send('click', that.model.id);
  });
};

scout.DesktopViewButton.prototype._setSelected = function(selected) {
  this.model.selected = selected;
  if(selected) {
    this._$viewButton.selectOne();
  }
};

scout.DesktopViewButton.prototype._setIconId = function(iconId) {
  this.model.iconId = iconId;
//  this._$viewButton.attr('data-iconId', iconId); //FIXME CGU how to dynamically set icon?

  if(iconId) {
    this._$viewButton.addClass('view-item-icon');
  }
  else {
    this._$viewButton.removeClass('view-item-icon');
  }
};

scout.DesktopViewButton.prototype._setText = function(text) {
  this.model.text = text;
  this._$viewButton.text = event.text;
};

scout.DesktopViewButton.prototype.onModelPropertyChange = function(event) {
  if (event.hasOwnProperty('selected')) {
    this._setSelected(event.selected);
  }
  if (event.hasOwnProperty('iconId')) {
    this._setIconId(event.iconId);
  }
  if (event.hasOwnProperty('text')) {
    this._setText(event.text);
  }
};
