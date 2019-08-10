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
scout.ModeSelectorLayout = function(modeSelector) {
  scout.ModeSelectorLayout.parent.call(this);
  this.modeSelector = modeSelector;
};
scout.inherits(scout.ModeSelectorLayout, scout.AbstractLayout);

scout.ModeSelectorLayout.prototype.preferredLayoutSize = function($container, options) {
  var prefSize = scout.ModeSelectorLayout.parent.prototype.preferredLayoutSize.call(this, $container, options);

  var oldStyle = this.modeSelector.$container.attr('style');
  this.modeSelector.$container.css({
    'width': 'auto',
    'height': 'auto'
  });

  var maxWidth = 0;
  this.modeSelector.modes.forEach(function(mode) {
    var modeWidth = mode.htmlComp.prefSize().width;
    if (modeWidth > maxWidth) {
      maxWidth = modeWidth;
    }
  });

  this.modeSelector.$container.attrOrRemove('style', oldStyle);

  prefSize.width = maxWidth * this.modeSelector.modes.length + this.modeSelector.htmlComp.insets().horizontal();
  return prefSize;
};
