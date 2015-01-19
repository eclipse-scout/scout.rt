/**
 * Single Layout. Expects the container to have exactly one child. Resizes the child so it has the same size as the container.
 */
scout.RadioButtonGroupLayout = function(radioButtonGroup) {
  scout.RadioButtonGroupLayout.parent.call(this);
  this.radioButtonGroup = radioButtonGroup;
};
scout.inherits(scout.RadioButtonGroupLayout, scout.AbstractLayout);

scout.RadioButtonGroupLayout.prototype.layout = function($container) {
  var htmlContainer = scout.HtmlComponent.get($container),
    htmlBody = this._htmlBody(),
    bodySize;

  bodySize = htmlContainer.getAvailableSize()
    .subtract(htmlContainer.getInsets())
    .subtract(htmlBody.getMargins());

  $.log.trace('(radioButtonGroup#layout) bodySize=' + bodySize);
  htmlBody.setSize(bodySize);
};

scout.RadioButtonGroupLayout.prototype.preferredLayoutSize = function($container) {
  var htmlContainer = scout.HtmlComponent.get($container),
    htmlBody = this._htmlBody(),
    prefSize;

  prefSize = htmlBody.getPreferredSize()
    .add(htmlContainer.getInsets())
    .add(htmlBody.getMargins());

  return prefSize;
};


scout.RadioButtonGroupLayout.prototype._htmlBody = function() {
  return scout.HtmlComponent.get(this.radioButtonGroup._$body);
};
