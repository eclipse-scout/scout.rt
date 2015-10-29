scout.GraphField = function() {
  scout.GraphField.parent.call(this);
};
scout.inherits(scout.GraphField, scout.ValueField);

scout.GraphField.prototype._render = function($parent) {
  this.addContainer($parent, 'graph-field');
  this.addLabel();
  this.addField($.makeDiv(this.ownerDocument()));
  this.addStatus();
};

scout.GraphField.prototype._renderProperties = function() {
  scout.GraphField.parent.prototype._renderProperties.call(this);

  this._renderValue();
  this._renderLoading();
};

/**
 * @override
 */
scout.GraphField.prototype._renderDisplayText = function() {
 // nop
};

scout.GraphField.prototype._renderValue = function() {
  if (this.graphImpl) {
    this.graphImpl.remove();
    this.graphImpl = null;
  }
  this.$field.empty();

  if (!this.value) {
    return;
  }

  var model = $.extend({}, this.value);
  model.parent = this;
  this.graphImpl = scout.create(scout.Graph, model);
  this.graphImpl.render(this.$field);
};

scout.GraphField.prototype._renderLoading = function(loading) {
  if (loading) {
    return;
  }
  // XXX BSH
};
