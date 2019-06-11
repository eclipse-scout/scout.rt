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
scout.Mode = function() {
  scout.Mode.parent.call(this);

  this.selected = false;
  this.ref = null; // Arbitrary reference value, can be used to find and select modes (see ModeSelector.js)
};
scout.inherits(scout.Mode, scout.Action);

scout.Mode.prototype._init = function(model) {
  model.owner = model.parent;
  scout.Mode.parent.prototype._init.call(this, model);
};

scout.Mode.prototype._render = function() {
  scout.Mode.parent.prototype._render.call(this);
  this.$container.addClass('mode');
};

scout.Mode.prototype._renderProperties = function() {
  scout.Mode.parent.prototype._renderProperties.call(this);
  this._renderSelected();
};

scout.Mode.prototype.setSelected = function(selected) {
  this.setProperty('selected', selected);
};

scout.Mode.prototype._renderSelected = function() {
  this.$container.select(this.selected);
};

/**
 * @Override Action.js
 */
scout.Mode.prototype.doAction = function() {
  if (!this.prepareDoAction()) {
    return false;
  }

  if (!this.selected) {
    this.setSelected(true);
  }

  return true;
};

/**
 * @Override Action.js
 */
scout.Mode.prototype.toggle = function() {
  if (!this.selected) {
    this.setSelected(true);
  }
};

scout.Mode.prototype._renderIconId = function() {
  scout.Mode.parent.prototype._renderIconId.call(this);

  this._updateLabelAndIconStyle();
  // Invalidate layout because mode may now be longer or shorter
  this.invalidateLayoutTree();
};

scout.Mode.prototype._renderText = function() {
  scout.Mode.parent.prototype._renderText.call(this);

  this._updateLabelAndIconStyle();
  // Invalidate layout because mode may now be longer or shorter
  this.invalidateLayoutTree();
};

scout.Mode.prototype._updateLabelAndIconStyle = function() {
  var hasText = !!this.text;
  this.get$Icon().toggleClass('with-label', hasText);
};
