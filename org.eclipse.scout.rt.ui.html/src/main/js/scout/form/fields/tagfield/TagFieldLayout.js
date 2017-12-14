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
 * have 25% of the available width, which means 75% is used to display tags.
 */
scout.TagFieldLayout.MIN_INPUT_TAG_RATIO = 0.25;

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

    $container.find('.tag-element').each(function() {
      var $tagElement = $(this);
      $tagElement.removeClass('hidden');

      if (!overflow) {
        prefTagsWidth += scout.graphics.size($tagElement).width;
        overflow = prefTagsWidth > maxTagsWidth;
      }

      if (overflow) {
        $tagElement.addClass('hidden');
      }
    });


    var inputWidth = availableSize.width - prefTagsWidth;
    this.tagField.setOverflowVisible(overflow); // FIXME [awe] must also subtract size from overflow icon

    scout.graphics.setSize(this.tagField.$field, inputWidth, 'auto');

    console.log('aW=' + availableSize.width
        + ' pTW=' + prefTagsWidth
        + ' iW=' + inputWidth);
  } else {
    this.tagField.$field.addClass('fullwidth');
  }
};
