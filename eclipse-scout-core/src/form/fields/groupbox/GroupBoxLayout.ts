/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractLayout, Dimension, EventHandler, FormField, graphics, GroupBox, HtmlComponent, HtmlCompPrefSizeOptions, HtmlEnvironment, MenuBarLayout, PropertyChangeEvent, ResponsiveManager, scout, scrollbars} from '../../../index';
import $ from 'jquery';

export class GroupBoxLayout extends AbstractLayout {
  groupBox: GroupBox;
  protected _htmlPropertyChangeHandler: EventHandler<PropertyChangeEvent<any, HtmlEnvironment>>;
  protected _fieldStatusWidth: number;

  constructor(groupBox: GroupBox) {
    super();
    this.groupBox = groupBox;

    this._initDefaults();

    this._htmlPropertyChangeHandler = this._onHtmlEnvironmentPropertyChange.bind(this);
    HtmlEnvironment.get().on('propertyChange', this._htmlPropertyChangeHandler);
    this.groupBox.one('remove', () => {
      HtmlEnvironment.get().off('propertyChange', this._htmlPropertyChangeHandler);
    });
  }

  protected _initDefaults() {
    this._fieldStatusWidth = HtmlEnvironment.get().fieldStatusWidth;
  }

  protected _onHtmlEnvironmentPropertyChange() {
    this._initDefaults();
    this.groupBox.invalidateLayoutTree();
  }

  override layout($container: JQuery) {
    let menuBarSize: Dimension,
      menuBarHeight = 0,
      statusWidth = 0,
      statusPosition = this.groupBox.statusPosition,
      htmlContainer = this.groupBox.htmlComp,
      htmlGbBody = this._htmlGbBody(),
      htmlMenuBar = this._htmlMenuBar(),
      $header = this.groupBox.$header,
      $title = this.groupBox.$title,
      containerSize = htmlContainer.availableSize()
        .subtract(htmlContainer.insets());

    // apply responsive transformations if necessary.
    if (this.groupBox.responsive) {
      ResponsiveManager.get().handleResponsive(this.groupBox, containerSize.width);
    }

    statusWidth = this._statusWidth();

    // Menu-bar
    if (htmlMenuBar) {
      if (this.groupBox.menuBarPosition === GroupBox.MenuBarPosition.TITLE) {
        // position: TITLE
        menuBarSize = this._updateHeaderMenuBar(htmlMenuBar, $header, $title, statusWidth);
      } else {
        // position: TOP and BOTTOM
        menuBarSize = this._updateMenuBar(htmlMenuBar, containerSize, statusWidth);
        menuBarHeight = menuBarSize.height;
        setWidthForStatus($title);
      }
      htmlMenuBar.setSize(menuBarSize);
    } else {
      setWidthForStatus($title);
    }

    if (statusPosition !== FormField.StatusPosition.TOP) {
      setWidthForStatus($header, statusWidth);
    } else {
      setWidthForStatus($header);
    }

    let notificationHeight = 0;
    if (this.groupBox.notification) {
      let notificationMargin = this.groupBox.notification.htmlComp.margins();
      let notificationPrefSize = this.groupBox.notification.htmlComp.prefSize({
        widthHint: containerSize.width - statusWidth
      });
      this.groupBox.notification.htmlComp.setSize(notificationPrefSize);
      notificationHeight = notificationPrefSize.height + notificationMargin.vertical();
    }

    let gbBodySize = containerSize.subtract(htmlGbBody.margins());
    gbBodySize.height -= this._headerHeight();
    gbBodySize.height -= notificationHeight;
    gbBodySize.height -= menuBarHeight;
    $.log.isTraceEnabled() && $.log.trace('(GroupBoxLayout#layout) gbBodySize=' + gbBodySize);
    htmlGbBody.setSize(gbBodySize);

    if (htmlGbBody.scrollable || this.groupBox.bodyLayoutConfig.minWidth > 0) {
      scrollbars.update(htmlGbBody.$comp);
    }

    // Make $element wider, so status is on the left
    function setWidthForStatus($element: JQuery, statusWidth?: number) {
      if (statusWidth) {
        let marginX = $element.cssMarginX() + statusWidth;
        $element.cssWidth('calc(100% - ' + marginX + 'px)');
      } else {
        $element.cssWidth('');
      }
    }
  }

