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
import {Predicate} from '../types';
import {Event} from '../index';

export default class WidgetEventTypeFilter {
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
