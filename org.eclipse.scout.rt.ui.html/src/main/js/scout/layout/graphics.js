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
 * This file contains helpers for graphical operations and JavaScript ports from java.awt classes
 */
scout.graphics = {

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
   * restoreScrollPositions   true            By default, the $elem's scrolling position is saved and restored
   *                                          during the execution of this method (because applying
   *                                          intermediate styles for measurement might change the current
   *                                          position). If the calling method does that itself, you should
   *                                          set this option to false to prevent overriding the stored
   *                                          scrolling position in $elem's data attributes.
   *
   * @memberOf scout.graphics
   */
  prefSize: function($elem, options) {
    // Return 0/0 if element is not displayed (display: none).
    // We don't use isVisible by purpose because isVisible returns false for elements with visibility: hidden which is wrong here (we would like to be able to measure hidden elements)
    if (!$elem[0] || $elem.isDisplayNone()) {
      return new scout.Dimension(0, 0);
    }

    options = options || {};

    var defaults = {
      includeMargin: false,
      useCssSize: false,
      widthHint: undefined,
      heightHint: undefined,
      restoreScrollPositions: true
    };
    options = $.extend({}, defaults, options);

    var oldStyle = $elem.attr('style');
    var oldScrollLeft = $elem.scrollLeft();
    var oldScrollTop = $elem.scrollTop();

    if (options.restoreScrollPositions) {
      scout.scrollbars.storeScrollPositions($elem);
    }

    // UseCssSize is necessary if the css rules have a fix height or width set.
    // Otherwise setting the width/height to auto could result in a different size
    var newWidth = (options.useCssSize ? '' : scout.nvl(options.widthHint, 'auto'));
    var newHeight = (options.useCssSize ? '' : scout.nvl(options.heightHint, 'auto'));

    // modify properties which prevent reading the preferred size
    $elem.css({
      'width': newWidth,
      'height': newHeight
    });

    // measure
    var bcr = $elem[0].getBoundingClientRect();
    var prefSize = new scout.Dimension(bcr.width, bcr.height);
    if (options.includeMargin) {
      prefSize.width += $elem.cssMarginX();
      prefSize.height += $elem.cssMarginY();
    }

    // reset the modified style attribute
    $elem.attrOrRemove('style', oldStyle);
    $elem.scrollLeft(oldScrollLeft);
    $elem.scrollTop(oldScrollTop);

    if (options.restoreScrollPositions) {
      scout.scrollbars.restoreScrollPositions($elem);
    }

    // Ensure resulting numbers are integers. getBoundingClientRect() might correctly return fractional values
    // (because of the browser's sub-pixel rendering). However, if we use those numbers to set the size
    // of an element using CSS, it gets rounded or cut off. The behavior is not defined amongst different
    // browser engines.
    // Example:
    // - Measured size from this method:      h = 345.239990234375
    // - Set the size to an element:          $elem.css('height', h + 'px')
    // - Results:
    //     IE                   <div id="elem" style="height: 345.23px">     [Fractional part cut off after two digits]
    //     Firefox & Chrome     <div id="elem" style="height: 345.24px">     [Fractional part rounded to three digits]
    prefSize.width = Math.ceil(prefSize.width);
    prefSize.height = Math.ceil(prefSize.height);

    return prefSize;
  },

  /* These functions are designed to be used with box-sizing:box-model. The only reliable
   * way to set the size of a component when working with box model is to use css('width/height'...)
   * in favor of width/height() functions.
   */

  /**
   * Returns the size of the element, insets included. The sizes are rounded up, unless the option 'exact' is set to true.
   *
   * OPTION                   DEFAULT VALUE   DESCRIPTION
   * ------------------------------------------------------------------------------------------------------
   * includeMargin            false           Whether to include $elem's margins in the returned size.
   *
   * exact                    false           When set to true the returned dimensions may contain fractional digits, otherwise the sizes are rounded up.
   */
  size: function($elem, options) {
    if (!$elem[0] || $elem.isDisplayNone()) {
      return new scout.Dimension(0, 0);
    }
    var bcr = $elem[0].getBoundingClientRect();
    var size = new scout.Dimension(bcr.width, bcr.height);
    var includeMargin = scout.nvl(options.includeMargin, false);
    var exact = scout.nvl(options.exact, false);
    if (includeMargin) {
      size.width += $elem.cssMarginX();
      size.height += $elem.cssMarginY();
    }
    // see comments in prefSize()
    if (exact) {
      size.width = size.width;
      size.height = size.height;
    } else {
      size.width = Math.ceil(size.width);
      size.height = Math.ceil(size.height);
    }
    return size;
  },

  /**
   * Returns the size of the component, insets included.
   * @param {boolean} [includeMargin] when set to true, returned dimensions include margins of component, default is <code>false</code>.
   * @deprecated use {@link scout.graphics.size instead}
   */
  getSize: function($comp, includeMargin) {
    return scout.graphics.size($comp, {
      includeMargin: includeMargin
    });
  },

  setSize: function($comp, vararg, height) {
    var size = vararg instanceof scout.Dimension ?
      vararg : new scout.Dimension(vararg, height);
    $comp
      .cssWidth(size.width)
      .cssHeight(size.height);
  },

  /**
   * Returns the size of a visible component or (0,0) when component is invisible.
   */
  getVisibleSize: function($comp, includeMargin) {
    if ($comp.length === 1 && $comp.isVisible()) {
      return scout.graphics.getSize($comp, includeMargin);
    } else {
      return new scout.Dimension(0, 0);
    }
  },

  /**
   * Returns the inset-dimensions of the component (padding, margin, border).
   */
  getInsets: function($comp, options) {
    options = options || {};
    var i,
      directions = ['top', 'right', 'bottom', 'left'],
      insets = [0, 0, 0, 0],
      includeMargin = options.includeMargin !== undefined ? options.includeMargin : false,
      includePadding = options.includePadding !== undefined ? options.includePadding : true,
      includeBorder = options.includeBorder !== undefined ? options.includeBorder : true;

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
    return new scout.Insets(insets[0], insets[1], insets[2], insets[3]);
  },

  getMargins: function($comp) {
    return scout.graphics.getInsets($comp, {
      includeMargin: true,
      includePadding: false,
      includeBorder: false
    });
  },

  getBounds: function($comp) {
    var parseCssPosition = function(prop) {
      var value = $comp.css(prop);
      return 'auto' === value ? 0 : parseInt(value, 10);
    };
    return new scout.Rectangle(
      parseCssPosition('left'),
      parseCssPosition('top'),
      $comp.outerWidth(true),
      $comp.outerHeight(true));
  },

  setBounds: function($comp, vararg, y, width, height) {
    var bounds = vararg instanceof scout.Rectangle ?
      vararg : new scout.Rectangle(vararg, y, width, height);
    $comp
      .cssLeft(bounds.x)
      .cssTop(bounds.y)
      .cssWidth(bounds.width)
      .cssHeight(bounds.height);
  },

  /**
   * Sets the location (CSS properties left, top) of the component.
   * @param vararg integer value for X position OR instance of scout.Point
   * @param y (optional) integer value for Y position
   * @returns
   */
  setLocation: function($comp, vararg, y) {
    var point = vararg instanceof scout.Point ?
      vararg : new scout.Point(vararg, y);
    $comp
      .cssLeft(point.x)
      .cssTop(point.y);
  },

  bounds: function($elem, includeSizeMargin, includePosMargin) {
    // TODO [7.0] cgu: merge with getBounds, ask a.we why parseCssPosition is used, or rename getBounds to cssBounds
    return scout.graphics._bounds($elem, $elem.position(), includeSizeMargin, includePosMargin);
  },

  offsetBounds: function($elem, includeSizeMargin, includePosMargin) {
    return scout.graphics._bounds($elem, $elem.offset(), includeSizeMargin, includePosMargin);
  },

  _bounds: function($elem, pos, includeSizeMargin, includePosMargin) {
    if (includePosMargin) {
      pos.left += $elem.cssMarginLeft();
      pos.top += $elem.cssMarginTop();
    }
    var size = scout.graphics.getSize($elem, includeSizeMargin);
    return new scout.Rectangle(pos.left, pos.top, size.width, size.height);
  },

  debugOutput: function($comp) {
    if (!$comp) {
      return '$comp is undefined';
    }
    $comp = $.ensure($comp);
    if ($comp.length === 0) {
      return '$comp doesn\t match any elements';
    }
    var attrs = '';
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
      var html = scout.nvl($comp.html(), '');
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

//----------------------------------------------------------------------------

/**
 * JavaScript port from java.awt.Point.
 */
scout.Point = function(x, y) {
  this.x = x || 0;
  this.y = y || 0;
};

scout.Point.prototype.toString = function() {
  return 'Point[x=' + this.x + ' y=' + this.y + ']';
};

scout.Point.prototype.equals = function(o) {
  if (!o) {
    return false;
  }
  return (this.x === o.x && this.y === o.y);
};

scout.Point.prototype.clone = function(o) {
  return new scout.Point(this.x, this.y);
};

scout.Point.prototype.floor = function() {
  return new scout.Point(Math.floor(this.x), Math.floor(this.y));
};

scout.Point.prototype.ceil = function() {
  return new scout.Point(Math.ceil(this.x), Math.ceil(this.y));
};

//----------------------------------------------------------------------------

/**
 * JavaScript port from java.awt.Dimension.
 * @param vararg width (number) or otherDimension (scout.Dimension)
 * @param height number or undefined (when vararg is scout.Dimension)
 */
scout.Dimension = function(vararg, height) {
  if (vararg instanceof scout.Dimension) {
    this.width = vararg.width;
    this.height = vararg.height;
  } else {
    this.width = vararg || 0;
    this.height = height || 0;
  }
};

scout.Dimension.prototype.toString = function() {
  return 'Dimension[width=' + this.width + ' height=' + this.height + ']';
};

scout.Dimension.prototype.equals = function(o) {
  if (!o) {
    return false;
  }
  return (this.width === o.width && this.height === o.height);
};

scout.Dimension.prototype.clone = function() {
  return new scout.Dimension(this.width, this.height);
};

scout.Dimension.prototype.subtract = function(insets) {
  return new scout.Dimension(
    this.width - insets.horizontal(),
    this.height - insets.vertical());
};

scout.Dimension.prototype.add = function(insets) {
  return new scout.Dimension(
    this.width + insets.horizontal(),
    this.height + insets.vertical());
};

scout.Dimension.prototype.floor = function() {
  return new scout.Dimension(Math.floor(this.width), Math.floor(this.height));
};

scout.Dimension.prototype.ceil = function() {
  return new scout.Dimension(Math.ceil(this.width), Math.ceil(this.height));
};

//----------------------------------------------------------------------------

/**
 * JavaScript port from java.awt.Rectangle.
 */
scout.Rectangle = function(x, y, width, height) {
  this.x = x || 0;
  this.y = y || 0;
  this.width = width || 0;
  this.height = height || 0;
};

scout.Rectangle.prototype.toString = function() {
  return 'Rectangle[x=' + this.x + ' y=' + this.y + ' width=' + this.width + ' height=' + this.height + ']';
};

scout.Rectangle.prototype.equals = function(o) {
  if (!o) {
    return false;
  }
  return (this.x === o.x && this.y === o.y && this.width === o.width && this.height === o.height);
};

scout.Rectangle.prototype.clone = function(o) {
  return new scout.Rectangle(this.x, this.y, this.width, this.height);
};

scout.Rectangle.prototype.center = function() {
  return new scout.Point(this.x + this.width / 2, this.y + this.height / 2);
};

scout.Rectangle.prototype.contains = function(x, y) {
  return y >= this.y && y < this.y + this.height && x >= this.x && x < this.x + this.width;
};

scout.Rectangle.prototype.subtract = function(insets) {
  return new scout.Rectangle(
    this.x + insets.left,
    this.y + insets.top,
    this.width - insets.horizontal(),
    this.height - insets.vertical());
};

/**
 * @returns {scout.Point} property x and y of this instance as new Point instance
 */
scout.Rectangle.prototype.point = function() {
  return new scout.Point(this.x, this.y);
};

/**
 * @returns {scout.Dimension} property width and height of this instance as new Dimension instance
 */
scout.Rectangle.prototype.dimension = function() {
  return new scout.Dimension(this.width, this.height);
};

scout.Rectangle.prototype.union = function(r) {
  var tx2 = this.width;
  var ty2 = this.height;
  if (tx2 < 0 || ty2 < 0) {
    // This rectangle has negative dimensions...
    // If r has non-negative dimensions then it is the answer.
    // If r is non-existant (has a negative dimension), then both
    // are non-existant and we can return any non-existant rectangle
    // as an answer.  Thus, returning r meets that criterion.
    // Either way, r is our answer.
    return new scout.Rectangle(r.x, r.y, r.width, r.height);
  }
  var rx2 = r.width;
  var ry2 = r.height;
  if (rx2 < 0 || ry2 < 0) {
    return new scout.Rectangle(this.x, this.y, this.width, this.height);
  }
  var tx1 = this.x;
  var ty1 = this.y;
  tx2 += tx1;
  ty2 += ty1;
  var rx1 = r.x;
  var ry1 = r.y;
  rx2 += rx1;
  ry2 += ry1;
  if (tx1 > rx1) {
    tx1 = rx1;
  }
  if (ty1 > ry1) {
    ty1 = ry1;
  }
  if (tx2 < rx2) {
    tx2 = rx2;
  }
  if (ty2 < ry2) {
    ty2 = ry2;
  }
  tx2 -= tx1;
  ty2 -= ty1;
  // tx2,ty2 will never underflow since both original rectangles
  // were already proven to be non-empty
  // they might overflow, though...
  if (tx2 > Number.MAX_VALUE) {
    tx2 = Number.MAX_VALUE;
  }
  if (ty2 > Number.MAX_VALUE) {
    ty2 = Number.MAX_VALUE;
  }
  return new scout.Rectangle(tx1, ty1, tx2, ty2);
};

scout.Rectangle.prototype.floor = function() {
  return new scout.Rectangle(Math.floor(this.x), Math.floor(this.y), Math.floor(this.width), Math.floor(this.height));
};

scout.Rectangle.prototype.ceil = function() {
  return new scout.Rectangle(Math.ceil(this.x), Math.ceil(this.y), Math.ceil(this.width), Math.ceil(this.height));
};

// ----------------------------------------------------------------------------

/**
 * JavaScript port from java.awt.Insets.
 */
scout.Insets = function(top, right, bottom, left) {
  this.top = top || 0;
  this.right = right || 0;
  this.bottom = bottom || 0;
  this.left = left || 0;
};

scout.Insets.prototype.toString = function() {
  return 'Insets[top=' + this.top + ' right=' + this.right + ' bottom=' + this.bottom + ' left=' + this.left + ']';
};

scout.Insets.prototype.equals = function(o) {
  return this.top === o.top &&
    this.right === o.right &&
    this.bottom === o.bottom &&
    this.left === o.left;
};

scout.Insets.prototype.clone = function() {
  return new scout.Insets(this.top, this.right, this.bottom, this.left);
};

scout.Insets.prototype.horizontal = function() {
  return this.right + this.left;
};

scout.Insets.prototype.vertical = function() {
  return this.top + this.bottom;
};

scout.Insets.prototype.floor = function() {
  return new scout.Insets(Math.floor(this.top), Math.floor(this.right), Math.floor(this.bottom), Math.floor(this.left));
};

scout.Insets.prototype.ceil = function() {
  return new scout.Insets(Math.ceil(this.top), Math.ceil(this.right), Math.ceil(this.bottom), Math.ceil(this.left));
};

// ----------------------------------------------------------------------------

/**
 * HtmlEnvironment is used in place of org.eclipse.scout.rt.ui.swing.DefaultSwingEnvironment.
 */
scout.HtmlEnvironment = {
  // -------------------------------
  // IMPORTANT:
  // Some of the following constants are also defined in sizes.css. If you change
  // them, be sure to apply them at both places. (Remember to consider margins)
  // -------------------------------
  formRowHeight: 30, // @logical-grid-height
  formRowGap: 10,
  formColumnWidth: 420,
  formColumnGap: 32, // 40 pixel actual form gap - fieldMandatoryIndicatorWidth
  smallColumnGap: 4,
  fieldLabelWidth: 140,
  fieldMandatoryIndicatorWidth: 8, // @mandatory-indicator-width
  fieldStatusWidth: 20 // @field-status-width
};
