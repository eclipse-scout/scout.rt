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
scout.TagFieldLayout = function(tagField) {
  this.tagField = tagField;
};
scout.inherits(scout.TagFieldLayout, scout.AbstractLayout);


/**
 * When there is not a lot of space in a single line field, the input field should at least
 * have 33% of the available width, which means 66% is used to display tags.
 */
scout.TagFieldLayout.MIN_INPUT_TAG_RATIO = 0.33;

scout.TagFieldLayout.prototype.layout = function($container) {
  if (this.tagField.gridData.h > 1) {
    this._layoutMultiline($container);
  } else {
    this._layoutSingleLine($container);
  }
};

scout.TagFieldLayout.prototype._layoutMultiline = function($container) {

};

scout.TagFieldLayout.prototype._layoutSingleLine = function($container) {
  var htmlContainer = scout.HtmlComponent.get($container);
  var hasTags = this.tagField.value && this.tagField.value.length > 0;

  if (hasTags) {
    this.tagField.$field.removeClass('fullwidth');
    var availableSize = htmlContainer.availableSize()
      .subtract(htmlContainer.insets());
    var maxTagsWidth = availableSize.width * (1 - scout.TagFieldLayout.MIN_INPUT_TAG_RATIO);
    var prefTagsWidth = 0;
    var overflow = false;

    // 1. check if overflow occurs
    var $te, i;
    var $tagElements = $container.find('.tag-element');
    var numTagElements = $tagElements.length;
    var teSizes = [];
    $tagElements.removeClass('hidden');

    // use a for loop, because don't want to loop all elements when we already know the rest is in overflow
    for (i = numTagElements - 1; i >= 0; i--) {
      $te = $($tagElements[i]);
      teSizes[i] = scout.graphics.size($te, {includeMargin: true});
      overflow = (prefTagsWidth + teSizes[i].width) > maxTagsWidth;
      if (overflow) {
        break;
      }
      prefTagsWidth += teSizes[i].width;
    }

    // 2. add overflow icon
    this.tagField.setOverflowVisible(overflow);

    if (overflow) {
      prefTagsWidth = scout.graphics.size(this.tagField.$overflowIcon, {includeMargin: true}).width;
      for (i = numTagElements - 1; i >= 0; i--) {
        $te = $($tagElements[i]);

        // all elements with a greater index are hidden for sure
        var teSize = teSizes[i];
        if (!teSize) {
          $te.addClass('hidden');
          continue;
        }

        // we must re-check the rest again, because we have added the
        // overflow icon and thus we have less space for tags
        if ((prefTagsWidth + teSizes[i].width) > maxTagsWidth) {
          $te.addClass('hidden');
        } else {
          prefTagsWidth += teSizes[i].width;
        }
      }
    }

    var inputWidth = availableSize.width - prefTagsWidth;
    scout.graphics.setSize(this.tagField.$field, inputWidth, 'auto');

    if (this.tagField.modelClass === "org.eclipse.scout.widgets.client.ui.forms.TagFieldForm$MainBox$ExamplesBox$DefaultField") {
    console.log('aW=' + availableSize.width
        + ' mTW=' + maxTagsWidth
        + ' pTW=' + prefTagsWidth
        + ' iW=' + inputWidth);
    }
  } else {
    this.tagField.$field.addClass('fullwidth');
  }
};
