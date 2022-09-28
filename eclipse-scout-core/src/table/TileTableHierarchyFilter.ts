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
import {Column, Session, Table, TableFilter, TableFilterModel, TableRow} from '../index';
import $ from 'jquery';

export default class TileTableHierarchyFilter implements TableFilter {
  declare model: TableFilterModel;

  session: Session;
  table: Table;
  column: Column;

  constructor() {
    // nop
  }

  init(model: TableFilterModel) {
    $.extend(this, model);
  }

  accept(row: TableRow): boolean {
    return !row.parentRow;
  }

  createLabel(): string {
    return this.table.session.text('ui.TileView');
  }
}
