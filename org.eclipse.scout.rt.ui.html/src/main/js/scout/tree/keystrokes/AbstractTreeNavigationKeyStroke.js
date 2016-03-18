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
scout.AbstractTreeNavigationKeyStroke = function(tree, modifierBitMask) {
  scout.AbstractTreeNavigationKeyStroke.parent.call(this);
  this.field = tree;
  this.stopPropagation = true;
  this.renderingHints.hAlign = scout.hAlign.RIGHT;

  this.ctrl = scout.keyStrokeModifier.isCtrl(modifierBitMask);
  this.shift = scout.keyStrokeModifier.isShift(modifierBitMask);
  this.alt = scout.keyStrokeModifier.isAlt(modifierBitMask);

  this.keyStrokeMode = scout.keyStrokeMode.DOWN;
};
scout.inherits(scout.AbstractTreeNavigationKeyStroke, scout.KeyStroke);

scout.AbstractTreeNavigationKeyStroke.prototype._accept = function(event) {
  var accepted = scout.AbstractTreeNavigationKeyStroke.parent.prototype._accept.call(this, event);
  if (!accepted) {
    return false;
  }

  if (this.field.selectedNodes.length > 0) {
    event._treeCurrentNode = this.field.selectedNodes[0];
  }
  return true;
};

scout.AbstractTreeNavigationKeyStroke.prototype.handle = function(event) {
  var newNodeSelection = this._handleInternal(event._treeCurrentNode);
  if (newNodeSelection) {
    this.field.selectNodes(newNodeSelection);
    this.field.scrollTo(newNodeSelection);
  }
};

scout.AbstractTreeNavigationKeyStroke.prototype._handleInternal = function(currentNode) {
  throw new Error('method must be overwritten by subclass');
};
