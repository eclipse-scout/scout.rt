/**
 * Single Layout. Expects the container to have exactly one child. Resizes the child so it has the same size as the container.
 */
scout.RadioButtonGroupLayout = function(radioButtons) {
  scout.RadioButtonGroupLayout.parent.call(this);
  this.radioButtons = radioButtons;
};
scout.inherits(scout.RadioButtonGroupLayout, scout.AbstractLayout);

scout.RadioButtonGroupLayout.prototype.preferredLayoutSize = function($container) {
  return new scout.Dimension(150, 30); // FIXME NBU: impl. prefSize
};

scout.RadioButtonGroupLayout.prototype.layout = function($container) {
  var i, x = 0,
    htmlContainer = scout.HtmlComponent.get($container),
    size = htmlContainer.getSize(),
    widthPerRb = size.width / this.radioButtons.length;
  for (i = 0; i < this.radioButtons.length; i++) {
    var htmlComp = scout.HtmlComponent.get(this.radioButtons[i].$container);
    htmlComp.setBounds(x, 0, widthPerRb, size.height);
    x += widthPerRb;
  }
};
