/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {ColumnUserFilter, FilterFieldsGroupBox, NumberColumn, NumberColumnUserFilterModel, NumberField, objects, PropertyChangeEvent, TableRow, TableUserFilterAddedEventData} from '../../index';
import $ from 'jquery';

export class NumberColumnUserFilter extends ColumnUserFilter implements NumberColumnUserFilterModel {
  declare model: NumberColumnUserFilterModel;
  declare column: NumberColumn;

  numberFrom: number;
  numberTo: number;
  numberFromField: NumberField;
  numberToField: NumberField;

  constructor() {
    super();

    this.numberFrom = null;
    this.numberFromField = null;
    this.numberTo = null;
    this.numberToField = null;
    this.hasFilterFields = true;
  }

  override createFilterAddedEventData(): TableUserFilterAddedEventData {
    let data = super.createFilterAddedEventData();
    data.numberFrom = this.numberFrom;
    data.numberTo = this.numberTo;
    return data;
  }

  override fieldsFilterActive(): boolean {
    return objects.isNumber(this.numberFrom) || objects.isNumber(this.numberTo);
  }

  override acceptByFields(key: any, normKey: number | string, row: TableRow): boolean {
    let
      hasFrom = objects.isNumber(this.numberFrom),
      hasTo = objects.isNumber(this.numberTo);
    if (hasFrom && hasTo) {
      return normKey >= this.numberFrom && normKey <= this.numberTo;
    }
    if (hasFrom) {
      return normKey >= this.numberFrom;
    }
    if (hasTo) {
      return normKey <= this.numberTo;
    }
  }

  override filterFieldsTitle(): string {
    return this.session.text('ui.NumberRange');
  }

  override addFilterFields(groupBox: FilterFieldsGroupBox) {
    this.numberFromField = groupBox.addFilterField(NumberField, 'ui.from') as NumberField;
    this.numberFromField.decimalFormat = this.column.decimalFormat;
    this.numberFromField.setValue(this.numberFrom);
    this.numberFromField.on('propertyChange', this._onPropertyChange.bind(this));

    this.numberToField = groupBox.addFilterField(NumberField, 'ui.to') as NumberField;
    this.numberToField.decimalFormat = this.column.decimalFormat;
    this.numberToField.setValue(this.numberTo);
    this.numberToField.on('propertyChange', this._onPropertyChange.bind(this));
  }

  protected _onPropertyChange(event: PropertyChangeEvent<any, NumberField>) {
    if (event.propertyName !== 'value') {
      return;
    }
    this.numberFrom = this.numberFromField.value;
    this.numberTo = this.numberToField.value;
    $.log.isDebugEnabled() && $.log.debug('(NumberColumnUserFilter#_onPropertyChange) numberFrom=' + this.numberFrom + ' numberTo=' + this.numberTo);
    this.triggerFilterFieldsChanged();
  }
}
