scout.GroupBoxLayout = function(groupBox) {
  scout.GroupBoxLayout.parent.call(this);
  this.groupBox = groupBox;
};
scout.inherits(scout.GroupBoxLayout, scout.AbstractLayout);

scout.GroupBoxLayout.prototype.layout = function($container) {
  var htmlContainer = scout.HtmlComponent.get($container),
    htmlGbBody = this._htmlGbBody(),
    gbBodySize;

  gbBodySize = htmlContainer.getSize()
    .subtract(htmlContainer.getInsets())
    .subtract(htmlGbBody.getMargins());
  gbBodySize.height -= this._titleHeight($container);
  gbBodySize.height -= this._buttonBarHeight($container);

  $.log.trace('(GroupBoxLayout#layout) gbBodySize=' + gbBodySize);
  htmlGbBody.setSize(gbBodySize);
};

scout.GroupBoxLayout.prototype.preferredLayoutSize = function($container) {
  var htmlContainer = scout.HtmlComponent.get($container),
    htmlGbBody = this._htmlGbBody(),
    prefSize;

  prefSize = htmlGbBody.getPreferredSize()
    .add(htmlContainer.getInsets())
    .add(htmlGbBody.getMargins());
  prefSize.height += this._titleHeight($container);
  prefSize.height += this._buttonBarHeight($container);

  return prefSize;
};

scout.GroupBoxLayout.prototype._titleHeight = function($container) {
  return scout.graphics.getVisibleSize($container.children('.group-box-title'), true).height;
};

scout.GroupBoxLayout.prototype._buttonBarHeight = function($container) {
  return scout.graphics.getVisibleSize($container.children('.button-bar'), true).height;
};

scout.GroupBoxLayout.prototype._htmlGbBody = function() {
  return scout.HtmlComponent.get(this.groupBox._$body);
};
