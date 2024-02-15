/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, Dimension, Insets, objects, Point, Rectangle, scout, scrollbars} from '../index';
import $ from 'jquery';

export interface PrefSizeOptions {
  /**
   * When set to true the returned dimensions may contain fractional digits, otherwise the sizes are rounded up. Default is false.
   */
  exact?: boolean;

  /**
   * Whether to include the margins in the returned size. Default is false.
   */
  includeMargin?: boolean;

  /**
   * If true, the width and height properties are set to '' while measuring, thus allowing existing CSS rules to influence the sizes.
   * If set to false, the sizes are set to 'auto' or the corresponding hint values. Default is false.
   */
  useCssSize?: boolean;

  /**
   * If useCssSize is false, this value is used as width (in pixels) instead of 'auto'.
   * Useful to get the preferred height for a given width.
   */
  widthHint?: number;

  /**
   * Same as 'widthHint' but for the height.
   */
  heightHint?: number;

  /**
   * Sets min/max-width/height in addition to with width/height if widthHint resp. heightHint is set.
   * The browser sometimes makes the element smaller or larger than specified by width/height, especially in a flex container.
   * To prevent that, set this option to true. Default is false, but may change in the future.
   */
  enforceSizeHints?: boolean;

  /**
   * By default, the $elem's scrolling position is saved and restored during the execution of this method (because applying
   * intermediate styles for measurement might change the current position). If the calling method does that itself, you should
   * set this option to false to prevent overriding the stored scrolling position in $elem's data attributes. Default is true.
   */
  restoreScrollPositions?: boolean;

  /**
   * If set, the $elem is checked for one of these classes.
   * If one of these classes is currently set on the $elem, a clone of the $elem without the classes is created and measured instead. See also {@link prefSizeWithoutAnimation}.
   */
  animateClasses?: string[];
}

export interface SizeOptions {
  /**
   * When set to true the returned dimensions may contain fractional digits, otherwise the sizes are rounded up. Default is false.
   */
  exact?: boolean;

  /**
   * Whether to include the margins in the returned size. Default is false.
   */
  includeMargin?: boolean;
}

export interface InsetsOptions {
  /**
   * Whether to include the margins in the returned insets. Default is false.
   */
  includeMargin?: boolean;

  /**
   * Whether to include the paddings in the returned insets. Default is true.
   */
  includePadding?: boolean;

  /**
   * Whether to include the borders in the returned insets. Default is true.
   */
  includeBorder?: boolean;
}

export interface BoundsOptions {
  /**
   * When set to true the returned size may contain fractional digits, otherwise the sizes are rounded up. X and Y are not affected by this option. Default is false.
   */
  exact?: boolean;

  /**
   * Whether to include the margins in the returned size. X and Y are not affected by this option. Default is false.
   */
  includeMargin?: boolean;
}

function setBounds($comp: JQuery, x: number, y: number, width: number, height: number);
function setBounds($comp: JQuery, bounds: Rectangle);
function setBounds($comp: JQuery, xOrBounds: number | Rectangle, y?: number, width?: number, height?: number) {
  let bounds = xOrBounds instanceof Rectangle ?
    xOrBounds : new Rectangle(xOrBounds, y, width, height);
  $comp
    .cssLeft(bounds.x)
    .cssTop(bounds.y)
    .cssWidth(bounds.width)
    .cssHeight(bounds.height);
}

function setSize($comp: JQuery, width: number, height: number);
function setSize($comp: JQuery, size: Dimension);
function setSize($comp: JQuery, widthOrSize: Dimension | number, height?: number) {
  let size = widthOrSize instanceof Dimension ?
    widthOrSize : new Dimension(widthOrSize, height);
  $comp
    .cssWidth(size.width)
    .cssHeight(size.height);
}

function setLocation($comp: JQuery, x: number, y: number);
function setLocation($comp: JQuery, location: Point);
/**
 * Sets the location (CSS properties left, top) of the component.
 */
function setLocation($comp: JQuery, xOrPoint: number | Point, y?: number) {
  let point = xOrPoint instanceof Point ?
    xOrPoint : new Point(xOrPoint, y);
  $comp
    .cssLeft(point.x)
    .cssTop(point.y);
}

