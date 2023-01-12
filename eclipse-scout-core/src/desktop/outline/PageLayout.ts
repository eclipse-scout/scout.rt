/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractLayout, Dimension, graphics, HtmlCompPrefSizeOptions, Outline, Page} from '../../index';

export class PageLayout extends AbstractLayout {
  outline: Outline;
  page: Page;

  constructor(outline: Outline, page: Page) {
    super();
    this.outline = outline;
    this.page = page;
  }

  override layout($container: JQuery) {
    let htmlContainer = this.page.htmlComp,
      $text = this.page.$text,
      $icon = this.page.$icon(),
      titleHeight = 0,
      iconHeight = 0,
      nodeMenuBar = this.outline.nodeMenuBar,
      detailMenuBar = this.outline.detailMenuBar,
      detailMenuBarHeight = 0,
      textWidth = 0;

    let containerSize = htmlContainer.availableSize({exact: true}) // exact is important to calculate text width correctly and to prevent node menubar from wrapping
      .subtract(htmlContainer.insets());
    textWidth = containerSize.width;

    if ($icon.length > 0) {
      textWidth -= graphics.prefSize($icon).width;
    }

    if (nodeMenuBar.visible) {
      let nodeMenuBarPrefSize = nodeMenuBar.htmlComp.prefSize();
      nodeMenuBar.htmlComp.setSize(nodeMenuBarPrefSize);
      textWidth -= nodeMenuBarPrefSize.add(nodeMenuBar.htmlComp.margins()).width;
    }

    $text.cssWidth(textWidth);

    if (detailMenuBar.visible) {
      detailMenuBarHeight = detailMenuBar.htmlComp.prefSize().height;
      let detailMenuBarSize = new Dimension(containerSize.width, detailMenuBarHeight)
        .subtract(detailMenuBar.htmlComp.margins());
      detailMenuBar.htmlComp.setSize(detailMenuBarSize);
    }

    if (this.outline.detailContent) {
      if ($icon.length > 0) {
        iconHeight = $icon.outerHeight(true);
      }
      if ($text.isVisible()) {
        titleHeight = $text.outerHeight(true);
      }
      titleHeight = Math.max(titleHeight, iconHeight);
      let htmlDetailContent = this.outline.detailContent.htmlComp;
      htmlDetailContent.setSize(new Dimension(containerSize.width, containerSize.height - titleHeight - detailMenuBarHeight - htmlDetailContent.margins().vertical()));
    }
  }

  override preferredLayoutSize($container: JQuery, options?: HtmlCompPrefSizeOptions): Dimension {
    let iconHeight = 0,
      htmlContainer = this.page.htmlComp,
      detailContentPrefSize = new Dimension(),
      $text = this.page.$text,
      $icon = this.page.$icon(),
      titlePrefHeight = 0,
      detailMenuBar = this.outline.detailMenuBar,
      detailMenuBarPrefSize = new Dimension(),
      nodeMenuBar = this.outline.nodeMenuBar,
      textWidth = 0;

    textWidth = options.widthHint;

    if ($icon.length > 0) {
      textWidth -= graphics.prefSize($icon).width;
    }

    if (nodeMenuBar.visible && nodeMenuBar.rendered) {
      textWidth -= nodeMenuBar.htmlComp.prefSize().width;
    }

    // needs a width to be able to calculate the pref height
    let textHeight = graphics.prefSize($text, {
      includeMargin: true,
      widthHint: textWidth,
      enforceSizeHints: true,
      exact: true
    }).height;

    if ($icon.length > 0) {
      iconHeight = $icon.outerHeight(true);
    }
    titlePrefHeight = Math.max(textHeight, iconHeight);

    if (detailMenuBar.visible && detailMenuBar.rendered) {
      detailMenuBarPrefSize = detailMenuBar.htmlComp.prefSize();
    }
    if (this.outline.detailContent) {
      let htmlDetailContent = this.outline.detailContent.htmlComp;
      options = $.extend({}, options, {enforceSizeHints: true, exact: true});
      detailContentPrefSize = htmlDetailContent.prefSize(options).add(htmlDetailContent.margins());
    }

    let prefSize = new Dimension(Math.max(detailContentPrefSize.width, detailMenuBarPrefSize.width), titlePrefHeight + detailMenuBarPrefSize.height + detailContentPrefSize.height);
    prefSize = prefSize.add(htmlContainer.insets());
    return prefSize;
  }
}
