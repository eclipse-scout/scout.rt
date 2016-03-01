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
scout.AbstractOutlineNavigationKeyStroke = function(tree) {
  scout.AbstractOutlineNavigationKeyStroke.parent.call(this);
  this.field = tree;
  this.stopPropagation = true;
  this.renderingHints.hAlign = scout.hAlign.RIGHT;

  this.ctrl = true;
  this.shift = true;

  this.keyStrokeMode = scout.keyStrokeMode.DOWN;
};
scout.inherits(scout.AbstractOutlineNavigationKeyStroke, scout.KeyStroke);

scout.AbstractOutlineNavigationKeyStroke.prototype._accept = function(event) {
  var accepted = scout.AbstractOutlineNavigationKeyStroke.parent.prototype._accept.call(this, event);
  if (!accepted) {
    return false;
  }

  var $currentNode = this.field.$selectedNodes().eq(0);
  event._$treeCurrentNode = $currentNode;
  event._treeCurrentNode = $currentNode.data('node');
  return true;
};

scout.AbstractOutlineNavigationKeyStroke.prototype.handle = function(event) {
  var newNodeSelection = this._handleInternal(event._$treeCurrentNode, event._treeCurrentNode);
  if (newNodeSelection) {
    this.field.selectNodes(newNodeSelection, true, true);
    this.field.scrollTo(newNodeSelection);
  }
};

scout.AbstractOutlineNavigationKeyStroke.prototype._handleInternal = function($currentNode, currentNode) {
  throw new Error('method must be overwritten by subclass');
};
