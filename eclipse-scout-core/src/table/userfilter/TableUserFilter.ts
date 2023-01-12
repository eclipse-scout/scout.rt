/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {EventEmitter, Filter, InitModelOf, objects, ObjectWithType, Session, SomeRequired, Table, TableRow, TableUserFilterModel} from '../../index';
import $ from 'jquery';

export abstract class TableUserFilter extends EventEmitter implements Filter<TableRow>, TableUserFilterModel, ObjectWithType {
  declare model: TableUserFilterModel;
  declare initModel: SomeRequired<this['model'], 'session' | 'table'>;

  objectType: string;
  session: Session;
  table: Table;
  filterType: string;

  constructor() {
    super();
    this.session = null;
  }

  init(model: InitModelOf<this>) {
    this.session = model.session;
    if (!this.session) {
      throw new Error('Session expected: ' + this);
    }
    this._init(model);
  }

  protected _init(model: InitModelOf<this>) {
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
