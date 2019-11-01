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
import {AbstractLayout} from '../index';
import {HtmlComponent} from '../index';
import {Dimension} from '../index';

export default class SimpleTabViewContentLayout extends AbstractLayout {

constructor(tabBox) {
  super();
  this.tabBox = tabBox;
}


layout($container) {
  var currentView = this.tabBox.currentView;
  if (!currentView || !currentView.rendered || !currentView.htmlComp) {
    return;
  }

  var htmlContainer = HtmlComponent.get($container);
  var size = htmlContainer.availableSize()
    .subtract(htmlContainer.insets())
    .subtract(currentView.htmlComp.margins());

  currentView.htmlComp.setSize(size);
}

preferredLayoutSize($container) {
  var currentView = this.tabBox.currentView;
  if (!currentView || !currentView.rendered || !currentView.htmlComp) {
    return new Dimension();
  }

  var htmlContainer = HtmlComponent.get($container);
  var prefSize = currentView.htmlComp.prefSize()
    .add(htmlContainer.insets())
    .add(currentView.htmlComp.margins());

  return prefSize;
}
}
