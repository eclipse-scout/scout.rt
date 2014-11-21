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

/**
 * Returns the parent or $comp or null when $comp has no parent.
 * Creates a new instance of HtmlComponent if the parent DOM element has no linked instance yet.
 */
scout.HtmlComponent.prototype.getParent = function() {
  var $parent = this.$comp.parent();
  if ($parent.length === 0) {
    return null;
  } else {
    return scout.HtmlComponent.optGet($parent);
  }
};

/**
 * Calls the layout manager of the component to layout its children.
 * @exception when component has no layout manager
 */
scout.HtmlComponent.prototype.layout = function() {
  if (!this.layoutManager) {
    throw new Error('Called layout() but component has no layout manager');
  }
  if (!this.valid) {
    this.layoutManager.layout(this.$comp);
    // Save size for later use (necessary if pixelBasedSizing is set to false)
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
 * @exception When component has no layout manager
 */
scout.HtmlComponent.prototype.getPreferredSize = function() {
  if (this.layoutManager) {
    var prefSize = this.layoutManager.preferredLayoutSize(this.$comp);
    $.log.trace('(HtmlComponent#getPreferredSize) ' + this.debug() + ' preferredSize=' + prefSize);
    return prefSize;
  } else {
    throw new Error('Called getPreferredSize() but component has no layout manager');
  }
};

/**
 * Returns the inset-dimensions of the component (padding and border, no margin).
 */
scout.HtmlComponent.prototype.getInsets = function(options) {
  return scout.graphics.getInsets(this.$comp, options);
};

scout.HtmlComponent.prototype.getMargins = function() {
  return scout.graphics.getMargins(this.$comp);
};

/**
 * Returns the size of the component, insets included.
 * @param includeMargins when set to true, returned dimensions include margins of component
 */
scout.HtmlComponent.prototype.getSize = function(includeMargins) {
  return scout.graphics.getSize(this.$comp, includeMargins);
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
    scout.graphics.setSize(this.$comp, size);
  }
  this.layout();
};

scout.HtmlComponent.prototype.getBounds = function() {
  return scout.graphics.getBounds(this.$comp);
};

scout.HtmlComponent.prototype.setBounds = function(bounds) {
  var oldBounds = this.getBounds();
  if (!oldBounds.equals(bounds) && this.layoutManager.invalidateOnResize) {
    this.invalidate();
  }
  scout.graphics.setBounds(this.$comp, bounds);
  this.layout();
};

/**
 * Sets the component to its preferred size.
 */
scout.HtmlComponent.prototype.pack = function() {
  var preferredSize = this.getPreferredSize();
  this.setSize(preferredSize);
};

scout.HtmlComponent.prototype.debug = function() {
  return scout.graphics.debugOutput(this.$comp);
};
