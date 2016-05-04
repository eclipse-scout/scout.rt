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
scout.SplitBoxCollapseKeyStroke = function(splitBox, keyStroke) {
  scout.SplitBoxCollapseKeyStroke.parent.call(this);
  this.field = splitBox;
  this.parseAndSetKeyStroke(keyStroke);
};
scout.inherits(scout.SplitBoxCollapseKeyStroke, scout.KeyStroke);

scout.SplitBoxCollapseKeyStroke.prototype.handle = function(event) {
  this.field.toggleFieldCollapsed();
};

scout.SplitBoxCollapseKeyStroke.prototype._postRenderKeyBox = function($drawingArea, $keyBox) {
  var handleOffset,
    $collapseHandle = this.field._collapseHandle.$container;

  $keyBox.addClass('split-box-collapse-key-box');
  handleOffset = $collapseHandle.offsetTo(this.field.$container);
  $keyBox
    .cssLeft(handleOffset.left - $keyBox.outerWidth())
    .cssTop(handleOffset.top);
};
