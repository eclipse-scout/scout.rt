/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BooleanColumn, BooleanColumnUserFilterStateDo, ColumnUserFilter, IUserFilterStateDo, scout, Table, TableUserFilter, UserFilterStateMapper, UserFilterStateMappers} from '../../index';

export class BooleanColumnUserFilterStateMapper extends UserFilterStateMapper<ColumnUserFilter, BooleanColumnUserFilterStateDo> {

  protected override _acceptFilter(filter: TableUserFilter): filter is ColumnUserFilter {
    return filter instanceof ColumnUserFilter && filter.column instanceof BooleanColumn;
  }

  protected override _acceptFilterState(filterState: IUserFilterStateDo): filterState is BooleanColumnUserFilterStateDo {
    return filterState instanceof BooleanColumnUserFilterStateDo;
  }

  protected override _toDo(table: Table, filter: ColumnUserFilter): BooleanColumnUserFilterStateDo {
    return scout.create(BooleanColumnUserFilterStateDo, {
      columnId: filter.column.buildUuid(),
      selectedValues: new Set(filter.selectedValues.map(value => this._valueToBoolean(value)))
    });
  }

  protected override _fromDo(table: Table, filterState: BooleanColumnUserFilterStateDo): ColumnUserFilter {
    const session = table.session;
    const column = table.columnById(filterState.columnId);
    return scout.create(column.filterType, {
      session: session,
      table: table,
      column: column,
      selectedValues: [...filterState.selectedValues].map(value => this._valueFromBoolean(value))
    });
  }

  protected _valueToBoolean(value: string | number) {
    if (value === 1) {
      return true;
    }
    if (value === -1) { // undefined
      return null;
    }
    return false;
  }

  protected _valueFromBoolean(value: boolean) {
    if (value === true) {
      return 1;
    }
    if (value === false) {
      return 0;
    }
    return -1; // undefined
  }
}

UserFilterStateMappers.get().register(BooleanColumnUserFilterStateMapper);
