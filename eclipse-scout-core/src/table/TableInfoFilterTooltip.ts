/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {InitModelOf, Menu, scout, SomeRequired, TableFooter, TableInfoFilterTooltipModel, Tooltip} from '../index';

export class TableInfoFilterTooltip extends Tooltip implements TableInfoFilterTooltipModel {
  declare model: TableInfoFilterTooltipModel;
  declare initModel: SomeRequired<this['model'], 'parent' | 'tableFooter'>;

  tableFooter: TableFooter;

  protected override _init(options: InitModelOf<this>) {
    super._init(options);
    this.tableFooter = options.tableFooter;
    let removeFilterMenu = scout.create(Menu, {
      parent: this,
      text: this.session.text('ui.RemoveFilter')
    });
    removeFilterMenu.on('action', this._onRemoveFilterClick.bind(this));
    this.setMenus(removeFilterMenu);
  }

  protected override _renderText() {
    let table = this.tableFooter.table,
      numRowsFiltered = table.filteredRows().length,
      filteredBy = table.filteredBy().join(', '); // filteredBy() returns an array
    this.$content.appendSpan()
      .text(this.session.text('ui.NumRowsFilteredBy', this.tableFooter.computeCountInfo(numRowsFiltered), filteredBy));
  }

  protected _onRemoveFilterClick() {
    this.tableFooter.table.resetUserFilter();
    this.destroy();
  }
}
