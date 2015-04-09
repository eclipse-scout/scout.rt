scout.GraphField = function() {
  scout.GraphField.parent.call(this);
};
scout.inherits(scout.GraphField, scout.ValueField);

scout.GraphField.prototype._render = function($parent) {
  this.addContainer($parent, 'graph-field');
  this.addLabel();
  this.addField($.makeDiv());
  this.addStatus();
};

scout.GraphField.prototype._renderProperties = function() {
  scout.GraphField.parent.prototype._renderProperties.call(this);

  this._renderValue(this.value);
  this._renderLoading(this.loading);
};

/**
 * @override
 */
scout.GraphField.prototype._renderDisplayText = function() {
 // nop
};

scout.GraphField.prototype._renderValue = function(value) {
  this.$field.empty();
  if (!value) {
    return;
  }
  // XXX scout.PhonebookColumn.renderPhoneBookEntry(this.$field, value);
};

scout.GraphField.prototype._renderLoading = function(loading) {
  if (loading) {
    return;
  }
  // XXX BSH
};
