scout.FormLayout = function(form) {
  scout.FormLayout.parent.call(this);
  this._form = form;
};
scout.inherits(scout.FormLayout, scout.AbstractLayout);

scout.FormLayout.prototype.layout = function($container) {
  var htmlContainer = scout.HtmlComponent.get($container),
    htmlRootGb = this._htmlRootGroupBox($container),
    rootGbSize;

  rootGbSize = htmlContainer.getAvailableSize()
    .subtract(htmlContainer.getInsets())
    .subtract(htmlRootGb.getMargins());

  if (this._form.isDialog()) {
    rootGbSize.height -= this._titleHeight($container);
  }

  $.log.trace('(FormLayout#layout) rootGbSize=' + rootGbSize);
  htmlRootGb.setSize(rootGbSize);
};

scout.FormLayout.prototype.preferredLayoutSize = function($container) {
  var htmlContainer = scout.HtmlComponent.get($container),
    htmlRootGb = this._htmlRootGroupBox($container),
    prefSize;

  prefSize = htmlRootGb.getPreferredSize()
    .add(htmlContainer.getInsets())
    .add(htmlRootGb.getMargins());
  prefSize.height += this._titleHeight($container);

  return prefSize;
};

scout.FormLayout.prototype._htmlRootGroupBox = function($container) {
  return scout.HtmlComponent.get($container.children('.root-group-box'));
};

scout.FormLayout.prototype._titleHeight = function($container) {
  // getVisibleSize doesn't work for dialogs, because the are still invisible when
  // the render() method is called (they'll become visible later, when the fade-in
  // animation is completed). That's why we must calculate the title size here:
  var height = 0,
    insets = scout.graphics.getInsets($container.children('.title-box'), {includeMargin: true});
  if (this._form.title) {
    height += scout.graphics.measureString(this._form.title, 'font-text-large').height;
  }
  if (this._form.subTitle) {
    height += scout.graphics.measureString(this._form.subTitle).height;
  }
  return height + insets.top + insets.bottom;

};
