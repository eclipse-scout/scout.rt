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
import {ObjectWithType, Session} from '../index';
import {QueryByType} from './QueryBy';

export default interface LookupCallModel<Key> extends ObjectWithType {
  session: Session;
  hierarchical?: boolean;
  loadIncremental?: boolean;

  /** indicates if the lookup call implements 'getByKeys' and therefore supports 'textsByKeys' */
  batch?: boolean;

  queryBy?: QueryByType;

  /** used on QueryBy.TEXT */
  searchText?: string;

  /** used on QueryBy.KEY */
  key?: Key;

  /** used on QueryBy.KEYS */
  keys?: Key[];

  /** used on QueryBy.REC */
  parentKey?: Key;

  active?: boolean;

  /**
   * A positive number, _not_ null or undefined!
   * This value is not directly used by this class but a child class my use it to limit the returned row count.
   * Default value is 100.
   */
  maxRowCount?: number;
}
