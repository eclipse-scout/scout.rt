scout.ImageField = function() {
  scout.ImageField.parent.call(this);
};
scout.inherits(scout.ImageField, scout.FormField);

scout.ImageField.prototype._render = function($parent) {
  var $fieldContainer, $field;

  $fieldContainer = $('<div>');
  $field = $('<img>').appendTo($fieldContainer);
  this.addContainer($parent, 'form-field');
  this.addLabel();
  this.addField($field, $fieldContainer);
  this.addStatus();
};

scout.ImageField.prototype._renderProperties = function() {
  scout.ImageField.parent.prototype._renderProperties.call(this);

  this._renderImageId(this.imageId);
};

scout.ImageField.prototype._renderImageId = function(imageId) {
  this.$field.attr('src', imageId);
};

