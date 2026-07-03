/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Cell, LookupRow, ObjectModel, ObjectModelWithId, ObjectOrModel, Primitive, Table, TableRow, TableRowDropTypesDo} from '../index';

export interface TableRowModel extends ObjectModel<TableRow>, ObjectModelWithId {
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
  /**
   * Specifies whether this row can be dragged with the mouse. Only has an effect if {@link Table.rowsDraggable} is true.
   *
   * Default is true.
   */
  draggable?: boolean;
  /**
   * Specifies if another row can be dropped before, after or onto this row. Only has an effect if {@link Table.rowsDraggable} is true.
   *
   * Default is {@link TableRowDropType#ALLOWED} for all positions.
   */
  dropTypes?: TableRowDropTypesDo;

  [property: string]: any; // allow custom properties
}
