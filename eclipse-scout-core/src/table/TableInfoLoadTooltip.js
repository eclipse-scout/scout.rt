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

export default class TableInfoLoadTooltip extends Tooltip {

  constructor() {
    super();
  }

  _init(options) {
    super._init(options);

    this.tableFooter = options.tableFooter;
  }

  _renderText() {
    let table = this.tableFooter.table,
      numRows = table.rows.length;

    this.$content.appendSpan().text(this.session.text('ui.NumRowsLoaded', this.tableFooter.computeCountInfo(numRows)));
    this.$content.appendBr();
    this.$content.appendSpan('link')
      .text(this.session.text('ui.ReloadData'))
      .on('click', this._onReloadClick.bind(this));
  }

  _onReloadClick() {
    this.tableFooter.table.reload();
    this.destroy();
  }
}
