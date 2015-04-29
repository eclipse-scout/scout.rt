scout.MessageBoxModelAdapter = function() {
  scout.MessageBoxModelAdapter.parent.call(this);
};
scout.inherits(scout.MessageBoxModelAdapter, scout.ModelAdapter);

scout.MessageBoxModelAdapter.prototype._createUi = function() {
  var ui = new scout.MessageBox(this, this.session);
  ui.on('buttonClick', this._onButtonClick.bind(this));
  return ui;
};

scout.MessageBoxModelAdapter.prototype._onButtonClick = function(event) {
  this.session.send(this.id, 'action', {
    option: event.option
  });
};

scout.MessageBoxModelAdapter.prototype.onModelAction = function(event) {
  if (event.type === 'closed') {
    this.destroy();
    this.session.desktop.onMessageBoxClosed(this);
  }
};
