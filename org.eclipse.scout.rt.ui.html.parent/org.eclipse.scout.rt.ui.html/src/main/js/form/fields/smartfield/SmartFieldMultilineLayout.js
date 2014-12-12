scout.SmartFieldMultilineLayout = function() {
  scout.SmartFieldMultilineLayout.parent.call(this);
};
scout.inherits(scout.SmartFieldMultilineLayout, scout.AbstractLayout);

scout.SmartFieldMultilineLayout.prototype.preferredLayoutSize = function($container) {
  var htmlContainer = scout.HtmlComponent.get($container);
  var outerSize = htmlContainer.getSize()
    .add(htmlContainer.getInsets());
  return outerSize;
};

scout.SmartFieldMultilineLayout.prototype.layout = function($container) {
  var htmlContainer = scout.HtmlComponent.get($container);
  var $inputField = $container.children('.multiline');
  var $multilines = $container.children('.multiline-field');
  var innerSize = htmlContainer.getSize()
    .subtract(htmlContainer.getInsets());

  $inputField.cssHeight(scout.HtmlEnvironment.formRowHeight);
  $multilines.cssHeight(innerSize.height - scout.HtmlEnvironment.formRowHeight);
};
