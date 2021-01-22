/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Dimension, graphics, Insets, NullLayout, scout} from '../index';
import $ from 'jquery';

/**
 * Wrapper for a JQuery selector. Used as replacement for javax.swing.JComponent.
 */
export default class HtmlComponent {
  constructor($comp, session) {
    if (!session) {
      throw new Error('session must be defined for ' + this.debug());
    }
    this.$comp = $comp;
    this.layout = new NullLayout();
    this.layoutData = null;
    this.valid = false;
    this.validateRoot = false;

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
     * May be set to temporarily disable layout validation (e.g. if the component gets modified during the layouting process).
     * It is still possible to invalidate its layout. But as long as this flag is set, it will not validate the layout.
     * It is the responsibility of the caller to ensure that the component is validated again (if necessary) when the layout validation suppression is removed.
     * @type {boolean}
     */
    this.suppressValidate = false;

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
    this.scrollable = false;
  }

  /**
   * Returns the parent or $comp or null when $comp has no parent.
   * Creates a new instance of HtmlComponent if the parent DOM element has no linked instance yet.
   */
  getParent() {
    let $parent = this.$comp.parent();
    if ($parent.length === 0) {
      return null;
    }
    return HtmlComponent.optGet($parent);
  }

  /**
   * @returns {boolean} true if the given htmlComponent is an ancestor, false if not
   */
  isDescendantOf(htmlComp) {
    let $parent = this.$comp.parent();
    while ($parent.length > 0) {
      if (HtmlComponent.optGet($parent) === htmlComp) {
        return true;
      }
      $parent = $parent.parent();
    }
    return false;
  }

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
  availableSize(options) {
    options = options || {};
    let size = this.size({
      exact: options.exact
    });

    if (this.scrollable) {
      let prefSize = this.prefSize({
        widthHint: size.width,
        removeMarginFromHints: false // Since the width of this component is used as hint, the margin must not be removed
      });
      if (prefSize.height > size.height) {
        size.height = prefSize.height;
      }
    }

    return size;
  }

  /**
   * Invalidates the component (sets the valid property to false and calls layout.invalidate()).
   * @param {HtmlComponent} [htmlSource] The component the invalidation originated from.
   *        Is always set if the invalidation is triggered by using invalidateLayoutTree, may be undefined otherwise.
   */
  invalidateLayout(htmlSource) {
    this.valid = false;
    this.prefSizeCached = {};
    if (this.layout) {
      this.layout.invalidate(htmlSource);
    }
  }

  /**
   * Calls the layout of the component to layout its children but only if the component is not valid.
   * @exception when component has no layout
   * @return {boolean} true if validation was successful, false if it could not be executed (e.g. because the element is invisible or detached)
   */
  validateLayout() {
    if (!this.layout) {
      throw new Error('Called layout() but component has no layout');
    }
    if (this.valid) {
      return true;
    }
    if (this.suppressValidate) {
      return false;
    }
    if (this.layouting) {
      return false;
    }
    if (!this._checkValidationPossible()) {
      return false;
    }

    this.layouting = true;
    this.layout.layout(this.$comp);
    this.layouting = false;
    this.layouted = true;
    // Save size for later use (necessary if pixelBasedSizing is set to false)
    this.sizeCached = this.size({exact: true});
    this.valid = true;
    return true;
  }

  _checkValidationPossible() {
    // Don't layout components which don't exist anymore, are invisible or are detached from the DOM
    if (!this.isAttachedAndVisible()) {
      return false;
    }

    // Don't layout if component is currently animated
    if (this.$comp.hasAnimationClass()) {
      this._validateLayoutAfterAnimation(this.$comp);
      return false;
    }

    // Check the visibility of the parents as well.
    // Also check if one of the parents is currently being animated.
    // To improve performance (the check might loop to the top of the DOM tree), the following code is
    // not executed if the parent already executed it, which is the case if the parent is being layouted.
    let parent = this.getParent();
    if (!parent || !parent.layouting) {
      let everyParentVisible = true;
      let $animatedParent = null;
      this.$comp.parents().each(function() {
        let $parent = $(this);
        if (!$parent.isVisible()) {
          everyParentVisible = false;
          return false;
        }
        if ($parent.hasAnimationClass()) {
          $animatedParent = $parent;
          return false;
        }
        return true; // continue loop
      });
      if (!everyParentVisible) {
        return false;
      }
      if ($animatedParent) {
        // Postpone the layout if there is a CSS animation in progress on one of the parent containers.
        // Otherwise, wrong sizes might be measured (depending on the CSS animation, e.g. grow/shrink).
        this._validateLayoutAfterAnimation($animatedParent);
        return false;
      }
    }
    return true;
  }

