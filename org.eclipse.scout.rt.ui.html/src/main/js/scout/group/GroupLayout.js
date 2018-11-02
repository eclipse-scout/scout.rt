/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
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
  var htmlComp = this.group.htmlComp;
  var containerSize = htmlComp.availableSize();
  containerSize.subtract(htmlComp.insets());

  var htmlHeader = this.group.htmlHeader;
  var headerSize = htmlHeader.prefSize(false);
  headerSize.width = containerSize.width;
  headerSize = headerSize.subtract(htmlHeader.margins());
  htmlHeader.setSize(headerSize);

  var htmlFooter = this.group.htmlFooter;
  if (htmlFooter.isVisible()) {
    var footerSize = htmlFooter.prefSize({
      includeMargin: false,
      useCssSize: true
    });
    footerSize.width = containerSize.width;
    footerSize = footerSize.subtract(htmlFooter.margins());
    htmlFooter.setSize(footerSize);
  }

  // 1st condition: Set size only if group is expanded
  // 2nd condition: There is no need to update it during the expand animation (the body will be layouted correctly before the animation starts)
  // 3rd condition: When Group.setCollapsed(false) has been called an event is triggered that might causes invalidating layout on other all groups (inclusive currently expanding group). The body of the currently expanding group is not rendered at this time.
  if (this.group.collapsed || this.group.bodyAnimating || !this.group.body.rendered) {
    return;
  }
  var bodySize;
  var htmlBody = this.group.body.htmlComp;

  var hasBody = htmlBody.prefSize(false).height > 0 && this.group.body.isVisible();
  if (hasBody) {
    bodySize = containerSize.subtract(htmlBody.margins());
    bodySize.height -= headerSize.height;
    if (htmlFooter.isVisible()) {
      bodySize.height -= htmlFooter.prefSize(true).height;
    }
    htmlBody.setSize(bodySize);
  }

  this.group.$collapseIcon.setVisible(hasBody);
};

scout.GroupLayout.prototype.invalidate = function(htmlSource) {
  var htmlBody = this.group.body.htmlComp;
  // If a child triggers a layout invalidation, the animation should be stopped and restarted because the body will likely have another height.
  // This will happen for sure if a child is an image which will be loaded during the animation.
  if (htmlBody && this.group.bodyAnimating && htmlSource && htmlSource.isDescendantOf(this.group.htmlComp)) {
    // Stop running animation
    this.group.body.$container.stop();

    // Resize to new height
    this.group.resizeBody();
  }
};

scout.GroupLayout.prototype.preferredLayoutSize = function($container, options) {
  options = options || {};
  var prefSize;
  var htmlComp = this.group.htmlComp;
  var htmlHeader = this.group.htmlHeader;
  var htmlBody = this.group.body.htmlComp;
  var htmlFooter = this.group.htmlFooter;

  // HeightHint not supported
  options.heightHint = null;

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
  prefSize = prefSize.add(htmlComp.insets({
    includeMargin: true
  }));
  prefSize.height += htmlHeader.prefSize(true).height;
  if (htmlFooter.isVisible()) {
    prefSize.height += htmlFooter.prefSize({
      useCssSize: true
    }).height;
  }
  return prefSize;
};
