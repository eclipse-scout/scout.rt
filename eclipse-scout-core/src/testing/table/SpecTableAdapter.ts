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
import {TableAdapter} from '../../index';
import {TableRowData} from '../../table/TableRowModel';

export default class SpecTableAdapter extends TableAdapter {
  override _onRowsDeleted(rowIds: string[]) {
    super._onRowsDeleted(rowIds);
  }

  override _onRowsSelected(rowIds: string[]) {
    super._onRowsSelected(rowIds);
  }

  override _onRowsChecked(rows: TableRowData[]) {
    super._onRowsChecked(rows);
  }

  override _onRowsExpanded(rows: TableRowData[]) {
    super._onRowsExpanded(rows);
  }

  override _sendFilter(rowIds: string[]) {
    super._sendFilter(rowIds);
  }
}
