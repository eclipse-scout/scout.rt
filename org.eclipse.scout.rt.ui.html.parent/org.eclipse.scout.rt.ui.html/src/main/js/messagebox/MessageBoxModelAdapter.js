scout.MessageBoxModelAdapter = function() {
  scout.MessageBoxModelAdapter.parent.call(this);
  this._ui = new scout.MessageBox(this);
};
scout.inherits(scout.MessageBoxModelAdapter, scout.ModelAdapter);

scout.MessageBoxModelAdapter.prototype._render = function($parent) {
  this._ui.render($parent);
};

scout.MessageBoxModelAdapter.prototype._renderProperties = function() {
  this._ui.renderProperties();
};

scout.MessageBoxModelAdapter.prototype._remove = function() {
  scout.MessageBoxModelAdapter.parent.prototype._remove.call(this);
  this._ui.remove();
};

scout.MessageBoxModelAdapter.prototype.onButtonClicked = function($button, event) {
  var option = $button.data('option');
  this.session.send('action', this.id, {option: option});
};

scout.MessageBoxModelAdapter.prototype.onModelAction = function(event) {
  if (event.type === 'closed') {
    this.destroy();
    this.session.desktop.onMessageBoxClosed(this);
  }
};
