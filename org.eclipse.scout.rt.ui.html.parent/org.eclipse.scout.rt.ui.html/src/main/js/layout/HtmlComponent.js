/**
 * Wrapper for a JQuery selector. Used as replacement for javax.swing.JComponent.
 */
scout.HtmlComponent = function($comp, session) {
  this.$comp = $comp;
  this.layoutManager = new scout.NullLayout();
  this.layoutData;
  this.valid = false;

  // Set this to false if your component automatically adjusts its size (e.g. by using css styling)
  // -> setSize won't be called
  this.pixelBasedSizing = true;

  if (!session) {
    throw new Error('session must be defined for ' + this);
  }
  this.session = session;
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

/**
 * Returns the current size of the component, insets included.
 * TODO AWE: (layout) prüfen ob hier tatsächlich die insets included sind. Müssten wir dann nicht outerWidth/-Height verwenden?
 */
scout.HtmlComponent.getSize = function($comp) {
  return new scout.Dimension(
      $comp.outerWidth(true),
      $comp.outerHeight(true));
};

scout.HtmlComponent.setSize = function($comp, vararg, height) {
  var size = vararg instanceof scout.Dimension ?
      vararg : new scout.Dimension(vararg, height);
  $comp.
    css('width', size.width + 'px').
    css('height', size.height+ 'px');
};

/**
 * Returns the size of a visible component or (0,0) when component is invisible.
 */
scout.HtmlComponent.getVisibleSize = function($comp) {
  if ($comp.length === 1 && $comp.isVisible()) {
    return scout.HtmlComponent.getSize($comp);
  } else {
    return new scout.Dimension(0, 0);
  }
};

// TODO AWE: (unit-test) getBounds + auto
scout.HtmlComponent.getBounds = function($comp) {
  var parseCssPosition = function(prop) {
    var value = $comp.css(prop);
    return 'auto' === value ? 0 :  parseInt(value, 10);
  };
  return new scout.Rectangle(
      parseCssPosition('left'),
      parseCssPosition('top'),
      $comp.outerWidth(true),
      $comp.outerHeight(true));
};

scout.HtmlComponent.setBounds = function($comp, vararg, y, width, height) {
  var bounds = vararg instanceof scout.Rectangle ?
      vararg : new scout.Rectangle(vararg, y, width, height);
  $comp.
    cssLeft(bounds.x).
    cssTop(bounds.y).
    cssWidth(bounds.width).
    cssHeight(bounds.height);
};

scout.HtmlComponent.debug = function($comp) {
  var attrs = '';
  if ($comp.attr('id')) {
    attrs += 'id=' + $comp.attr('id');
  }
  if ($comp.attr('class')) {
    attrs += ' class=' + $comp.attr('class');
  }
  if (attrs.length === 0) {
    attrs = $comp.html().substring(0, 30) + '...';
  }
  return 'HtmlComponent[' + attrs.trim() + ']';
};


/**
 * Returns the parent or $comp. Creates a new instance of HtmlComponent if the parent DOM element has no linked instance.
 */
scout.HtmlComponent.prototype.getParent = function() {
  var $parent = this.$comp.parent(),
    htmlParent = scout.HtmlComponent.optGet($parent);

  if ($parent.length === 0) {
    return null;
  }
  return htmlParent;
};

scout.HtmlComponent.prototype.layout = function() {
  if (!this.layoutManager) {
    $.log.warn('(HtmlComponent#layout) Called layout() but component ' + this.debug() + ' has no layout manager');
    // throw 'Tried to layout component ' + this.debug() +' but component has no layout manager';
    // TODO AWE: (layout) entscheiden, ob wir dieses throw "scharf machen" wollen oder nicht
    return;
  }

  if (!this.valid) {
    this.layoutManager.layout(this.$comp);
    //Save size for later use (necessary if pixelBasedSizing is set to false)
    this.size = this.getSize();
    this.valid = true;
  }
};

scout.HtmlComponent.prototype.invalidate = function() {
  this.valid = false;
  if (this.layoutManager) {
    this.layoutManager.invalidate();
  }
};

scout.HtmlComponent.prototype.revalidate = function() {
  this.session.layoutValidator.revalidate(this);
};

/**
 * Marks the end of the parent invalidation. <p>
 * A component is a validate root if its size does not depend on the visibility or bounds of its children.<p>
 * Example: It is not necessary to relayout the whole form if just the label of a form field gets invisible.
 * Only the form field container needs to be relayouted. In this case the form field container is the validate root.
 */
scout.HtmlComponent.prototype.isValidateRoot = function() {
  if (this.validateRoot) {
    return true;
  }

  if (!this.layoutData || !this.layoutData.isValidateRoot) {
    return false;
  }

  return this.layoutData.isValidateRoot();
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
  return scout.HtmlComponent.getSize(this.$comp);
};

/**
 * Sets the size of the component, insets included. Which means: the method subtracts the components insets
 * from the given size before setting the width/height of the component.
 */
scout.HtmlComponent.prototype.setSize = function(size) {
  var oldSize = this.size;
  if (!size.equals(oldSize) && this.layoutManager.invalidateOnResize) {
    this.invalidate();
  }
  if (this.pixelBasedSizing) {
    scout.HtmlComponent.setSize(this.$comp, size);
  }
  this.layout();
};

scout.HtmlComponent.prototype.getBounds = function() {
  return scout.HtmlComponent.getBounds(this.$comp);
};

scout.HtmlComponent.prototype.setBounds = function(bounds) {
  var oldBounds = this.getBounds();
  if (!oldBounds.equals(bounds) && this.layoutManager.invalidateOnResize) {
    this.invalidate();
  }
  scout.HtmlComponent.setBounds(this.$comp, bounds);
  this.layout();
};

scout.HtmlComponent.prototype.debug = function() {
  return scout.HtmlComponent.debug(this.$comp);
};
