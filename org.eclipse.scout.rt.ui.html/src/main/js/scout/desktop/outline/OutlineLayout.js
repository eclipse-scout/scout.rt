/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {TreeLayout} from '../../index';
import {Dimension} from '../../index';
import {graphics} from '../../index';

export default class OutlineLayout extends TreeLayout {

constructor(outline) {
  super( outline);
  this.outline = outline;
}


_layout($container) {
  var containerSize,
    htmlContainer = this.outline.htmlComp;

  super._layout( $container);

  containerSize = htmlContainer.availableSize()
    .subtract(htmlContainer.insets());

  if (this.outline.embedDetailContent) {
    var selectedNode = this.outline.selectedNodes[0];
    if (selectedNode && selectedNode.rendered) {
      var pageHtmlComp = selectedNode.htmlComp;
      // pageHtmlComp is null if there is no detail form and no detail menubar
      if (pageHtmlComp) {
        // Fix width so that prefSize returns the appropriate height (necessary for elements with text wrap)
        pageHtmlComp.$comp.cssWidth(containerSize.width);

        var prefSize = pageHtmlComp.prefSize();
        pageHtmlComp.setSize(new Dimension(containerSize.width, prefSize.height));
        selectedNode.height = prefSize.height + pageHtmlComp.margins().vertical();
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

  // layout menu bars
  this.outline.titleMenuBar.validateLayout();
  this.outline.nodeMenuBar.validateLayout();
  this.outline.detailMenuBar.validateLayout();
}

_setDataHeight(heightOffset) {
  var titleSize = null;
  if (this.outline.titleVisible) {
    titleSize = graphics.size(this.outline.$title, true);
  }
  super._setDataHeight( heightOffset + (titleSize === null ? 0 : titleSize.height));
}
}
