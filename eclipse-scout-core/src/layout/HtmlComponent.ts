/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractLayout, BoundsOptions, Dimension, graphics, Insets, InsetsOptions, LayoutData, NullLayout, Point, PrefSizeOptions, Rectangle, scout, Session, SizeOptions} from '../index';
import $ from 'jquery';

export interface HtmlCompPrefSizeOptions extends PrefSizeOptions {
  /**
   * Whether to include the margin in the returned size. Default is false.
   */
  includeMargin?: boolean;

  /**
   *  When set, horizontal padding, border and margin are removed from it so that the actual layout does not need to take care of it. Default is null.
   */
  widthHint?: number;

  /**
   * When set, vertical padding, border and margin are removed from it so that the actual layout does not need to take care of it. Default is null.
   */
  heightHint?: number;

  /**
   * Whether or not to automatically remove the margin from the hints. Default is true.
   */
  removeMarginFromHints?: boolean;

  /**
   * Whether or not to automatically remove the insets (padding and border) from the hints. Default is true.
   */
  removeInsetsFromHints?: boolean;

  /**
   * If true, only the preferred width should be calculated. Whether this has an effect depends on the used layout. Default is false.
   */
  widthOnly?: boolean;
}

/**
 * A HTMLComponent wraps a {@link JQuery} object and provides a possibility to layout its children using {@link AbstractLayout}.
 * A layout is necessary if it is not sufficient to use CSS for layouting, e.g. for elements that need to be positioned and sized dynamically and exactly.
 * <p>
 * Every component is responsible to layout its child components using the specified {@link layout}. The layout calculates and updates the sizes and/or positions for the children using {@link setSize} or {@link setBounds}.
 * If the size or bounds change, the child components get invalid and need to layout their children as well. So the layouting happens top-down.
 * <p>
 * If a child component is modified, e.g. changes its visibility or another attribute that impacts the size or position, it needs to be invalidated. Since this not only affects the parent component but possibly every ancestor,
 * the whole ancestor component tree needs to be invalidated. This is done by using {@link invalidateLayoutTree}. So the invalidation happens bottom-up.
 */
export class HtmlComponent {
  $comp: JQuery;
  layout: AbstractLayout;
  layoutData: LayoutData;
  /**
   * Flag to indicate that the component has been layouted at least once. Invalidation should NOT reset this flag.
   */
  layouted: boolean;
  /**
   * Flag to indicate that the component is being layouted.
   */
  layouting: boolean;
  /**
   * Set pixelBasedSizing to false if your component automatically adjusts its size,
   * e.g. by using CSS styling -> setSize won't be called.
   */
  pixelBasedSizing: boolean;
  /**
   * Object which stores the computed preferred size. Key is a string containing the width and height hints.
   * @see #computePrefSizeKey(options);
   */
  prefSizeCached: Record<string, Dimension>;
  scrollable: boolean;
  session: Session;
  sizeCached: Dimension;
  /**
   * May be set to temporarily disable invalidation (e.g. if the component gets modified during the layouting process)
   */
  suppressInvalidate: boolean;
  /**
   * May be set to temporarily disable layout validation (e.g. if the component gets modified during the layouting process).
   * It is still possible to invalidate its layout. But as long as this flag is set, it will not validate the layout.
   * It is the responsibility of the caller to ensure that the component is validated again (if necessary) when the layout validation suppression is removed.
   */
  suppressValidate: boolean;
  valid: boolean;
  validateRoot: boolean;

  constructor($comp: JQuery, session: Session) {
    if (!session) {
      throw new Error('session must be defined for ' + this.debug());
    }
    this.$comp = $comp;
    this.layout = new NullLayout();
    this.layoutData = null;
    this.valid = false;
    this.validateRoot = false;
    this.layouted = false;
    this.layouting = false;
    this.suppressInvalidate = false;
    this.suppressValidate = false;
    this.pixelBasedSizing = true;
    this.sizeCached = null;
    this.prefSizeCached = {};
    this.session = session;
    this.scrollable = false;
  }

  /**
   * Returns the {@link HtmlComponent} of the parent DOM element or null if there is no parent DOM element or the parent DOM element is not linked to a HtmlComponent.
   */
  getParent(): HtmlComponent {
    let $parent = this.$comp.parent();
    if ($parent.length === 0) {
      return null;
    }
    return HtmlComponent.optGet($parent);
  }