  protected _updateMenuBar(htmlMenuBar: HtmlComponent, containerSize: Dimension, statusWidth: number): Dimension {
    let $groupBox = this.groupBox.$container;
    let $menuBar = htmlMenuBar.$comp;
    if (!this.groupBox.mainBox &&
      ($groupBox.hasClass('menubar-position-top') && $groupBox.hasClass('has-scroll-shadow-top')) ||
      ($groupBox.hasClass('menubar-position-bottom') && $groupBox.hasClass('has-scroll-shadow-bottom'))) {
      // Replace margin resp. status space with a padding so that menubar line is drawn as width as the shadow
      // The left margin (mandatory indicator space) could actually be removed by css,
      // but doing it here does not require any css adjustments for customized group boxes.
      let margin = graphics.margins($menuBar);
      if (margin.left > 0) {
        $menuBar.cssPaddingLeft(margin.left);
        $menuBar.cssMarginLeft(0);
      }
      if (statusWidth > 0) {
        $menuBar.cssPaddingRight(statusWidth);
        statusWidth = 0;
      }
    } else {
      $menuBar.cssPaddingLeft('');
      $menuBar.cssMarginLeft('');
      $menuBar.cssPaddingRight('');
    }
    return this._menuBarSize(htmlMenuBar, containerSize, statusWidth);
  }

  protected _menuBarSize(htmlMenuBar: HtmlComponent, containerSize: Dimension, statusWidth: number): Dimension {
    let menuBarSize = MenuBarLayout.size(htmlMenuBar, containerSize);
    if (!this.groupBox.mainBox) {
      // adjust size of menubar as well if it is in a regular group box
      menuBarSize.width -= statusWidth;
    }
    return menuBarSize;
  }

  protected _updateHeaderMenuBar(htmlMenuBar: HtmlComponent, $header: JQuery, $title: JQuery, statusWidth: number): Dimension {
    // Use Math.floor/ceil and +1 to avoid rounding issues with text-ellipsis and title label
    let menuBarSize = htmlMenuBar.prefSize({
      exact: true
    });
    let headerSize = graphics.prefSize($header).subtract(graphics.insets($header));
    let headerWidth = headerSize.width - statusWidth;
    let $control = $header.children('.group-box-control');
    if ($control.length > 0) {
      headerWidth -= graphics.size($control, true).width;
    }
    let titleWidth = Math.ceil(graphics.prefSize($title, true).width) + 1;
    let menuBarWidth = menuBarSize.width;

    if ((titleWidth + menuBarWidth) < headerWidth) {
      // title and menu-bar both fit into the title
      // let menu-bar use all the available width
      menuBarWidth = Math.floor(headerWidth - titleWidth);
      menuBarSize.width = menuBarWidth;
      $title.cssWidth('');
    } else {
      // title and menu-bar don't fit into the title
      // scale down until both fit into the title, try to keep the same width-ratio (r)
      let scaleFactor = (titleWidth + menuBarWidth) / headerWidth;
      let rLabel = (titleWidth / headerWidth) / scaleFactor;
      let rMenuBar = (menuBarWidth / headerWidth) / scaleFactor;

      if (rLabel < rMenuBar) {
        rLabel = Math.max(0.33, rLabel);
        rMenuBar = 1.0 - rLabel;
      } else {
        rMenuBar = Math.max(0.33, rMenuBar);
        rLabel = 1.0 - rMenuBar;
      }

      titleWidth = rLabel * headerWidth;
      menuBarWidth = rMenuBar * headerWidth;

      menuBarSize.width = Math.floor(menuBarWidth);
      $title.cssWidth(Math.floor(titleWidth));
    }
    return menuBarSize;
  }

  protected _$status(): JQuery {
    return this.groupBox.$status;
  }

