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
scout.OutlineTileField = function() {
  scout.OutlineTileField.parent.call(this);
  this.tileOutlineOverview = null;
};
scout.inherits(scout.OutlineTileField, scout.FormField);

scout.OutlineTileField.prototype._init = function(model) {
  scout.OutlineTileField.parent.prototype._init.call(this, model);


};

scout.OutlineTileField.prototype._render = function() {
  this.addContainer(this.$parent, 'outline-tile-field');
  // FIXME [awe] imex - check if we can do this in _init (desktop is not ready at this point in time)
  this.tileOutlineOverview = scout.create('TileOutlineOverview', {
    parent: this,
    outline: this.session.desktop.outline
  });

  this.tileOutlineOverview.render(this.$container);
  this.addField(this.tileOutlineOverview.$container);
};
