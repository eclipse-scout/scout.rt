/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractLayout, Dimension, HtmlComponent, HtmlCompPrefSizeOptions, SimpleTabBox} from '../index';

export class SimpleTabViewContentLayout extends AbstractLayout {
  tabBox: SimpleTabBox;

  constructor(tabBox: SimpleTabBox) {
    super();
    this.tabBox = tabBox;
  }

  override layout($container: JQuery) {
    let currentView = this.tabBox.currentView;
    if (!currentView || !currentView.rendered || !currentView.htmlComp) {
      return;
    }

    let htmlContainer = HtmlComponent.get($container);
    let size = htmlContainer.availableSize({exact: true})
      .subtract(htmlContainer.insets())
      .subtract(currentView.htmlComp.margins());

    currentView.htmlComp.setSize(size);
  }

  override preferredLayoutSize($container: JQuery, options?: HtmlCompPrefSizeOptions): Dimension {
    let currentView = this.tabBox.currentView;
    if (!currentView || !currentView.rendered || !currentView.htmlComp) {
      return new Dimension();
    }

    let htmlContainer = HtmlComponent.get($container);
    return currentView.htmlComp.prefSize()
      .add(htmlContainer.insets())
      .add(currentView.htmlComp.margins());
  }
}
