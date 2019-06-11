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
scout.TileButton = function() {
  scout.TileButton.parent.call(this);
  this.processButton = false;
};
scout.inherits(scout.TileButton, scout.Button);

scout.TileButton.prototype._render = function() {
  if (this.parent.displayStyle !== scout.FormFieldTile.DisplayStyle.DASHBOARD) {
    scout.TileButton.parent.prototype._render.call(this);
    return;
  }
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
  if (this.parent.displayStyle !== scout.FormFieldTile.DisplayStyle.DASHBOARD) {
    scout.TileButton.parent.prototype._renderIconId.call(this);
    return;
  }
  this.$field.removeClass('with-icon without-icon');
  if (this.iconId) {
    if (!this.$iconContainer) {
      this.$iconContainer = this.$field.prependDiv('icon-container');
    }
    this.$iconContainer.icon(this.iconId);
    this.$field.addClass('with-icon');
  } else {
    if (this.$iconContainer) {
      this.$iconContainer.remove();
      this.$iconContainer = null;
    }
    this.$field.addClass('without-icon');
  }
  // Invalidate layout because button may now be longer or shorter
  this.invalidateLayoutTree();
};

scout.TileButton.prototype._renderTooltipText = function() {
  // Because tile buttons don't have a visible status, display the tooltip text as normal "hover" tooltip
  if (scout.strings.hasText(this.tooltipText)) {
    scout.tooltips.install(this.$container, {
      parent: this,
      text: this.tooltipText
    });
  } else {
    scout.tooltips.uninstall(this.$container);
  }
};
