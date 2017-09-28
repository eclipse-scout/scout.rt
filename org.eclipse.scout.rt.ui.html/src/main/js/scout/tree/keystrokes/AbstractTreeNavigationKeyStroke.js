/*******************************************************************************
 * Copyright (c) 2014-2016 BSI Business Systems Integration AG.
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
  this.repeatable = true;
  this.stopPropagation = true;
  this.renderingHints.hAlign = scout.hAlign.RIGHT;

  this.ctrl = scout.keyStrokeModifier.isCtrl(modifierBitMask);
  this.shift = scout.keyStrokeModifier.isShift(modifierBitMask);
  this.alt = scout.keyStrokeModifier.isAlt(modifierBitMask);

  this.keyStrokeMode = scout.KeyStrokeMode.DOWN;
};
scout.inherits(scout.AbstractTreeNavigationKeyStroke, scout.KeyStroke);

scout.AbstractTreeNavigationKeyStroke.prototype._accept = function(event) {
  var accepted = scout.AbstractTreeNavigationKeyStroke.parent.prototype._accept.call(this, event);
  if (!accepted) {
    return false;
  }

  if (this.field.selectedNodes.length > 0) {
    event._treeCurrentNode = this.field.selectedNodes[0];
    event._$treeCurrentNode = event._treeCurrentNode.$node;
  }
  return true;
};

scout.AbstractTreeNavigationKeyStroke.prototype.handle = function(event) {
  var newSelection = this._computeNewSelection(event._treeCurrentNode);
  if (newSelection) {
    this.selectNodesAndReveal(newSelection, true);
  }
};

scout.AbstractTreeNavigationKeyStroke.prototype._computeNewSelection = function(currentNode) {
  return [];
};

scout.AbstractTreeNavigationKeyStroke.prototype.selectNodesAndReveal = function(newSelection, debounceSend) {
  this.field.selectNodes(newSelection, debounceSend);
  this.field.revealSelection();
};
