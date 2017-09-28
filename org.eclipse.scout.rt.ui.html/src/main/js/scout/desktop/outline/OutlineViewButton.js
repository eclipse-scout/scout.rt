/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.OutlineViewButton = function() {
  scout.OutlineViewButton.parent.call(this);
  this._addWidgetProperties('outline');
};
scout.inherits(scout.OutlineViewButton, scout.ViewButton);

/**
 * @override
 */
scout.OutlineViewButton.prototype._doAction = function() {
  scout.OutlineViewButton.parent.prototype._doAction.call(this);
  if (this.outline) {
    this.session.desktop.setOutline(this.outline);
    this.session.desktop.bringOutlineToFront(this.outline);
  }
};

scout.OutlineViewButton.prototype.onOutlineChange = function(outline) {
  var selected = !!outline && this.outline === outline;
  this.setSelected(selected);
};
