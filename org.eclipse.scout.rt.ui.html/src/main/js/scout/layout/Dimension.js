/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */

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
