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
scout.ImageLayout = function(image) {
  scout.ImageLayout.parent.call(this);
  this.image = image;
};
scout.inherits(scout.ImageLayout, scout.AbstractLayout);

scout.ImageLayout.prototype.preferredLayoutSize = function($container) {
  var img = $container[0];
  if (img && img.complete && img.naturalWidth > 0 && img.naturalHeight > 0) {
    return new scout.Dimension(img.naturalWidth, img.naturalHeight);
  }
  return scout.ImageLayout.parent.prototype.preferredLayoutSize.call(this, $container);
};
