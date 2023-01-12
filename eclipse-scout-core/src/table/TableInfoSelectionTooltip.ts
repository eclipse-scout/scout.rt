/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {InitModelOf, Menu, scout, SomeRequired, TableFooter, TableInfoSelectionTooltipModel, Tooltip} from '../index';

export class TableInfoSelectionTooltip extends Tooltip implements TableInfoSelectionTooltipModel {
  declare model: TableInfoSelectionTooltipModel;
  declare initModel: SomeRequired<this['model'], 'parent' | 'tableFooter'>;

  tableFooter: TableFooter;

  protected override _init(options: InitModelOf<this>) {
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
