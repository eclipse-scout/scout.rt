/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {InitModelOf, Menu, scout, SomeRequired, TableFooter, TableInfoLoadTooltipModel, Tooltip} from '../index';

export class TableInfoLoadTooltip extends Tooltip implements TableInfoLoadTooltipModel {
  declare model: TableInfoLoadTooltipModel;
  declare initModel: SomeRequired<this['model'], 'parent' | 'tableFooter'>;

  tableFooter: TableFooter;

  protected override _init(options: InitModelOf<this>) {
    super._init(options);
    this.tableFooter = options.tableFooter;
    let reloadDataMenu = scout.create(Menu, {
      parent: this,
      text: this.session.text('ui.ReloadData')
    });
    reloadDataMenu.on('action', this._onReloadClick.bind(this));
    this.setMenus(reloadDataMenu);
  }

  protected override _renderText() {
    let table = this.tableFooter.table,
      numRows = table.rows.length;
    this.$content.text(this.session.text('ui.NumRowsLoaded', this.tableFooter.computeCountInfo(numRows)));
  }

  protected _onReloadClick() {
    this.tableFooter.table.reload();
    this.destroy();
  }
}
