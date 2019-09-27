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
scout.GroupBoxLayout = function(groupBox) {
  scout.GroupBoxLayout.parent.call(this);
  this.groupBox = groupBox;

  this._initDefaults();

  scout.htmlEnvironment.on('propertyChange', this._onHtmlEnvironmenPropertyChange.bind(this));
};
scout.inherits(scout.GroupBoxLayout, scout.AbstractLayout);

scout.GroupBoxLayout.prototype._initDefaults = function() {
  this._statusWidth = scout.htmlEnvironment.fieldStatusWidth;
};

scout.GroupBoxLayout.prototype._onHtmlEnvironmenPropertyChange = function() {
  this._initDefaults();
  this.groupBox.invalidateLayoutTree();
};

scout.GroupBoxLayout.prototype.layout = function($container) {
  var gbBodySize,
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
    scout.responsiveManager.handleResponsive(this.groupBox, containerSize.width);
  }

  if ($status && $status.isVisible()) {
    this._layoutStatus();
    statusWidth = $status.outerWidth(true);
  }

  // Menu-bar
  if (htmlMenuBar) {
    if (this.groupBox.menuBarPosition === scout.GroupBox.MenuBarPosition.TITLE) {
      // position: TITLE
      var titleSize = scout.graphics.prefSize(this.groupBox.$title);
      var titleLabelWidth = scout.graphics.prefSize(this.groupBox.$label, true).width;
      var menuBarSize = htmlMenuBar.prefSize({exact: true});
      var menuBarWidth = menuBarSize.width;
      var titleWidth = titleSize.width - statusWidth;

      // Use Math.floor to avoid rounding issues with text-ellipsis
      if ((titleLabelWidth + menuBarWidth) < titleWidth) {
        // label and menu-bar both fit into the title
        // let menu-bar use all the available width
        menuBarWidth = titleWidth - titleLabelWidth;
        menuBarSize.width = Math.floor(menuBarWidth);
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
  if (statusPosition === scout.FormField.StatusPosition.TOP) {
    if (this.groupBox.menuBarPosition !== scout.GroupBox.MenuBarPosition.TITLE) {
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
    scout.scrollbars.update(htmlGbBody.$comp);
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
};

scout.GroupBoxLayout.prototype._layoutStatus = function() {
  var htmlContainer = this.groupBox.htmlComp,
    containerPadding = htmlContainer.insets({
      includeBorder: false
    }),
    top = containerPadding.top,
    right = containerPadding.right,
    $groupBoxTitle = this.groupBox.$title,
    titleInnerHeight = $groupBoxTitle.innerHeight(),
    $status = this.groupBox.$status,
    statusMargins = scout.graphics.margins($status),
    statusPosition = this.groupBox.statusPosition;

  $status.cssWidth(this._statusWidth);
  if (statusPosition === scout.FormField.StatusPosition.DEFAULT) {
    $status
      .cssTop(top + $groupBoxTitle.cssMarginTop())
      .cssRight(right)
      .cssHeight(titleInnerHeight - statusMargins.vertical())
      .cssLineHeight(titleInnerHeight - statusMargins.vertical());
  }
};

scout.GroupBoxLayout.prototype.preferredLayoutSize = function($container, options) {
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
    undoResponsive = scout.responsiveManager.handleResponsive(this.groupBox, options.widthHint);
  }

  if (gridData) {
    widthInPixel = gridData.widthInPixel;
    heightInPixel = gridData.heightInPixel;
  }
  if (widthInPixel && heightInPixel) {
    // If width and height are set there is no need to calculate anything -> just return it as preferred size
    prefSize = new scout.Dimension(widthInPixel, heightInPixel)
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
    prefSize = new scout.Dimension(0, 0);
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
    scout.responsiveManager.reset(this.groupBox);
  }

  return prefSize;
};

scout.GroupBoxLayout.prototype._titleHeight = function() {
  return scout.graphics.prefSize(this.groupBox.$title, true).height;
};

scout.GroupBoxLayout.prototype._notificationHeight = function(options) {
  options = options || {};
  if (!this.groupBox.notification) {
    return 0;
  }
  options.includeMargin = true;
  return this.groupBox.notification.htmlComp.prefSize(options).height;
};

scout.GroupBoxLayout.prototype._menuBarSize = function(htmlMenuBar, containerSize, statusWidth) {
  var menuBarSize = scout.MenuBarLayout.size(htmlMenuBar, containerSize);
  if (!this.groupBox.mainBox) {
    // adjust size of menubar as well if it is in a regular group box
    menuBarSize.width -= statusWidth;
  }
  return menuBarSize;
};

/**
 * Return menu-bar when it exists and it is visible.
 */
scout.GroupBoxLayout.prototype._htmlMenuBar = function() {
  if (this.groupBox.menuBar && this.groupBox.menuBarVisible) {
    var htmlMenuBar = scout.HtmlComponent.get(this.groupBox.menuBar.$container);
    if (htmlMenuBar.isVisible()) {
      return htmlMenuBar;
    }
  }
  return null;
};

scout.GroupBoxLayout.prototype._htmlGbBody = function() {
  return scout.HtmlComponent.get(this.groupBox.$body);
};
