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
import {ColumnUserFilter, objects} from '../../index';
import $ from 'jquery';

export default class NumberColumnUserFilter extends ColumnUserFilter {

  constructor() {
    super();

    this.numberFrom = null;
    this.numberFromField = null;
    this.numberTo = null;
    this.numberToField = null;

    this.hasFilterFields = true;
  }

  /**
   * @override ColumnUserFilter
   */
  createFilterAddedEventData() {
    let data = super.createFilterAddedEventData();
    data.numberFrom = this.numberFrom;
    data.numberTo = this.numberTo;
    return data;
  }

  /**
   * @override ColumnUserFilter
   */
  fieldsFilterActive() {
    return objects.isNumber(this.numberFrom) || objects.isNumber(this.numberTo);
  }

  /**
   * @override ColumnUserFilter
   */
  acceptByFields(key, normKey, row) {
    let
      hasFrom = objects.isNumber(this.numberFrom),
      hasTo = objects.isNumber(this.numberTo);
    if (hasFrom && hasTo) {
      return normKey >= this.numberFrom && normKey <= this.numberTo;
    } else if (hasFrom) {
      return normKey >= this.numberFrom;
    } else if (hasTo) {
      return normKey <= this.numberTo;
    }
  }

  /**
   * @override ColumnUserFilter
   */
  filterFieldsTitle() {
    return this.session.text('ui.NumberRange');
  }

  /**
   * @override ColumnUserFilter
   */
  addFilterFields(groupBox) {
    this.numberFromField = groupBox.addFilterField('NumberField', 'ui.from');
    this.numberFromField.decimalFormat = this.column.decimalFormat;
    this.numberFromField.setValue(this.numberFrom);
    this.numberFromField.on('propertyChange', this._onPropertyChange.bind(this));

    this.numberToField = groupBox.addFilterField('NumberField', 'ui.to');
    this.numberToField.decimalFormat = this.column.decimalFormat;
    this.numberToField.setValue(this.numberTo);
    this.numberToField.on('propertyChange', this._onPropertyChange.bind(this));
  }

  _onPropertyChange(event) {
    if (event.propertyName !== 'value') {
      return;
    }
    this.numberFrom = this.numberFromField.value;
    this.numberTo = this.numberToField.value;
    $.log.isDebugEnabled() && $.log.debug('(NumberColumnUserFilter#_onPropertyChange) numberFrom=' + this.numberFrom + ' numberTo=' + this.numberTo);
    this.triggerFilterFieldsChanged(event);
  }
}
