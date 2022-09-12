/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays, Dimension, Insets, objects, Point, Rectangle, scout, scrollbars} from '../index';
import $ from 'jquery';

/**
 * This file contains helpers for graphical operations
 */

/**
 * Returns the preferred size of $elem.
 * Precondition: $elem and it's parents must not be hidden (display: none. Visibility: hidden would be ok
 * because in this case the browser reserves the space the element would be using).
 *
 * OPTION                   DEFAULT VALUE   DESCRIPTION
 * ------------------------------------------------------------------------------------------------------
 * includeMargin            false           Whether to include $elem's margins in the returned size.
 *
 * useCssSize               false           If true, the width and height properties are set to '' while
 *                                          measuring, thus allowing existing CSS rules to influence the
 *                                          sizes. If set to false, the sizes are set to 'auto' or the
 *                                          corresponding hint values (see below).
 *
 * widthHint                undefined       If useCssSize is false, this value is used as width (in pixels)
 *                                          instead of 'auto'. Useful to get the preferred height for a
 *                                          given width.
 *
 * heightHint               undefined       Same as 'widthHint' but for the height.
 *
 * enforceSizeHints         false           Sets min/max-width/height in addition to with width/height if widthHint resp. heightHint is set.
 *                                          The browser sometimes makes the element smaller or larger than specified by width/height, especially in a flex container.
 *                                          To prevent that, set this option to true. Default is false, but may change in the future.
 *
 * restoreScrollPositions   true            By default, the $elem's scrolling position is saved and restored
 *                                          during the execution of this method (because applying
 *                                          intermediate styles for measurement might change the current
 *                                          position). If the calling method does that itself, you should
 *                                          set this option to false to prevent overriding the stored
 *                                          scrolling position in $elem's data attributes.
 * animateClasses           undefined       If set, the $elem is checked for one of these classes.
 *                                          If one class is currently set on the $elem, a clone of the $elem without the class
 *                                          is created and measured instead. See also {@link #prefSizeWithoutAnimation}.
 *
 * @param $elem
 *          the jQuery element to measure
 * @param options
 *          an optional options object (see table above). Short-hand version: If a boolean is passed instead
 *          of an object, the value is automatically converted to the option "includeMargin".
 */
export function prefSize($elem, options) {
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
    return prefSizeWithoutAnimation($elem, options);
  }

  let oldStyle = $elem.attr('style');
  let oldScrollLeft = $elem.scrollLeft();
  let oldScrollTop = $elem.scrollTop();

  if (options.restoreScrollPositions) {
    scrollbars.storeScrollPositions($elem);
  }

  // UseCssSize is necessary if the css rules have a fix height or width set.
  // Otherwise setting the width/height to auto could result in a different size
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
  $elem.scrollLeft(oldScrollLeft);
  $elem.scrollTop(oldScrollTop);

  if (options.restoreScrollPositions) {
    scrollbars.restoreScrollPositions($elem);
  }

  return exactPrefSize(prefSize, options);
}

/**
 * Ensure resulting numbers are integers. getBoundingClientRect() might correctly return fractional values
 * (because of the browser's sub-pixel rendering). However, if we use those numbers to set the size
 * of an element using CSS, it gets rounded or cut off. The behavior is not defined amongst different
 * browser engines.
 * Example:
 * - Measured size from this method:      h = 345.239990234375
 * - Set the size to an element:          $elem.css('height', h + 'px')
 * - Results:
 *    Firefox & Chrome     <div id="elem" style="height: 345.24px">     [Fractional part rounded to three digits]
 */
export function exactPrefSize(prefSize, options) {
  let exact = scout.nvl(options.exact, false);
  if (!exact) {
    prefSize.width = Math.ceil(prefSize.width);
    prefSize.height = Math.ceil(prefSize.height);
  }
  return prefSize;
}

/**
 * If the $container is currently animated by CSS, create a clone, remove the animating CSS class and measure the clone instead.
 * This may be necessary because the animation might change the size of the element.
 * If prefSize is called during the animation, the current size is returned instead of the one after the animation.
 */
