/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {LookupCall, QueryBy, Session} from '../index';
import {ObjectType} from '../ObjectFactory';

export default interface LookupCallModel<TKey> {
  session: Session;
  objectType?: ObjectType<LookupCall<TKey>>;
  hierarchical?: boolean;
  loadIncremental?: boolean;

  /** indicates if the lookup call implements 'getByKeys' and therefore supports 'textsByKeys' */
  batch?: boolean;

  queryBy?: QueryBy;

  /** used on QueryBy.TEXT */
  searchText?: string;

  /** used on QueryBy.KEY */
  key?: TKey;

  /** used on QueryBy.KEYS */
  keys?: TKey[];

  /** used on QueryBy.REC */
  parentKey?: TKey;

  active?: boolean;

  /**
   * A positive number, _not_ null or undefined!
   * This value is not directly used by this class but a child class my use it to limit the returned row count.
   * Default value is 100.
   */
  maxRowCount?: number;
}
