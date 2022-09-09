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
import {Dimension, graphics, TreeLayout} from '../../index';

export default class OutlineLayout extends TreeLayout {

  constructor(outline) {
    super(outline);
    this.outline = outline;
  }

  _layout($container) {
    let containerSize,
      htmlContainer = this.outline.htmlComp;

    super._layout($container);

    containerSize = htmlContainer.availableSize({exact: true})
      .subtract(htmlContainer.insets());

    if (this.outline.embedDetailContent) {
      let selectedNode = this.outline.selectedNodes[0];
      if (selectedNode && selectedNode.rendered) {
        let pageHtmlComp = selectedNode.htmlComp;
        // pageHtmlComp is null if there is no detail form and no detail menubar
        if (pageHtmlComp) {
          let prefSize = pageHtmlComp.prefSize({widthHint: containerSize.width});
          pageHtmlComp.setSize(new Dimension(containerSize.width, prefSize.height));
          selectedNode.height = prefSize.height + pageHtmlComp.margins().vertical();
        }
      }
    }

    // layout menu bars
    this.outline.titleMenuBar.validateLayout();
    this.outline.nodeMenuBar.validateLayout();
    this.outline.detailMenuBar.validateLayout();
  }

  _setDataHeight(heightOffset) {
    /** @type {Dimension} */
    let titleSize = null;
    if (this.outline.titleVisible) {
      titleSize = graphics.size(this.outline.$title, true);
    }
    super._setDataHeight(heightOffset + (titleSize === null ? 0 : titleSize.height));
  }
}
