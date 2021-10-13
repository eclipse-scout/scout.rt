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
import {AbstractLayout, Dimension, graphics, HtmlComponent} from '../index';
import $ from 'jquery';

export default class TagBarLayout extends AbstractLayout {

  constructor(tagBar) {
    super();
    this.tagBar = tagBar;
  }

  layout($container) {
    let htmlContainer = HtmlComponent.get($container);
    let hasTags = this.tagBar.tags && this.tagBar.tags.length > 0;

    if (hasTags) {
      let availableSize = htmlContainer.availableSize()
        .subtract(htmlContainer.insets());
      let maxTagsWidth = availableSize.width;
      let prefTagsWidth = 0;
      let overflow = false;

      // 1. check if overflow occurs
      let $te, i;
      let $tagElements = $container.find('.tag-element');
      let numTagElements = $tagElements.length;
      let teSizes = [];
      $tagElements.removeClass('hidden');

      // use a for loop, because don't want to loop all elements when we already know the rest is in overflow
      for (i = numTagElements - 1; i >= 0; i--) {
        $te = $($tagElements[i]);
        teSizes[i] = graphics.size($te, {
          includeMargin: true
        });
        overflow = (prefTagsWidth + teSizes[i].width) > maxTagsWidth;
        if (overflow) {
          break;
        }
        prefTagsWidth += teSizes[i].width;
      }

      // 2. add overflow icon
      this.tagBar.setOverflowVisible(overflow);

      if (overflow) {
        prefTagsWidth = graphics.size(this.tagBar.$overflowIcon, {
          includeMargin: true
        }).width;
        for (i = numTagElements - 1; i >= 0; i--) {
          $te = $($tagElements[i]);

          // all elements with a greater index are hidden for sure
          let teSize = teSizes[i];
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
    }
  }

  preferredLayoutSize($container, options) {
    let htmlContainer = HtmlComponent.get($container);
    let hasTags = this.tagBar.tags && this.tagBar.tags.length > 0;
    let availableSize = htmlContainer.availableSize();
    let prefTagsWidth = 0;
    let prefTagsHeight = 0;

    if (!hasTags) {
      return new Dimension(0, availableSize.height);
    }

    let $tagElements = $container.find('.tag-element');
    $tagElements.removeClass('hidden');
    $tagElements.each((i, elem) => {
      let size = graphics.size($(elem), {
        includeMargin: true
      });
      prefTagsWidth += size.width;
      prefTagsHeight = Math.max(size.height, prefTagsHeight);
    });
    return new Dimension(prefTagsWidth, prefTagsHeight).add(htmlContainer.insets());
  }
}
