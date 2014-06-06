scout.Menu = function() {
  scout.Menu.parent.call(this);
};

scout.inherits(scout.Menu, scout.ModelAdapter);

scout.Menu.EVENT_ABOUT_TO_SHOW = 'aboutToShow';

scout.Menu.prototype.sendAboutToShow = function(event) {
  this.session.send(scout.Menu.EVENT_ABOUT_TO_SHOW, this.id);
};

scout.Menu.prototype._setVisible = function(visible) {
  // TODO CGU implement
};

scout.Menu.prototype._setEnabled = function(enabled) {
  // TODO CGU implement
};
