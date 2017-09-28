/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.NumberColumnUserFilter = function() {
  scout.NumberColumnUserFilter.parent.call(this);

  this.numberFrom;
  this.numberFromField;
  this.numberTo;
  this.numberToField;

  this.hasFilterFields = true;
};
scout.inherits(scout.NumberColumnUserFilter, scout.ColumnUserFilter);

/**
 * @override ColumnUserFilter
 */
scout.NumberColumnUserFilter.prototype.createFilterAddedEventData = function() {
  var data = scout.NumberColumnUserFilter.parent.prototype.createFilterAddedEventData.call(this);
  data.numberFrom = this.numberFrom;
  data.numberTo = this.numberTo;
  return data;
};

/**
 * @override ColumnUserFilter
 */
scout.NumberColumnUserFilter.prototype.fieldsFilterActive = function() {
  return scout.objects.isNumber(this.numberFrom) || scout.objects.isNumber(this.numberTo);
};

/**
 * @override ColumnUserFilter
 */
scout.NumberColumnUserFilter.prototype.acceptByFields = function(key, normKey, row) {
  var
    hasFrom = scout.objects.isNumber(this.numberFrom),
    hasTo = scout.objects.isNumber(this.numberTo);
  if (hasFrom && hasTo) {
    return normKey >= this.numberFrom && normKey <= this.numberTo;
  } else if (hasFrom) {
    return normKey >= this.numberFrom;
  } else if (hasTo) {
    return normKey <= this.numberTo;
  }
};

/**
 * @override ColumnUserFilter
 */
scout.NumberColumnUserFilter.prototype.filterFieldsTitle = function() {
  return this.session.text('ui.NumberRange');
};

/**
 * @override ColumnUserFilter
 */
scout.NumberColumnUserFilter.prototype.addFilterFields = function(groupBox) {
  this.numberFromField = groupBox.addFilterField('NumberField', 'ui.from');
  this.numberFromField.decimalFormat = this.column.decimalFormat;
  this.numberFromField.setValue(this.numberFrom);
  this.numberFromField.on('propertyChange', this._onPropertyChange.bind(this));

  this.numberToField = groupBox.addFilterField('NumberField', 'ui.to');
  this.numberToField.decimalFormat = this.column.decimalFormat;
  this.numberToField.setValue(this.numberTo);
  this.numberToField.on('propertyChange', this._onPropertyChange.bind(this));
};

scout.NumberColumnUserFilter.prototype._onPropertyChange = function(event) {
  if (event.propertyName !== 'value') {
    return;
  }
  this.numberFrom = this.numberFromField.value;
  this.numberTo = this.numberToField.value;
  $.log.debug('(NumberColumnUserFilter#_onPropertyChange) numberFrom=' + this.numberFrom + ' numberTo=' + this.numberTo);
  this.triggerFilterFieldsChanged(event);
};
