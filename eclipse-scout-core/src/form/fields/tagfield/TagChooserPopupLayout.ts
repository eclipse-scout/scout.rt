/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Dimension, HtmlCompPrefSizeOptions, PopupLayout, scout, TableLayoutResetter, TagChooserPopup} from '../../../index';

export class TagChooserPopupLayout extends PopupLayout {
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
