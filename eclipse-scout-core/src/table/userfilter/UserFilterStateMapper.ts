/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {IUserFilterStateDo, scout, Table, TableUserFilter} from '../../index';

export abstract class UserFilterStateMapper<TFilter extends TableUserFilter = TableUserFilter, TFilterState extends IUserFilterStateDo = IUserFilterStateDo> {

  tryToDo(table: Table, filter: TableUserFilter): IUserFilterStateDo {
    if (this._acceptFilter(filter)) {
      return this._toDo(table, filter);
    }
    return null;
  }

  tryFromDo(table: Table, filterState: IUserFilterStateDo): TableUserFilter {
    if (this._acceptFilterState(filterState)) {
      return this._fromDo(table, filterState);
    }
    return null;
  }

  protected _createColumnFilter(table: Table, columnId: string, model: any): TFilter {
    const session = table.session;
    const column = table.columnById(columnId);
    return scout.create(column.filterType, {
      session: session,
      table: table,
      column: column,
      ...model
    });
  }

  protected abstract _acceptFilter(filter: TableUserFilter): filter is TFilter;

  protected abstract _acceptFilterState(filterState: IUserFilterStateDo): filterState is TFilterState;

  protected abstract _toDo(table: Table, filter: TFilter): TFilterState;

  protected abstract _fromDo(table: Table, filterState: TFilterState): TFilter;
}
