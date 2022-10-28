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
import {Cell, LookupRow, Table, TableRow} from '../index';
import {Primitive} from '../types';
import {ObjectModel} from '../scout';

export default interface TableRowModel extends ObjectModel<TableRow, TableRowModel> {
  /**
   * Cell instances or cell values.
   */
  cells?: (Primitive | object | Cell)[];
  checked?: boolean;
  enabled?: boolean;
  iconId?: string;
  nodeId?: string;
  cssClass?: string;
  parentRow?: string | TableRow | TableRowData;
  parent: Table;
  expanded?: boolean;
  dataMap?: Record<PropertyKey, any>;
  lookupRow?: LookupRow<any>;
}

export type TableRowData = Omit<TableRowModel, 'parent'>;
