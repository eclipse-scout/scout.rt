/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Tooltip} from '../index';

export default class TableInfoSelectionTooltip extends Tooltip {

  constructor() {
    super();
  }

  _init(options) {
    super._init(options);
    this.tableFooter = options.tableFooter;
    let selectNoneMenu = scout.create('Menu', {
      parent: this,
      text: this.session.text('ui.SelectNone')
    });
    selectNoneMenu.on('action', this._onSelectNoneClick.bind(this));
    let selectAllMenu = scout.create('Menu', {
      parent: this,
      text: this.session.text('ui.SelectAll')
    });
    selectAllMenu.on('action', this._onSelectAllClick.bind(this));
    this.setMenus([selectNoneMenu, selectAllMenu]);
  }

  _renderText() {
    let table = this.tableFooter.table,
      numRowsSelected = table.selectedRows.length;
    this.$content.text(this.session.text('ui.NumRowsSelected', this.tableFooter.computeCountInfo(numRowsSelected)));
  }

  _onSelectNoneClick() {
    this.tableFooter.table.deselectAll();
    this.destroy();
  }

  _onSelectAllClick() {
    this.tableFooter.table.selectAll();
    this.destroy();
  }
}
