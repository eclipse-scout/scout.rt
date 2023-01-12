/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Event, Predicate} from '../index';

export class WidgetEventTypeFilter {
  filters: Predicate<Event>[];

  constructor() {
    this.filters = [];
  }

  addFilter(filterFunc: Predicate<Event>) {
    this.filters.push(filterFunc);
  }

  addFilterForEventType(eventType: string) {
    this.filters.push(event => event.type === eventType);
  }

  filter(event: Event): boolean {
    return this.filters.some(filterFunc => filterFunc(event));
  }

  reset() {
    this.filters = [];
  }
}
