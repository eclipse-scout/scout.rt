/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {AbstractLayout, graphics} from '../../index';

export default class ViewButtonBoxLayout extends AbstractLayout {

  constructor(viewButtonBox) {
    super();
    this.viewButtonBox = viewButtonBox;
  }

  layout($container) {
    let tabs = this.viewButtonBox.tabButtons.filter(tab => {
        return tab.visible;
      }),
      viewMenuTab = this.viewButtonBox.viewMenuTab,
      htmlComp = this.viewButtonBox.htmlComp,
      containerWidth = htmlComp.size().width,
      tabWidth = containerWidth / tabs.length;

    if (viewMenuTab.visible && viewMenuTab.selectedButton.rendered) {
      if (viewMenuTab.selectedButton) {
        tabWidth = (containerWidth - graphics.size(viewMenuTab.dropdown.$container, {
          exact: true
        }).width) / (tabs.length + 1);
        viewMenuTab.selectedButton.$container.cssWidth(tabWidth);
      }

      containerWidth -= graphics.size(viewMenuTab.$container, {
        exact: true
      }).width;
    }

    tabs.forEach((tab, index) => {
      if (tabs.length - 1 === index) {
        // to avoid pixel fault due to rounding issues calculate the rest for the last tab.
        // Round up to the second digit otherwise at least Chrome may still show the background of the view button box (at least in compact mode)
        tab.$container.cssWidth(Math.ceil(containerWidth * 100) / 100);
      } else {
        tab.$container.cssWidth(tabWidth);
        containerWidth -= tab.$container.cssWidth();
      }
    }, this);
  }

  preferredLayoutSize($container) {
    // View buttons have an absolute css height set -> useCssSize = true
    return graphics.prefSize($container, {
      useCssSize: true
    });
  }
}
