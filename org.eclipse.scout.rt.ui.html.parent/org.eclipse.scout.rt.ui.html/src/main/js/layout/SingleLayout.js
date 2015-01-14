/**
 * Resizes the child so it has the same size as the container.<br>
 * If no child is provided, the first child in the container is used.
 */
scout.SingleLayout = function(htmlChild) {
  scout.SingleLayout.parent.call(this);
  this._htmlChild = htmlChild;
};
scout.inherits(scout.SingleLayout, scout.AbstractLayout);

scout.SingleLayout.prototype.preferredLayoutSize = function($container) {
  if (!this._htmlChild) {
    this._htmlChild = this._getHtmlSingleChild($container);
  }
  return this._htmlChild.getPreferredSize();
};

scout.SingleLayout.prototype.layout = function($container) {
  var htmlContainer = scout.HtmlComponent.get($container);
  var childSize = htmlContainer.getSize()
    .subtract(htmlContainer.getInsets());

  if (!this._htmlChild) {
    this._htmlChild = this._getHtmlSingleChild($container);
  }
  this._htmlChild.setSize(childSize);
};

scout.SingleLayout.prototype._getHtmlSingleChild = function($container) {
  return scout.HtmlComponent.get($container.children().first());
};
