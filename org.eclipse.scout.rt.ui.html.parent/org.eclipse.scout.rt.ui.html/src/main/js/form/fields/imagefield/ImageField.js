scout.ImageField = function() {
  scout.ImageField.parent.call(this);
  this._$scrollable;
};
scout.inherits(scout.ImageField, scout.FormField);

scout.ImageField.prototype._render = function($parent) {
  var $fieldContainer, $field;

  //Create div to avoid resizing of the <img>
  $fieldContainer = $('<div>').css('overflow', 'hidden');

  if (this.scrollBarEnabled) {
    this._$scrollable = scout.scrollbars.install($fieldContainer, {
      invertColors: true
    });
  } else {
    this._$scrollable = $fieldContainer;
  }

  $field = $('<img>').appendTo(this._$scrollable);

  this.addContainer($parent, 'image-field');
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
  this.$field.on('load', scout.scrollbars.update.bind(this, this._$scrollable));
};
