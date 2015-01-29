scout.GroupBoxLayout = function(groupBox) {
  scout.GroupBoxLayout.parent.call(this);
  this.groupBox = groupBox;
};
scout.inherits(scout.GroupBoxLayout, scout.AbstractLayout);

scout.GroupBoxLayout.prototype.layout = function($container) {
  var htmlContainer = scout.HtmlComponent.get($container),
    htmlGbBody = this._htmlGbBody(),
    htmlGbBodyContainer = this._htmlGbBodyContainer(),
    gbBodySize;

  gbBodySize = htmlContainer.getAvailableSize()
    .subtract(htmlContainer.getInsets())
    .subtract(htmlGbBody.getMargins());
  gbBodySize.height -= this._titleHeight($container);

  $.log.trace('(GroupBoxLayout#layout) gbBodySize=' + gbBodySize);
  htmlGbBody.setSize(gbBodySize);

  if (htmlGbBodyContainer.scrollable) {
    scout.scrollbars.update(htmlGbBodyContainer.$comp);
  }
};

scout.GroupBoxLayout.prototype.preferredLayoutSize = function($container) {
  var htmlContainer = scout.HtmlComponent.get($container),
    htmlGbBody = this._htmlGbBody(),
    prefSize;

  prefSize = htmlGbBody.getPreferredSize()
    .add(htmlContainer.getInsets())
    .add(htmlGbBody.getMargins());
  prefSize.height += this._titleHeight($container);

  return prefSize;
};

scout.GroupBoxLayout.prototype._titleHeight = function($container) {
  return scout.graphics.getVisibleSize($container.children('.group-box-title'), true).height;
};

scout.GroupBoxLayout.prototype._htmlGbBody = function() {
  return scout.HtmlComponent.get(this.groupBox.$body);
};

scout.GroupBoxLayout.prototype._htmlGbBodyContainer = function() {
  return scout.HtmlComponent.get(this.groupBox.$body);
};
