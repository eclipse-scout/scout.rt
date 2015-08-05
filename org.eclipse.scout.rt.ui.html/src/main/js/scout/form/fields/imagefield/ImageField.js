scout.ImageField = function() {
  scout.ImageField.parent.call(this);
};
scout.inherits(scout.ImageField, scout.FormField);

scout.ImageField.prototype._render = function($parent) {
  this.addContainer($parent, 'image-field', new scout.ImageFieldLayout(this));
  this.addFieldContainer($('<div>'));

  // add drag and drop event listeners to field container, img field might be hidden (e.g. if no image has been set)
  this.dragAndDropHandler = scout.dragAndDrop.handler(this,
      scout.dragAndDrop.SCOUT_TYPES.FILE_TRANSFER,
      function() { return this.dropType; }.bind(this),
      function() { return this.dropMaximumSize; }.bind(this));
  this.dragAndDropHandler.install(this.$fieldContainer);

  var $field = $('<img>')
    .addClass('image')
    .appendTo(this.$fieldContainer)
    .on('load', this._onImageLoad.bind(this))
    .on('error', this._onImageError.bind(this));

  this.addLabel();
  this.addField($field);
  this.addStatus();
};

scout.ImageField.prototype._onImageLoad = function(event) {
  this.$field.removeClass('broken');
  scout.scrollbars.update(this.$fieldContainer);
  this.revalidateLayoutTree();
};

scout.ImageField.prototype._onImageError = function(event) {
  this.$field.addClass('empty broken');
  scout.scrollbars.update(this.$fieldContainer);
  this.revalidateLayoutTree();
};

scout.ImageField.prototype._renderProperties = function() {
  scout.ImageField.parent.prototype._renderProperties.call(this);
  this._renderImageUrl();
  this._renderAutoFit();
  this._renderMenus();
  this._renderScrollBarEnabled();
};

scout.ImageField.prototype._remove = function() {
  scout.scrollbars.uninstall(this.$fieldContainer, this.session);
  scout.ImageField.parent.prototype._remove.call(this);
};

scout.ImageField.prototype._renderImageUrl = function() {
  this.$field.attr('src', this.imageUrl);
  // Hide <img> when it has no content to suppress the browser's 'broken image' icon
  this.$field.toggleClass('empty', !this.imageUrl);
};

scout.ImageField.prototype._renderAutoFit = function() {
  this.$field.toggleClass('autofit', this.autoFit);
  scout.scrollbars.update(this.$fieldContainer);
};

scout.ImageField.prototype._renderScrollBarEnabled = function() {
  // Note: Inner alignment has to be updated _before_ installing the scrollbar, because the inner
  // alignment uses absolute positioning, which confuses the scrollbar calculations.
  this._updateInnerAlignment();

  if (this.scrollBarEnabled) {
    scout.scrollbars.install(this.$fieldContainer, this.session, {
      invertColors: true
    });
  } else {
    scout.scrollbars.uninstall(this.$fieldContainer, this.session);
  }
};

scout.ImageField.prototype._renderGridData = function() {
  this._updateInnerAlignment();
};

scout.ImageField.prototype._updateInnerAlignment = function() {
  // Enable inner alignment only when scrollbars are disabled
  this.updateInnerAlignment({
    useHorizontalAlignment: (!this.scrollBarEnabled),
    useVerticalAlignment: (!this.scrollBarEnabled)
  });
};
