/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
/**
 * Wrapper for a JQuery selector. Used as replacement for javax.swing.JComponent.
 */
scout.HtmlComponent = function($comp, session) {
  if (!session) {
    throw new Error('session must be defined for ' + this.debug());
  }
  this.$comp = $comp;
  this.layout = new scout.NullLayout();
  this.layoutData;
  this.valid = false;

  /**
   * Flag to indicate that the component has been layouted at least once. Invalidation should NOT reset this flag.
   */
  this.layouted = false;

  /**
   * May be set to temporarily disable invalidation (e.g. if the component gets modified during the layouting process)
   */
  this.suppressInvalidate = false;

  /**
   * Set pixelBasedSizing to false if your component automatically adjusts its size,
   * e.g. by using CSS styling -> setSize won't be called.
   */
  this.pixelBasedSizing = true;
  this.session = session;
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
 * @returns true if the given htmlComponent is an ancestor, false if not
 */
scout.HtmlComponent.prototype.isDescendantOf = function(htmlComp) {
  var $parent = this.$comp.parent();
  while ($parent.length > 0) {
    if (scout.HtmlComponent.optGet($parent) === htmlComp) {
      return true;
    }
    $parent = $parent.parent();
  }
  return false;
};

/**
 * Computes the preferred height if the component is scrollable and returns it if it is greater than the actual size.
 * If it is not scrollable, the actual height is returned.<p>
 * The returned width is always the actual width because there are no horizontal scrollbars.
 */
scout.HtmlComponent.prototype.getAvailableSize = function() {
  var size = this.getSize(),
    prefSize;

  if (this.scrollable) {
    prefSize = this.getPreferredSize();
    if (prefSize.height > size.height) {
      size.height = prefSize.height;
    }
  }

  return size;
};

/**
 * Invalidates the component (sets the valid property to false).
 */
scout.HtmlComponent.prototype.invalidateLayout = function() {
  this.valid = false;
  if (this.layout) {
    this.layout.invalidate();
  }
};

/**
 * Calls the layout of the component to layout its children but only if the component is not valid.
 * @exception when component has no layout
 */
scout.HtmlComponent.prototype.validateLayout = function() {
  if (!this.layout) {
    throw new Error('Called layout() but component has no layout');
  }
  if (!this.valid) {
    this.layouting = true;
    this.layout.layout(this.$comp);
    this.layouting = false;
    this.layouted = true;
    // Save size for later use (necessary if pixelBasedSizing is set to false)
    this.size = this.getSize();
    this.valid = true;
  }
};

/**
 * Performs invalidateLayout() and validateLayout() subsequently.
 */
scout.HtmlComponent.prototype.revalidateLayout = function() {
  this.invalidateLayout();
  this.validateLayout();
};

/**
 * Invalidates the component-tree up to the next validate root, but only if invalidateParents is set to true.
 */
scout.HtmlComponent.prototype.invalidateLayoutTree = function(invalidateParents) {
  if (this.suppressInvalidate) {
    return;
  }
  if (scout.nvl(invalidateParents, true)) {
    this.session.layoutValidator.invalidateTree(this); // will call invalidateLayout(), which sets this.valid = false
  } else {
    this.valid = false;
    this.session.layoutValidator.invalidate(this);
  }
};

/**
 * Layouts all invalid components
 */
scout.HtmlComponent.prototype.validateLayoutTree = function() {
  this.session.layoutValidator.validate();
};

/**
 * Performs invalidateLayoutTree() and validateLayoutTree() subsequently.
 */
scout.HtmlComponent.prototype.revalidateLayoutTree = function() {
  this.invalidateLayoutTree();
  this.validateLayoutTree();
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
 * Sets the given layout.
 */
scout.HtmlComponent.prototype.setLayout = function(layout) {
  this.layout = layout;
  if (layout.cssClass) {
    this.$comp.addClass(layout.cssClass);
  }
};

/**
 * Returns the preferred size of the component, insets included.
 * @exception When component has no layout
 */
scout.HtmlComponent.prototype.getPreferredSize = function(options) {
  if (!this.layout) {
    throw new Error('Called getPreferredSize() but component has no layout');
  }
  options = options || {};
  var prefSize = this.layout.preferredLayoutSize(this.$comp, options);
  $.log.trace('(HtmlComponent#getPreferredSize) ' + this.debug() + ' widthHint=' + options.widthHint + ' heightHint=' + options.heightHint + ' preferredSize=' + prefSize);
  return prefSize;
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
  if (!this.isAttachedAndVisible()) {
    // don't invalidate the layout if component is invisible because sizes may not be read correctly and therefore prefSize will be wrong
    return;
  }
  var oldSize = this.size;
  if (!size.equals(oldSize)) {
    this.invalidateLayout();
  }
  if (this.pixelBasedSizing) {
    scout.graphics.setSize(this.$comp, size);
  }
  this.validateLayout();
};

scout.HtmlComponent.prototype.getBounds = function() {
  return scout.graphics.getBounds(this.$comp);
};

scout.HtmlComponent.prototype.offsetBounds = function() {
  return scout.graphics.offsetBounds(this.$comp);
};

/**
 * Delegation to scout.graphics.setLocation
 * @param location scout.Point
 */
scout.HtmlComponent.prototype.setLocation = function(location) {
  scout.graphics.setLocation(this.$comp, location);
};

scout.HtmlComponent.prototype.setBounds = function(bounds) {
  if (!this.isAttachedAndVisible()) {
    // don't invalidate the layout if component is invisible because sizes may not be read correctly and therefore prefSize will be wrong
    return;
  }
  var oldBounds = this.getBounds();
  if (!oldBounds.equals(bounds)) {
    this.invalidateLayout();
  }
  scout.graphics.setBounds(this.$comp, bounds);
  this.validateLayout();
};

/**
 * Sets the component to its preferred size.
 */
scout.HtmlComponent.prototype.pack = function() {
  var preferredSize = this.getPreferredSize();
  this.setSize(preferredSize);
};

/**
 * Checks whether $comp is in the DOM or has been removed or detached.<br>
 * Also returns false if the $comp does not belong to a window (defaultView) anymore. This may happen if it belonged to a popup window which is now closed
 */
scout.HtmlComponent.prototype.isAttached = function() {
  return this.$comp.isAttached() && this.$comp.window(true);
};

scout.HtmlComponent.prototype.isVisible = function() {
  return this.$comp.isVisible();
};

scout.HtmlComponent.prototype.isAttachedAndVisible = function() {
  return this.isAttached() && this.isVisible();
};

scout.HtmlComponent.prototype.debug = function() {
  return scout.graphics.debugOutput(this.$comp);
};

/* --- STATIC HELPERS ------------------------------------------------------------- */

/**
 * Creates a new HtmlComponent and links it to the given $comp element, so it can be
 * retrieved again with scout.HtmlComponent.get($comp).
 *
 * @memberOf scout.HtmlComponent
 */
scout.HtmlComponent.install = function($comp, session) {
  if (!$comp) {
    throw new Error('Missing argument "$comp"');
  }
  if (!session) {
    throw new Error('Missing argument "session"');
  }

  var htmlComp = new scout.HtmlComponent($comp, session);
  // link DOM element with the new instance
  $comp.data('htmlComponent', htmlComp);

  return htmlComp;
};

/**
 * Static method to get the HtmlComponent associated with the given DOM $comp.
 * Throws an error when data 'htmlComponent' is not set.
 *
 * @memberOf scout.HtmlComponent
 */
scout.HtmlComponent.get = function($comp) {
  var htmlComp = this.optGet($comp);
  if (!htmlComp) {
    var details = '';
    if ($comp) {
      details = '\nClass: ' + $comp.attr('class');
      details += '\nId: ' + $comp.attr('id');
      details += '\nAttached: ' + $comp.isAttached();
    }
    throw new Error('data "htmlComponent" is undefined.' + details);
  }
  return htmlComp;
};

/**
 * @memberOf scout.HtmlComponent
 */
scout.HtmlComponent.optGet = function($comp) {
  return $comp && $comp.data('htmlComponent');
};
