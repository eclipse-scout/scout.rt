/**
 * This file contains various constant, classes and functions used for layouting.
 */
scout.LayoutConstants = {
    'MIN':0,
    'PREF':1,
    'MAX':2,
    'EPS':1e-6
};

/**
 * Abstract layout class with functions used by all layout algorithms.
 */
scout.AbstractLayout = function() {
  this.valid = false;
  this.validityBasedOnParentSize = new scout.Dimension();
};

scout.AbstractLayout.prototype._verifyLayout = function($parent) {
  var htmlParent = scout.HtmlComponent.get($parent),
    parentSize = htmlParent.getSize();
  if (!this.valid || !this.validityBasedOnParentSize.equals(parentSize)) {
    this.validityBasedOnParentSize = parentSize;
    this.validateLayout($parent);
    this.valid = true;
  }
};

/**
 * Returns the size of a visible component or (0,0) when component is invisible.
 */
scout.AbstractLayout.prototype._getVisibleSize = function($comp) {
  if ($comp.length === 1 && $comp.isVisible()) {
    // TODO AWE: (layout) static methode auf HtmlComponent -->
    return new scout.Dimension($comp.outerWidth(true), $comp.outerHeight(true));
  } else {
    return new scout.Dimension(0, 0);
  }
};

scout.AbstractLayout.prototype.invalidate = function() {
  this.valid = false;
};

/**
 * Form layout.
 */
scout.FormLayout = function() {
  scout.FormLayout.parent.call(this);
};
scout.inherits(scout.FormLayout, scout.AbstractLayout);

scout.FormLayout.prototype.layout = function($container) {
  var htmlRootGb = this._getHtmlRootGroupBox($container),
    contSize = scout.HtmlComponent.get($container).getSize(),
    rootGbInsets = htmlRootGb.getInsets(),
    rootGbSize = new scout.Dimension(
      contSize.width - rootGbInsets.left - rootGbInsets.right,
      contSize.height - rootGbInsets.top - rootGbInsets.bottom - this._getMenuBarHeight($container));
  $.log.trace('(FormLayout#layout) contSize=' + contSize);
  htmlRootGb.setSize(rootGbSize);
};

scout.FormLayout.prototype.preferredLayoutSize = function($container) {
  var prefSize = this._getHtmlRootGroupBox($container).getPreferredSize();
  prefSize.height += this.getMenuBarHeight($container);
  return prefSize;
};

scout.FormLayout.prototype._getHtmlRootGroupBox = function($container) {
  var $rootGb = $container.children('.root-group-box');
  return scout.HtmlComponent.get($rootGb);
};

scout.FormLayout.prototype._getMenuBarHeight = function($container) {
  return this._getVisibleSize($container.children('.menubar')).height;
};

/**
 * Group-Box layout.
 */
scout.GroupBoxLayout = function() {
  scout.GroupBoxLayout.parent.call(this);
};
scout.inherits(scout.GroupBoxLayout, scout.AbstractLayout);

scout.GroupBoxLayout.prototype.layout = function($container) {
  var contSize = scout.HtmlComponent.get($container).getSize();
  $.log.trace('(GroupBoxLayout#layout) contSize=' + contSize);
  this._getHtmlBody($container).setSize(new scout.Dimension(
      contSize.width,
      contSize.height -
        this._getTitleHeight($container) -
        this._getButtonBarHeight($container)));
};

scout.GroupBoxLayout.prototype.preferredLayoutSize = function($container) {
  var bodySize = this._getHtmlBody($container).getPreferredSize();
  return new scout.Dimension(
      bodySize.width,
      bodySize.height +
        this._getTitleHeight($container) +
        this._getButtonBarHeight($container));
};

scout.GroupBoxLayout.prototype._getTitleHeight = function($container) {
  return this._getVisibleSize($container.children('.group-box-title')).height;
};

scout.GroupBoxLayout.prototype._getButtonBarHeight = function($container) {
  return this._getVisibleSize($container.children('.button-bar')).height;
};

scout.GroupBoxLayout.prototype._getHtmlBody = function($container) {
  return scout.HtmlComponent.get($container.children('.group-box-body'));
};

/**
 * Form-Field Layout, for a form-field with label, status-label and a field-
 */
scout.FormFieldLayout = function() {
  scout.FormFieldLayout.parent.call(this);
  this.labelWidth = scout.HtmlEnvironment.fieldLabelWidth;
  this.statusWidth = 10;
};
scout.inherits(scout.FormFieldLayout, scout.AbstractLayout);

scout.FormFieldLayout.prototype.layout = function($container) {
  var htmlComp = scout.HtmlComponent.get($container),
    contSize = htmlComp.getSize(),
    widthSum = 0,
    $label = $container.children('label'),
    $status = $container.children('.status'),
    $field = $container.children('.field');
  if ($label.isVisible()) {
    scout.HtmlComponent.setBounds($label, 0, 0, this.labelWidth, contSize.height);
    // with this property we achieve "vertical-align:middle" which doesn't work for non-table-cell elements
    $label.css('line-height', contSize.height + 'px');
    widthSum += this.labelWidth;
  }
  if ($status.isVisible()) {
    scout.HtmlComponent.setBounds($status, widthSum, 0, this.statusWidth, contSize.height);
    $status.css('line-height', contSize.height + 'px');
    widthSum += this.statusWidth;
  }
  var fieldBounds = new scout.Rectangle(widthSum, 0, contSize.width - widthSum, contSize.height),
    htmlField = scout.HtmlComponent.optGet($field);
  // TODO AWE: (layout) dafür sorgen, dass wir hier immer ein get() machen können
  if (htmlField) {
    htmlField.setBounds(fieldBounds);
  } else {
    scout.HtmlComponent.setBounds($field, fieldBounds);
  }
};

