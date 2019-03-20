/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
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
  this._addPreserveOnPropertyChangeProperties(['outline']);
  this._addCloneProperties(['outline']);
};
scout.inherits(scout.OutlineViewButton, scout.ViewButton);

scout.OutlineViewButton.prototype._init = function(model) {
  scout.OutlineViewButton.parent.prototype._init.call(this, model);
  this._setOutline(this.outline);
};

scout.OutlineViewButton.prototype._setOutline = function(outline) {
  this._setProperty('outline', outline);
  if (this.outline) {
    this.outline.setIconId(this.iconId);
  }
};

scout.OutlineViewButton.prototype._setIconId = function(iconId) {
  this._setProperty('iconId', iconId);
  if (this.outline) {
    this.outline.setIconId(this.iconId);
  }
};

/**
 * @override
 */
scout.OutlineViewButton.prototype._doAction = function() {
  scout.OutlineViewButton.parent.prototype._doAction.call(this);
  if (this.outline) {
    this.session.desktop.setOutline(this.outline);
    this.session.desktop.bringOutlineToFront();
  }
};

scout.OutlineViewButton.prototype.onOutlineChange = function(outline) {
  var selected = !!outline && this.outline === outline;
  this.setSelected(selected);
};
