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
import {Menu, scout, TableFooter, TableInfoLoadTooltipModel, Tooltip} from '../index';
import {InitModelOf} from '../scout';
import {SomeRequired} from '../types';

export default class TableInfoLoadTooltip extends Tooltip implements TableInfoLoadTooltipModel {
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