scout.FormFieldLayout.prototype.preferredLayoutSize = function($container) {
  var width = 0,
    height = scout.HtmlEnvironment.formRowHeight,
    $label = $container.children('label'),
    $status = $container.children('.status'),
    $field = $container.children('.field');
  if ($label.isVisible()) {
    width += this.labelWidth;
  }
  if ($status.isVisible()) {
    width += this.statusWidth;
  }
  if ($field.isVisible()) {
    // TODO AWE: (layout) dafür sorgen, dass wir hier immer ein get() machen können
    var prefSize, htmlField = scout.HtmlComponent.optGet($field);
    if (htmlField) {
      prefSize = htmlField.getPreferredSize();
    } else {
      prefSize = new scout.Dimension($field.width(), $field.height());
    }
    width += prefSize.width;
    height = Math.max(height, prefSize.height);
  }
  return new scout.Dimension(width, height);
};


/**
 * Text-Field Layout, used to calculate the preferred size of a HTML text-field. Note that this is not the same as the
 * "auto" size of the HTML element. Browsers typically render a text-field larger than the minimum size to display the whole text.
 */
scout.TextFieldLayout = function() {
  scout.TextFieldLayout.parent.call(this);
};
scout.inherits(scout.TextFieldLayout, scout.AbstractLayout);

scout.TextFieldLayout.prototype.preferredLayoutSize = function($container) {
   return scout.graphics.measureString($container.val());
};

/**
 * Button Field Layout, for fields with a button.
 */
scout.ButtonFieldLayout = function() {
  scout.ButtonFieldLayout.parent.call(this);
};
scout.inherits(scout.ButtonFieldLayout, scout.AbstractLayout);

scout.ButtonFieldLayout.prototype.layout = function($container) {
  // button has no children - nothing to do here
};

// TODO AWE: (layout) use HtmlComponent#getInsets here
scout.ButtonFieldLayout.prototype.preferredLayoutSize = function($container) {
  var $button = $container.children('button'),
    hMargin = $button.outerWidth(true) - $button.width(),
    vMargin = $button.outerHeight(true) - $button.height(),
    textSize = scout.graphics.measureString($button.html());
  return new scout.Dimension(textSize.width + hMargin, textSize.height + vMargin);
};

/**
 * Tab-box Layout.
 */
scout.TabBoxLayout = function() {
  scout.TabBoxLayout.parent.call(this);
};
scout.inherits(scout.TabBoxLayout, scout.AbstractLayout);

scout.TabBoxLayout.prototype.layout = function($container) {
  var htmlCont = scout.HtmlComponent.get($container),
    contSize = htmlCont.getSize(),
    $tabArea =  $container.children('.tab-area'),
    tabAreaHeight = 0;
  if ($tabArea.isVisible()) {
    // TODO AWE: (tab-box) tabArea neu layouten, inkl. tab-runs - muss noch definiert werden, wie wir das darstellen
    // TODO AWE: (layout) function machen um "nur width" zu setzen.
    $tabArea.css('width', contSize.width + 'px');
    tabAreaHeight = $tabArea.outerHeight(true);
  }
  scout.HtmlComponent.get($container.children('.tab-content')).setSize(
      new scout.Dimension(contSize.width, contSize.height - tabAreaHeight));
};

scout.TabBoxLayout.prototype.preferredLayoutSize = function($container) {
  var $tabArea = $container.children('.tab-area'),
    $tabContent = $container.children('.tab-content'),
    tabAreaSize = this._getVisibleSize($tabArea),
    tabContentSize = scout.HtmlComponent.get($tabContent).getPreferredSize();
  //TODO AWE: (tab-box) impl. prefSize
  // size of tab-area
  // ... calculate tab-runs = Die container breite nehmen, mit buttons
  //     füllen, wenn die summe der buttons > breite ist, eine neue zeile
  //     beginnen.
  // size of content (find largest group-box)
  // ... hier haben wir das problem, das die content-panes nicht visible
  //     bzw. gar nicht im DOM sind. Trotzdem müssen wir irgendwie die
  //     grösse der groupbox berechnen können
  return new scout.Dimension(
    Math.max(tabAreaSize.width, tabContentSize.width),
    tabAreaSize.height + tabContentSize.height);
};

/**
 * Null Layout.
 */
scout.NullLayout = function() {
  scout.NullLayout.parent.call(this);
};
scout.inherits(scout.NullLayout, scout.AbstractLayout);

scout.NullLayout.prototype.layout = function($container) {
  // NOP
};

/**
 * Single Layout. Expects the container to have exactly one child. Resizes the child so it has the same size as the container.
 */
scout.SingleLayout = function() {
  scout.SingleLayout.parent.call(this);
};
scout.inherits(scout.SingleLayout, scout.AbstractLayout);

scout.SingleLayout.prototype.preferredLayoutSize = function($container) {
  return this._getHtmlSingleChild($container).getPreferredSize();
};

scout.SingleLayout.prototype.layout = function($container) {
  this._getHtmlSingleChild($container).setSize(
      scout.HtmlComponent.get($container).getSize());
};

scout.SingleLayout.prototype._getHtmlSingleChild = function($container) {
  return scout.HtmlComponent.get($container.children().first());
};
