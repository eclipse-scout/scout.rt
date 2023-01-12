/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Cell, LookupRow, ObjectModel, ObjectOrModel, Primitive, Table, TableRow} from '../index';

export interface TableRowModel extends ObjectModel<TableRow> {
  parent?: Table;
  /**
   * {@link Cell} instances or cell values.
   */
  cells?: (Primitive | object | Cell)[];
  checked?: boolean;
  enabled?: boolean;
  iconId?: string;
  cssClass?: string;
  parentRow?: string | ObjectOrModel<TableRow>;
  expanded?: boolean;
  lookupRow?: LookupRow<any>;

  [property: string]: any; // allow custom properties
}
