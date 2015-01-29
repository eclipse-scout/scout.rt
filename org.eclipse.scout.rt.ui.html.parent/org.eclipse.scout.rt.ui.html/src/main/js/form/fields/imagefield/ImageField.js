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

  $field = $('<img>').appendTo(this.$fieldContainer);

  this.addContainer($parent, 'image-field');
  this.addLabel();
  this.addField($field, this.$fieldContainer);
  this.addStatus();
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
    if (scout.strings.startsWith(this.imageId, "font:")) {
      var icon = this.imageId.substr(5, 1);
      // FIXME AWE: (font-icon) --> IMG entfernen, mit DIV ersetzen
    } else {
      this.$field.attr('src', scout.fields.imageUrl(this, this.imageId));
    }
  } else if (this.image) {
    this.$field.attr('src', scout.fields.imageUrl(this, this.image));
  }
  this.$field.on('load', scout.scrollbars.update.bind(this, this.$fieldContainer));
  // FIXME AWE: (image) IMG/DIV entfernen, wenn kein bild mehr da
};
