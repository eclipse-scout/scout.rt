/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {TableAdapter, TableRowModel} from '../../index';

export class SpecTableAdapter extends TableAdapter {
  override _onRowsDeleted(rowIds: string[]) {
    super._onRowsDeleted(rowIds);
  }

  override _onRowsSelected(rowIds: string[]) {
    super._onRowsSelected(rowIds);
  }

  override _onRowsChecked(rows: TableRowModel[]) {
    super._onRowsChecked(rows);
  }

  override _onRowsExpanded(rows: TableRowModel[]) {
    super._onRowsExpanded(rows);
  }

  override _sendFilter(rowIds: string[]) {
    super._sendFilter(rowIds);
  }
}
