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
   * @memberOf scout.graphics
   */
  prefSize: function($elem, includeMargin, useCssSize) {
    // Return 0/0 if element is not displayed (display: none).
    // We don't use isVisible by purpose because isVisible returns false for elements with visibility: hidden which is wrong here (we would like to be able to measure hidden elements)
    if ($elem.isDisplayNone()) {
      return new scout.Dimension(0, 0);
    }

    var oldStyle = $elem.attr('style');
    var oldScrollLeft = $elem.scrollLeft();
    var oldScrollTop = $elem.scrollTop();

    // UseCssSize is necessary if the css rules have a fix height or width set.
    // Otherwise setting the width/height to auto could result in a different size
    var newWidth = (useCssSize ? '' : 'auto');
    var newHeight = (useCssSize ? '' : 'auto');

    // modify properties which prevent reading the preferred size
    $elem.css({
      'width': newWidth,
      'height': newHeight,
      'white-space': 'no-wrap'
    });

    var prefSize;
    // measure
    if (useCssSize) {
      prefSize = scout.graphics.getSize($elem, includeMargin);
    } else {
      prefSize = scout.graphics.getScrollSizes($elem, includeMargin);
    }

    // reset the modified style attribute
    $elem.attrOrRemove('style', oldStyle);
    $elem.scrollLeft(oldScrollLeft);
    $elem.scrollTop(oldScrollTop);

    return prefSize;
  },

  getScrollSizes: function($comp, includeMargin) {
    var marginWidth = 0,
      marginHeight = 0,
      scrollWidth = $comp[0] ? $comp[0].scrollWidth : 0,
      scrollHeight = $comp[0] ? $comp[0].scrollHeight : 0;
    includeMargin = scout.nvl(includeMargin, false);
    if (includeMargin) {
      marginHeight += parseFloat(scout.nvl($comp.css('margin-top'), 0)) + parseFloat(scout.nvl($comp.css('margin-bottom'), 0));
      marginWidth += parseFloat(scout.nvl($comp.css('margin-left'), 0)) + parseFloat(scout.nvl($comp.css('margin-right'), 0));
    }
    return new scout.Dimension(
      scrollWidth + marginWidth + parseFloat($comp.css("border-left-width")) + parseFloat($comp.css("border-right-width")),
      scrollHeight + marginHeight + parseFloat($comp.css("border-top-width")) + parseFloat($comp.css("border-bottom-width")));
  },

  /* These functions are designed to be used with box-sizing:box-model. The only reliable
   * way to set the size of a component when working with box model is to use css('width/height'...)
   * in favor of width/height() functions.
   */
  /**
   * Returns the size of the component, insets included.
   * @param includeMargin when set to true, returned dimensions include margins of component
   */
  getSize: function($comp, includeMargin) {
    includeMargin = scout.nvl(includeMargin, false);
    return new scout.Dimension(
      $comp.outerWidth(includeMargin),
      $comp.outerHeight(includeMargin));
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
    //FIXME cgu: merge with getBounds, ask awe why parseCssPosition is used, or rename getBounds to cssBounds
    return scout.graphics._bounds($elem, $elem.position(), includeSizeMargin, includePosMargin);
  },

  offsetBounds: function($elem, includeSizeMargin, includePosMargin) {
    return scout.graphics._bounds($elem, $elem.offset(), includeSizeMargin, includePosMargin);
  },

  _bounds: function($elem, pos, includeSizeMargin, includePosMargin) {
    if (includeSizeMargin === undefined) {
      includeSizeMargin = false;
    }
    if (includePosMargin === undefined) {
      includePosMargin = false;
    }
    if (includePosMargin) {
      pos.left += $elem.cssMarginLeft();
      pos.top += $elem.cssMarginTop();
    }
    return new scout.Rectangle(pos.left, pos.top, $elem.outerWidth(includeSizeMargin), $elem.outerHeight(includeSizeMargin));
  },

  debugOutput: function($comp) {
    if (!$comp) {
      return '$comp is undefined';
    }
    if (!($comp instanceof jQuery)) {
      $comp = $($comp);
    }
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
    return 'Element[' + attrs.trim() + ']';
  }
};

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

/**
 * JavaScript port from java.awt.Rectangle.
 */
scout.Rectangle = function(x, y, width, height) {
  this.x = x;
  this.y = y;
  this.width = width;
  this.height = height;
};

scout.Rectangle.prototype.equals = function(o) {
  if (!o) {
    return false;
  }
  return (this.x === o.x && this.y === o.y && this.width === o.width && this.height === o.height);
};

scout.Rectangle.prototype.toString = function() {
  return 'Rectangle[x=' + this.x + ' y=' + this.y + ' width=' + this.width + ' height=' + this.height + ']';
};

scout.Rectangle.prototype.contains = function(x, y) {
  return y >= this.y && y < this.y + this.height && x >= this.x && x < this.x + this.width;
};

scout.Rectangle.prototype.subtract = function(insets) {
  return new scout.Rectangle(
    this.x + insets.left,
    this.y + insets.top,
    this.width - insets.right,
    this.height - insets.bottom);
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

/**
 * JavaScript port from java.awt.Insets.
 */
scout.Insets = function(top, right, bottom, left) {
  this.top = top || 0;
  this.right = right || 0;
  this.bottom = bottom || 0;
  this.left = left || 0;
};

scout.Insets.prototype.equals = function(o) {
  return this.top === o.top &&
    this.right === o.right &&
    this.bottom === o.bottom &&
    this.left === o.left;
};

scout.Insets.prototype.horizontal = function() {
  return this.right + this.left;
};

scout.Insets.prototype.vertical = function() {
  return this.top + this.bottom;
};

scout.Insets.prototype.toString = function() {
  return 'Insets[top=' + this.top + ' right=' + this.right + ' bottom=' + this.bottom + ' left=' + this.left + ']';
};

/**
 * JavaScript port from java.util.TreeSet.
 */
scout.TreeSet = function() {
  this.array = [];
  this.properties = {};
};

scout.TreeSet.prototype.add = function(value) {
  if (!this.contains(value)) {
    this.array.push(value);
    this.array.sort();
    this.properties[value] = true;
  }
};

scout.TreeSet.prototype.size = function() {
  return this.array.length;
};

scout.TreeSet.prototype.contains = function(value) {
  return (value in this.properties);
};

scout.TreeSet.prototype.last = function() {
  return this.array[this.array.length - 1];
};

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
