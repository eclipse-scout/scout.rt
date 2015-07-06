scout.GroupBoxLayout = function(groupBox) {
  scout.GroupBoxLayout.parent.call(this);
  this._groupBox = groupBox;
  this._statusWidth = scout.HtmlEnvironment.fieldStatusWidth;
};
scout.inherits(scout.GroupBoxLayout, scout.AbstractLayout);

scout.GroupBoxLayout.prototype.layout = function($container) {
  var titleMarginX, menuBarSize, gbBodySize,
    htmlContainer = scout.HtmlComponent.get($container),
    htmlGbBody = this._htmlGbBody(),
    htmlGbBodyContainer = this._htmlGbBodyContainer(),
    htmlMenuBar = this._htmlMenuBar(),
    $groupBoxTitle = this._groupBox._$groupBoxTitle,
    $pseudoStatus = this._groupBox.$pseudoStatus;

  if (htmlMenuBar) {
    // required to trigger layout() of menu-bar
    menuBarSize = htmlMenuBar.getPreferredSize();
    htmlMenuBar.setSize(menuBarSize);
  } else {
    menuBarSize = new scout.Dimension(0, 0);
  }

  gbBodySize = htmlContainer.getAvailableSize()
    .subtract(htmlContainer.getInsets())
    .subtract(htmlGbBody.getMargins());
  gbBodySize.height -= this._titleHeight($container);
  gbBodySize.height -= menuBarSize.height;

  if ($pseudoStatus.isVisible()) {
    $pseudoStatus.cssWidth(this._statusWidth);
    titleMarginX = $groupBoxTitle.cssMarginX() + $pseudoStatus.outerWidth(true);
    $groupBoxTitle.css('width', 'calc(100% - ' + titleMarginX + 'px');
  }

  $.log.trace('(GroupBoxLayout#layout) gbBodySize=' + gbBodySize);
  htmlGbBody.setSize(gbBodySize);

  if (htmlGbBodyContainer.scrollable) {
    scout.scrollbars.update(htmlGbBodyContainer.$comp);
  }
};

scout.GroupBoxLayout.prototype.preferredLayoutSize = function($container) {
  var htmlContainer = scout.HtmlComponent.get($container),
    htmlGbBody = this._htmlGbBody(),
    htmlMenuBar,
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

  htmlMenuBar = this._htmlMenuBar();
  if (htmlMenuBar) {
    prefSize.height += htmlMenuBar.getSize(true).height;
  }

  return prefSize;
};

scout.GroupBoxLayout.prototype._titleHeight = function($container) {
  return scout.graphics.getVisibleSize($container.children('.group-box-title'), true).height;
};

/**
 * Return menu-bar when it exists and it is visible.
 */
scout.GroupBoxLayout.prototype._htmlMenuBar = function() {
  if (this._groupBox.menuBar) {
    var htmlMenuBar = scout.HtmlComponent.get(this._groupBox.menuBar.$container);
    if (htmlMenuBar.isVisible()) {
      return htmlMenuBar;
    }
  }
  return null;
};

scout.GroupBoxLayout.prototype._htmlGbBody = function() {
  return scout.HtmlComponent.get(this._groupBox.$body);
};

scout.GroupBoxLayout.prototype._htmlGbBodyContainer = function() {
  return scout.HtmlComponent.get(this._groupBox.$body);
};
