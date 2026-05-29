/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {IUserFilterStateDo, ObjectRegistries, ObjectRegistry, Table, TableUserFilter, UserFilterStateMapper} from '../../index';

/**
 * Central registry for all available {@link UserFilterStateMapper} instances.
 */
export class UserFilterStateMappers extends ObjectRegistry<UserFilterStateMapper> {

  static get(): UserFilterStateMappers {
    return ObjectRegistries.get(UserFilterStateMappers);
  }

  static all(): UserFilterStateMapper[] {
    return UserFilterStateMappers.get().all();
  }

  /**
   * Loops through {@link all} registered mappers, calls {@link UserFilterStateMapper.tryFromDo}
   * and returns the first non-null result.
   */
  static fromDo(table: Table, filterState: IUserFilterStateDo): TableUserFilter {
    for (let mapper of UserFilterStateMappers.all()) {
      let filter = mapper.tryFromDo(table, filterState);
      if (filter) {
        return filter;
      }
    }
    return null;
  }

  /**
   * Loops through {@link all} registered mappers, calls {@link UserFilterStateMapper.tryToDo}
   * and returns the first non-null result.
   */
  static toDo(table: Table, filter: TableUserFilter): IUserFilterStateDo {
    for (let mapper of UserFilterStateMappers.all()) {
      let filterState = mapper.tryToDo(table, filter);
      if (filterState) {
        return filterState;
      }
    }
    return null;
  }
}
