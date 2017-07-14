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
scout.SearchOutlineLayout = function(outline) {
  scout.SearchOutlineLayout.parent.call(this, outline);
  this.outline = outline;
};
scout.inherits(scout.SearchOutlineLayout, scout.OutlineLayout);

scout.SearchOutlineLayout.prototype._setDataHeight = function(heightOffset) {
  // Add search panel height to heightOffset
  var searchPanelSize = scout.graphics.size(this.outline.$searchPanel, true);
  heightOffset += searchPanelSize.height;

  scout.SearchOutlineLayout.parent.prototype._setDataHeight.call(this, heightOffset);
};
