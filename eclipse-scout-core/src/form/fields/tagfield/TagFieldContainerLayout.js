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
import {AbstractLayout} from '../../../index';
import {HtmlComponent} from '../../../index';
import {Dimension} from '../../../index';

export default class TagFieldContainerLayout extends AbstractLayout {

constructor(tagField) {
  super();
  this.tagField = tagField;
}


/**
 * When there is not a lot of space in a single line field, the input field should at least
 * have 33% of the available width, which means 66% is used to display tags.
 */
static MIN_INPUT_TAG_RATIO = 0.33;

layout($container) {
  var htmlContainer = HtmlComponent.get($container);
  var hasTags = this.tagField.value && this.tagField.value.length > 0;
  var $input = this.tagField.$field;
  var htmlTagBar = this.tagField.tagBar.htmlComp;
  this.tagField.tagBar.setVisible(hasTags);

  if (hasTags) {
    $input.removeClass('fullwidth');
    var availableSize = htmlContainer.availableSize()
      .subtract(htmlContainer.insets());
    var maxTagBarWidth = availableSize.width;

    // when input field is not visible, tags may use the whole width, otherwise only a part of it
    if ($input.isVisible()) {
      maxTagBarWidth = availableSize.width * (1 - TagFieldContainerLayout.MIN_INPUT_TAG_RATIO);
    }

    var prefTagBarSize = htmlTagBar.prefSize();
    var tagBarWidth = Math.min(maxTagBarWidth, prefTagBarSize.width);
    htmlTagBar.setSize(new Dimension(tagBarWidth, availableSize.height));

    var inputWidth = availableSize.width - tagBarWidth;
    $input.cssWidth(inputWidth);
  } else {
    // remove style to delete previously set layout attributes
    $input
      .addClass('fullwidth')
      .removeAttr('style');
  }
}
}
