/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.GroupLayout = function(group) {
  scout.GroupLayout.parent.call(this);
  this.group = group;
};
scout.inherits(scout.GroupLayout, scout.AbstractLayout);

scout.GroupLayout.prototype.layout = function($container) {
  var bodySize;
  var htmlComp = this.group.htmlComp;
  var htmlHeader = this.group.htmlHeader;
  var htmlBody = this.group.body.htmlComp;
  var containerSize = htmlComp.availableSize();

  containerSize.subtract(htmlComp.insets());
  if (!this.group.collapsed) {
    bodySize = containerSize.subtract(htmlBody.margins());
    bodySize.height -= htmlHeader.prefSize(true).height;

    htmlBody.setSize(bodySize);
  }
};

scout.GroupLayout.prototype.preferredLayoutSize = function($container, options) {
  var prefSize;
  var htmlComp = this.group.htmlComp;
  var htmlHeader = this.group.htmlHeader;
  var htmlBody = this.group.body.htmlComp;

  if (this.group.bodyAnimating) {
    // Return the current size when the body is collapsing or expanding
    // so that the widgets on the bottom and on top move smoothly with the animation
    prefSize = htmlBody.size(true);
  } else if (this.group.collapsed || !this.group.body.rendered) {
    // Body may not be rendered even if collapsed is false if property has changed but _renderCollapse not called yet
    // (if revalidateLayoutTree is called during collapsed property event)
    prefSize = new scout.Dimension(0, 0);
  } else {
    prefSize = htmlBody.prefSize(options)
      .add(htmlBody.margins());
  }
  prefSize = prefSize.add(htmlComp.insets());
  prefSize.height += htmlHeader.prefSize(true).height;
  return prefSize;
};
