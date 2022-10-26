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
import {Menu, scout, TableFooter, TableInfoSelectionTooltipModel, Tooltip} from '../index';

export default class TableInfoSelectionTooltip extends Tooltip implements TableInfoSelectionTooltipModel {
  declare model: TableInfoSelectionTooltipModel;

  tableFooter: TableFooter;

  constructor() {
    super();
  }

  protected override _init(options: TableInfoSelectionTooltipModel) {
    super._init(options);
    this.tableFooter = options.tableFooter;
    let selectNoneMenu = scout.create(Menu, {
      parent: this,
      text: this.session.text('ui.SelectNone')
    });
    selectNoneMenu.on('action', this._onSelectNoneClick.bind(this));
    let selectAllMenu = scout.create(Menu, {
      parent: this,
      text: this.session.text('ui.SelectAll')
    });
    selectAllMenu.on('action', this._onSelectAllClick.bind(this));
    this.setMenus([selectNoneMenu, selectAllMenu]);
  }

  protected override _renderText() {
    let table = this.tableFooter.table,
      numRowsSelected = table.selectedRows.length;
    this.$content.text(this.session.text('ui.NumRowsSelected', this.tableFooter.computeCountInfo(numRowsSelected)));
  }

  protected _onSelectNoneClick() {
    this.tableFooter.table.deselectAll();
    this.destroy();
  }

  protected _onSelectAllClick() {
    this.tableFooter.table.selectAll();
    this.destroy();
  }
}
