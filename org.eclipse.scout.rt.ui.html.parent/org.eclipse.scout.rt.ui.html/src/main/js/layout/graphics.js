/**
* This file contains helpers for graphical operations and JavaScript ports from java.awt classes
 */
scout.graphics = {
  measureString: function(text) {
    var $div = $('#StringMeasurement');
    if ($div.length === 0) {
      throw new Error('DIV StringMeasurement does\'nt exist');
    }
    $div.html(text);
    return new scout.Dimension($div.width(), $div.height());
  },
  /* These functions are designed to be used with box-sizing:box-model. The only reliable
   * way to set the size of a component when working with box model is to use css('width/height'...)
   * in favor of width/height() functions.
   */
  /**
   * Returns the current size of the component, insets included.
   * TODO AWE: (layout) prüfen ob hier tatsächlich die insets included sind. Müssten wir dann nicht outerWidth/-Height verwenden?
   */
  getSize: function($comp) {
    return new scout.Dimension(
        $comp.outerWidth(true),
        $comp.outerHeight(true));
  },
  setSize: function($comp, vararg, height) {
    var size = vararg instanceof scout.Dimension ?
        vararg : new scout.Dimension(vararg, height);
    $comp.
      cssWidth(size.width).
      cssHeight(size.height);
  },
  /**
   * Returns the size of a visible component or (0,0) when component is invisible.
   */
  getVisibleSize: function($comp) {
    if ($comp.length === 1 && $comp.isVisible()) {
      return scout.graphics.getSize($comp);
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
      includeMargin = options.includeMargin !== undefined ? options.includeMargin : true,
      includePadding = options.includePadding !== undefined ? options.includePadding : true,
      includeBorder = options.includeBorder !== undefined ? options.includeBorder : true,
      cssToInt = function(cssProp) {
        return parseInt($comp.css(cssProp), 10);
      };

    for (i = 0; i < directions.length; i++) {
      // parseInt will ignore 'px' in string returned from css() method
      if (includeMargin) {
        insets[i] += cssToInt('margin-' + directions[i]);
      }
      if (includePadding) {
        insets[i] += cssToInt('padding-' + directions[i]);
      }
      if (includeBorder) {
        insets[i] += cssToInt('border-' + directions[i] + '-width');
      }
    }
    return new scout.Insets(insets[0], insets[1], insets[2], insets[3]);
  },
  // TODO AWE: (unit-test) getBounds + auto
  getBounds: function($comp) {
    var parseCssPosition = function(prop) {
      var value = $comp.css(prop);
      return 'auto' === value ? 0 :  parseInt(value, 10);
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
    $comp.
      cssLeft(bounds.x).
      cssTop(bounds.y).
      cssWidth(bounds.width).
      cssHeight(bounds.height);
  },
  offsetBounds: function($elem, includeMargins) {
    if (includeMargins === undefined) {
      includeMargins = false;
    }
    var pos = $elem.offset();
    return new scout.Rectangle(pos.left, pos.top, $elem.outerWidth(includeMargins), $elem.outerHeight(includeMargins));
  },
  debugOutput: function($comp) {
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

  return this.x === o.x &&
    this.y === o.y;
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

  return this.width === o.width &&
    this.height === o.height;
};

scout.Dimension.prototype.subtractInsets = function(insets) {
  return new scout.Dimension(
      this.width - insets.left - insets.right,
      this.height - insets.top - insets.bottom);
};

scout.Dimension.prototype.addInsets = function(insets) {
  return new scout.Dimension(
      this.width + insets.left + insets.right,
      this.height + insets.top + insets.bottom);
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
  return this.x === o.x &&
    this.y === o.y &&
    this.width === o.width &&
    this.height === o.height;
};

scout.Rectangle.prototype.toString = function() {
 return 'Rectangle[x=' + this.x + ' y=' + this.y + ' width=' + this.width + ' height=' + this.height + ']';
};

scout.Rectangle.prototype.union = function(r) {
  var tx2 = this.width;
  var ty2 = this.height;
  if ((tx2 | ty2) < 0) {
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
  if ((rx2 | ry2) < 0) {
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
  if (tx1 > rx1) tx1 = rx1;
  if (ty1 > ry1) ty1 = ry1;
  if (tx2 < rx2) tx2 = rx2;
  if (ty2 < ry2) ty2 = ry2;
  tx2 -= tx1;
  ty2 -= ty1;
  // tx2,ty2 will never underflow since both original rectangles
  // were already proven to be non-empty
  // they might overflow, though...
  if (tx2 > Number.MAX_VALUE) tx2 = Number.MAX_VALUE;
  if (ty2 > Number.MAX_VALUE) ty2 = Number.MAX_VALUE;
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
  'formRowHeight': 30,
  'formRowGap': 10,
  'formColumnWidth': 360,
  'formColumnGap': 50,
  'fieldLabelWidth': 140
};
