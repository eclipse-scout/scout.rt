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
scout.PlaceholderTile = function() {
  scout.PlaceholderTile.parent.call(this);
  this.cssClass = 'placeholder-tile';
  this.displayStyle = scout.Tile.DisplayStyle.PLAIN;
};
scout.inherits(scout.PlaceholderTile, scout.Tile);

scout.PlaceholderTile.prototype._setSelectable = function(selectable) {
  // Placeholder tiles should not be selectable
  scout.PlaceholderTile.parent.prototype._setSelectable.call(this, false);
};
