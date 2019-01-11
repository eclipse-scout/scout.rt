/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
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
   * Flag to indicate that the component is being layouted.
   */
  this.layouting = false;

  /**
   * May be set to temporarily disable invalidation (e.g. if the component gets modified during the layouting process)
   */
  this.suppressInvalidate = false;

  /**
   * Set pixelBasedSizing to false if your component automatically adjusts its size,
   * e.g. by using CSS styling -> setSize won't be called.
   */
  this.pixelBasedSizing = true;
  this.sizeCached = null;

  /**
   * Object which stores the computed preferred size. Key is a string containing the width and height hints.
   * @see #computePrefSizeKey(options);
   */
  this.prefSizeCached = {};
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
 * If it is not scrollable, the actual height is returned.
 * <p>
 * The returned size contains insets (padding and border) but no margin. The width is always the actual width because there are no horizontal scrollbars.
 *
 * OPTION    DEFAULT VALUE   DESCRIPTION
 * -------------------------------------
 * exact     false           When set to true the returned dimensions may contain fractional digits, otherwise the sizes are rounded up.
 *
 * @param (options) may contain the options of the above table
 */
scout.HtmlComponent.prototype.availableSize = function(options) {
  options = options || {};
  var size = this.size({
    exact: options.exact
  });

  if (this.scrollable) {
    var prefSize = this.prefSize({
      widthHint: size.width,
      removeMarginFromHints: false // Since the width of this component is used as hint, the margin must not be removed
    });
    if (prefSize.height > size.height) {
      size.height = prefSize.height;
    }
  }

  return size;
};

/**
 * Invalidates the component (sets the valid property to false and calls layout.invalidate()).
 * @param {scout.HtmlComponent} htmlSource The component the invalidation originated from.
 *        Is always set if the invalidation is triggered by using invalidateLayoutTree, may be undefined otherwise.
 */
scout.HtmlComponent.prototype.invalidateLayout = function(htmlSource) {
  this.valid = false;
  this.prefSizeCached = {};
  if (this.layout) {
    this.layout.invalidate(htmlSource);
  }
};

/**
 * Calls the layout of the component to layout its children but only if the component is not valid.
 * @exception when component has no layout
 * @return true if validation was successful, false if it could not be executed (e.g. because the element is invisible or detached)
 */
