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
      $groupBoxTitle = this.groupBox.$title,
      $label = this.groupBox.$label,
      containerSize = htmlContainer.availableSize()
        .subtract(htmlContainer.insets());

    // apply responsive transformations if necessary.
    if (this.groupBox.responsive) {
      ResponsiveManager.get().handleResponsive(this.groupBox, containerSize.width);
    }

    var $status = this._$status();
    if ($status && $status.isVisible()) {
      this._layoutStatus();
      statusWidth = $status.outerWidth(true);
    }

    // Menu-bar
    if (htmlMenuBar) {
      if (this.groupBox.menuBarPosition === GroupBox.MenuBarPosition.TITLE) {
        // position: TITLE
        // Use Math.floor/ceil and +1 to avoid rounding issues with text-ellipsis and title label
        menuBarSize = htmlMenuBar.prefSize({
          exact: true
        });
        let titleSize = graphics.prefSize(this.groupBox.$title);
        let titleLabelWidth = Math.ceil(graphics.prefSize(this.groupBox.$label, true).width) + 1;
        let menuBarWidth = menuBarSize.width;
        let titleWidth = titleSize.width - statusWidth;

        if ((titleLabelWidth + menuBarWidth) < titleWidth) {
          // label and menu-bar both fit into the title
          // let menu-bar use all the available width
          menuBarWidth = Math.floor(titleWidth - titleLabelWidth);
          menuBarSize.width = menuBarWidth;
          $label.cssWidth('');

        } else {
          // label and menu-bar don't fit into the title
          // scale down until both fit into the title, try to keep the same width-ratio (r)
          let scaleFactor = (titleLabelWidth + menuBarWidth) / titleWidth;
          let rLabel = (titleLabelWidth / titleWidth) / scaleFactor;
          let rMenuBar = (menuBarWidth / titleWidth) / scaleFactor;

          if (rLabel < rMenuBar) {
            rLabel = Math.max(0.33, rLabel);
            rMenuBar = 1.0 - rLabel;
          } else {
            rMenuBar = Math.max(0.33, rMenuBar);
            rLabel = 1.0 - rMenuBar;
          }

          titleLabelWidth = rLabel * titleWidth;
          menuBarWidth = rMenuBar * titleWidth;

          menuBarSize.width = Math.floor(menuBarWidth);
          $label.cssWidth(Math.floor(titleLabelWidth));
        }
      } else {
        // position: TOP and BOTTOM
        menuBarSize = this._menuBarSize(htmlMenuBar, containerSize, statusWidth);
        menuBarHeight = menuBarSize.height;
        setWidthForStatus($label);
      }
      htmlMenuBar.setSize(menuBarSize);
    } else {
      setWidthForStatus($label);
    }

    // Position of label and title
    setWidthForStatus($groupBoxTitle);
    if (statusPosition === FormField.StatusPosition.TOP) {
      if (this.groupBox.menuBarPosition !== GroupBox.MenuBarPosition.TITLE) {
        setWidthForStatus($label, statusWidth);
      }
    } else {
      setWidthForStatus($groupBoxTitle, statusWidth);
    }

    if (this.groupBox.notification) {
      setWidthForStatus(this.groupBox.notification.$container, statusWidth);
    }

    gbBodySize = containerSize.subtract(htmlGbBody.margins());
    gbBodySize.height -= this._titleHeight();
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
      $groupBoxTitle = this.groupBox.$title,
      titleInnerHeight = $groupBoxTitle.innerHeight(),
      $status = this._$status(),
      statusMargins = graphics.margins($status),
      statusPosition = this.groupBox.statusPosition;

    $status.cssWidth(this._statusWidth);
    if (statusPosition === FormField.StatusPosition.DEFAULT) {
      $status
        .cssTop(top + $groupBoxTitle.cssMarginTop())
        .cssRight(right)
        .cssHeight(titleInnerHeight - statusMargins.vertical())
        .cssLineHeight(titleInnerHeight - statusMargins.vertical());
    }
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
    prefSize.height += this._titleHeight();
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

  _titleHeight() {
    return graphics.prefSize(this.groupBox.$title, true).height;
  }

  _notificationHeight(options) {
    options = options || {};
    if (!this.groupBox.notification) {
      return 0;
    }
    options.includeMargin = true;
    return this.groupBox.notification.htmlComp.prefSize(options).height;
  }

  _menuBarSize(htmlMenuBar, containerSize, statusWidth) {
    let menuBarSize = MenuBarLayout.size(htmlMenuBar, containerSize);
    if (!this.groupBox.mainBox) {
      // adjust size of menubar as well if it is in a regular group box
      menuBarSize.width -= statusWidth;
    }
    return menuBarSize;
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
