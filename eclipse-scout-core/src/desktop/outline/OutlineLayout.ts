/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Dimension, graphics, Outline, TreeLayout} from '../../index';

export class OutlineLayout extends TreeLayout {
  outline: Outline;

  constructor(outline: Outline) {
    super(outline);
    this.outline = outline;
  }

  protected override _layout($container: JQuery) {
    let htmlContainer = this.outline.htmlComp;

    super._layout($container);

    let containerSize = htmlContainer.availableSize({exact: true}).subtract(htmlContainer.insets());

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

  protected override _setDataHeight(heightOffset: number) {
    let titleSize: Dimension = null;
    if (this.outline.titleVisible) {
      titleSize = graphics.size(this.outline.$title, true);
    }
    super._setDataHeight(heightOffset + (titleSize === null ? 0 : titleSize.height));
  }
}
