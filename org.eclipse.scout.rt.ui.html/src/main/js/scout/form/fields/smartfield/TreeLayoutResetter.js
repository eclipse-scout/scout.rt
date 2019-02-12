/*******************************************************************************
 * Copyright (c) 2014-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/

/**
 * This class is used to reset and restore styles in the DOM, so we can measure the preferred size of the tree.
 */
scout.TreeLayoutResetter = function(tree) {
  this._tree = tree;
  this.cssSelector = '.tree';
};

scout.TreeLayoutResetter.prototype.modifyDom = function() {
  this._ensureFirstLast();
  this._tree.$container
    .css('display', 'inline-block')
    .css('width', 'auto')
    .css('height', 'auto');
  this._tree.$data
    .css('display', 'inline-block');
};

scout.TreeLayoutResetter.prototype.restoreDom = function() {
  this._tree.$container
    .css('display', 'block')
    .css('width', '100%')
    .css('height', '100%');
  this._tree.$data
    .css('display', 'block');
};

scout.TreeLayoutResetter.prototype._ensureFirstLast = function() {
  var $nodes = this._tree.$data
    .children('.tree-node')
    .removeClass('first last');
  $nodes.first()
    .addClass('first');
  $nodes.last()
    .addClass('last');
};
