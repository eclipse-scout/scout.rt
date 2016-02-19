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
  var navigationSize, taskBarSize, htmlTaskBar, htmlBench, htmlBenchSize,
    navigationWidth = 0,
    taskBarHeight = 0,
    htmlContainer = this.desktop.htmlComp,
    htmlNavigation = this.desktop.navigation.htmlComp,
    containerSize = htmlContainer.getAvailableSize();

  containerSize = containerSize.subtract(htmlContainer.getInsets());
  if (this.desktop._hasNavigation()) {
    if (this.desktop._hasBench()) {
      navigationWidth = Math.max(this.desktop.splitter.position, scout.DesktopNavigation.MIN_SPLITTER_SIZE); // ensure newSize is not negative
    } else {
      navigationWidth = containerSize.width;
    }

    navigationSize = new scout.Dimension(navigationWidth, containerSize.height)
      .subtract(htmlNavigation.getMargins());
    htmlNavigation.setSize(navigationSize);
  }

  if (this.desktop._hasTaskBar()) {
    this.desktop._$taskBar.css('left', navigationWidth);

    htmlTaskBar = scout.HtmlComponent.get(this.desktop._$taskBar);
    taskBarHeight = this.desktop._$taskBar.outerHeight(true);
    taskBarSize = new scout.Dimension(containerSize.width - navigationWidth, taskBarHeight)
      .subtract(htmlTaskBar.getMargins());
    htmlTaskBar.setSize(taskBarSize);
  }

  if (this.desktop._hasBench()) {
    this.desktop.$bench.css('left', navigationWidth);

    htmlBench = scout.HtmlComponent.get(this.desktop.$bench);
    htmlBenchSize = new scout.Dimension(containerSize.width - navigationWidth, containerSize.height - taskBarHeight)
      .subtract(htmlBench.getMargins());
    htmlBench.setSize(htmlBenchSize);
  }
};
