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
