/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {icons, scout, StaticLookupCall} from '../index';

export default class TileTableHeaderSortByLookupCall extends StaticLookupCall {

  constructor() {
    super();
    this.table = null;
  }

  _init(model) {
    super._init(model);
  }

  _data() {
    let lookupRows = [];
    this.table.visibleColumns().forEach(function(column) {
      if (column.isSortingPossible()) {
        lookupRows.push([
          {
            column: column,
            asc: true
          },
          scout.nvl(column.text, column.headerTooltipText) + ' (' + this.session.text('ui.ascending') + ')',
          icons.LONG_ARROW_UP_BOLD
        ]);
        lookupRows.push([
          {
            column: column,
            asc: false
          },
          scout.nvl(column.text, column.headerTooltipText) + ' (' + this.session.text('ui.descending') + ')',
          icons.LONG_ARROW_DOWN_BOLD
        ]);
      }
    }, this);
    return lookupRows;
  }

  _dataToLookupRow(data) {
    return scout.create('LookupRow', {
      key: data[0],
      text: data[1],
      iconId: data[2]
    });
  }
}
