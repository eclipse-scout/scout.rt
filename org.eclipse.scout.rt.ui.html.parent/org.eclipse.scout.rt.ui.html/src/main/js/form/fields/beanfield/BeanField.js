/**
 * Base class for fields where the value should be visualized.
 */
scout.BeanField = function() {
  scout.BeanField.parent.call(this);
};
scout.inherits(scout.BeanField, scout.ValueField);

scout.BeanField.prototype._render = function($parent) {
  this.addContainer($parent);
  this.addLabel();
  this.addField($('<div>'));
  this.addStatus();
};

scout.BeanField.prototype._renderProperties = function() {
  scout.BeanField.parent.prototype._renderProperties.call(this);
  this._renderValue(this.value);
};

/**
 * @override
 */
scout.BeanField.prototype._renderDisplayText = function() {
 // nop
};

scout.BeanField.prototype._renderValue = function() {
  // to be implemented by the subclass
};
