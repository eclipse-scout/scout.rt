scout.ImageField = function() {
  scout.ImageField.parent.call(this);
  //FIXME CGU viewport is the wrong name -> refactor every viewport occurrence (maybe $content?)
  this._$viewport;
};
scout.inherits(scout.ImageField, scout.FormField);

scout.ImageField.prototype._render = function($parent) {
  var $fieldContainer, $field;

  //Create div to avoid resizing of the <img>
  $fieldContainer = $('<div>').css('overflow', 'hidden');

  if (this.scrollBarEnabled) {
    this._$viewport = scout.scrollbars.install($fieldContainer, {
      invertColors: true
    });
  } else {
    this._$viewport = $fieldContainer;
  }

  $field = $('<img>').appendTo(this._$viewport);

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
  this.$field.on('load', scout.scrollbars.update.bind(this, this._$viewport));
};