  protected _layoutStatus() {
    let htmlContainer = this.groupBox.htmlComp,
      containerPadding = htmlContainer.insets({
        includeBorder: false
      }),
      top = containerPadding.top,
      right = containerPadding.right,
      $header = this.groupBox.$header,
      headerInnerHeight = $header.innerHeight(),
      $status = this._$status(),
      statusMargins = graphics.margins($status),
      statusPosition = this.groupBox.statusPosition;

    $status.cssWidth(this._fieldStatusWidth);
    if (statusPosition === FormField.StatusPosition.DEFAULT) {
      top += $header.cssMarginTop();
    }

    $status
      .cssTop(top)
      .cssRight(right)
      .cssHeight(headerInnerHeight - statusMargins.vertical());
  }

  override preferredLayoutSize($container: JQuery, options?: HtmlCompPrefSizeOptions): Dimension {
    options = options || {};
    let htmlContainer = this.groupBox.htmlComp,
      htmlGbBody = this._htmlGbBody(),
      prefSize: Dimension,
      widthInPixel = 0,
      heightInPixel = 0,
      gridData = this.groupBox.gridData,
      undoResponsive = false;

    if (this.groupBox.responsive && options.widthHint) {
      undoResponsive = ResponsiveManager.get().handleResponsive(this.groupBox, options.widthHint);
    }

    if (gridData) {
      widthInPixel = gridData.widthInPixel;
      heightInPixel = gridData.heightInPixel;
    }
    if (widthInPixel && heightInPixel) {
      // If width and height are set there is no need to calculate anything -> just return it as preferred size
      prefSize = new Dimension(widthInPixel, heightInPixel)
        .add(htmlContainer.insets());
      return prefSize;
    }
    // Use explicit width as hint if set
    if (!options.widthHint && widthInPixel) {
      options.widthHint = widthInPixel;
    }
    // HeightHint not supported
    options.heightHint = null;

    if (this.groupBox.expanded) {
      prefSize = htmlGbBody.prefSize(options)
        .add(htmlGbBody.margins());

      let htmlMenuBar = this._htmlMenuBar();
      if (htmlMenuBar && this.groupBox.menuBarPosition !== GroupBox.MenuBarPosition.TITLE) {
        prefSize.height += htmlMenuBar.prefSize(options).height;
      }

      if (this.groupBox.notification) {
        let notificationPrefSize = this.groupBox.notification.htmlComp.prefSize({
          widthHint: scout.nvl(options.widthHint, prefSize.width) - this._statusWidth(),
          includeMargin: true
        });
        prefSize.height += notificationPrefSize.height;
      }
    } else {
      prefSize = new Dimension(0, 0);
    }
    prefSize = prefSize.add(htmlContainer.insets());
    prefSize.height += this._headerHeight();

    // predefined height or width in pixel override other values
    if (widthInPixel) {
      prefSize.width = widthInPixel;
    }
    if (heightInPixel) {
      prefSize.height = heightInPixel;
    }

    if (undoResponsive) {
      ResponsiveManager.get().reset(this.groupBox);
    }

    return prefSize;
  }

  protected _headerHeight(): number {
    return graphics.prefSize(this.groupBox.$header, true).height;
  }

  protected _statusWidth(): number {
    let statusWidth = 0;
    let $status = this._$status();
    if ($status && $status.isVisible()) {
      this._layoutStatus();
      statusWidth = $status.outerWidth(true);
    }
    return statusWidth;
  }

  /**
   * Return menu-bar when it exists and it is visible.
   */
  protected _htmlMenuBar(): HtmlComponent {
    if (this.groupBox.menuBar && this.groupBox.menuBarVisible) {
      let htmlMenuBar = HtmlComponent.optGet(this.groupBox.menuBar.$container);
      if (htmlMenuBar && htmlMenuBar.isVisible()) {
        return htmlMenuBar;
      }
    }
    return null;
  }

  protected _htmlGbBody(): HtmlComponent {
    return HtmlComponent.get(this.groupBox.$body);
  }
}
