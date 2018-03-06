/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSI CRM Software License v1.0
 * which accompanies this distribution as bsi-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.PageTileButton = function() {
  scout.PageTileButton.parent.call(this);
  this.page = null;
  var dim = scout.TileGridLayoutConfig.getTileDimensions();
  this.gridDataHints.heightInPixel = dim.height;
  this.gridDataHints.widthInPixel = dim.width;
};
scout.inherits(scout.PageTileButton, scout.TileButton);

scout.PageTileButton.prototype._init = function(model) {
  scout.PageTileButton.parent.prototype._init.call(this, model);

  this.label = this.page.text;
  this.iconId = this.page.overviewIconId;

  this.on('click', function(event) {
    this.outline.selectNode(this.page);
  }.bind(this));
};

scout.PageTileButton.prototype.notifyPageChanged = function() {
  this.setLabel(this.page.text);
  this.setIconId(this.page.overviewIconId);
};