export function prefSizeWithoutAnimation($elem, options) {
  let animateClasses = arrays.ensure(options.animateClasses);
  animateClasses = animateClasses.filter(cssClass => {
    return $elem.hasClass(cssClass);
  });
  options = $.extend({}, options);
  options.animateClasses = null;

  if (animateClasses.length === 0) {
    return prefSize($elem, options);
  }

  animateClasses = arrays.format(animateClasses, ' ');
  let $clone = $elem
    .clone()
    .removeClass(animateClasses)
    .appendTo($elem.parent());
  let prefSizeResult = prefSize($clone, options);
  $clone.remove();
  return prefSizeResult;
}

/* These functions are designed to be used with box-sizing:box-model. The only reliable
 * way to set the size of a component when working with box model is to use css('width/height'...)
 * in favor of width/height() functions.
 */

/**
 * Returns the size of the element, insets included. The sizes are rounded up, unless the option 'exact' is set to true.
 *
 * @param $elem
 *          the jQuery element to measure
 * @param {object|boolean} [options]
 *          an optional options object (see table above). Short-hand version: If a boolean is passed instead
 *          of an object, the value is automatically converted to the option "includeMargin".
 * @param {boolean} [options.includeMargin] Whether to include $elem's margins in the returned size. Default is false.
 * @param {boolean} [options.exact] When set to true the returned dimensions may contain fractional digits, otherwise the sizes are rounded up. Default is false.
 * @returns {Dimension}
 */
export function size($elem, options) {
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
}

/**
 * @returns {Dimension} the size of the element specified by the style.
 */
export function cssSize($elem) {
  return new Dimension($elem.cssWidth(), $elem.cssHeight());
}

/**
 * @returns {Dimension} the max size of the element specified by the style.
 */
export function cssMaxSize($elem) {
  return new Dimension($elem.cssMaxWidth(), $elem.cssMaxHeight());
}

/**
 * @returns {Dimension} the min size of the element specified by the style.
 */
export function cssMinSize($elem) {
  return new Dimension($elem.cssMinWidth(), $elem.cssMinHeight());
}

export function setSize($comp, vararg, height) {
  let size = vararg instanceof Dimension ?
    vararg : new Dimension(vararg, height);
  $comp
    .cssWidth(size.width)
    .cssHeight(size.height);
}

/**
 * Returns the inset-dimensions of the component (padding, margin, border).
 *
 * OPTION                   DEFAULT VALUE   DESCRIPTION
 * ------------------------------------------------------------------------------------------------------
 * includeMargin            false           Whether to include $elem's margins in the returned insets.
 *
 * includePadding           true            Whether to include $elem's paddings in the returned insets.
 *
 * includeBorder            true            Whether to include $elem's borders in the returned insets.
 *
 * @param $elem
 *          the jQuery element to measure
 * @param options
 *          an optional options object (see table above). Short-hand version: If a boolean is passed instead
 *          of an object, the value is automatically converted to the option "includeMargin".
 */
export function insets($comp, options) {
  if (typeof options === 'boolean') {
    options = {
      includeMargin: options
    };
  } else {
    options = options || {};
  }

  let i,
    directions = ['top', 'right', 'bottom', 'left'],
    insets = [0, 0, 0, 0],
    includeMargin = scout.nvl(options.includeMargin, false),
    includePadding = scout.nvl(options.includePadding, true),
    includeBorder = scout.nvl(options.includeBorder, true);

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
}

export function margins($comp) {
  return insets($comp, {
    includeMargin: true,
    includePadding: false,
    includeBorder: false
  });
}

export function setMargins($comp, margins) {
  $comp.css({
    marginLeft: margins.left,
    marginRight: margins.right,
    marginTop: margins.top,
    marginBottom: margins.bottom
  });
}

export function paddings($comp) {
  return insets($comp, {
    includeMargin: false,
    includePadding: true,
    includeBorder: false
  });
}

