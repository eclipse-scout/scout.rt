/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  ColumnUserFilter, ColumnUserFilterModel, DateColumn, DateColumnUserFilterModel, DateField, dates, FilterFieldsGroupBox, InitModelOf, PropertyChangeEvent, TableMatrix, TableMatrixDateGroup, TableMatrixNumberGroup, TableRow,
  TableUserFilterAddedEventData
} from '../../index';
import $ from 'jquery';

export class DateColumnUserFilter extends ColumnUserFilter implements ColumnUserFilterModel {
  declare model: DateColumnUserFilterModel;
  declare column: DateColumn;

  dateFrom: Date;
  dateTo: Date;
  dateFromField: DateField;
  dateToField: DateField;

  constructor() {
    super();

    this.dateFrom = null;
    this.dateFromField = null;
    this.dateTo = null;
    this.dateToField = null;
    this.hasFilterFields = true;
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this.dateFrom = dates.parseJsonDate(model.dateFrom);
    this.dateTo = dates.parseJsonDate(model.dateTo);
  }

  override axisGroup(): TableMatrixNumberGroup | TableMatrixDateGroup {
    if (this.column.hasDate) {
      // Default grouping for date columns is year
      return TableMatrix.DateGroup.YEAR;
    }
    // No grouping for time columns
    return TableMatrix.DateGroup.NONE;
  }

  override createFilterAddedEventData(): TableUserFilterAddedEventData {
    let data = super.createFilterAddedEventData();
    data.dateFrom = dates.toJsonDate(this.dateFrom);
    data.dateTo = dates.toJsonDate(this.dateTo);
    return data;
  }

  override fieldsFilterActive(): boolean {
    return !!this.dateFrom || !!this.dateTo;
  }

  override acceptByFields(key: Date, normKey?: number | string, row?: TableRow): boolean {
    // if date is empty and dateFrom/dateTo is set, the row should never match
    if (!key) {
      return false;
    }

    let
      keyValue = key.valueOf(),
      fromValue = this.dateFrom ? this.dateFrom.valueOf() : null,
      // Shift the toValue to 1ms before midnight/next day. Thus any time of the selected day is accepted.
      toValue = this.dateTo ? dates.shift(this.dateTo, 0, 0, 1).valueOf() - 1 : null;

    if (fromValue && toValue) {
      return keyValue >= fromValue && keyValue <= toValue;
    }
    if (fromValue) {
      return keyValue >= fromValue;
    }
    if (toValue) {
      return keyValue <= toValue;
    }

    // acceptByFields is only called when filter fields are active
    throw new Error('illegal state');
  }

  override filterFieldsTitle(): string {
    return this.session.text('ui.DateRange');
  }

  override addFilterFields(groupBox: FilterFieldsGroupBox) {
    this.dateFromField = groupBox.addFilterField(DateField, 'ui.from') as DateField;
    this.dateFromField.setValue(this.dateFrom);
    this.dateFromField.on('propertyChange', this._onPropertyChange.bind(this));

    this.dateToField = groupBox.addFilterField(DateField, 'ui.to') as DateField;
    this.dateToField.setValue(this.dateTo);
    this.dateToField.on('propertyChange', this._onPropertyChange.bind(this));
  }

  protected _onPropertyChange(event: PropertyChangeEvent<any, DateField>) {
    if (event.propertyName !== 'value') {
      return;
    }
    this.dateFrom = this.dateFromField.value;
    this.dateTo = this.dateToField.value;
    $.log.isDebugEnabled() && $.log.debug('(DateColumnUserFilter#_onAcceptInput) dateFrom=' + this.dateFrom + ' dateTo=' + this.dateTo);
    this.triggerFilterFieldsChanged();
  }

  override modifyFilterFields() {
    this.dateFromField.$field.on('input', '', $.debounce(this._onInput.bind(this)));
    this.dateToField.$field.on('input', '', $.debounce(this._onInput.bind(this)));
  }

  protected _onInput(event: JQuery.TriggeredEvent) {
    if (!this.dateFromField.rendered) {
      // popup has been closed in the mean time
      return;
    }
    this.dateFrom = this.dateFromField.value;
    this.dateTo = this.dateToField.value;
    this.triggerFilterFieldsChanged();
  }
}
