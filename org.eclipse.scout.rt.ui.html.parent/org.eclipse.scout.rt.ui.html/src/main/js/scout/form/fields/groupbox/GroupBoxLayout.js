scout.GroupBoxLayout = function(groupBox) {
  scout.GroupBoxLayout.parent.call(this);
  this._groupBox = groupBox;
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
  gbBodySize.height -= this._menuBarHeight($container);

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

  if (htmlGbBody.$comp.isVisible()) {
    prefSize = htmlGbBody.getPreferredSize()
      .add(htmlGbBody.getMargins());
  }
  else {
    prefSize = new scout.Dimension(0, 0);
  }
  prefSize = prefSize.add(htmlContainer.getInsets());
  prefSize.height += this._titleHeight($container);
  prefSize.height += this._menuBarHeight($container);

  return prefSize;
};

scout.GroupBoxLayout.prototype._titleHeight = function($container) {
  return scout.graphics.getVisibleSize($container.children('.group-box-title'), true).height;
};

scout.GroupBoxLayout.prototype._menuBarHeight = function($container) {
  return scout.graphics.getVisibleSize($container.children('.menubar'), true).height;
};

scout.GroupBoxLayout.prototype._htmlGbBody = function() {
  return scout.HtmlComponent.get(this._groupBox.$body);
};

scout.GroupBoxLayout.prototype._htmlGbBodyContainer = function() {
  return scout.HtmlComponent.get(this._groupBox.$body);
};
