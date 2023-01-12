/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {LookupRow, QueryBy} from '../index';

export interface LookupResult<Key> {
  lookupRows: LookupRow<Key>[];
  queryBy: QueryBy;

  byAll?: boolean;

  byText?: boolean;
  text?: string;

  byKey?: boolean;
  key?: Key;

  byKeys?: boolean;

  byRec?: boolean;
  rec?: Key;

  exception?: string;
}
