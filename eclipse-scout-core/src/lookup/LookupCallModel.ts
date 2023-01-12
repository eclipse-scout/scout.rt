/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {LookupCall, ObjectModel, QueryBy, Session} from '../index';

export interface LookupCallModel<TKey> extends ObjectModel<LookupCall<TKey>> {
  session?: Session;
  hierarchical?: boolean;
  loadIncremental?: boolean;

  /** indicates if the lookup call implements 'getByKeys' and therefore supports 'textsByKeys' */
  batch?: boolean;

  queryBy?: QueryBy;

  /** used on {@link QueryBy.TEXT} */
  searchText?: string;

  /** used on {@link QueryBy.KEY} */
  key?: TKey;

  /** used on {@link QueryBy.KEYS} */
  keys?: TKey[];

  /** used on {@link QueryBy.REC} */
  parentKey?: TKey;

  active?: boolean;

  /**
   * A positive number, _not_ null or undefined!
   * This value is not directly used by this class but a child class my use it to limit the returned row count.
   * Default value is 100.
   */
  maxRowCount?: number;

  [property: string]: any; // allow custom properties
}
