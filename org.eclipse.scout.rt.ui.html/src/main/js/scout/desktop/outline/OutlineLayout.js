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
scout.OutlineLayout = function(outline) {
  scout.OutlineLayout.parent.call(this, outline);
  this.outline = outline;
};
scout.inherits(scout.OutlineLayout, scout.TreeLayout);

scout.OutlineLayout.prototype._setDataHeight = function(heightOffset) {
  // Add title height to heightOffset
  if (this.outline.titleVisible) {
    var titleSize = scout.graphics.getSize(this.outline.$title, true);
    heightOffset += titleSize.height;
  }

  scout.OutlineLayout.parent.prototype._setDataHeight.call(this, heightOffset);
};
