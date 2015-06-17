scout.HtmlFieldLayout = function(formField) {
  scout.HtmlFieldLayout.parent.call(this, formField);
};
scout.inherits(scout.HtmlFieldLayout, scout.FormFieldLayout);

scout.HtmlFieldLayout.prototype.layout = function($container) {
  scout.HtmlFieldLayout.parent.prototype.layout.call(this, $container);
  scout.scrollbars.update(this.formField.$field);
};
