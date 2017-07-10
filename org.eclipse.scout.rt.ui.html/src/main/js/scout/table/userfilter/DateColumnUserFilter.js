scout.DateColumnUserFilter = function() {
  scout.DateColumnUserFilter.parent.call(this);

  this.dateFrom;
  this.dateFromField;
  this.dateTo;
  this.dateToField;

  this.hasFilterFields = true;
};
scout.inherits(scout.DateColumnUserFilter, scout.ColumnUserFilter);

/**
 * @override TableUserFilter.js
 */
scout.DateColumnUserFilter.prototype._init = function(model) {
  scout.DateColumnUserFilter.parent.prototype._init.call(this, model);
  this.dateFrom = scout.dates.parseJsonDate(this.dateFrom);
  this.dateTo = scout.dates.parseJsonDate(this.dateTo);
};

/**
 * @override ColumnUserFilter.js
 */
scout.DateColumnUserFilter.prototype.axisGroup = function() {
  if (this.column.hasDate) {
    // Default grouping for date columns is year
    return scout.TableMatrix.DateGroup.YEAR;
  } else {
    // No grouping for time columns
    return scout.TableMatrix.DateGroup.NONE;
  }
};

/**
 * @override ColumnUserFilter.js
 */
scout.DateColumnUserFilter.prototype.createFilterAddedEventData = function() {
  var data = scout.DateColumnUserFilter.parent.prototype.createFilterAddedEventData.call(this);
  data.dateFrom = scout.dates.toJsonDate(this.dateFrom);
  data.dateTo = scout.dates.toJsonDate(this.dateTo);
  return data;
};

/**
 * @override ColumnUserFilter.js
 */
scout.DateColumnUserFilter.prototype.fieldsFilterActive = function() {
  return this.dateFrom || this.dateTo;
};

/**
 * @override ColumnUserFilter.js
 */
scout.DateColumnUserFilter.prototype.acceptByFields = function(key, normKey, row) {
  // if date is empty and dateFrom/dateTo is set, the row should never match
  if (!key) {
    return false;
  }

  var
    keyValue = key.valueOf(),
    fromValue = this.dateFrom ? this.dateFrom.valueOf() : null,
    // Shift the toValue to 1ms before midnight/next day. Thus any time of the selected day is accepted.
    toValue = this.dateTo ? scout.dates.shift(this.dateTo, 0, 0, 1).valueOf() - 1 : null;

  if (fromValue && toValue) {
    return keyValue >= fromValue && keyValue <= toValue;
  } else if (fromValue) {
    return keyValue >= fromValue;
  } else if (toValue) {
    return keyValue <= toValue;
  }

  // acceptByFields is only called when filter fields are active
  throw new Error('illegal state');
};

/**
 * @implements ColumnUserFilter.js
 */
scout.DateColumnUserFilter.prototype.filterFieldsTitle = function() {
  return this.session.text('ui.DateRange');
};

/**
 * TODO [7.0] awe, cgu: (value-field) refactor DateField.js -
 * rename timestampAsDate to value (also on JsonDateField)
 * use Date object everywhere and todays 'timestamp' date-string
 * only when we communicate with the UI server. Then remove the toJsonDate
 * function used here and work with the Date object. Implement a _setValue
 * method to convert the date-string into a Date object in DateField.js
 *
 * @override ColumnUserFilter.js
 */
scout.DateColumnUserFilter.prototype.addFilterFields = function(groupBox) {
  this.dateFromField = groupBox.addFilterField('DateField', 'ui.from');
  this.dateFromField.setValue(this.dateFrom);
  this.dateFromField.on('propertyChange', this._onPropertyChange.bind(this));

  this.dateToField = groupBox.addFilterField('DateField', 'ui.to');
  this.dateToField.setValue(this.dateTo);
  this.dateToField.on('propertyChange', this._onPropertyChange.bind(this));
};

scout.DateColumnUserFilter.prototype._onPropertyChange = function(event) {
  if (event.propertyName !== 'value') {
    return;
  }
  this.dateFrom = this.dateFromField.value;
  this.dateTo = this.dateToField.value;
  $.log.debug('(DateColumnUserFilter#_onAcceptInput) dateFrom=' + this.dateFrom + ' dateTo=' + this.dateTo);
  this.triggerFilterFieldsChanged(event);
};

scout.DateColumnUserFilter.prototype.modifyFilterFields = function() {
  this.dateFromField.$field.on('input', '', $.debounce(this._onInput.bind(this)));
  this.dateToField.$field.on('input', '', $.debounce(this._onInput.bind(this)));
};

scout.DateColumnUserFilter.prototype._onInput = function(event) {
  if (!this.dateFromField.rendered) {
    // popup has been closed in the mean time
    return;
  }
  this.dateFrom = this.dateFromField.value;
  this.dateTo = this.dateToField.value;
  this.triggerFilterFieldsChanged(event);
};
