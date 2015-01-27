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
    this.session.detachHelper.pushScrollable(this._$scrollable);
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

scout.ImageField.prototype._remove = function() {
  if (this.scrollBarEnabled) {
    this.session.detachHelper.removeScrollable(this._$scrollable);
  }
};

// FIXME AWE: (resource loading) wir müssen unterscheiden zwischen statischen images (icons, ressourcen) 
// und dynamischen bildern erstere haben eine imageId, letztere nicht

scout.ImageField.prototype._renderImageId = function(imageId) {
  this._renderImageOrId();
//  this.$field.attr('src', imageId);
//  this.$field.on('load', scout.scrollbars.update.bind(this, this._$scrollable));
};

scout.ImageField.prototype._renderImageOrId = function() {
  if (this.imageId) {
    if (scout.strings.startsWith(this.imageId, "font:")) {
      var icon = this.imageId.substr(5, 1);
      // FIXME AWE: (font-icon) --> IMG entfernen, mit DIV ersetzen
    } else {
      this.$field.attr('src', '/static/' + this.imageId + '?sessionId=' + this.session.jsonSessionId);
    }
  } else if (this.image) {

  }
  // FIXME AWE: (image) IMG/DIV entfernen, wenn kein bild mehr da
};
