/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {AbstractLayout, Dimension, FormField, graphics, GroupBox, HtmlComponent, HtmlEnvironment, MenuBarLayout, ResponsiveManager, scrollbars} from '../../../index';
import $ from 'jquery';

export default class GroupBoxLayout extends AbstractLayout {

  constructor(groupBox) {
    super();
    this.groupBox = groupBox;

    this._initDefaults();

    this._htmlPropertyChangeHandler = this._onHtmlEnvironmentPropertyChange.bind(this);
    HtmlEnvironment.get().on('propertyChange', this._htmlPropertyChangeHandler);
    this.groupBox.one('remove', () => {
      HtmlEnvironment.get().off('propertyChange', this._htmlPropertyChangeHandler);
    });
  }

  _initDefaults() {
    this._statusWidth = HtmlEnvironment.get().fieldStatusWidth;
  }

  _onHtmlEnvironmentPropertyChange() {
    this._initDefaults();
    this.groupBox.invalidateLayoutTree();
  }

  layout($container) {
    let gbBodySize,
      menuBarSize,
      menuBarHeight = 0,
      statusWidth = 0,
      statusPosition = this.groupBox.statusPosition,
      htmlContainer = this.groupBox.htmlComp,
      htmlGbBody = this._htmlGbBody(),
      htmlMenuBar = this._htmlMenuBar(),
      tooltip = this.groupBox._tooltip(),
      $header = this.groupBox.$header,
      $title = this.groupBox.$title,
      containerSize = htmlContainer.availableSize()
        .subtract(htmlContainer.insets());

    // apply responsive transformations if necessary.
    if (this.groupBox.responsive) {
      ResponsiveManager.get().handleResponsive(this.groupBox, containerSize.width);
    }

    let $status = this._$status();
    if ($status && $status.isVisible()) {
      this._layoutStatus();
      statusWidth = $status.outerWidth(true);
    }

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

    if (this.groupBox.notification) {
      setWidthForStatus(this.groupBox.notification.$container, statusWidth);
    }

    gbBodySize = containerSize.subtract(htmlGbBody.margins());
    gbBodySize.height -= this._headerHeight();
    gbBodySize.height -= this._notificationHeight();
    gbBodySize.height -= menuBarHeight;
    $.log.isTraceEnabled() && $.log.trace('(GroupBoxLayout#layout) gbBodySize=' + gbBodySize);
    htmlGbBody.setSize(gbBodySize);

    // Make sure tooltip is at correct position after layouting, if there is one
    if (tooltip && tooltip.rendered) {
      tooltip.position();
    }

    if (htmlGbBody.scrollable || this.groupBox.bodyLayoutConfig.minWidth > 0) {
      scrollbars.update(htmlGbBody.$comp);
    }

    // Make $element wider, so status is on the left
    function setWidthForStatus($element, statusWidth) {
      if (statusWidth) {
        let marginX = $element.cssMarginX() + statusWidth;
        $element.cssWidth('calc(100% - ' + marginX + 'px)');
      } else {
        $element.cssWidth('');
      }
    }
  }

  _updateMenuBar(htmlMenuBar, containerSize, statusWidth) {
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

  _menuBarSize(htmlMenuBar, containerSize, statusWidth) {
    let menuBarSize = MenuBarLayout.size(htmlMenuBar, containerSize);
    if (!this.groupBox.mainBox) {
      // adjust size of menubar as well if it is in a regular group box
      menuBarSize.width -= statusWidth;
    }
    return menuBarSize;
  }

  _updateHeaderMenuBar(htmlMenuBar, $header, $title, statusWidth) {
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

  _$status() {
    return this.groupBox.$status;
  }

  _layoutStatus() {
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

    $status.cssWidth(this._statusWidth);
    if (statusPosition === FormField.StatusPosition.DEFAULT) {
      top += $header.cssMarginTop();
    }

    $status
      .cssTop(top)
      .cssRight(right)
      .cssHeight(headerInnerHeight - statusMargins.vertical());
  }

  preferredLayoutSize($container, options) {
    options = options || {};
    let htmlContainer = this.groupBox.htmlComp,
      htmlGbBody = this._htmlGbBody(),
      htmlMenuBar,
      prefSize,
      widthInPixel = 0,
      heightInPixel = 0,
      gridData = this.groupBox.gridData,
      undoResponsive = false;

    if (this.groupBox.responsive &&
      options.widthHint) {
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

      htmlMenuBar = this._htmlMenuBar();
      if (htmlMenuBar && this.groupBox.menuBarPosition !== GroupBox.MenuBarPosition.TITLE) {
        prefSize.height += htmlMenuBar.prefSize(options).height;
      }
    } else {
      prefSize = new Dimension(0, 0);
    }
    prefSize = prefSize.add(htmlContainer.insets());
    prefSize.height += this._headerHeight();
    prefSize.height += this._notificationHeight(options);

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

  _headerHeight() {
    return graphics.prefSize(this.groupBox.$header, true).height;
  }

  _notificationHeight(options) {
    options = options || {};
    if (!this.groupBox.notification) {
      return 0;
    }
    options.includeMargin = true;
    return this.groupBox.notification.htmlComp.prefSize(options).height;
  }

  /**
   * Return menu-bar when it exists and it is visible.
   */
  _htmlMenuBar() {
    if (this.groupBox.menuBar && this.groupBox.menuBarVisible) {
      let htmlMenuBar = HtmlComponent.optGet(this.groupBox.menuBar.$container);
      if (htmlMenuBar && htmlMenuBar.isVisible()) {
        return htmlMenuBar;
      }
    }
    return null;
  }

  _htmlGbBody() {
    return HtmlComponent.get(this.groupBox.$body);
  }
}
