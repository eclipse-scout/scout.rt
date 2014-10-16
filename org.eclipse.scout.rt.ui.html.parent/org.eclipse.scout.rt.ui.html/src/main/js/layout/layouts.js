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
  // Set this flag to false if the controls in your container don't depend on size changes of the container
  // (e.g. width and height is set by the css).
  // -> Layout() won't be called if the size of the container changes
  this.invalidateOnResize = true;

  this.validityBasedOnParentSize = new scout.Dimension();
};

scout.AbstractLayout.prototype.invalidate = function() {
  // may be implemented by subclasses
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
  return scout.HtmlComponent.getVisibleSize($container.children('.menubar')).height;
};

/**
 * Group-Box layout.
 */
scout.GroupBoxLayout = function() {
  scout.GroupBoxLayout.parent.call(this);
};
scout.inherits(scout.GroupBoxLayout, scout.AbstractLayout);

scout.GroupBoxLayout.prototype.layout = function($container) {
  var htmlComp = scout.HtmlComponent.get($container),
    bodySize = htmlComp.getSize().subtractInsets(htmlComp.getInsets()),
    newSize;

  $.log.trace('(GroupBoxLayout#layout) bodySize=' + bodySize);

  newSize = new scout.Dimension(
      bodySize.width,
      bodySize.height -
        this._getTitleHeight($container) -
        this._getButtonBarHeight($container));
  this._getHtmlBody($container).setSize(newSize);
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
  return scout.HtmlComponent.getVisibleSize($container.children('.group-box-title')).height;
};

scout.GroupBoxLayout.prototype._getButtonBarHeight = function($container) {
  return scout.HtmlComponent.getVisibleSize($container.children('.button-bar')).height;
};

scout.GroupBoxLayout.prototype._getHtmlBody = function($container) {
  return scout.HtmlComponent.get($container.children('.group-box-body'));
};

/**
 * Form-Field Layout, for a form-field with label, status, mandatory-indicator and a field
 */
scout.FormFieldLayout = function() {
  scout.FormFieldLayout.parent.call(this);
  this.labelWidth = scout.HtmlEnvironment.fieldLabelWidth;
};
scout.inherits(scout.FormFieldLayout, scout.AbstractLayout);

scout.FormFieldLayout.prototype.layout = function($container) {
  var fieldBounds, htmlField;
  var htmlComp = scout.HtmlComponent.get($container),
    contSize = htmlComp.getSize().subtractInsets(htmlComp.getInsets()),
    leftWidth = 0,
    rightWidth = 0,
    $label = $container.children('label'),
    $status = $container.children('.status'),
    $mandatory = $container.children('.mandatory-indicator'),
    $field = $container.children('.field'),
    $icon = $container.children('.icon');

  if ($label.isVisible()) {
    scout.HtmlComponent.setBounds($label, 0, 0, this.labelWidth, contSize.height);
    // with this property we achieve "vertical-align:middle" which doesn't work for non-table-cell elements
    $label.css('line-height', contSize.height + 'px');
    leftWidth += this.labelWidth;
  }
  if ($mandatory.length > 0) {
    $mandatory.cssLeft(leftWidth);
    leftWidth += $mandatory.outerWidth(true);
  }
  if ($status.length > 0) {
    $status.css('line-height', contSize.height + 'px');
    rightWidth += $status.outerWidth(true);
  }

  fieldBounds = new scout.Rectangle(leftWidth, 0, contSize.width - leftWidth - rightWidth, contSize.height);
  htmlField = scout.HtmlComponent.optGet($field);
  // TODO AWE: (layout) dafür sorgen, dass wir hier immer ein get() machen können
  if (htmlField) {
    htmlField.setBounds(fieldBounds);
  } else {
    scout.HtmlComponent.setBounds($field, fieldBounds);
  }

  //Icon is placed inside the field (as overlay)
  if ($icon.length > 0) {
    $icon.css('right', $field.cssBorderRightWidth() + rightWidth + 'px');
  }
};

scout.FormFieldLayout.prototype.preferredLayoutSize = function($container) {
  var width = 0,
    height = scout.HtmlEnvironment.formRowHeight,
    $label = $container.children('label'),
    $status = $container.children('.status'),
    $mandatory = $container.children('.mandatory-indicator'),
    $field = $container.children('.field');
  if ($label.isVisible()) {
    width += this.labelWidth;
  }
  if ($mandatory.length > 0) {
    width += $mandatory.outerWidth(true);
  }
  if ($status.length > 0) {
    width += $status.outerWidth(true);
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
    contSize = htmlCont.getSize().subtractInsets(htmlCont.getInsets()),
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
    tabAreaSize = scout.HtmlComponent.getVisibleSize($tabArea),
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
  $container.children().each(function() {
    var htmlComp = scout.HtmlComponent.get($(this));
    if (htmlComp) {
      htmlComp.layout();
    }
  });
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
  var htmlComp = scout.HtmlComponent.get($container);
  this._getHtmlSingleChild($container).setSize(
      htmlComp.getSize().subtractInsets(htmlComp.getInsets()));
};

scout.SingleLayout.prototype._getHtmlSingleChild = function($container) {
  return scout.HtmlComponent.get($container.children().first());
};


/**
 * Table Layout.
 */
scout.TableLayout = function(table) {
  scout.TableLayout.parent.call(this);
  this.table = table;
  this.invalidateOnResize = false;
};
scout.inherits(scout.TableLayout, scout.AbstractLayout);

scout.TableLayout.prototype.layout = function($container) {
  var menubar = this.table.menubar,
    footer = this.table.footer,
    $header = this.table._$header,
    $data = this.table.$data,
    height = 0;

  if (menubar.$container.isVisible()){
    height += scout.HtmlComponent.getSize(menubar.$container).height;
  }
  if (footer) {
    height += scout.HtmlComponent.getSize(footer.$container).height;
  }
  if ($header.isVisible()) {
    height += scout.HtmlComponent.getSize($header).height;
  }
  $data.css('height', 'calc(100% - '+ height + 'px)');

  this.valid2 = true;
};

scout.TableLayout.prototype.preferredLayoutSize = function($comp) {
  return scout.HtmlComponent.getSize($comp);
};
