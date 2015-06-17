scout.ImageFieldLayout = function(formField) {
  scout.ImageFieldLayout.parent.call(this, formField);
};
scout.inherits(scout.ImageFieldLayout, scout.FormFieldLayout);

scout.ImageFieldLayout.prototype.layout = function($container) {
  scout.ImageFieldLayout.parent.prototype.layout.call(this, $container);
  scout.scrollbars.update(this.formField.$fieldContainer);
};

scout.ImageFieldLayout.prototype.naturalSize = function(formField) {
  var img = formField.$field[0];
  return new scout.Dimension(img.naturalWidth, img.naturalHeight);
};
