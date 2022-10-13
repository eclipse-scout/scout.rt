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
import {EventEmitter, Filter, objects, Session, Table, TableRow, TableUserFilterModel} from '../../index';
import $ from 'jquery';

export default abstract class TableUserFilter extends EventEmitter implements Filter<TableRow>, TableUserFilterModel {
  declare model: TableUserFilterModel;

  session: Session;
  table: Table;
  filterType: string;

  constructor() {
    super();
    this.session = null;
  }

  init(model: TableUserFilterModel) {
    this.session = model.session;
    if (!this.session) {
      throw new Error('Session expected: ' + this);
    }
    this._init(model);
  }

  protected _init(model: TableUserFilterModel) {
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

  equals(filter: Filter<TableRow>): boolean {
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
