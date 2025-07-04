/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {DateColumnUserFilter, DateColumnUserFilterStateDo, dates, IUserFilterStateDo, scout, Table, TableUserFilter, UserFilterStateMapper, UserFilterStateMappers} from '../../index';

export class DateColumnUserFilterStateMapper extends UserFilterStateMapper<DateColumnUserFilter, DateColumnUserFilterStateDo> {

  protected override _acceptFilter(filter: TableUserFilter): filter is DateColumnUserFilter {
    return filter instanceof DateColumnUserFilter;
  }

  protected override _acceptFilterState(filterState: IUserFilterStateDo): filterState is DateColumnUserFilterStateDo {
    return filterState instanceof DateColumnUserFilterStateDo;
  }

  protected override _toDo(table: Table, filter: DateColumnUserFilter): DateColumnUserFilterStateDo {
    return scout.create(DateColumnUserFilterStateDo, {
      columnId: filter.column.buildUuid(),
      selectedValues: new Set(filter.selectedValues),
      dateFrom: filter.dateFrom,
      dateTo: filter.dateTo
    });
  }

  protected override _fromDo(table: Table, filterState: DateColumnUserFilterStateDo): DateColumnUserFilter {
    const session = table.session;
    const column = table.columnByUuid(filterState.columnId);
    return scout.create(column.filterType, {
      session: session,
      table: table,
      column: column,
      selectedValues: [...filterState.selectedValues],
      dateFrom: dates.toJsonDate(filterState.dateFrom), // FIXME bsh [js-preferences] There has to be an easier way!
      dateTo: dates.toJsonDate(filterState.dateTo)
    });
  }
}

UserFilterStateMappers.get().register(DateColumnUserFilterStateMapper);
