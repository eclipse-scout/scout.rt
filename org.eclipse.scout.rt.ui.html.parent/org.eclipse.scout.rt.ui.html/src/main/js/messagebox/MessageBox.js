scout.MessageBox = function() {
  scout.MessageBox.parent.call(this);
  this._ui = new scout.MessageBoxUI(this);
};
scout.inherits(scout.MessageBox, scout.ModelAdapter);

scout.MessageBox.prototype._render = function($parent) {
  this._ui.render($parent);
};

scout.MessageBox.prototype._renderProperties = function() {
  this._ui.renderProperties();
};

scout.MessageBox.prototype._remove = function() {
  scout.MessageBox.parent.prototype._remove.call(this);
  this._ui.remove();
};

scout.MessageBox.prototype.onButtonClicked = function($button, event) {
  var option = $button.data('option');
  this.session.send('action', this.id, {option: option});
};

scout.MessageBox.prototype.onModelAction = function(event) {
  if (event.type === 'closed') {
    this.destroy();
    this.session.desktop.onMessageBoxClosed(this);
  }
};
