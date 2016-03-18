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
scout.TreeCollapseAllKeyStroke = function(tree, keyStrokeModifier) {
  scout.TreeCollapseAllKeyStroke.parent.call(this, tree, keyStrokeModifier);
  this.which = [scout.keys.HOME];
  this.renderingHints.hAlign = scout.hAlign.LEFT;
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    return this.field.$data;
  }.bind(this);
};
scout.inherits(scout.TreeCollapseAllKeyStroke, scout.AbstractTreeNavigationKeyStroke);

scout.TreeCollapseAllKeyStroke.prototype._handleInternal = function(currentNode) {
  this.field.collapseAll();
  if (this.field.nodes.length === 0) {
    return null;
  }
  return this.field.nodes[0];
};
