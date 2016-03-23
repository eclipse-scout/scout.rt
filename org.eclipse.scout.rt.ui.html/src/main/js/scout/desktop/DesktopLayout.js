/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.DesktopLayout = function(desktop) {
  scout.DesktopLayout.parent.call(this);
  this.desktop = desktop;
};
scout.inherits(scout.DesktopLayout, scout.AbstractLayout);

scout.DesktopLayout.prototype.layout = function($container) {
  var navigationSize, headerSize, htmlHeader, htmlBench, htmlBenchSize, htmlNavigation, left,
    navigationWidth = 0,
    headerHeight = 0,
    animated = this.desktop.animateLayoutChange,
    containerSize = this.containerSize();

  if (this.desktop.navigation) {
    navigationWidth = this.calculateNavigationWidth(containerSize);
    if (this.desktop.splitter) {
      this.desktop.splitter.updatePosition(navigationWidth);
    }

    if (this.desktop.navigationVisible) {
      htmlNavigation = this.desktop.navigation.htmlComp;
      navigationSize = new scout.Dimension(navigationWidth, containerSize.height)
        .subtract(htmlNavigation.getMargins());
      htmlNavigation.setSize(navigationSize);
    }
  }

  if (this.desktop.header) {
    // positioning
    if (!animated) {
      this.desktop.header.$container.cssLeft(navigationWidth);
    } else {
      if (this.desktop.headerVisible) {
        this.desktop.header.$container.cssLeft(containerSize.width);
        left = navigationWidth;
      } else {
        left = containerSize.width;
      }
      this.desktop.header.$container.animate({
        left: left
      }, {
        queue: false,
        complete: this.desktop.onLayoutAnimationComplete.bind(this.desktop)
      });
    }

    // sizing
    htmlHeader = this.desktop.header.htmlComp;
    headerHeight = htmlHeader.$comp.outerHeight(true);
    if (this.desktop.headerVisible) {
      headerSize = new scout.Dimension(containerSize.width - navigationWidth, headerHeight)
        .subtract(htmlHeader.getMargins());
      htmlHeader.setSize(headerSize);
    }
  }

  if (this.desktop.bench) {
    // positioning
    this.desktop.bench.$container.cssTop(headerHeight);
    if (!animated) {
      this.desktop.bench.$container.cssLeft(navigationWidth);
    } else {
      if (this.desktop.benchVisible) {
        this.desktop.bench.$container.cssLeft(containerSize.width);
        left = navigationWidth;
      } else {
        left = containerSize.width;
      }
      this.desktop.bench.$container.animate({
        left: left
      }, {
        queue: false,
        complete: this.desktop.onLayoutAnimationComplete.bind(this.desktop)
      });
    }

    // sizing
    if (this.desktop.benchVisible) {
      htmlBench = this.desktop.bench.htmlComp;
      htmlBenchSize = new scout.Dimension(containerSize.width - navigationWidth, containerSize.height - headerHeight)
        .subtract(htmlBench.getMargins());
      htmlBench.setSize(htmlBenchSize);
    }
  }
};

scout.DesktopLayout.prototype.containerSize = function() {
  var htmlContainer = this.desktop.htmlComp,
    containerSize = htmlContainer.getAvailableSize();

  return containerSize.subtract(htmlContainer.getInsets());
};

scout.DesktopLayout.prototype.calculateNavigationWidth = function(containerSize) {
  if (!this.desktop.navigationVisible) {
    return 0;
  }
  var navigationLayoutData = this.desktop.navigation.htmlComp.layoutData;
  if (navigationLayoutData.fullWidth) {
    return containerSize.width;
  }
  var splitterPosition = this.desktop.splitter.position;
  var outline = this.desktop.outline;
  if (!this.desktop.resizing && outline && outline.autoToggleBreadcrumbStyle) {
    // If autoToggleBreadcrumbStyle is true, BREADCRUMB_STYLE_WIDTH triggers the toggling between the two modes.
    // This code ensures this rule is never violated (necessary if mode is toggled programmatically rather than by the user)
    if (outline.displayStyle === scout.Tree.DisplayStyle.BREADCRUMB) {
      splitterPosition = scout.DesktopNavigation.BREADCRUMB_STYLE_WIDTH;
    } else if (splitterPosition <= scout.DesktopNavigation.BREADCRUMB_STYLE_WIDTH) {
      splitterPosition = scout.DesktopNavigation.DEFAULT_STYLE_WIDTH;
    }
  }
  return Math.max(splitterPosition, scout.DesktopNavigation.MIN_SPLITTER_SIZE); // ensure newSize is not negative
};
