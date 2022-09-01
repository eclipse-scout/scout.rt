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
import {Column, EventEmitter, objects, Session, Table, TableFilter, TableFilterModel, TableRow} from '../../index';
import $ from 'jquery';

export default abstract class TableUserFilter extends EventEmitter implements TableFilter {
  declare model: TableFilterModel;

  session: Session;
  table: Table;
  column: Column;
  filterType: string;

  constructor() {
    super();
    this.session = null;
  }

  init(model: TableFilterModel) {
    this.session = model.session;
    if (!this.session) {
      throw new Error('Session expected: ' + this);
    }
    this._init(model);
  }

  protected _init(model: TableFilterModel) {
    $.extend(this, model);
  }

  createFilterAddedEventData(): TableUserFilterAddedEventData {
    return {
      filterType: this.filterType
    };
  }

  createFilterRemovedEventData(): TableUserFilterRemovedEventData {
    return {
      filterType: this.filterType
    };
  }

  createKey(): string {
    return this.filterType;
  }

  abstract createLabel(): string;

  abstract accept(row: TableRow): boolean;

  equals(filter: TableFilter): boolean {
    if (!(filter instanceof TableUserFilter)) {
      return false;
    }
    return objects.equals(this.createKey(), filter.createKey());
  }
}

export interface TableUserFilterAddedEventData {
  filterType: string;
  columnId?: string;
  selectedValues?: (string | number)[];

  text?: string;
  freeText?: string;

  dateFrom?: string;
  dateTo?: string;

  numberFrom?: number;
  numberTo?: number;
}

export interface TableUserFilterRemovedEventData {
  filterType: string;
  columnId?: string;
}
