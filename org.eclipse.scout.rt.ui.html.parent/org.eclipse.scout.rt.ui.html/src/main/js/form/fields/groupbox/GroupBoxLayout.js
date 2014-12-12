scout.GroupBoxLayout = function() {
  scout.GroupBoxLayout.parent.call(this);
};
scout.inherits(scout.GroupBoxLayout, scout.AbstractLayout);

scout.GroupBoxLayout.prototype.layout = function($container) {
  var htmlContainer = scout.HtmlComponent.get($container),
    htmlGbBody = this._getHtmlGbBody($container),
    gbBodySize;

  gbBodySize = htmlContainer.getSize()
    .subtract(htmlContainer.getInsets())
    .subtract(htmlGbBody.getMargins());
  gbBodySize.height -= this._getTitleHeight($container);
  gbBodySize.height -= this._getButtonBarHeight($container);

  $.log.trace('(GroupBoxLayout#layout) gbBodySize=' + gbBodySize);
  htmlGbBody.setSize(gbBodySize);
};

scout.GroupBoxLayout.prototype.preferredLayoutSize = function($container) {
  var htmlContainer = scout.HtmlComponent.get($container),
    htmlGbBody = this._getHtmlGbBody($container),
    prefSize;

  prefSize = htmlGbBody.getPreferredSize()
    .add(htmlContainer.getInsets())
    .add(htmlGbBody.getMargins());
  prefSize.height += this._getTitleHeight($container);
  prefSize.height += this._getButtonBarHeight($container);

  return prefSize;
};

scout.GroupBoxLayout.prototype._getTitleHeight = function($container) {
  return scout.graphics.getVisibleSize($container.children('.group-box-title'), true).height;
};

scout.GroupBoxLayout.prototype._getButtonBarHeight = function($container) {
  return scout.graphics.getVisibleSize($container.children('.button-bar'), true).height;
};

scout.GroupBoxLayout.prototype._getHtmlGbBody = function($container) {
  return scout.HtmlComponent.get($container.children('.group-box-body'));
};
