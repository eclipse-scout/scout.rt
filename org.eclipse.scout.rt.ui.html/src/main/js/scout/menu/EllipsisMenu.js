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
scout.EllipsisMenu = function() {
  scout.EllipsisMenu.parent.call(this);
  this.hidden = true;
  this.ellipsis = true;
  this.stackable = false;
  this.horizontalAlignment = 1;
  this.iconId = scout.icons.ELLIPSIS_V;
  this.inheritAccessibility = false;
  this.tabbable = false;
  this.rightAligned = false;
  this._addPreserveOnPropertyChangeProperties(['childActions']);
};
scout.inherits(scout.EllipsisMenu, scout.Menu);

scout.EllipsisMenu.prototype._render = function() {
  scout.EllipsisMenu.parent.prototype._render.call(this);
  this.$container.addClass('ellipsis');
};

scout.EllipsisMenu.prototype.setChildActions = function(childActions) {
  scout.EllipsisMenu.parent.prototype.setChildActions.call(this, childActions);

  if (childActions) {
    // close all actions that have been added to the ellipsis
    childActions.forEach(function(ca) {
      ca.setSelected(false);
    });
  }
};

scout.EllipsisMenu.prototype._renderProperties = function() {
  scout.EllipsisMenu.parent.prototype._renderProperties.call(this);
  this._renderHidden();
};

// add the set hidden function to the ellipsis
scout.EllipsisMenu.prototype.setHidden = function(hidden) {
  this.setProperty('hidden', hidden);
};

scout.EllipsisMenu.prototype._renderHidden = function() {
  this.$container.setVisible(!this.hidden);
};

scout.EllipsisMenu.prototype.isTabTarget = function() {
  return scout.Menu.prototype.isTabTarget.call(this) && !this.hidden;
};
