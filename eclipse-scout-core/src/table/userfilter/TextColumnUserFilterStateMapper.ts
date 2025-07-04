/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {IUserFilterStateDo, scout, Table, TableUserFilter, TextColumnUserFilter, TextColumnUserFilterStateDo, UserFilterStateMapper, UserFilterStateMappers} from '../../index';

export class TextColumnUserFilterStateMapper extends UserFilterStateMapper<TextColumnUserFilter, TextColumnUserFilterStateDo> {

  protected override _acceptFilter(filter: TableUserFilter): filter is TextColumnUserFilter {
    return filter instanceof TextColumnUserFilter;
  }

  protected override _acceptFilterState(filterState: IUserFilterStateDo): filterState is TextColumnUserFilterStateDo {
    return filterState instanceof TextColumnUserFilterStateDo;
  }

  protected override _toDo(table: Table, filter: TextColumnUserFilter): TextColumnUserFilterStateDo {
    return scout.create(TextColumnUserFilterStateDo, {
      columnId: filter.column.buildUuid(),
      selectedValues: new Set(filter.selectedValues),
      textFilter: filter.freeText
    });
  }

  protected override _fromDo(table: Table, filterState: TextColumnUserFilterStateDo): TextColumnUserFilter {
    const session = table.session;
    const column = table.columnByUuid(filterState.columnId);
    return scout.create(column.filterType, {
      session: session,
      table: table,
      column: column,
      selectedValues: [...filterState.selectedValues],
      freeText: filterState.textFilter
    });
  }
}

UserFilterStateMappers.get().register(TextColumnUserFilterStateMapper);
