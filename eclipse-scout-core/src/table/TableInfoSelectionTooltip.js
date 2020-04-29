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
import {Tooltip} from '../index';

export default class TableInfoSelectionTooltip extends Tooltip {

  constructor() {
    super();
  }

  _init(options) {
    super._init(options);

    this.tableFooter = options.tableFooter;
  }

  _renderText() {
    let table = this.tableFooter.table,
      numRowsSelected = table.selectedRows.length;

    this.$content.appendSpan().text(this.session.text('ui.NumRowsSelected', this.tableFooter.computeCountInfo(numRowsSelected)));
    this.$content.appendBr();
    this.$content.appendSpan('link')
      .text(this.session.text('ui.SelectNone'))
      .on('click', this._onSelectNoneClick.bind(this));
    this.$content.appendBr();
    this.$content.appendSpan('link')
      .text(this.session.text('ui.SelectAll'))
      .on('click', this._onSelectAllClick.bind(this));
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
