/**
 * Null Layout.
 */
scout.NullLayout = function() {
  scout.NullLayout.parent.call(this);
};
scout.inherits(scout.NullLayout, scout.AbstractLayout);

scout.NullLayout.prototype.layout = function($container) {
  $container.children().each(function() {
    var htmlComp = scout.HtmlComponent.get($(this));
    if (htmlComp) {
      htmlComp.validateLayout();
    }
  });
};
