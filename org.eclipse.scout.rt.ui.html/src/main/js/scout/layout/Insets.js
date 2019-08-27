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
 * JavaScript port from java.awt.Insets.
 */
scout.Insets = function(vararg, right, bottom, left) {
  if (vararg instanceof scout.Insets) {
    this.top = vararg.top;
    this.right = vararg.right;
    this.bottom = vararg.bottom;
    this.left = vararg.left;
  } else {
    this.top = vararg || 0;
    this.right = right || 0;
    this.bottom = bottom || 0;
    this.left = left || 0;
  }
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