scout.HtmlComponent.prototype.validateLayout = function() {
  if (!this.layout) {
    throw new Error('Called layout() but component has no layout');
  }
  // Don't layout components which don't exist anymore, are invisible or are detached from the DOM
  if (!this.isAttachedAndVisible()) {
    return false;
  }
  var parent = this.getParent();
  // Check the visibility of the parents as well.
  // This check loops to the top of the DOM tree.
  // To improve performance, the check (isEveryParentVisible) is not executed if the parent already did it, which is the case if parent is being layouted.
  if ((!parent || !parent.layouting) && !this.$comp.isEveryParentVisible()) {
    return false;
  }
  if (!this.valid) {
    this.layouting = true;
    this.layout.layout(this.$comp);
    this.layouting = false;
    this.layouted = true;
    // Save size for later use (necessary if pixelBasedSizing is set to false)
    this.sizeCached = this.size();
    this.valid = true;
  }
  return true;
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
scout.HtmlComponent.prototype.revalidateLayoutTree = function(invalidateParents) {
  if (this.suppressInvalidate) {
    return;
  }
  this.invalidateLayoutTree(invalidateParents);
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
 * Returns the preferred size of the component, insets included, margin excluded<p>
 * The preferred size is cached until the component will be invalidated.
 * Hence, subsequent calls to this function will return the cached preferred size unless the component is invalidated.
 * <p>
 *
 * OPTION                  DEFAULT VALUE   DESCRIPTION
 * --------------------------------------------------
 * includeMargin           false           Whether to include the margin in the returned size.
 * widthHint               null            When set, horizontal padding, border and margin are removed from it so that the actual layout does not need to take care of it.
 * heightHint              null            When set, vertical padding, border and margin are removed from it so that the actual layout does not need to take care of it.
 * removeMarginFromHints   true            Whether or not to automatically remove the margin from the hints.
 *
 * @param (options) an optional options object. Short-hand version: If a boolean is passed instead of an object, the value is automatically converted to the option "includeMargin".
 *                  May contain the options of the above table. All other options are passed as they are to the layout when @{link layout.preferredLayoutSize()} is called.
 *                  Possible options may be found at @{link scout.graphics.prefSize()}, but it depends on the actual layout if these options have an effect or not.
 * @exception When component has no layout
 */
scout.HtmlComponent.prototype.prefSize = function(options) {
  if (!this.isVisible()) {
    return new scout.Dimension(0, 0);
  }

  if (typeof options === 'boolean') {
    options = {
      includeMargin: options
    };
  } else {
    // Create a copy to not modify the original options
    options = $.extend({}, options);
  }
  var includeMargin = scout.nvl(options.includeMargin, false);
  options.includeMargin = null;
  if (!this.layout) {
    throw new Error('Called prefSize() but component has no layout');
  }

  var prefSizeCacheKey = this.computePrefSizeKey(options);
  var prefSizeCached = this.prefSizeCached[prefSizeCacheKey];
  if (!$.isEmptyObject(prefSizeCached)) {
    $.log.isTraceEnabled() && $.log.trace('(HtmlComponent#prefSize) ' + this.debug() + ' widthHint=' + options.widthHint + ' heightHint=' + options.heightHint + ' prefSizeCached=' + prefSizeCached);
    if (includeMargin) {
      prefSizeCached = prefSizeCached.add(this.margins());
    }
    return prefSizeCached;
  }

  if (options.widthHint || options.heightHint) {
    this._adjustSizeHintsForPrefSize(options);
  }

  var prefSize = this.layout.preferredLayoutSize(this.$comp, options);
  this._adjustPrefSizeWithMinMaxSize(prefSize);
  this.prefSizeCached[prefSizeCacheKey] = prefSize;

  $.log.isTraceEnabled() && $.log.trace('(HtmlComponent#prefSize) ' + this.debug() + ' widthHint=' + options.widthHint + ' heightHint=' + options.heightHint + ' prefSize=' + prefSize);
  if (includeMargin) {
    prefSize = prefSize.add(this.margins());
  }
  return prefSize;
};

scout.HtmlComponent.prototype.computePrefSizeKey = function(options) {
  return 'wHint' + scout.nvl(options.widthHint, '-1') + 'hHint' + scout.nvl(options.heightHint, '-1');
};

scout.HtmlComponent.prototype._adjustSizeHintsForPrefSize = function(options) {
  // Remove padding, border and margin from the width and heightHint so that the actual layout does not need to take care of it
  var removeMargin = scout.nvl(options.removeMarginFromHints, true);
  options.removeMarginFromHints = null;
  if (options.widthHint) {
    options.widthHint -= this.insets(removeMargin).horizontal();
  }
  if (options.heightHint) {
    options.heightHint -= this.insets(removeMargin).vertical();
  }
};

scout.HtmlComponent.prototype._adjustPrefSizeWithMinMaxSize = function(prefSize) {
  // Component may define a min or max height/height -> adjust the pref size accordingly
  var minHeight = this.$comp.cssMinHeight();
  var maxHeight = this.$comp.cssMaxHeight();
  var minWidth = this.$comp.cssMinWidth();
  var maxWidth = this.$comp.cssMaxWidth();
  prefSize.height = Math.max(prefSize.height, minHeight);
  prefSize.height = Math.min(prefSize.height, maxHeight);
  prefSize.width = Math.max(prefSize.width, minWidth);
  prefSize.width = Math.min(prefSize.width, maxWidth);
};

/**
 * Returns the inset-dimensions of the component (padding and border, no margin).
 */
scout.HtmlComponent.prototype.insets = function(options) {
  return scout.graphics.insets(this.$comp, options);
};

scout.HtmlComponent.prototype.margins = function() {
  return scout.graphics.margins(this.$comp);
};

/**
 * Returns the size of the component, insets included.
 * @param options, see {@link scout.graphics#size} for details.
 */
scout.HtmlComponent.prototype.size = function(options) {
  return scout.graphics.size(this.$comp, options);
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
  var oldSize = this.sizeCached;
  if (!size.equals(oldSize)) {
    this.invalidateLayout();
  }
  if (this.pixelBasedSizing) {
    scout.graphics.setSize(this.$comp, size);
  }
  this.validateLayout();
};

scout.HtmlComponent.prototype.bounds = function(options) {
  return scout.graphics.bounds(this.$comp, options);
};

scout.HtmlComponent.prototype.position = function() {
  return scout.graphics.position(this.$comp);
};

scout.HtmlComponent.prototype.offsetBounds = function(options) {
  return scout.graphics.offsetBounds(this.$comp, options);
};

scout.HtmlComponent.prototype.offset = function() {
  return scout.graphics.offset(this.$comp);
};

/**
 * Delegation to scout.graphics.setLocation
 * @param location scout.Point
 */
scout.HtmlComponent.prototype.setLocation = function(location) {
  scout.graphics.setLocation(this.$comp, location);
};

scout.HtmlComponent.prototype.location = function() {
  return scout.graphics.location(this.$comp);
};

scout.HtmlComponent.prototype.setBounds = function(bounds) {
  if (!this.isAttachedAndVisible()) {
    // don't invalidate the layout if component is invisible because sizes may not be read correctly and therefore prefSize will be wrong
    return;
  }
  var oldBounds = this.offsetBounds();
  if (!oldBounds.dimension().equals(bounds.dimension())) {
    this.invalidateLayout();
  }
  scout.graphics.setBounds(this.$comp, bounds);
  this.validateLayout();
};

/**
 * Sets the component to its preferred size.
 */
scout.HtmlComponent.prototype.pack = function() {
  var preferredSize = this.prefSize();
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
