/**
* This file contains JavaScript ports from java.awt classes.
 */

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
  return this.x === o.x &&
    this.y === o.y;
};

/**
 * JavaScript port from java.awt.Dimension.
 */
scout.Dimension = function(width, height) {
  this.width = width || 0;
  this.height = height || 0;
};

scout.Dimension.prototype.toString = function() {
  return 'Dimension[width=' + this.width + ' height=' + this.height + ']';
};

scout.Dimension.prototype.equals = function(o) {
  return this.width === o.width &&
    this.height === o.height;
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
  'formRowHeight': 23,
  'formRowGap': 6,
  'formColumnWidth': 360,
  'formColumnGap': 12
};

scout.graphics = {
  'measureString': function(text) {
    var $div = $('#StringMeasurement');
    if ($div.length === 0) {
      throw 'DIV StringMeasurement does\'nt exist';
    }
    $div.html(text);
    return new scout.Dimension($div.width(), $div.height());
  }
};