  /**
   * @returns true if the given htmlComponent is an ancestor, false if not.
   */
  isDescendantOf(htmlComp: HtmlComponent): boolean {
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
   * Computes the preferred height if the component is scrollable and returns it, if it is greater than the actual size.
   * If it is not scrollable, the actual height is returned.
   * <p>
   * The returned size contains insets (padding and border) but no margin. The width is always the actual width because there are no horizontal scrollbars.
   */
  availableSize(options?: SizeOptions): Dimension {
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
   * Invalidates the component (sets the valid property to false and calls {@link AbstractLayout.invalidate}.
   * @param htmlSource The component the invalidation originated from.
   *        Is always set if the invalidation is triggered by using {@link invalidateLayoutTree}, may be undefined otherwise.
   */
  invalidateLayout(htmlSource?: HtmlComponent) {
    this.valid = false;
    this.prefSizeCached = {};
    if (this.layout) {
      this.layout.invalidate(htmlSource);
    }
  }

  /**
   * Calls the layout of the component to lay out its children but only if the component is not valid.
   * @returns true if validation was successful, false if it could not be executed (e.g. because the element is invisible or detached)
   * @exception when component has no layout
   */
  validateLayout(): boolean {
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

  protected _checkValidationPossible(): boolean {
    // Don't valid layout of components which don't exist anymore, are invisible or are detached from the DOM
    if (!this.isAttachedAndVisible()) {
      return false;
    }

    // Postpone the layout if there is an animation in progress, because measured sizes may be wrong during the animation.
    if (this.$comp.hasAnimationClass()) {
      this._validateLayoutAfterAnimation(this.$comp);
      return false;
    }
    let animatePromise = this.$comp.data('animate-promise');
    if (animatePromise) {
      this._validateLayoutAfterPromise(animatePromise);
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
          return false; // end loop
        }
        if ($parent.hasAnimationClass()) {
          $animatedParent = $parent;
          return false; // end loop
        }
        animatePromise = $parent.data('animate-promise');
        if (animatePromise) {
          return false; // end loop
        }
        // continue loop
      });
      if (!everyParentVisible) {
        return false;
      }
      if ($animatedParent) {
        this._validateLayoutAfterAnimation($animatedParent);
        return false;
      }
      if (animatePromise) {
        this._validateLayoutAfterPromise(animatePromise);
        return false;
      }
    }
    return true;
  }

  protected _validateLayoutAfterAnimation($animatedElement: JQuery) {
    $animatedElement.oneAnimationEnd(this.validateLayout.bind(this));
  }

  protected _validateLayoutAfterPromise(promise: JQuery.Promise<any>) {
    // Note: the always() callback is executed synchronously after the animation is done, regardless of whether it has been completed or aborted
    promise.always(this.validateLayout.bind(this));
  }

  /**
   * Performs invalidateLayout() and validateLayout() subsequently.
   */
  revalidateLayout() {
    this.invalidateLayout();
    this.validateLayout();
  }

  /**
   * Uses the {@link LayoutValidator} to invalidate the html component tree up to the next validate root. If invalidateParents is set to false, only the current component is invalidated.
   * Default for invalidateParents is true.
   * <p>
   * Invalidating basically means setting {@link valid} to false so that the next validation cycle will layout the component again. See also {@link invalidate}.
   * <p>
   * The caller does not need to trigger a validation manually. The {@link LayoutValidator} will schedule the next validation automatically.
   */
  invalidateLayoutTree(invalidateParents?: boolean) {
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
   * Performs {@link invalidateLayoutTree} and {@link validateLayoutTree} subsequently.
   */
  revalidateLayoutTree(invalidateParents?: boolean) {
    if (this.suppressInvalidate) {
      return;
    }
    this.invalidateLayoutTree(invalidateParents);
    this.validateLayoutTree();
  }

  /**
   * Marks the end of the parent invalidation.
   * <p>
   * A component is a validate root if its size does not depend on the visibility or bounds of its children.
   * <p>
   * Example: It is not necessary to relayout the whole form if just the label of a form field gets invisible.
   * Only the form field container needs to be relayouted. In this case the form field container is the validate root.
   */
  isValidateRoot(): boolean {
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
  setLayout(layout: AbstractLayout) {
    this.layout = layout;
    if (layout.cssClass) {
      this.$comp.addClass(layout.cssClass);
    }
  }

  /**
   * Returns the preferred size of the component, insets included, margin excluded.
   * <p>
   * The preferred size is cached until the component will be invalidated.
   * Hence, subsequent calls to this function will return the cached preferred size unless the component is invalidated.
   * <p>
   *
   * @param options an optional options object. See {@link HtmlCompPrefSizeOptions} for the available options.
   *                All other options are passed as they are to the layout when {@link AbstractLayout.preferredLayoutSize} is called.
   *                But it depends on the actual layout if these options have an effect or not.
   *                Short-hand version: If a boolean is passed instead of an object, the value is automatically converted to the option "includeMargin".
   * @exception When component has no layout
   */
  prefSize(options?: HtmlCompPrefSizeOptions | boolean): Dimension {
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

  computePrefSizeKey(options: HtmlCompPrefSizeOptions): string {
    return 'wHint' + scout.nvl(options.widthHint, '-1') + 'hHint' + scout.nvl(options.heightHint, '-1') + 'wOnly' + scout.nvl(options.widthOnly, '-1');
  }

  /**
   * Removes padding, border and margin from the width and heightHint so that the actual layout does not need to take care of it.
   * Also makes sure the hints consider the min and max size set by CSS.
   */
  protected _adjustSizeHintsForPrefSize(options: HtmlCompPrefSizeOptions, minSize: Dimension, maxSize: Dimension) {
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
   * @internal
   */
  _adjustPrefSizeWithMinMaxSize(prefSize: Dimension, minSize?: Dimension, maxSize?: Dimension) {
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
  insets(options?: InsetsOptions | boolean): Insets {
    return graphics.insets(this.$comp, options);
  }

  margins(): Insets {
    return graphics.margins(this.$comp);
  }

  borders(): Insets {
    return graphics.borders(this.$comp);
  }

  cssMinSize(): Dimension {
    return graphics.cssMinSize(this.$comp);
  }

  cssMaxSize(): Dimension {
    return graphics.cssMaxSize(this.$comp);
  }

  /**
   * @param options an optional options object. Short-hand version: If a boolean is passed instead
   *                of an object, the value is automatically converted to the option "includeMargin".
   * @returns the size of the component, insets included.
   * @see graphics.size
   */
  size(options?: SizeOptions | boolean): Dimension {
    return graphics.size(this.$comp, options);
  }

  /**
   * Sets the size of the component, insets included. Which means: the method subtracts the components insets
   * from the given size before setting the width/height of the component.
   */
  setSize(size: Dimension) {
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

  /**
   * Delegates to {@link graphics.bounds}.
   */
  bounds(options?: BoundsOptions | boolean): Rectangle {
    return graphics.bounds(this.$comp, options);
  }

  /**
   * Delegates to {@link graphics.position}.
   */
  position(): Point {
    return graphics.position(this.$comp);
  }

  /**
   * Delegates to {@link graphics.offsetBounds}.
   */
  offsetBounds(options?: BoundsOptions | boolean): Rectangle {
    return graphics.offsetBounds(this.$comp, options);
  }

  /**
   * Delegates to {@link graphics.offset}.
   */
  offset(): Point {
    return graphics.offset(this.$comp);
  }

  /**
   * Delegates to {@link graphics.setLocation}.
   */
  setLocation(location: Point) {
    graphics.setLocation(this.$comp, location);
  }

  /**
   * Delegates to {@link graphics.location}.
   */
  location(): Point {
    return graphics.location(this.$comp);
  }

  setBounds(bounds: Rectangle) {
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
   * Checks whether {@link $comp} is in the DOM or has been removed or detached.<br>
   * Also returns false if the {@link $comp} does not belong to a window (defaultView) anymore. This may happen if it belonged to a popup window which is now closed.
   */
  isAttached(): boolean {
    return this.$comp.isAttached() && !!this.$comp.window(true);
  }

  isVisible(): boolean {
    return this.$comp.isVisible();
  }

  isAttachedAndVisible(): boolean {
    return this.isAttached() && this.isVisible();
  }

  debug(): string {
    return graphics.debugOutput(this.$comp);
  }

  /* --- STATIC HELPERS ------------------------------------------------------------- */

  /**
   * Creates a new {@link HtmlComponent} and links it to the given $comp element, so it can be
   * retrieved again with <code>HtmlComponent.get($comp)</code>.
   */
  static install($comp: JQuery, session: Session): HtmlComponent {
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
   * Returns the {@link HtmlComponent} associated with the given DOM $comp.
   * Throws an error when data 'htmlComponent' is not set.
   */
  static get($comp: JQuery): HtmlComponent {
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
   * Returns the {@link HtmlComponent} associated with the given DOM $comp.
   * Returns null if data 'htmlComponent' is not set.
   */
  static optGet($comp: JQuery): HtmlComponent {
    return $comp && $comp.data('htmlComponent');
  }
}
