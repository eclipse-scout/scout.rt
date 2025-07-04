/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {IUserFilterStateDo, scout, Table, TableTextUserFilter, TableTextUserFilterStateDo, TableUserFilter, UserFilterStateMapper, UserFilterStateMappers} from '../../index';

export class TableTextUserFilterStateMapper extends UserFilterStateMapper<TableTextUserFilter, TableTextUserFilterStateDo> {

  protected override _acceptFilter(filter: TableUserFilter): filter is TableTextUserFilter {
    return filter instanceof TableTextUserFilter;
  }

  protected override _acceptFilterState(filterState: IUserFilterStateDo): filterState is TableTextUserFilterStateDo {
    return filterState instanceof TableTextUserFilterStateDo;
  }

  protected override _toDo(table: Table, filter: TableTextUserFilter): TableTextUserFilterStateDo {
    return scout.create(TableTextUserFilterStateDo, {
      text: filter.text
    });
  }

  protected override _fromDo(table: Table, filterState: TableTextUserFilterStateDo): TableTextUserFilter {
    const session = table.session;
    return scout.create(TableTextUserFilter, {
      session: session,
      table: table,
      text: filterState.text
    });
  }
}

UserFilterStateMappers.get().register(TableTextUserFilterStateMapper);