export function borders($comp) {
  return insets($comp, {
    includeMargin: false,
    includePadding: false,
    includeBorder: true
  });
}

/**
 * Sets the location (CSS properties left, top) of the component.
 * @param vararg integer value for X position OR instance of Point
 * @param y (optional) integer value for Y position
 * @returns
 */
export function setLocation($comp, vararg, y) {
  let point = vararg instanceof Point ?
    vararg : new Point(vararg, y);
  $comp
    .cssLeft(point.x)
    .cssTop(point.y);
}

/**
 * Returns a Point consisting of the component's "cssLeft" and
 * "cssTop" values (reverse operation to setLocation).
 */
export function location($comp) {
  return new Point($comp.cssLeft(), $comp.cssTop());
}

/**
 * Returns the bounds of the element relative to the offset parent, insets included.
 * The sizes are rounded up, unless the option 'exact' is set to true.
 *
 * OPTION                   DEFAULT VALUE   DESCRIPTION
 * ------------------------------------------------------------------------------------------------------
 * includeMargin            false           Whether to include $elem's margins in the returned size. X and Y are not affected by this option.
 *
 * exact                    false           When set to true the returned size may contain fractional digits, otherwise the sizes are rounded up. X and Y are not affected by this option.
 *
 * @param $elem
 *          the jQuery element to measure
 * @param options
 *          an optional options object (see table above). Short-hand version: If a boolean is passed instead
 *          of an object, the value is automatically converted to the option "includeMargin".
 */
export function bounds($elem, options) {
  return _bounds($elem, $elem.position(), options);
}

/**
 * @returns {Point} the position relative to the offset parent ($elem.position()).
 */
export function position($elem) {
  let pos = $elem.position();
  return new Point(pos.left, pos.top);
}

/**
 * Returns the bounds of the element relative to the document, insets included.
 * The sizes are rounded up, unless the option 'exact' is set to true.
 *
 * OPTION                   DEFAULT VALUE   DESCRIPTION
 * ------------------------------------------------------------------------------------------------------
 * includeMargin            false           Whether to include $elem's margins in the returned size. X and Y are not affected by this option.
 *
 * exact                    false           When set to true the returned size may contain fractional digits, otherwise the sizes are rounded up. X and Y are not affected by this option.
 *
 * @param $elem
 *          the jQuery element to measure
 * @param options
 *          an optional options object (see table above). Short-hand version: If a boolean is passed instead
 *          of an object, the value is automatically converted to the option "includeMargin".
 */
export function offsetBounds($elem, options) {
  return _bounds($elem, $elem.offset(), options);
}

/**
 * @returns {Point} the position relative to the document ($elem.offset()).
 */
export function offset($elem) {
  let pos = $elem.offset();
  return new Point(pos.left, pos.top);
}

export function _bounds($elem, pos, options) {
  let s = size($elem, options);
  return new Rectangle(pos.left, pos.top, s.width, s.height);
}

export function setBounds($comp, vararg, y, width, height) {
  let bounds = vararg instanceof Rectangle ?
    vararg : new Rectangle(vararg, y, width, height);
  $comp
    .cssLeft(bounds.x)
    .cssTop(bounds.y)
    .cssWidth(bounds.width)
    .cssHeight(bounds.height);
}

/**
 * @returns {Rectangle} the bounds of the element specified by the style.
 */
export function cssBounds($elem) {
  return new Rectangle($elem.cssLeft(), $elem.cssTop(), $elem.cssWidth(), $elem.cssHeight());
}

export function debugOutput($comp) {
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

export default {
  borders,
  bounds,
  cssBounds,
  cssMaxSize,
  cssMinSize,
  cssSize,
  debugOutput,
  exactPrefSize,
  insets,
  location,
  margins,
  offset,
  offsetBounds,
  paddings,
  position,
  prefSize,
  prefSizeWithoutAnimation,
  setBounds,
  setLocation,
  setMargins,
  setSize,
  size
};
