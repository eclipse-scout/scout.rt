/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {LookupBoxModel, ObjectOrChildModel, Table} from '../../../index';

export interface ListBoxModel<TLookup, TValue = TLookup[]> extends LookupBoxModel<TLookup, TValue> {
  table?: ObjectOrChildModel<Table>;
}
