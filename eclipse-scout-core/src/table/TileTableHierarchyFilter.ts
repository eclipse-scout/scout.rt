/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Filter, InitModelOf, Table, TableRow} from '../index';
import $ from 'jquery';

export interface TileTableHierarchyFilterModel {
  table: Table;
}

export class TileTableHierarchyFilter implements TileTableHierarchyFilterModel, Filter<TableRow> {
  declare model: TileTableHierarchyFilterModel;

  table: Table;

  init(model: InitModelOf<this>) {
    $.extend(this, model);
  }

  accept(row: TableRow): boolean {
    return !row.parentRow;
  }

  createLabel(): string {
    return this.table.session.text('ui.TileView');
  }
}
