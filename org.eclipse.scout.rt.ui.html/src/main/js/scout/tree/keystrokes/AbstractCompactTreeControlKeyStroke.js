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
scout.AbstractCompactTreeControlKeyStroke = function(compactProcessTree) {
  scout.AbstractCompactTreeControlKeyStroke.parent.call(this);
  this.repeatable = true;
  this.field = compactProcessTree;
  this.keyStrokeMode = scout.KeyStrokeMode.DOWN;
};
scout.inherits(scout.AbstractCompactTreeControlKeyStroke, scout.KeyStroke);

scout.AbstractCompactTreeControlKeyStroke.prototype._accept = function(event) {
  var accepted = scout.AbstractCompactTreeControlKeyStroke.parent.prototype._accept.call(this, event);
  if (!accepted) {
    return false;
  }

  if (!this.field.nodes || !this.field.nodes.length) {
    return false;
  }

  var $currentNode = this.field.$nodesContainer.find('.section-node.selected'),
    currentNode = $currentNode.data('node');

  var nextNode = this._findNextNode($currentNode, currentNode);
  if (nextNode) {
    event._nextNode = nextNode;
    return true;
  } else {
    return false;
  }
};

scout.AbstractCompactTreeControlKeyStroke.prototype.handle = function(event) {
  this.field.selectNodes(event._nextNode);
  this.field.checkNode(event._nextNode, true);
};

scout.AbstractCompactTreeControlKeyStroke.prototype._findNextNode = function($currentNode, currentNode) {
  throw new Error('method must be overwritten by subclass');
};
