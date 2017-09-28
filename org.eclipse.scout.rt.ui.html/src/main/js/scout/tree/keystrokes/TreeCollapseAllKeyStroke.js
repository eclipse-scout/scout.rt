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
scout.TreeCollapseAllKeyStroke = function(tree, keyStrokeModifier) {
  scout.TreeCollapseAllKeyStroke.parent.call(this, tree, keyStrokeModifier);
  this.which = [scout.keys.HOME];
  this.renderingHints.hAlign = scout.hAlign.RIGHT;
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    if (this.field.visibleNodesFlat.length > 0) {
      return this.field.visibleNodesFlat[0].$node;
    }
  }.bind(this);
};
scout.inherits(scout.TreeCollapseAllKeyStroke, scout.AbstractTreeNavigationKeyStroke);

scout.TreeCollapseAllKeyStroke.prototype.handle = function(event) {
  this.field.collapseAll();
  if (this.field.visibleNodesFlat.length > 0) {
    this.selectNodesAndReveal(this.field.visibleNodesFlat[0]);
  }
};
