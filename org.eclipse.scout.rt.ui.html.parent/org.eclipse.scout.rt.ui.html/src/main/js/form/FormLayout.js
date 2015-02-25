scout.FormLayout = function(form) {
  scout.FormLayout.parent.call(this);
  this._form = form;
};
scout.inherits(scout.FormLayout, scout.AbstractLayout);

scout.FormLayout.prototype.layout = function($container) {
  var htmlContainer = scout.HtmlComponent.get($container),
    htmlRootGb = this._getHtmlRootGroupBox($container),
    rootGbSize;

  rootGbSize = htmlContainer.getAvailableSize()
    .subtract(htmlContainer.getInsets())
    .subtract(htmlRootGb.getMargins());
  rootGbSize.height -= this._getMenuBarHeight($container);
  rootGbSize.height -= this._getTitleHeight($container);

  $.log.trace('(FormLayout#layout) rootGbSize=' + rootGbSize);
  htmlRootGb.setSize(rootGbSize);
};

scout.FormLayout.prototype.preferredLayoutSize = function($container) {
  var htmlContainer = scout.HtmlComponent.get($container),
    htmlRootGb = this._getHtmlRootGroupBox($container),
    prefSize;

  prefSize = htmlRootGb.getPreferredSize()
    .add(htmlContainer.getInsets())
    .add(htmlRootGb.getMargins());
  prefSize.height += this._getMenuBarHeight($container);
  prefSize.height += this._getTitleHeight($container);

  return prefSize;
};

scout.FormLayout.prototype._getHtmlRootGroupBox = function($container) {
  var $rootGb = $container.children('.root-group-box');
  return scout.HtmlComponent.get($rootGb);
};

scout.FormLayout.prototype._getMenuBarHeight = function($container) {
  return scout.graphics.getVisibleSize($container.children('.menubar'), true).height;
};

scout.FormLayout.prototype._getTitleHeight = function($container) {
  var height = 0,
    insets = scout.graphics.getInsets($container.children('.title-box'), {includeMargin: true});
  if (this._form.title) {
    height += scout.graphics.measureString(this._form.title, 'font-text-large').height;
  }
  if (this._form.subTitle) {
    height += scout.graphics.measureString(this._form.subTitle).height;
  }
  height += insets.top + insets.bottom;
  return height;
};

