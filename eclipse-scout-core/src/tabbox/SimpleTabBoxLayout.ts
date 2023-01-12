/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractLayout, Dimension, HtmlComponent, HtmlCompPrefSizeOptions, SimpleTabBox} from '../index';

export class SimpleTabBoxLayout extends AbstractLayout {
  tabBox: SimpleTabBox;

  constructor(tabBox: SimpleTabBox) {
    super();
    this.tabBox = tabBox;
  }

  override layout($container: JQuery) {
    let htmlContainer = HtmlComponent.get($container),
      htmlViewContent = HtmlComponent.get(this.tabBox.$viewContent);

    let containerSize = htmlContainer.availableSize({exact: true}).subtract(htmlContainer.insets());
    let tabAreaSize = this._layoutTabArea(containerSize);
    let viewContentSize = containerSize.subtract(htmlViewContent.margins());
    viewContentSize.height -= tabAreaSize.height;
    htmlViewContent.setSize(viewContentSize);
  }

  /**
   * @returns used of the tab area
   */
  protected _layoutTabArea(containerSize: Dimension): Dimension {
    if (!this.tabBox.rendered) {
      return new Dimension(0, 0);
    }
    // expected the tab area is layouted dynamically only
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
  override preferredLayoutSize($container: JQuery, options?: HtmlCompPrefSizeOptions): Dimension {
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
