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
scout.PageLayout = function(outline, page) {
  scout.PageLayout.parent.call(this, outline);
  this.outline = outline;
  this.page = page;
};
scout.inherits(scout.PageLayout, scout.AbstractLayout);

scout.PageLayout.prototype.layout = function($container) {
  var containerSize, detailMenuBarSize, formTop,
    htmlContainer = this.page.htmlComp,
    detailMenuBar = this.outline.detailMenuBar,
    htmlDetailMenuBar = detailMenuBar.htmlComp;

  containerSize = htmlContainer.getAvailableSize()
    .subtract(htmlContainer.getInsets());

  if (detailMenuBar.visible) {
    detailMenuBarSize = scout.MenuBarLayout.size(htmlDetailMenuBar, containerSize);
    htmlDetailMenuBar.setSize(detailMenuBarSize);
  }

  if (this.page.detailForm && this.page.detailForm === this.outline.detailForm) {
    formTop = this.page.detailForm.$container.position().top;
    this.page.detailForm.htmlComp.setSize(new scout.Dimension(containerSize.width, containerSize.height - formTop));
  }
};

scout.PageLayout.prototype.preferredLayoutSize = function($container) {
  var prefSize,
    htmlContainer = this.page.htmlComp,
    detailMenuBar = this.outline.detailMenuBar,
    htmlDetailMenuBar = detailMenuBar.htmlComp,
    formPrefSize = new scout.Dimension(),
    formTop = 0,
    detailMenuBarPrefSize= new scout.Dimension(),
    detailMenuBarTop = 0;

  if (this.page.detailForm && this.page.detailForm === this.outline.detailForm) {
    formPrefSize = this.page.detailForm.htmlComp.getPreferredSize();
    formTop = this.page.detailForm.$container.position().top;
  } else if (detailMenuBar.visible) {
    detailMenuBarPrefSize = htmlDetailMenuBar.getPreferredSize(true);
    detailMenuBarTop = detailMenuBar.$container.position().top;
  }

  prefSize = new scout.Dimension(Math.max(formPrefSize.width, detailMenuBarPrefSize.width), detailMenuBarTop + detailMenuBarPrefSize.height + formTop + formPrefSize.height);
  prefSize = prefSize.add(htmlContainer.getInsets());
  prefSize.height = prefSize.height - htmlContainer.getInsets().top; //TODO CGU remove after $node has a $text node, then we can properly measure $text instead of using top
  return prefSize;
};
