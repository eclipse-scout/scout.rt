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
import {Table, TableRow} from '../index';
import {RefModel} from '../types';
import {ObjectType} from '../ObjectFactory';

export default interface TableRowModel {
  objectType?: ObjectType<TableRow, TableRowModel>;
  /**
   * Cell instances or cell values.
   */
  cells?: any[];
  checked?: boolean;
  enabled?: boolean;
  id?: string;
  iconId?: string;
  cssClass?: string;
  parentRow?: string | TableRow | RefModel<TableRowModel>;
  parent: Table;
  expanded?: boolean;
  dataMap?: Record<PropertyKey, any>;
}

export type TableRowData = Omit<TableRowModel, 'parent'>;
