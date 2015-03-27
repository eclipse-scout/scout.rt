scout.MessageBoxModelAdapter = function() {
  scout.MessageBoxModelAdapter.parent.call(this);
};
scout.inherits(scout.MessageBoxModelAdapter, scout.ModelAdapter);

scout.MessageBoxModelAdapter.prototype._createUi = function() {
  return new scout.MessageBox(this, this.session);
};

scout.MessageBoxModelAdapter.prototype.render = function($parent) {
  var ui = new scout.MessageBox(this, this.session);
  scout.MessageBoxModelAdapter.parent.prototype.render.call(this, $parent, ui);
};

scout.MessageBoxModelAdapter.prototype.onButtonClicked = function($button, event) {
  var option = $button.data('option');
  this.session.send(this.id, 'action', {
    option: option
  });
};

scout.MessageBoxModelAdapter.prototype.onModelAction = function(event) {
  if (event.type === 'closed') {
    this.destroy();
    this.session.desktop.onMessageBoxClosed(this);
  }
};
