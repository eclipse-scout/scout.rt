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
scout.inherits(scout.PageLayout, scout.TreeLayout);

scout.PageLayout.prototype.layout = function($container) {
  scout.PageLayout.parent.prototype.layout.call(this, $container);
  var containerSize,
    htmlContainer = this.page.htmlComp,
    formHtmlComp = this.page.detailForm.htmlComp,
    formTop = this.page.detailForm.$container.position().top;

  containerSize = htmlContainer.getAvailableSize()
    .subtract(htmlContainer.getInsets());

  formHtmlComp.setSize(new scout.Dimension(containerSize.width, containerSize.height - formTop));
};

scout.PageLayout.prototype.preferredLayoutSize = function($container) {
  var formPrefSize = this.page.detailForm.htmlComp.getPreferredSize(),
    formTop = this.page.detailForm.$container.position().top;
  return new scout.Dimension(formPrefSize.width, formTop + formPrefSize.height);
};
