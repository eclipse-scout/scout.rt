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
import {PopupLayout, scout} from '../../../index';

export default class TagChooserPopupLayout extends PopupLayout {

  constructor(popup) {
    super(popup);
    this.doubleCalcPrefSize = false;
  }

  layout($container) {
    super.layout($container);

    // layout table
    let htmlComp = this.popup.htmlComp;
    let size = htmlComp.size().subtract(htmlComp.insets());
    this.popup.table.htmlComp.setSize(size);

    this.popup.position();
  }

  /**
   * @override AbstractLayout.js
   */
  preferredLayoutSize($container) {
    let tableHandler = scout.create('TableLayoutResetter', this.popup.table);
    tableHandler.modifyDom();
    let prefSize = super.preferredLayoutSize($container);
    tableHandler.restoreDom();
    return prefSize;
  }
}
