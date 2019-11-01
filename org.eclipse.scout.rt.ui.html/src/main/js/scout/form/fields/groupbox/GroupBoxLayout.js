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
import {AbstractLayout} from '../../../index';
import {HtmlEnvironment} from '../../../index';
import {scrollbars} from '../../../index';
import {MenuBarLayout} from '../../../index';
import {GroupBox} from '../../../index';
import {HtmlComponent} from '../../../index';
import {graphics} from '../../../index';
import {ResponsiveManager} from '../../../index';
import {Dimension} from '../../../index';
import {FormField} from '../../../index';
import * as $ from 'jquery';

export default class GroupBoxLayout extends AbstractLayout {

constructor(groupBox) {
  super();
  this.groupBox = groupBox;

  this._initDefaults();

  HtmlEnvironment.get().on('propertyChange', this._onHtmlEnvironmenPropertyChange.bind(this));
}


_initDefaults() {
  this._statusWidth = HtmlEnvironment.get().fieldStatusWidth;
}

_onHtmlEnvironmenPropertyChange() {
  this._initDefaults();
  this.groupBox.invalidateLayoutTree();
}

layout($container) {
  var gbBodySize,
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
    $status = this.groupBox.$status,
    containerSize = htmlContainer.availableSize()
    .subtract(htmlContainer.insets());

  // apply responsive transformations if necessary.
  if (this.groupBox.responsive) {
    ResponsiveManager.get().handleResponsive(this.groupBox, containerSize.width);
  }

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
      var titleSize = graphics.prefSize(this.groupBox.$title);
      var titleLabelWidth = Math.ceil(graphics.prefSize(this.groupBox.$label, true).width) + 1;
      var menuBarWidth = menuBarSize.width;
      var titleWidth = titleSize.width - statusWidth;

      if ((titleLabelWidth + menuBarWidth) < titleWidth) {
        // label and menu-bar both fit into the title
        // let menu-bar use all the available width
        menuBarWidth = Math.floor(titleWidth - titleLabelWidth);
        menuBarSize.width = menuBarWidth;
        $label.cssWidth('');

      } else {
        // label and menu-bar don't fit into the title
        // scale down until both fit into the title, try to keep the same width-ratio (r)
        var scaleFactor = (titleLabelWidth + menuBarWidth) / titleWidth;
        var rLabel = (titleLabelWidth / titleWidth) / scaleFactor;
        var rMenuBar = (menuBarWidth / titleWidth) / scaleFactor;

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
      var marginX = $element.cssMarginX() + statusWidth;
      $element.cssWidth('calc(100% - ' + marginX + 'px)');
    } else {
      $element.cssWidth('');
    }
  }
}

_layoutStatus() {
  var htmlContainer = this.groupBox.htmlComp,
    containerPadding = htmlContainer.insets({
      includeBorder: false
    }),
    top = containerPadding.top,
    right = containerPadding.right,
    $groupBoxTitle = this.groupBox.$title,
    titleInnerHeight = $groupBoxTitle.innerHeight(),
    $status = this.groupBox.$status,
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
  var htmlContainer = this.groupBox.htmlComp,
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
    if (htmlMenuBar) {
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
  var menuBarSize = MenuBarLayout.size(htmlMenuBar, containerSize);
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
    var htmlMenuBar = HtmlComponent.get(this.groupBox.menuBar.$container);
    if (htmlMenuBar.isVisible()) {
      return htmlMenuBar;
    }
  }
  return null;
}

_htmlGbBody() {
  return HtmlComponent.get(this.groupBox.$body);
}
}
