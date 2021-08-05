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
import {ColumnUserFilter, dates, TableMatrix} from '../../index';
import $ from 'jquery';

export default class DateColumnUserFilter extends ColumnUserFilter {

  constructor() {
    super();

    this.dateFrom = null;
    this.dateFromField = null;
    this.dateTo = null;
    this.dateToField = null;

    this.hasFilterFields = true;
  }

  /**
   * @override TableUserFilter.js
   */
  _init(model) {
    super._init(model);
    this.dateFrom = dates.parseJsonDate(this.dateFrom);
    this.dateTo = dates.parseJsonDate(this.dateTo);
  }

  /**
   * @override ColumnUserFilter.js
   */
  axisGroup() {
    if (this.column.hasDate) {
      // Default grouping for date columns is year
      return TableMatrix.DateGroup.YEAR;
    }
    // No grouping for time columns
    return TableMatrix.DateGroup.NONE;
  }

  /**
   * @override ColumnUserFilter.js
   */
  createFilterAddedEventData() {
    let data = super.createFilterAddedEventData();
    data.dateFrom = dates.toJsonDate(this.dateFrom);
    data.dateTo = dates.toJsonDate(this.dateTo);
    return data;
  }

  /**
   * @override ColumnUserFilter.js
   */
  fieldsFilterActive() {
    return this.dateFrom || this.dateTo;
  }

  /**
   * @override ColumnUserFilter.js
   */
  acceptByFields(key, normKey, row) {
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
    } else if (fromValue) {
      return keyValue >= fromValue;
    } else if (toValue) {
      return keyValue <= toValue;
    }

    // acceptByFields is only called when filter fields are active
    throw new Error('illegal state');
  }

  /**
   * @override
   */
  filterFieldsTitle() {
    return this.session.text('ui.DateRange');
  }

  /**
   * @override
   */
  addFilterFields(groupBox) {
    this.dateFromField = groupBox.addFilterField('DateField', 'ui.from');
    this.dateFromField.setValue(this.dateFrom);
    this.dateFromField.on('propertyChange', this._onPropertyChange.bind(this));

    this.dateToField = groupBox.addFilterField('DateField', 'ui.to');
    this.dateToField.setValue(this.dateTo);
    this.dateToField.on('propertyChange', this._onPropertyChange.bind(this));
  }

  _onPropertyChange(event) {
    if (event.propertyName !== 'value') {
      return;
    }
    this.dateFrom = this.dateFromField.value;
    this.dateTo = this.dateToField.value;
    $.log.isDebugEnabled() && $.log.debug('(DateColumnUserFilter#_onAcceptInput) dateFrom=' + this.dateFrom + ' dateTo=' + this.dateTo);
    this.triggerFilterFieldsChanged(event);
  }

  modifyFilterFields() {
    this.dateFromField.$field.on('input', '', $.debounce(this._onInput.bind(this)));
    this.dateToField.$field.on('input', '', $.debounce(this._onInput.bind(this)));
  }

  _onInput(event) {
    if (!this.dateFromField.rendered) {
      // popup has been closed in the mean time
      return;
    }
    this.dateFrom = this.dateFromField.value;
    this.dateTo = this.dateToField.value;
    this.triggerFilterFieldsChanged(event);
  }
}
