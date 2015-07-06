/**
 * Adapter for 'message box' model element.
 * Unlike adapters like 'form-adapter', 'message-box' UI and 'message-box' adapter is separated to be used without model, e.g. to render fatal errors.
 */
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
    this._onMessageBoxClosed(event);
  } else {
    $.log.warn('Model event not handled. Widget: MessageBox. Event: ' + event.type + '.');
  }
};

scout.MessageBoxModelAdapter.prototype._onMessageBoxClosed = function(event) {
  this.destroy();
};