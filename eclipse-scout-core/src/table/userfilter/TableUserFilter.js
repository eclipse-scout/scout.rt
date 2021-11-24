/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {objects, TableFilter} from '../../index';
import $ from 'jquery';

export default class TableUserFilter extends TableFilter {

  constructor() {
    super();
  }

  init(model) {
    this.session = model.session;
    if (!this.session) {
      throw new Error('Session expected: ' + this);
    }
    this._init(model);
  }

  _init(model) {
    $.extend(this, model);
  }

  createFilterAddedEventData() {
    return {
      filterType: this.filterType
    };
  }

  createFilterRemovedEventData() {
    return {
      filterType: this.filterType
    };
  }

  createKey() {
    return this.filterType;
  }

  createLabel() {
    // to be implemented by subclasses
    return '';
  }

  accept(row) {
    // to be implemented by subclasses
  }

  equals(filter) {
    if (!(filter instanceof TableUserFilter)) {
      return false;
    }
    return objects.equals(this.createKey(), filter.createKey());
  }
}
