scout.ImageField = function() {
  scout.ImageField.parent.call(this);
  this.$fieldContainer;
};
scout.inherits(scout.ImageField, scout.FormField);

scout.ImageField.prototype._render = function($parent) {
  var $field;

  // Create div to avoid resizing of the <img>
  this.$fieldContainer = $('<div>').css('overflow', 'hidden');

  if (this.scrollBarEnabled) {
    scout.scrollbars.install(this.$fieldContainer, {
      invertColors: true
    });
    this.session.detachHelper.pushScrollable(this.$fieldContainer);
  }

  $field = $('<img>')
    .appendTo(this.$fieldContainer)
    .on('load', this._onImageLoad.bind(this));

  this.addContainer($parent, 'image-field', new scout.ImageFieldLayout(this));
  this.addLabel();
  this.addField($field, this.$fieldContainer);
  this.addStatus();
};

scout.ImageField.prototype._onImageLoad = function(event) {
  scout.scrollbars.update(this.$fieldContainer);
  scout.HtmlComponent.get(this.$container).revalidate();
  this.session.layoutValidator.validate();
};

scout.ImageField.prototype._renderProperties = function() {
  scout.ImageField.parent.prototype._renderProperties.call(this);
  this._renderImageOrId();
};

scout.ImageField.prototype._remove = function() {
  if (this.scrollBarEnabled) {
    this.session.detachHelper.removeScrollable(this.$fieldContainer);
  }
};

scout.ImageField.prototype._renderImageId = function() {
  this._renderImageOrId();
};

scout.ImageField.prototype._renderImage = function() {
  this._renderImageOrId();
};

scout.ImageField.prototype._renderImageOrId = function() {
  if (this.imageId) {
    this.$field.attr('src', scout.helpers.imageUrl(this, this.imageId));
  } else if (this.image) {
    this.$field.attr('src', scout.helpers.imageUrl(this, this.image));
  }
};
