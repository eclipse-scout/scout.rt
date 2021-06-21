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

export default class TableInfoLoadTooltip extends Tooltip {

  constructor() {
    super();
  }

  _init(options) {
    super._init(options);
    this.tableFooter = options.tableFooter;
    let reloadDataMenu = scout.create('Menu', {
      parent: this,
      text: this.session.text('ui.ReloadData')
    });
    reloadDataMenu.on('action', this._onReloadClick.bind(this));
    this.setMenus(reloadDataMenu);
  }

  _renderText() {
    let table = this.tableFooter.table,
      numRows = table.rows.length;
    this.$content.text(this.session.text('ui.NumRowsLoaded', this.tableFooter.computeCountInfo(numRows)));
  }

  _onReloadClick() {
    this.tableFooter.table.reload();
    this.destroy();
  }
}
