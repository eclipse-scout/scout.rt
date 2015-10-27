/**
 * This layout only layout the INPUT and DIV part of the multi-line smart-field, not the entire form-field.
 */
scout.SmartFieldMultilineLayout = function() {
  scout.SmartFieldMultilineLayout.parent.call(this);
};
scout.inherits(scout.SmartFieldMultilineLayout, scout.AbstractLayout);

scout.SmartFieldMultilineLayout.prototype.layout = function($container) {
  var htmlContainer = scout.HtmlComponent.get($container),
    $inputField = $container.children('.multiline'),
    $multilines = $container.children('.multiline-field'),
    innerSize = htmlContainer.getAvailableSize()
    .subtract(htmlContainer.getInsets());

  $inputField.cssHeight(scout.HtmlEnvironment.formRowHeight);
  $multilines.cssHeight(innerSize.height - scout.HtmlEnvironment.formRowHeight);
};
