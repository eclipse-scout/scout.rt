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

scout.OutlineLayout.prototype._layout = function($container) {
  var containerSize,
    htmlContainer = this.outline.htmlComp;

  scout.OutlineLayout.parent.prototype._layout.call(this, $container);

  containerSize = htmlContainer.getAvailableSize()
    .subtract(htmlContainer.getInsets());

  if (this.outline.embedDetailContent) {
    var selectedNode = this.outline.selectedNodes[0];
    if (selectedNode && selectedNode.rendered) {
      var pageHtmlComp = selectedNode.htmlComp;
      // pageHtmlComp is null if there is no detail form and no detail menubar
      if (pageHtmlComp) {
        // Fix width so that prefSize returns the appropriate height (necessary for elements with text wrap)
        pageHtmlComp.$comp.cssWidth(containerSize.width);

        var prefSize = pageHtmlComp.getPreferredSize();
        pageHtmlComp.setSize(new scout.Dimension(containerSize.width, prefSize.height));
        selectedNode.height = prefSize.height + pageHtmlComp.getMargins().vertical();
      }
    }

    // Remove width and height from non selected nodes (at this point we don't know the previously selected node anymore, so we need to process all visible nodes)
    // It is not enough to only process rendered nodes, we need to update the detached nodes as well
    this.outline.visibleNodesFlat.forEach(function(node) {
      var $node = node.$node;
      if (!$node) {
        // Do nothing if node has never been rendered
        return;
      }
      // check for style.height to prevent unnecessary updates, no need to update nodes without a fixed height
      if ($node.isSelected() || !$node[0].style.height || $node[0].style.height === 'auto') {
        return;
      }

      $node.css('height', 'auto')
        .css('width', 'auto');
      node.height = $node.outerHeight(true);
    });
  }
};

scout.OutlineLayout.prototype._setDataHeight = function(heightOffset) {
  var titleSize = null;
  if (this.outline.titleVisible) {
    titleSize = scout.graphics.getSize(this.outline.$title, true);
  }
  scout.OutlineLayout.parent.prototype._setDataHeight.call(this, heightOffset + (titleSize === null ? 0 : titleSize.height));
};
