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

export default class TableInfoFilterTooltip extends Tooltip {

  constructor() {
    super();
  }

  _init(options) {
    super._init(options);
    this.tableFooter = options.tableFooter;
    let removeFilterMenu = scout.create('Menu', {
      parent: this,
      text: this.session.text('ui.RemoveFilter')
    });
    removeFilterMenu.on('action', this._onRemoveFilterClick.bind(this));
    this.setMenus(removeFilterMenu);
  }

  _renderText() {
    let table = this.tableFooter.table,
      numRowsFiltered = table.filteredRows().length,
      filteredBy = table.filteredBy().join(', '); // filteredBy() returns an array
    this.$content.appendSpan()
      .text(this.session.text('ui.NumRowsFilteredBy', this.tableFooter.computeCountInfo(numRowsFiltered), filteredBy));
  }

  _onRemoveFilterClick() {
    this.tableFooter.table.resetUserFilter();
    this.destroy();
  }
}
