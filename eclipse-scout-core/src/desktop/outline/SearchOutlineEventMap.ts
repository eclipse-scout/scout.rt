/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Event, OutlineEventMap, PropertyChangeEvent, SearchOutline} from '../../index';

export interface SearchOutlineEventMap extends OutlineEventMap {
  'search': Event<SearchOutline>;
  'resetSearch': Event<SearchOutline>;
  'propertyChange:limited': PropertyChangeEvent<number>;
  'propertyChange:maxSearchFieldLength': PropertyChangeEvent<number>;
  'propertyChange:minSearchTokenLength': PropertyChangeEvent<number>;
  'propertyChange:resultCount': PropertyChangeEvent<string>;
  'propertyChange:searchQuery': PropertyChangeEvent<string>;
  'propertyChange:searchStatus': PropertyChangeEvent<string>;
}
