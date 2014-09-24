/**
 * Wrapper for a JQuery selector. Used as replacement for javax.swing.JComponent.
 */
scout.HtmlComponent = function($comp) {
  this.$comp = $comp;
  this.layoutManager;
  this.layoutData;
  // link DOM element with this instance
  $comp.data('htmlComponent', this);
};

/**
 * Static method to get the HtmlComponent associated with the given DOM $comp.
 * Throws an error when data 'htmlComponent' is not set.
 */
scout.HtmlComponent.get = function($comp) {
  var htmlComp = this.optGet($comp);
  if (!htmlComp) {
    throw new Error('data "htmlComponent" is undefined');
  }
  return htmlComp;
};

scout.HtmlComponent.optGet = function($comp) {
  return $comp.data('htmlComponent');
};

/* These functions are designed to be used with box-sizing:box-model. The only reliable
 * way to set the size of a component when working with box model is to use css('width/height'...)
 * in favor of width/height() functions.
 */

scout.HtmlComponent.setSize = function($comp, vararg, height) {
  var size = vararg instanceof scout.Dimension ?
      vararg : new scout.Dimension(vararg, height);
  $comp.
    css('width', size.width + 'px').
    css('height', size.height+ 'px');
};

scout.HtmlComponent.setBounds = function($comp, vararg, y, width, height) {
  var bounds = vararg instanceof scout.Rectangle ?
      vararg : new scout.Rectangle(vararg, y, width, height);
  $comp.
    css('left', bounds.x + 'px').
    css('top', bounds.y + 'px').
    css('width', bounds.width + 'px').
    css('height', bounds.height + 'px');
};

/**
 * Returns the parent or $comp. Creates a new instance of HtmlComponent if the parent DOM element has no linked instance.
 */
scout.HtmlComponent.prototype.getParent = function() {
  var $parent = this.$comp.parent(),
    htmlParent = scout.HtmlComponent.optGet($parent);
  return htmlParent || new scout.HtmlComponent($parent);
};

scout.HtmlComponent.prototype.layout = function() {
  if (this.layoutManager) {
    this.layoutManager.layout(this.$comp);
  } else {
    $.log.warn('(HtmlComponent#layout) Called layout() but component ' + this.debug() + ' has no layout manager');
    // throw 'Tried to layout component ' + this.debug() +' but component has no layout manager';
    // TODO AWE: (layout) entscheiden, ob wir dieses throw "scharf machen" wollen oder nicht
  }
};

/**
 * Sets the given layout manager.
 */
scout.HtmlComponent.prototype.setLayout = function(layoutManager) {
  this.layoutManager = layoutManager;
};

/**
 * Returns the preferred size of the component, insets included.
 */
scout.HtmlComponent.prototype.getPreferredSize = function() {
  var prefSize;
  if (this.layoutManager) {
    prefSize = this.layoutManager.preferredLayoutSize(this.$comp);
    $.log.trace('(HtmlComponent#getPreferredSize) ' + this.debug() + ' impl. preferredSize=' + prefSize);
  } else {
    // TODO AWE: (layout) hier koennten wir eigentlich einen fehler werfen, weil das nicht passieren sollte
    prefSize = scout.Dimension(this.$comp.width(), this.$comp.height());
    $.log.trace('(HtmlComponent#getPreferredSize) ' + this.debug() + ' size of HTML element=' + prefSize);
  }
  return prefSize;
};

/**
 * Returns the inset-dimensions of the component (padding, margin, border).
 */
scout.HtmlComponent.prototype.getInsets = function() {
  var directions = ['top', 'right', 'bottom', 'left'],
    insets = [0, 0, 0, 0],
    i,
  cssToInt = function($comp, cssProp) {
      return parseInt($comp.css(cssProp), 10);
    };
  for (i = 0; i < directions.length; i++) {
    // parseInt will ignore 'px' in string returned from css() method
    insets[i] += cssToInt(this.$comp, 'margin-' + directions[i]);
    insets[i] += cssToInt(this.$comp, 'padding-' + directions[i]);
    insets[i] += cssToInt(this.$comp, 'border-' + directions[i] + '-width');
  }
  return new scout.Insets(insets[0], insets[1], insets[2], insets[3]);
};

/**
 * Returns the current size of the component, insets included.
 * TODO AWE: (layout) prüfen ob hier tatsächlich die insets included sind. Müssten wir dann nicht outerWidth/-Height verwenden?
 */
scout.HtmlComponent.prototype.getSize = function() {
  return new scout.Dimension(
      this.$comp.width(),
      this.$comp.height());
};

/**
 * Sets the size of the component, insets included. Which means: the method subtracts the components insets
 * from the given size before setting the width/height of the component.
 */
scout.HtmlComponent.prototype.setSize = function(size) {
  var oldSize = this.getSize();
  if (!oldSize.equals(size)) {
    this.layoutManager.invalidate();
  }
  scout.HtmlComponent.setSize(this.$comp, size);
  this.layout();
};

scout.HtmlComponent.prototype.getBounds = function() {
  return new scout.Rectangle(
      this.$comp.css('left'),
      this.$comp.css('top'),
      this.$comp.width(),
      this.$comp.height());
};

scout.HtmlComponent.prototype.setBounds = function(bounds) {
  var oldBounds = this.getBounds();
  if (!oldBounds.equals(bounds)) {
    this.layoutManager.invalidate();
  }
  scout.HtmlComponent.setBounds(this.$comp, bounds);
  this.layout();
};

scout.HtmlComponent.prototype.debug = function() {
  var attrs = '';
  if (this.$comp.attr('id')) {
    attrs += 'id=' + this.$comp.attr('id');
  }
  if (this.$comp.attr('class')) {
    attrs += ' class=' + this.$comp.attr('class');
  }
  if (attrs.length === 0) {
    attrs = this.$comp.html().substring(0, 30) + '...';
  }
  return 'HtmlComponent[' + attrs.trim() + ']';
};
