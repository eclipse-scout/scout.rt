/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
scout.OutlineTileField = function() {
  scout.OutlineTileField.parent.call(this);

  // outline is not a widget, because at the time where _init runs, outline is not yet available
  this.outline = null;
  this.tileOutlineOverview = null;
};
scout.inherits(scout.OutlineTileField, scout.FormField);

scout.OutlineTileField.prototype._render = function() {
  this._ensureTileOutlineOverview();
  this.addContainer(this.$parent, 'outline-tile-field');
  this.tileOutlineOverview.render(this.$container);
  this.addField(this.tileOutlineOverview.$container);
};

/**
 * We cannot create the TileOutlineOverview instance in the _init function, because at the time where _init runs, outlines are not yet available.
 */
scout.OutlineTileField.prototype._ensureTileOutlineOverview = function() {
  if (this.tileOutlineOverview) {
    return;
  }
  var outline = this.outline ? this.session.getWidget(this.outline) : null;
  this.tileOutlineOverview = scout.create('TileOutlineOverview', {
    parent: this,
    outline: outline
  });
};
