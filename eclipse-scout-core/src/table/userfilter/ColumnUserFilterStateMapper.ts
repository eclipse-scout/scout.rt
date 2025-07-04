/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {ColumnUserFilter, ColumnUserFilterStateDo, IUserFilterStateDo, scout, Table, TableUserFilter, UserFilterStateMapper, UserFilterStateMappers} from '../../index';

export class ColumnUserFilterStateMapper extends UserFilterStateMapper<ColumnUserFilter, ColumnUserFilterStateDo> {

  protected override _acceptFilter(filter: TableUserFilter): filter is ColumnUserFilter {
    return filter instanceof ColumnUserFilter;
  }

  protected override _acceptFilterState(filterState: IUserFilterStateDo): filterState is ColumnUserFilterStateDo {
    return filterState instanceof ColumnUserFilterStateDo;
  }

  protected override _toDo(table: Table, filter: ColumnUserFilter): ColumnUserFilterStateDo {
    return scout.create(ColumnUserFilterStateDo, {
      columnId: filter.column.buildUuid(),
      selectedValues: new Set(filter.selectedValues)
    });
  }

  protected override _fromDo(table: Table, filterState: ColumnUserFilterStateDo): ColumnUserFilter {
    const session = table.session;
    const column = table.columnByUuid(filterState.columnId);
    return scout.create(column.filterType, {
      session: session,
      table: table,
      column: column,
      selectedValues: [...filterState.selectedValues]
    });
  }
}

// default mapper, lower priority than other mappers
UserFilterStateMappers.get().register(ColumnUserFilterStateMapper, 6_000);
