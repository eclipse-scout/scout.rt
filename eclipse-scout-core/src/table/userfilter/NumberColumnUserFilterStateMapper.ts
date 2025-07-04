/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {IUserFilterStateDo, NumberColumnUserFilter, NumberColumnUserFilterStateDo, scout, Table, TableUserFilter, UserFilterStateMapper, UserFilterStateMappers} from '../../index';

export class NumberColumnUserFilterStateMapper extends UserFilterStateMapper<NumberColumnUserFilter, NumberColumnUserFilterStateDo> {

  protected override _acceptFilter(filter: TableUserFilter): filter is NumberColumnUserFilter {
    return filter instanceof NumberColumnUserFilter;
  }

  protected override _acceptFilterState(filterState: IUserFilterStateDo): filterState is NumberColumnUserFilterStateDo {
    return filterState instanceof NumberColumnUserFilterStateDo;
  }

  protected override _toDo(table: Table, filter: NumberColumnUserFilter): NumberColumnUserFilterStateDo {
    return scout.create(NumberColumnUserFilterStateDo, {
      columnId: filter.column.buildUuid(),
      selectedValues: new Set(filter.selectedValues),
      numberFrom: filter.numberFrom,
      numberTo: filter.numberTo
    });
  }

  protected override _fromDo(table: Table, filterState: NumberColumnUserFilterStateDo): NumberColumnUserFilter {
    const session = table.session;
    const column = table.columnByUuid(filterState.columnId);
    return scout.create(column.filterType, {
      session: session,
      table: table,
      column: column,
      selectedValues: [...filterState.selectedValues],
      numberFrom: filterState.numberFrom,
      numberTo: filterState.numberTo
    });
  }
}

UserFilterStateMappers.get().register(NumberColumnUserFilterStateMapper);
