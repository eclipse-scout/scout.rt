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
scout.DesktopBenchLayout = function(bench) {
  scout.DesktopBenchLayout.parent.call(this);
  this.bench = bench;
};
scout.inherits(scout.DesktopBenchLayout, scout.AbstractLayout);

scout.DesktopBenchLayout.prototype.layout = function($container) {
  var viewSize, outlineContentSize, htmlOutlineContent, htmlView,
    desktop = this.bench.desktop,
    htmlContainer = this.bench.htmlComp,
    containerSize = htmlContainer.getAvailableSize();

  containerSize = containerSize.subtract(htmlContainer.getInsets());
  if (desktop._outlineContent) {
    htmlOutlineContent = desktop._outlineContent.htmlComp;
    outlineContentSize = containerSize.subtract(htmlOutlineContent.getMargins());
    htmlOutlineContent.setSize(outlineContentSize);
  }

  var selectedViewTab = desktop.viewTabsController.selectedViewTab();
  if (selectedViewTab) {
    htmlView = selectedViewTab._view.htmlComp;
    viewSize = containerSize.subtract(htmlView.getMargins());
    htmlView.setSize(viewSize);
  }
};
