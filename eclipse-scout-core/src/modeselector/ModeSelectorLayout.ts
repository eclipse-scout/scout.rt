/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {AbstractLayout, Dimension, HtmlCompPrefSizeOptions, ModeSelector} from '../index';

export default class ModeSelectorLayout extends AbstractLayout {
  modeSelector: ModeSelector;

  constructor(modeSelector: ModeSelector) {
    super();
    this.modeSelector = modeSelector;
  }

  override layout($container: JQuery) {
    super.layout($container);

    if (this.modeSelector.$slider) {
      // Hide slider here so that size changes triggered by the layout are not animated (css changes applied while an element is hidden are not animated).
      // The slider will then become visible again (if necessary) in _updateSlider()
      this.modeSelector.$slider.setVisible(false);
    }
    // @ts-ignore
    this.modeSelector._updateSlider();
  }

  override preferredLayoutSize($container: JQuery, options?: HtmlCompPrefSizeOptions): Dimension {
    let prefSize = super.preferredLayoutSize($container, options);

    let oldStyle = this.modeSelector.$container.attr('style');
    this.modeSelector.$container.css({
      'width': 'auto',
      'height': 'auto'
    });

    let maxWidth = 0;
    this.modeSelector.modes
      .filter(mode => mode.rendered)
      .forEach(mode => {
        let oldModeStyle = mode.$container.attr('style');
        mode.$container.css('flex', 'none');
        let modeWidth = mode.htmlComp.prefSize().width;
        if (modeWidth > maxWidth) {
          maxWidth = modeWidth;
        }
        mode.$container.attrOrRemove('style', oldModeStyle);
      });

    this.modeSelector.$container.attrOrRemove('style', oldStyle);

    prefSize.width = maxWidth * this.modeSelector.modes.length + this.modeSelector.htmlComp.insets().horizontal();
    return prefSize;
  }
}
