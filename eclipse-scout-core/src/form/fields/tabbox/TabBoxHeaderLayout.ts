/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractLayout, Dimension, EventHandler, FormField, graphics, HtmlComponent, HtmlCompPrefSizeOptions, HtmlEnvironment, MenuBarLayout, Rectangle, TabAreaLayout, TabBoxHeader} from '../../../index';

export class TabBoxHeaderLayout extends AbstractLayout {
  tabBoxHeader: TabBoxHeader;
  htmlPropertyChangeHandler: EventHandler;
  fieldStatusWidth: number;

  constructor(tabBoxHeader: TabBoxHeader) {
    super();
    this.tabBoxHeader = tabBoxHeader;
    this.fieldStatusWidth = null;

    this._initDefaults();

    this.htmlPropertyChangeHandler = this._onHtmlEnvironmentPropertyChange.bind(this);
    HtmlEnvironment.get().on('propertyChange', this.htmlPropertyChangeHandler);
    this.tabBoxHeader.one('remove', () => {
      HtmlEnvironment.get().off('propertyChange', this.htmlPropertyChangeHandler);
    });
  }

  protected _initDefaults() {
    this.fieldStatusWidth = HtmlEnvironment.get().fieldStatusWidth;
  }

  protected _onHtmlEnvironmentPropertyChange() {
    this._initDefaults();
    this.tabBoxHeader.invalidateLayoutTree();
  }

  override layout($container: JQuery) {
    let htmlContainer = HtmlComponent.get($container),
      tabArea = this.tabBoxHeader.tabArea,
      tabAreaMargins = tabArea.htmlComp.margins(),
      tabAreaPrefSize,
      menuBar = this.tabBoxHeader.menuBar,
      menuBarMargins = menuBar.htmlComp.margins(),
      menuBarMinimumSize,
      $status = this.tabBoxHeader.tabBox.$status,
      statusSizeLarge = new Dimension(),
      insets = htmlContainer.insets(),
      containerSize = htmlContainer.availableSize({
        exact: true
      }).subtract(htmlContainer.insets());

    menuBarMinimumSize = menuBar.htmlComp.prefSize({
      widthHint: 0
    });

    if (this.tabBoxHeader.tabBox.statusPosition === FormField.StatusPosition.TOP && $status && $status.isVisible()) {
      statusSizeLarge.height = $status.outerHeight(true);
      statusSizeLarge.width = this.fieldStatusWidth + graphics.margins($status).horizontal();
    }

    tabAreaPrefSize = tabArea.htmlComp.prefSize({
      widthHint: containerSize.width - menuBarMinimumSize.width - menuBarMargins.horizontal() - statusSizeLarge.width,
      exact: false
    });

    // layout tabArea
    tabArea.htmlComp.setSize(new Dimension(
      tabAreaPrefSize.width,
      containerSize.height - tabAreaMargins.vertical()
    ));

    (menuBar.htmlComp.layout as MenuBarLayout).collapsed = (tabArea.htmlComp.layout as TabAreaLayout).overflowTabs.length > 0;
    // layout menuBar
    menuBar.htmlComp.setBounds(new Rectangle(
      insets.left + tabAreaPrefSize.width + tabAreaMargins.horizontal(),
      insets.top,
      containerSize.width - tabAreaPrefSize.width - tabAreaMargins.horizontal() - menuBarMargins.horizontal() - statusSizeLarge.width,
      containerSize.height - menuBarMargins.vertical()
    ));

    // layout status
    if (this.tabBoxHeader.tabBox.statusPosition === FormField.StatusPosition.TOP && $status && $status.isVisible()) {
      $status.cssWidth(this.fieldStatusWidth)
        .cssRight(insets.right)
        .cssHeight(containerSize.height - graphics.margins($status).vertical());
    }
  }

  override preferredLayoutSize($container: JQuery, options: HtmlCompPrefSizeOptions): Dimension {
    let htmlContainer = HtmlComponent.get($container),
      insets = htmlContainer.insets(),
      wHint = (options.widthHint || htmlContainer.availableSize().width) - htmlContainer.insets().horizontal(),
      prefSize = new Dimension(),
      $status = this.tabBoxHeader.tabBox.$status,
      statusSizeLarge = new Dimension(),
      tabArea = this.tabBoxHeader.tabArea,
      tabAreaMargins = tabArea.htmlComp.margins(),
      menuBar = this.tabBoxHeader.menuBar,
      menuBarMargins = menuBar.htmlComp.margins();

    let menuBarMinSize = menuBar.htmlComp.prefSize({
      widthHint: 0
    });

    if (this.tabBoxHeader.tabBox.statusPosition === FormField.StatusPosition.TOP && $status && $status.isVisible()) {
      statusSizeLarge.height = $status.outerHeight(true);
      statusSizeLarge.width = this.fieldStatusWidth + graphics.margins($status).horizontal();

      prefSize.width += statusSizeLarge.width;
      prefSize.height = Math.max(prefSize.height, statusSizeLarge.height);
    }

    let tabAreaPrefSize = tabArea.htmlComp.prefSize({
      widthHint: wHint - menuBarMinSize.width - menuBarMargins.horizontal() - tabAreaMargins.horizontal() - statusSizeLarge.width
    });

    prefSize.width += tabAreaPrefSize.width + tabAreaMargins.horizontal();
    prefSize.height = Math.max(prefSize.height, tabAreaPrefSize.height + tabAreaMargins.vertical());

    let menuBarPrefSize = menuBar.htmlComp.prefSize({
      widthHint: wHint - tabAreaPrefSize.width - tabAreaMargins.horizontal() - menuBarMargins.horizontal() - statusSizeLarge.width
    });

    prefSize.width += menuBarPrefSize.width + menuBarMargins.horizontal();
    prefSize.height = Math.max(prefSize.height, menuBarPrefSize.height + menuBarMargins.vertical());

    return prefSize.add(insets);
  }
}
