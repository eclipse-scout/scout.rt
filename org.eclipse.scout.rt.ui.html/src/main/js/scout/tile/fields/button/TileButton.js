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
scout.TileButton = function() {
  scout.TileButton.parent.call(this);
  this.processButton = false;
};
scout.inherits(scout.TileButton, scout.Button);

scout.TileButton.prototype._render = function() {
  var $button = this.$parent.makeDiv();
  this.$buttonLabel = $button.appendSpan('label');

  this.addContainer(this.$parent, 'tile-button');
  this.addField($button);

  // Disable inner form field layout, because the tile button should always occupy
  // the entire container area. This is achieved by using CSS "fixed table layout".
  // (If we would set the size of the inner field manually, the CSS rendering would
  // not be able to correctly determine the table's width.)
  this.htmlComp.setLayout(new scout.NullLayout());

  this.$container
    .on('click', this._onClick.bind(this))
    .unfocusable();
};

scout.TileButton.prototype._remove = function() {
  this.$iconContainer = null;
  scout.TileButton.parent.prototype._remove.call(this);
};

scout.TileButton.prototype._renderIconId = function() {
  if (this.iconId) {
    if (!this.$iconContainer) {
      this.$iconContainer = this.$field.prependDiv('icon-container');
    }
    this.$iconContainer.icon(this.iconId);
  } else {
    if (this.$iconContainer) {
      this.$iconContainer.remove();
      this.$iconContainer = null;
    }
  }
  // Invalidate layout because button may now be longer or shorter
  this.htmlComp.invalidateLayoutTree();
};