  _validateLayoutAfterAnimation($animatedElement) {
    $animatedElement.oneAnimationEnd(this.validateLayout.bind(this));
  }

  /**
   * Performs invalidateLayout() and validateLayout() subsequently.
   */
  revalidateLayout() {
    this.invalidateLayout();
    this.validateLayout();
  }

  /**
   * Invalidates the component-tree up to the next validate root, but only if invalidateParents is set to true.
   */
  invalidateLayoutTree(invalidateParents) {
    if (this.suppressInvalidate) {
      return;
    }
    if (scout.nvl(invalidateParents, true)) {
      this.session.layoutValidator.invalidateTree(this); // will call invalidateLayout(), which sets this.valid = false
    } else {
      this.invalidateLayout();
      this.session.layoutValidator.invalidate(this);
    }
  }

  /**
   * Layouts all invalid components
   */
  validateLayoutTree() {
    if (this.suppressValidate) {
      return;
    }
    this.session.layoutValidator.validate();
  }

  /**
   * Performs invalidateLayoutTree() and validateLayoutTree() subsequently.
   */
  revalidateLayoutTree(invalidateParents) {
    if (this.suppressInvalidate) {
      return;
    }
    this.invalidateLayoutTree(invalidateParents);
    this.validateLayoutTree();
  }

  /**
   * Marks the end of the parent invalidation. <p>
   * A component is a validate root if its size does not depend on the visibility or bounds of its children.<p>
   * Example: It is not necessary to relayout the whole form if just the label of a form field gets invisible.
   * Only the form field container needs to be relayouted. In this case the form field container is the validate root.
   */
  isValidateRoot() {
    if (this.validateRoot) {
      return true;
    }
    if (!this.layoutData || !this.layoutData.isValidateRoot) {
      return false;
    }
    return this.layoutData.isValidateRoot();
  }

  /**
   * Sets the given layout.
   */
  setLayout(layout) {
    this.layout = layout;
    if (layout.cssClass) {
      this.$comp.addClass(layout.cssClass);
    }
  }

