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
  var navigationSize, headerSize, htmlHeader, htmlBench, htmlBenchSize,
    navigationWidth = 0,
    headerHeight = 0,
    htmlContainer = this.desktop.htmlComp,
    htmlNavigation = this.desktop.navigation.htmlComp,
    containerSize = htmlContainer.getAvailableSize();

  containerSize = containerSize.subtract(htmlContainer.getInsets());
  if (this.desktop._hasNavigation()) {
    navigationWidth = this._calculateNavigationWidth(containerSize);
    if (this.desktop.splitter) {
      this.desktop.splitter.updatePosition(navigationWidth);
    }

    navigationSize = new scout.Dimension(navigationWidth, containerSize.height)
      .subtract(htmlNavigation.getMargins());
    htmlNavigation.setSize(navigationSize);
  }

  if (this.desktop._hasHeader()) {
    this.desktop.header.$container.css('left', navigationWidth);

    htmlHeader = this.desktop.header.htmlComp;
    headerHeight = htmlHeader.$comp.outerHeight(true);
    headerSize = new scout.Dimension(containerSize.width - navigationWidth, headerHeight)
      .subtract(htmlHeader.getMargins());
    htmlHeader.setSize(headerSize);
  }

  if (this.desktop._hasBench()) {
    this.desktop.bench.$container.css('left', navigationWidth);

    htmlBench = this.desktop.bench.htmlComp;
    htmlBenchSize = new scout.Dimension(containerSize.width - navigationWidth, containerSize.height - headerHeight)
      .subtract(htmlBench.getMargins());
    htmlBench.setSize(htmlBenchSize);
  }
};

scout.DesktopLayout.prototype._calculateNavigationWidth = function(containerSize) {
  if (!this.desktop._hasBench()) {
    return containerSize.width;
  }
  var splitterPosition = this.desktop.splitter.position;
  var outline = this.desktop.outline;
  if (!this.desktop.resizing && outline && outline.autoToggleBreadcrumbStyle) {
    if (outline.displayStyle === scout.Tree.DisplayStyle.BREADCRUMB) {
      splitterPosition = scout.DesktopNavigation.BREADCRUMB_STYLE_WIDTH;
    } else if (splitterPosition <= scout.DesktopNavigation.BREADCRUMB_STYLE_WIDTH) {
      splitterPosition = scout.DesktopNavigation.DEFAULT_STYLE_WIDTH;
    }
  }
  return Math.max(splitterPosition, scout.DesktopNavigation.MIN_SPLITTER_SIZE); // ensure newSize is not negative
};
