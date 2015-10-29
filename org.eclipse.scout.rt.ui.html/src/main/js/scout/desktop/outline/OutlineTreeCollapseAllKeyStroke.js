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
scout.OutlineTreeCollapseAllKeyStroke = function(tree) {
  scout.OutlineTreeCollapseAllKeyStroke.parent.call(this, tree);
  this.which = [scout.keys.HOME];
  this.renderingHints.hAlign = scout.hAlign.LEFT;
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    return this.field.$title || this.field.$data;
  }.bind(this);
};
scout.inherits(scout.OutlineTreeCollapseAllKeyStroke, scout.AbstractOutlineTreeNavigationKeyStroke);

scout.OutlineTreeCollapseAllKeyStroke.prototype._handleInternal = function($currentNode, currentNode) {
  this.field.collapseAll();
  return this.field.$nodes().first().data('node');
};

scout.OutlineTreeCollapseAllKeyStroke.prototype._accept = function(event) {
  return scout.TreeCollapseAllKeyStroke.parent.prototype._accept.call(this, event);
};