  /**
   * Returns the preferred size of the component, insets included, margin excluded.<p>
   * The preferred size is cached until the component will be invalidated.
   * Hence, subsequent calls to this function will return the cached preferred size unless the component is invalidated.
   * <p>
   *
   * @param {object|boolean} [options] an optional options object. Short-hand version: If a boolean is passed instead of an object, the value is automatically converted to the option "includeMargin".
   *                  May contain the options described below. All other options are passed as they are to the layout when layout.preferredLayoutSize() is called.
   *                  Possible options may be found at graphics.prefSize(), but it depends on the actual layout if these options have an effect or not.
   * @param {boolean|null} [options.includeMargin] Whether to include the margin in the returned size. Default is false.
   * @param {number} [options.widthHint] When set, horizontal padding, border and margin are removed from it so that the actual layout does not need to take care of it. Default is null.
   * @param {number} [options.heightHint] When set, vertical padding, border and margin are removed from it so that the actual layout does not need to take care of it. Default is null.
   * @param {boolean|null} [options.removeMarginFromHints] Whether or not to automatically remove the margin from the hints. Default is true.
   * @param {boolean|null} [options.removeInsetsFromHints] Whether or not to automatically remove the insets (padding and border) from the hints. Default is true.
   * @exception When component has no layout
   */
  prefSize(options) {
    if (!this.isVisible()) {
      return new Dimension(0, 0);
    }

    if (typeof options === 'boolean') {
      options = {
        includeMargin: options
      };
    } else {
      // Create a copy to not modify the original options
      options = $.extend({}, options);
    }
    let includeMargin = scout.nvl(options.includeMargin, false);
    options.includeMargin = null;
    if (!this.layout) {
      throw new Error('Called prefSize() but component has no layout');
    }

    let prefSizeCacheKey = this.computePrefSizeKey(options);
    let prefSizeCached = this.prefSizeCached[prefSizeCacheKey];
    if (!$.isEmptyObject(prefSizeCached)) {
      $.log.isTraceEnabled() && $.log.trace('(HtmlComponent#prefSize) ' + this.debug() + ' widthHint=' + options.widthHint + ' heightHint=' + options.heightHint + ' prefSizeCached=' + prefSizeCached);
      if (includeMargin) {
        prefSizeCached = prefSizeCached.add(this.margins());
      }
      return prefSizeCached.clone();
    }

    let minSize = this.cssMinSize();
    let maxSize = this.cssMaxSize();
    if (options.widthHint || options.heightHint) {
      this._adjustSizeHintsForPrefSize(options, minSize, maxSize);
    }

    let prefSize = this.layout.preferredLayoutSize(this.$comp, options);
    this._adjustPrefSizeWithMinMaxSize(prefSize, minSize, maxSize);
    this.prefSizeCached[prefSizeCacheKey] = prefSize;

    $.log.isTraceEnabled() && $.log.trace('(HtmlComponent#prefSize) ' + this.debug() + ' widthHint=' + options.widthHint + ' heightHint=' + options.heightHint + ' prefSize=' + prefSize);
    if (includeMargin) {
      prefSize = prefSize.add(this.margins());
    }
    return prefSize.clone();
  }

  computePrefSizeKey(options) {
    return 'wHint' + scout.nvl(options.widthHint, '-1') + 'hHint' + scout.nvl(options.heightHint, '-1') + 'wOnly' + scout.nvl(options.widthOnly, '-1');
  }

  /**
   * Remove padding, border and margin from the width and heightHint so that the actual layout does not need to take care of it.
   * Also makes sure the hints consider the min and max size set by CSS.
   */
  _adjustSizeHintsForPrefSize(options, minSize, maxSize) {
    let removeMargins = scout.nvl(options.removeMarginFromHints, true);
    let removeInsets = scout.nvl(options.removeInsetsFromHints, true);
    options.removeMarginFromHints = null;
    options.removeInsetsFromHints = null;
    if (!options.widthHint && !options.heightHint) {
      return;
    }
    let margins = removeMargins ? this.margins() : new Insets();
    let insets = removeInsets ? this.insets() : new Insets();
    if (options.widthHint) {
      // The order is important! Box-sizing: border-box is expected.
      options.widthHint -= margins.horizontal();
      options.widthHint = Math.max(options.widthHint, minSize.width);
      options.widthHint = Math.min(options.widthHint, maxSize.width);
      options.widthHint -= insets.horizontal();
    }
    if (options.heightHint) {
      // The order is important! Box-sizing: border-box is expected.
      options.heightHint -= margins.vertical();
      options.heightHint = Math.max(options.heightHint, minSize.height);
      options.heightHint = Math.min(options.heightHint, maxSize.height);
      options.heightHint -= insets.vertical();
    }
  }

  /**
   * The html element may define a min or max height/height -> adjust the pref size accordingly
   */
  _adjustPrefSizeWithMinMaxSize(prefSize, minSize, maxSize) {
    minSize = minSize || this.cssMinSize();
    maxSize = maxSize || this.cssMaxSize();
    prefSize.height = Math.max(prefSize.height, minSize.height);
    prefSize.height = Math.min(prefSize.height, maxSize.height);
    prefSize.width = Math.max(prefSize.width, minSize.width);
    prefSize.width = Math.min(prefSize.width, maxSize.width);
  }

  /**
   * Returns the inset-dimensions of the component (padding and border, no margin).
   */
  insets(options) {
    return graphics.insets(this.$comp, options);
  }

  margins() {
    return graphics.margins(this.$comp);
  }

