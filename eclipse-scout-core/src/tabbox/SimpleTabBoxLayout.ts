/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractLayout, Dimension, HtmlComponent, HtmlCompPrefSizeOptions, Rectangle, SimpleTabBox} from '../index';

export class SimpleTabBoxLayout extends AbstractLayout {
  tabBox: SimpleTabBox;

  constructor(tabBox: SimpleTabBox) {
    super();
    this.tabBox = tabBox;
  }

  override layout($container: JQuery) {
    const htmlContainer = HtmlComponent.get($container);
    const containerSize = htmlContainer.availableSize({exact: true}).subtract(htmlContainer.insets());

    const htmlTabArea = HtmlComponent.get(this.tabBox.$tabArea);
    const htmlViewContent = HtmlComponent.get(this.tabBox.$viewContent);

    const tabAreaPosition = this.tabBox.tabArea.position;
    const tabAreaPrefSize = htmlTabArea.prefSize();

    let tabAreaBounds: Rectangle;
    let viewContentBounds: Rectangle;

    if (tabAreaPosition === 'top' || tabAreaPosition === 'bottom') {
      const tabAreaSize: Dimension = new Dimension(containerSize.width, tabAreaPrefSize.height);
      const viewContentSize: Dimension = new Dimension(containerSize.width, containerSize.height - tabAreaSize.height);
      if (tabAreaPosition === 'top') {
        tabAreaBounds = new Rectangle(0, 0, tabAreaSize.width, tabAreaSize.height);
        viewContentBounds = new Rectangle(0, tabAreaSize.height, viewContentSize.width, viewContentSize.height);
      } else if (tabAreaPosition === 'bottom') {
        tabAreaBounds = new Rectangle(0, viewContentSize.height, tabAreaSize.width, tabAreaSize.height);
        viewContentBounds = new Rectangle(0, 0, viewContentSize.width, viewContentSize.height);
      }
    } else if (tabAreaPosition === 'right' || tabAreaPosition === 'left') {
      const tabAreaSize: Dimension = new Dimension(tabAreaPrefSize.width, containerSize.height);
      const viewContentSize: Dimension = new Dimension(containerSize.width - tabAreaSize.width, containerSize.height);
      if (tabAreaPosition === 'right') {
        tabAreaBounds = new Rectangle(viewContentSize.width, 0, tabAreaSize.width, tabAreaSize.height);
        viewContentBounds = new Rectangle(0, 0, viewContentSize.width, viewContentSize.height);
      } else if (tabAreaPosition === 'left') {
        tabAreaBounds = new Rectangle(0, 0, tabAreaSize.width, tabAreaSize.height);
        viewContentBounds = new Rectangle(tabAreaSize.width, 0, viewContentSize.width, viewContentSize.height);
      }
    }

    htmlTabArea.setBounds(tabAreaBounds);
    htmlViewContent.setBounds(viewContentBounds);
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

    const tabAreaPosition = this.tabBox.tabArea.position;
    let prefWidth, prefHeight;
    if (tabAreaPosition === 'top' || tabAreaPosition === 'bottom') {
      prefWidth = Math.max(viewTabsSize.width, viewContentSize.width);
      prefHeight = viewContentSize.height + viewTabsSize.height;
    } else if (tabAreaPosition === 'right' || tabAreaPosition === 'left') {
      prefWidth = viewTabsSize.width + viewContentSize.width;
      prefHeight = Math.max(viewContentSize.height, viewTabsSize.height);
    }

    return new Dimension(prefWidth, prefHeight);
  }
}
