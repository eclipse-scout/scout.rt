/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractLayout, Dimension, HtmlCompPrefSizeOptions, ModeSelector} from '../index';

export class ModeSelectorLayout extends AbstractLayout {
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
