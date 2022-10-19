/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Dimension, HtmlCompPrefSizeOptions, PopupLayout, scout, TableLayoutResetter, TagChooserPopup} from '../../../index';

export default class TagChooserPopupLayout extends PopupLayout {
  declare popup: TagChooserPopup;

  constructor(popup: TagChooserPopup) {
    super(popup);
    this.doubleCalcPrefSize = false;
  }

  override layout($container: JQuery) {
    super.layout($container);

    // layout table
    let htmlComp = this.popup.htmlComp;
    let size = htmlComp.size().subtract(htmlComp.insets());
    this.popup.table.htmlComp.setSize(size);

    this.popup.position();
  }

  override preferredLayoutSize($container: JQuery, options?: HtmlCompPrefSizeOptions): Dimension {
    let tableHandler = scout.create(TableLayoutResetter, this.popup.table);
    tableHandler.modifyDom();
    let prefSize = super.preferredLayoutSize($container);
    tableHandler.restoreDom();
    return prefSize;
  }
}
