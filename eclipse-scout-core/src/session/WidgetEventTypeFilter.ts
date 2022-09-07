/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
export default class WidgetEventTypeFilter {

  constructor() {
    this.filters = [];
  }

  addFilter(filterFunc) {
    this.filters.push(filterFunc);
  }

  addFilterForEventType(eventType) {
    this.filters.push(event => {
      return event.type === eventType;
    });
  }

  filter(event) {
    return this.filters.some(filterFunc => {
      return filterFunc(event);
    });
  }

  reset() {
    this.filters = [];
  }
}
