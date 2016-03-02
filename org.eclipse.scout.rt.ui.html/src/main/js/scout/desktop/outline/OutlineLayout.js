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
scout.OutlineLayout = function(outline) {
  scout.OutlineLayout.parent.call(this, outline);
  this.outline = outline;
};
scout.inherits(scout.OutlineLayout, scout.TreeLayout);

scout.OutlineLayout.prototype.layout = function($container) {
  var containerSize,
    htmlContainer = this.outline.htmlComp;

  containerSize = htmlContainer.getAvailableSize()
    .subtract(htmlContainer.getInsets());

  if (this.outline.embedDetailForm) {
    var selectedNode = this.outline.selectedNodes[0];
    if (selectedNode) {
      var pageHtmlComp = selectedNode.htmlComp;
      // pageHtmlComp is null if there is no detail form and no detail menubar
      if (pageHtmlComp) {
        var prefSize = pageHtmlComp.getPreferredSize();
        pageHtmlComp.setSize(new scout.Dimension(containerSize.width, prefSize.height));
      }
    }
    // Remove width and height from non selected nodes
    this.outline.$nodes().each(function(i, elem) {
      var $elem = $(elem);
      if (!$elem.isSelected()) {
        $elem.css('height', 'auto')
          .css('width', 'auto');
      }
    });
  }
  scout.OutlineLayout.parent.prototype.layout.call(this, $container);
};

scout.OutlineLayout.prototype._setDataHeight = function(heightOffset) {
  // Add title height to heightOffset
  if (this.outline.titleVisible) {
    var titleSize = scout.graphics.getSize(this.outline.$title, true);
    heightOffset += titleSize.height;
  }

  scout.OutlineLayout.parent.prototype._setDataHeight.call(this, heightOffset);
};
