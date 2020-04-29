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
import {AbstractLayout, Dimension, HtmlComponent} from '../index';

export default class SimpleTabBoxLayout extends AbstractLayout {

  constructor(tabBox) {
    super();
    this.tabBox = tabBox;
  }

  layout($container) {
    let containerSize, viewContentSize,
      htmlContainer = HtmlComponent.get($container),
      htmlViewContent = HtmlComponent.get(this.tabBox.$viewContent),
      tabAreaSize;

    containerSize = htmlContainer.availableSize({
      exact: true
    })
      .subtract(htmlContainer.insets());

    tabAreaSize = this._layoutTabArea(containerSize);

    viewContentSize = containerSize.subtract(htmlViewContent.margins());
    viewContentSize.height -= tabAreaSize.height;
    htmlViewContent.setSize(viewContentSize);
  }

  /**
   * @param containerSize
   * @returns {Dimension} used of the tab area
   */
  _layoutTabArea(containerSize) {
    if (!this.tabBox.rendered) {
      return new Dimension(0, 0);
    }
    // exprected the tab area is layouted dynamically only
    let htmlViewTabs = HtmlComponent.get(this.tabBox.$tabArea),
      prefSize = htmlViewTabs.prefSize(),
      margins = htmlViewTabs.margins();
    let size = new Dimension(containerSize.width, prefSize.height + margins.top + margins.bottom);
    htmlViewTabs.setSize(size);
    return size;
  }

  /**
   * Preferred size of the tab-box aligns every tab-item in a single line, so that each item is visible.
   */
  preferredLayoutSize($container, options) {
    options = options || {};
    let htmlContainer = HtmlComponent.get($container),
      htmlViewContent = HtmlComponent.get(this.tabBox.$viewContent),
      htmlViewTabs = HtmlComponent.get(this.tabBox.$tabArea),
      viewTabsSize = new Dimension(),
      viewContentSize = new Dimension();

    // HeightHint not supported
    options.heightHint = null;

    if (htmlViewTabs.isVisible()) {
      viewTabsSize = htmlViewTabs.prefSize()
        .add(htmlViewTabs.margins());
    }

    viewContentSize = htmlViewContent.prefSize(options)
      .add(htmlContainer.insets())
      .add(htmlViewContent.margins());

    return new Dimension(
      Math.max(viewTabsSize.width, viewContentSize.width),
      viewContentSize.height + viewTabsSize.height);
  }
}
