scout.ImageField = function() {
  scout.ImageField.parent.call(this);
  this.$fieldContainer;
  this._addAdapterProperties('menus');
};
scout.inherits(scout.ImageField, scout.FormField);

scout.ImageField.prototype._render = function($parent) {
  var $field;

  this.addContainer($parent, 'image-field', new scout.ImageFieldLayout(this));

  this.addFieldContainer($('<div>'));

  if (this.scrollBarEnabled) {
    scout.scrollbars.install(this.$fieldContainer, {
      invertColors: true
    });
    this.session.detachHelper.pushScrollable(this.$fieldContainer);
  }

  $field = $('<img>')
    .appendTo(this.$fieldContainer)
    .on('load', this._onImageLoad.bind(this));

  this.addLabel();
  this.addMandatoryIndicator();
  this.addField($field);
  this.addStatus();
  if (this.menus) {
    for (var j = 0; j < this.menus.length; j++) {
      this.keyStrokeAdapter.registerKeyStroke(this.menus[j]);
    }
  }
};

scout.ImageField.prototype._onImageLoad = function(event) {
  scout.scrollbars.update(this.$fieldContainer);
  this.revalidateLayoutTree();
};

scout.ImageField.prototype._renderProperties = function() {
  scout.ImageField.parent.prototype._renderProperties.call(this);
  this._renderImageOrId();
  this._renderAutoFit();
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
    this.$field.attr('src', scout.helpers.dynamicResourceUrl(this, this.imageId));
  } else if (this.image) {
    this.$field.attr('src', scout.helpers.dynamicResourceUrl(this, this.image));
  }
  // Hide <img> when it has no content to suppress the browser's 'broken image' icon
  this.$field.toggleClass('empty', !(this.imageId || this.image));
};

scout.ImageField.prototype._renderAutoFit = function() {
  this.$field.toggleClass('autofit', this.autoFit);
};
