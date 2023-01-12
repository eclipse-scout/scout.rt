/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractLayout, Dimension, HtmlComponent, TagField} from '../../../index';

export class TagFieldContainerLayout extends AbstractLayout {
  tagField: TagField;

  constructor(tagField: TagField) {
    super();
    this.tagField = tagField;
  }

  /**
   * When there is not a lot of space in a single line field, the input field should at least
   * have 33% of the available width, which means 66% is used to display tags.
   */
  static MIN_INPUT_TAG_RATIO = 0.33;

  override layout($container: JQuery) {
    let htmlContainer = HtmlComponent.get($container);
    let hasTags = this.tagField.value && this.tagField.value.length > 0;
    let $input = this.tagField.$field;
    let htmlTagBar = this.tagField.tagBar.htmlComp;
    this.tagField.tagBar.setVisible(hasTags);

    if (hasTags) {
      $input.removeClass('fullwidth');
      let availableSize = htmlContainer.availableSize()
        .subtract(htmlContainer.insets());
      let maxTagBarWidth = availableSize.width;

      // when input field is not visible, tags may use the whole width, otherwise only a part of it
      if ($input.isVisible()) {
        maxTagBarWidth = availableSize.width * (1 - TagFieldContainerLayout.MIN_INPUT_TAG_RATIO);
      }

      let prefTagBarSize = htmlTagBar.prefSize(true);
      let tagBarWidth = Math.min(maxTagBarWidth, prefTagBarSize.width);
      htmlTagBar.setSize(new Dimension(tagBarWidth, prefTagBarSize.height).subtract(htmlTagBar.margins()));

      let inputWidth = availableSize.width - tagBarWidth;
      $input.cssWidth(inputWidth);
    } else {
      // remove style to delete previously set layout attributes
      $input
        .addClass('fullwidth')
        .removeAttr('style');
    }
  }
}
