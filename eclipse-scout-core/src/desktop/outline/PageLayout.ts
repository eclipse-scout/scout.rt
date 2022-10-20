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
import {AbstractLayout, Dimension, graphics} from '../../index';

export default class PageLayout extends AbstractLayout {

  constructor(outline, page) {
    super();
    this.outline = outline;
    this.page = page;
  }

  layout($container) {
    let containerSize, detailMenuBarSize,
      htmlContainer = this.page.htmlComp,
      $text = this.page.$text,
      $icon = this.page.$icon(),
      titleHeight = 0,
      iconHeight = 0,
      nodeMenuBar = this.outline.nodeMenuBar,
      nodeMenuBarPrefSize = 0,
      detailMenuBar = this.outline.detailMenuBar,
      detailMenuBarHeight = 0,
      textWidth = 0;

    containerSize = htmlContainer.availableSize({exact: true}) // exact is important to calculate text width correctly and to prevent node menubar from wrapping
      .subtract(htmlContainer.insets());
    textWidth = containerSize.width;

    if ($icon.length > 0) {
      textWidth -= graphics.prefSize($icon).width;
    }

    if (nodeMenuBar.visible) {
      nodeMenuBarPrefSize = nodeMenuBar.htmlComp.prefSize();
      nodeMenuBar.htmlComp.setSize(nodeMenuBarPrefSize);
      textWidth -= nodeMenuBarPrefSize.add(nodeMenuBar.htmlComp.margins()).width;
    }

    $text.cssWidth(textWidth);

    if (detailMenuBar.visible) {
      detailMenuBarHeight = detailMenuBar.htmlComp.prefSize().height;
      detailMenuBarSize = new Dimension(containerSize.width, detailMenuBarHeight)
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

  preferredLayoutSize($container, options) {
    let prefSize, textHeight,
      iconHeight = 0,
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
    textHeight = graphics.prefSize($text, {
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

    prefSize = new Dimension(Math.max(detailContentPrefSize.width, detailMenuBarPrefSize.width), titlePrefHeight + detailMenuBarPrefSize.height + detailContentPrefSize.height);
    prefSize = prefSize.add(htmlContainer.insets());
    return prefSize;
  }
}