  borders() {
    return graphics.borders(this.$comp);
  }

  cssMinSize() {
    return graphics.cssMinSize(this.$comp);
  }

  cssMaxSize() {
    return graphics.cssMaxSize(this.$comp);
  }

  /**
   * Returns the size of the component, insets included.
   * @param options, see {@link graphics#size} for details.
   */
  size(options) {
    return graphics.size(this.$comp, options);
  }

  /**
   * Sets the size of the component, insets included. Which means: the method subtracts the components insets
   * from the given size before setting the width/height of the component.
   *
   * @param {Dimension} size
   */
  setSize(size) {
    if (!this.isAttachedAndVisible()) {
      // don't invalidate the layout if component is invisible because sizes may not be read correctly and therefore prefSize will be wrong
      return;
    }
    let oldSize = this.sizeCached;
    if (!size.equals(oldSize)) {
      this.invalidateLayout();
    }
    if (this.pixelBasedSizing) {
      graphics.setSize(this.$comp, size);
    }
    this.validateLayout();
  }

  bounds(options) {
    return graphics.bounds(this.$comp, options);
  }

  position() {
    return graphics.position(this.$comp);
  }

  offsetBounds(options) {
    return graphics.offsetBounds(this.$comp, options);
  }

  offset() {
    return graphics.offset(this.$comp);
  }

  /**
   * Delegation to graphics.setLocation
   * @param location Point
   */
  setLocation(location) {
    graphics.setLocation(this.$comp, location);
  }

  location() {
    return graphics.location(this.$comp);
  }

  setBounds(bounds) {
    if (!this.isAttachedAndVisible()) {
      // don't invalidate the layout if component is invisible because sizes may not be read correctly and therefore prefSize will be wrong
      return;
    }
    let oldSize = this.sizeCached;
    if (!bounds.dimension().equals(oldSize)) {
      this.invalidateLayout();
    }
    if (this.pixelBasedSizing) {
      graphics.setBounds(this.$comp, bounds);
    }
    this.validateLayout();
  }

  /**
   * Sets the component to its preferred size.
   */
  pack() {
    let preferredSize = this.prefSize();
    this.setSize(preferredSize);
  }

  /**
   * Checks whether $comp is in the DOM or has been removed or detached.<br>
   * Also returns false if the $comp does not belong to a window (defaultView) anymore. This may happen if it belonged to a popup window which is now closed
   */
  isAttached() {
    return this.$comp.isAttached() && this.$comp.window(true);
  }

  isVisible() {
    return this.$comp.isVisible();
  }

  isAttachedAndVisible() {
    return this.isAttached() && this.isVisible();
  }

  debug() {
    return graphics.debugOutput(this.$comp);
  }

  /* --- STATIC HELPERS ------------------------------------------------------------- */

  /**
   * Creates a new HtmlComponent and links it to the given $comp element, so it can be
   * retrieved again with HtmlComponent.get($comp).
   *
   * @memberOf HtmlComponent
   */
  static install($comp, session) {
    if (!$comp) {
      throw new Error('Missing argument "$comp"');
    }
    if (!session) {
      throw new Error('Missing argument "session"');
    }

    let htmlComp = new HtmlComponent($comp, session);
    // link DOM element with the new instance
    $comp.data('htmlComponent', htmlComp);

    return htmlComp;
  }

  /**
   * Static method to get the HtmlComponent associated with the given DOM $comp.
   * Throws an error when data 'htmlComponent' is not set.
   *
   * @memberOf HtmlComponent
   */
  static get($comp) {
    let htmlComp = this.optGet($comp);
    if (!htmlComp) {
      let details = '';
      if ($comp) {
        details = '\nClass: ' + $comp.attr('class');
        details += '\nId: ' + $comp.attr('id');
        details += '\nAttached: ' + $comp.isAttached();
      }
      throw new Error('data "htmlComponent" is undefined.' + details);
    }
    return htmlComp;
  }

  /**
   * @memberOf HtmlComponent
   */
  static optGet($comp) {
    return $comp && $comp.data('htmlComponent');
  }
}
