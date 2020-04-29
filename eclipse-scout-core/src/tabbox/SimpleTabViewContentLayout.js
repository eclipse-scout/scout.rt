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
import {AbstractLayout, Dimension, HtmlComponent} from '../index';

export default class SimpleTabViewContentLayout extends AbstractLayout {

  constructor(tabBox) {
    super();
    this.tabBox = tabBox;
  }

  layout($container) {
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

  preferredLayoutSize($container) {
    let currentView = this.tabBox.currentView;
    if (!currentView || !currentView.rendered || !currentView.htmlComp) {
      return new Dimension();
    }

    let htmlContainer = HtmlComponent.get($container);
    let prefSize = currentView.htmlComp.prefSize()
      .add(htmlContainer.insets())
      .add(currentView.htmlComp.margins());

    return prefSize;
  }
}