/**
 * Helpers for graphical operations
 */
export const graphics = {

  /**
   * Returns the preferred size of $elem.
   *
   * Precondition: $elem and its parents must not be hidden ('display: none' - other styles like 'visibility: hidden'
   * or 'opacity: 0' would be ok because in this case the browser reserves the space the element would be using).
   *
   * The `style` and `class` properties are temporarily altered to allow the element to assume its "natural size".
   * A marker CSS class `measure` is added that can be used to reset element-specific CSS constraints (e.g. flexbox).
   *
   * @param $elem
   *          the jQuery element to measure
   * @param options
   *          an optional options object. Shorthand version: If a boolean is passed instead
   *          of an object, the value is automatically converted to the option "includeMargin".
   */
  prefSize($elem: JQuery, options?: PrefSizeOptions | boolean): Dimension {
    // Return 0/0 if element is not displayed (display: none).
    // We don't use isVisible by purpose because isVisible returns false for elements with visibility: hidden which is wrong here (we would like to be able to measure hidden elements)
    if (!$elem[0] || $elem.isDisplayNone()) {
      return new Dimension(0, 0);
    }

    if (typeof options === 'boolean') {
      options = {
        includeMargin: options
      };
    } else {
      options = options || {};
    }

    let defaults = {
      includeMargin: false,
      useCssSize: false,
      widthHint: undefined,
      heightHint: undefined,
      restoreScrollPositions: true
    };
    options = $.extend({}, defaults, options);

    if (options.animateClasses && options.animateClasses.length > 0) {
      return graphics.prefSizeWithoutAnimation($elem, options);
    }

    let oldStyle = $elem.attr('style');
    let oldClass = $elem.attr('class');
    let oldScrollLeft = $elem.scrollLeft();
    let oldScrollTop = $elem.scrollTop();

    if (options.restoreScrollPositions) {
      scrollbars.storeScrollPositions($elem);
    }

    // UseCssSize is necessary if the css rules have a fix height or width set.
    // Otherwise, setting the width/height to auto could result in a different size
    let newWidth = (options.useCssSize ? '' : scout.nvl(options.widthHint, 'auto'));
    let newHeight = (options.useCssSize ? '' : scout.nvl(options.heightHint, 'auto'));

    let cssProperties = {
      'width': newWidth,
      'height': newHeight
    };
    if (scout.nvl(options.enforceSizeHints, false)) {
      if (objects.isNumber(newWidth)) {
        cssProperties['max-width'] = newWidth;
        cssProperties['min-width'] = newWidth;
      }
      if (objects.isNumber(newHeight)) {
        cssProperties['max-height'] = newHeight;
        cssProperties['min-height'] = newHeight;
      }
    }

    // modify properties which prevent reading the preferred size
    $elem.addClass('measure');
    $elem.css(cssProperties);

    // measure
    let bcr = $elem[0].getBoundingClientRect();
    let prefSize = new Dimension(bcr.width, bcr.height);
    if (options.includeMargin) {
      prefSize.width += $elem.cssMarginX();
      prefSize.height += $elem.cssMarginY();
    }

    // reset the modified style attribute
    $elem.attrOrRemove('style', oldStyle);
    $elem.attrOrRemove('class', oldClass);
    $elem.scrollLeft(oldScrollLeft);
    $elem.scrollTop(oldScrollTop);

    if (options.restoreScrollPositions) {
      scrollbars.restoreScrollPositions($elem);
    }

    return graphics.exactPrefSize(prefSize, options);
  },

  /**
   * Ensure resulting numbers are integers. getBoundingClientRect() might correctly return fractional values
   * (because of the browser's sub-pixel rendering). However, if we use those numbers to set the size
   * of an element using CSS, it gets rounded or cut off. The behavior is not defined amongst different
   * browser engines.
   * <p>
   * Example:
   * - Measured size from this method:      h = 345.239990234375
   * - Set the size to an element:          $elem.css('height', h + 'px')
   * - Results:
   *    Firefox & Chrome     <div id="elem" style="height: 345.24px">     [Fractional part rounded to three digits]
   */
  exactPrefSize(prefSize: Dimension, options: PrefSizeOptions): Dimension {
    let exact = scout.nvl(options.exact, false);
    if (!exact) {
      prefSize.width = Math.ceil(prefSize.width);
      prefSize.height = Math.ceil(prefSize.height);
    }
    return prefSize;
  },

  /**
   * If the $elem is currently animated by CSS, create a clone, remove the animating CSS class and measure the clone instead.
   * This may be necessary because the animation might change the size of the element.
   * If prefSize is called during the animation, the current size is returned instead of the one after the animation.
   */
  prefSizeWithoutAnimation($elem: JQuery, options: PrefSizeOptions): Dimension {
    let animateClasses = arrays.ensure(options.animateClasses);
    animateClasses = animateClasses.filter(cssClass => {
      return $elem.hasClass(cssClass);
    });
    options = $.extend({}, options);
    options.animateClasses = null;

    if (animateClasses.length === 0) {
      return graphics.prefSize($elem, options);
    }

    let animateClassesStr = arrays.format(animateClasses, ' ');
    let $clone = $elem
      .clone()
      .removeClass(animateClassesStr)
      .appendTo($elem.parent());
    let prefSizeResult = graphics.prefSize($clone, options);
    $clone.remove();
    return prefSizeResult;
  },

  /* These functions are designed to be used with box-sizing:box-model. The only reliable
   * way to set the size of a component when working with box model is to use css('width/height'...)
   * in favor of width/height() functions.
   */

  /**
   * Returns the size of the element, insets included. The sizes are rounded up, unless the option 'exact' is set to true.
   *
   * @param $elem
   *          the jQuery element to measure
   * @param options
   *          an optional options object. Shorthand version: If a boolean is passed instead
   *          of an object, the value is automatically converted to the option "includeMargin".
   */
  size($elem: JQuery, options?: SizeOptions | boolean): Dimension {
    if (!$elem[0] || $elem.isDisplayNone()) {
      return new Dimension(0, 0);
    }

    if (typeof options === 'boolean') {
      options = {
        includeMargin: options
      };
    } else {
      options = options || {};
    }

    let bcr = $elem[0].getBoundingClientRect();
    let size = new Dimension(bcr.width, bcr.height);
    let includeMargin = scout.nvl(options.includeMargin, false);
    if (includeMargin) {
      size.width += $elem.cssMarginX();
      size.height += $elem.cssMarginY();
    }
    // see comments in prefSize()
    let exact = scout.nvl(options.exact, false);
    if (!exact) {
      size.width = Math.ceil(size.width);
      size.height = Math.ceil(size.height);
    }
    return size;
  },

  /**
   * @returns the size of the element specified by the style.
   */
  cssSize($elem: JQuery): Dimension {
    return new Dimension($elem.cssWidth(), $elem.cssHeight());
  },

  /**
   * @returns the max size of the element specified by the style.
   */
  cssMaxSize($elem: JQuery): Dimension {
    return new Dimension($elem.cssMaxWidth(), $elem.cssMaxHeight());
  },

  /**
   * @returns the min size of the element specified by the style.
   */
  cssMinSize($elem: JQuery): Dimension {
    return new Dimension($elem.cssMinWidth(), $elem.cssMinHeight());
  },

  setSize,

  /**
   * Returns the inset-dimensions of the component (padding, margin, border).
   *
   * @param $elem
   *          the jQuery element to measure
   * @param options
   *          an optional options object. Shorthand version: If a boolean is passed instead
   *          of an object, the value is automatically converted to the option {@link InsetsOptions.includeMargin}.
   */
  insets($comp: JQuery, options?: InsetsOptions | boolean): Insets {
    let opts: InsetsOptions;
    if (typeof options === 'boolean') {
      opts = {
        includeMargin: options
      };
    } else {
      opts = options || {};
    }

    let i,
      directions = ['top', 'right', 'bottom', 'left'],
      insets = [0, 0, 0, 0],
      includeMargin = scout.nvl(opts.includeMargin, false),
      includePadding = scout.nvl(opts.includePadding, true),
      includeBorder = scout.nvl(opts.includeBorder, true);

    for (i = 0; i < directions.length; i++) {
      if (includeMargin) {
        insets[i] += $comp.cssPxValue('margin-' + directions[i]);
      }
      if (includePadding) {
        insets[i] += $comp.cssPxValue('padding-' + directions[i]);
      }
      if (includeBorder) {
        insets[i] += $comp.cssPxValue('border-' + directions[i] + '-width');
      }
    }
    return new Insets(insets[0], insets[1], insets[2], insets[3]);
  },

  margins($comp: JQuery): Insets {
    return graphics.insets($comp, {
      includeMargin: true,
      includePadding: false,
      includeBorder: false
    });
  },

  setMargins($comp: JQuery, margins: Insets) {
    $comp.css({
      marginLeft: margins.left,
      marginRight: margins.right,
      marginTop: margins.top,
      marginBottom: margins.bottom
    });
  },

  paddings($comp: JQuery): Insets {
    return graphics.insets($comp, {
      includeMargin: false,
      includePadding: true,
      includeBorder: false
    });
  },

  borders($comp: JQuery): Insets {
    return graphics.insets($comp, {
      includeMargin: false,
      includePadding: false,
      includeBorder: true
    });
  },

  setLocation,

  /**
   * Returns a Point consisting of the component's "cssLeft" and
   * "cssTop" values (reverse operation to setLocation).
   */
  location($comp: JQuery): Point {
    return new Point($comp.cssLeft(), $comp.cssTop());
  },

  /**
   * Returns the bounds of the element relative to the offset parent, insets included.
   * The sizes are rounded up, unless the option 'exact' is set to true.
   *
   * @param $elem
   *          the jQuery element to measure
   * @param options
   *          an optional options object. Shorthand version: If a boolean is passed instead
   *          of an object, the value is automatically converted to the option "includeMargin".
   */
  bounds($elem: JQuery, options?: BoundsOptions | boolean): Rectangle {
    return graphics._bounds($elem, $elem.position(), options);
  },

  /**
   * @returns {Point} the position relative to the offset parent ($elem.position()).
   */
  position($elem: JQuery): Point {
    let pos = $elem.position();
    return new Point(pos.left, pos.top);
  },

  /**
   * Returns the bounds of the element relative to the document, insets included.
   * The sizes are rounded up, unless the option 'exact' is set to true.
   *
   * @param $elem
   *          the jQuery element to measure
   * @param options
   *          an optional options object. Shorthand version: If a boolean is passed instead
   *          of an object, the value is automatically converted to the option "includeMargin".
   */
  offsetBounds($elem: JQuery, options?: BoundsOptions | boolean): Rectangle {
    return graphics._bounds($elem, $elem.offset(), options);
  },

  /**
   * @returns the position relative to the document, see also {@link JQuery.offset}.
   */
  offset($elem: JQuery): Point {
    let pos = $elem.offset();
    return new Point(pos.left, pos.top);
  },

  /** @internal */
  _bounds($elem: JQuery, pos: JQuery.Coordinates, options?: BoundsOptions | boolean): Rectangle {
    let s = graphics.size($elem, options);
    return new Rectangle(pos.left, pos.top, s.width, s.height);
  },

  setBounds,

  /**
   * @returns the bounds of the element specified by the style.
   */
  cssBounds($elem: JQuery): Rectangle {
    return new Rectangle($elem.cssLeft(), $elem.cssTop(), $elem.cssWidth(), $elem.cssHeight());
  },

  debugOutput($comp: JQuery | HTMLElement): string {
    if (!$comp) {
      return '$comp is undefined';
    }
    $comp = $.ensure($comp);
    if ($comp.length === 0) {
      return '$comp doesn\t match any elements';
    }
    let attrs = '';
    if ($comp.attr('id')) {
      attrs += 'id=' + $comp.attr('id');
    }
    if ($comp.attr('class')) {
      attrs += ' class=' + $comp.attr('class');
    }
    if ($comp.attr('data-modelclass')) {
      attrs += ' data-modelclass=' + $comp.attr('data-modelclass');
    }
    if (attrs.length === 0) {
      let html = scout.nvl($comp.html(), '');
      if (html.length > 30) {
        html = html.substring(0, 30) + '...';
      }
      attrs = html;
    }
    if (!$comp.isAttached()) {
      attrs += ' attached=false';
    }
    return 'Element[' + attrs.trim() + ']';
  }
};
