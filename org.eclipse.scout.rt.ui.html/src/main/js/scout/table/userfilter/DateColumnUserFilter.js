scout.DateColumnUserFilter = function() {
  scout.DateColumnUserFilter.parent.call(this);

  this.dateFrom;
  this.dateFromField;
  this.dateTo;
  this.dateToField;
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
scout.DateColumnUserFilter.prototype.createAddFilterEventData = function() {
  var data = scout.DateColumnUserFilter.parent.prototype.createAddFilterEventData.call(this);
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
  var
    keyValue = key.valueOf(),
    fromValue = this.dateFrom ? this.dateFrom.valueOf() : null,
    toValue  = this.dateTo ? this.dateTo.valueOf() : null;
  if (fromValue && toValue) {
    return keyValue >= fromValue && keyValue <= toValue;
  } else if (fromValue) {
    return keyValue >= fromValue;
  } else if (toValue) {
    return keyValue <= toValue;
  }
};

/**
 * @implements ColumnUserFilter.js
 */
scout.DateColumnUserFilter.prototype.filterFieldsTitle = function() {
  return this.session.text('ui.DateRange');
};

/**
 * FIXME AWE: (filter) refactor DateField.js -
 * rename timestampAsDate to value (also on JsonDateField)
 * use Date object everywhere and todays 'timestamp' date-string
 * only when we communicate with the UI server. Then remove the _toJsonDate
 * function used here and work with the Date object. Implement a _syncValue
 * method to convert the date-string into a Date object in DateField.js
 *
 * @override ColumnUserFilter.js
 */
scout.DateColumnUserFilter.prototype.addFilterFields = function(groupBox) {
  this.dateFromField = groupBox.addFilterField('DateField', 'ui.from', 0);
  this.dateFromField.timestamp = _toJsonDate(this.dateFrom);
  this.dateFromField.on('timestampChanged', this._onDisplayTextChanged.bind(this));

  this.dateToField = groupBox.addFilterField('DateField', 'ui.to', 1);
  this.dateToField.timestamp = _toJsonDate(this.dateTo);
  this.dateToField.on('timestampChanged', this._onDisplayTextChanged.bind(this));

  function _toJsonDate(date) {
    return date ? scout.dates.toJsonDate(date) : null;
  }
};

scout.DateColumnUserFilter.prototype._onDisplayTextChanged = function(event) {
  this.dateFrom = this.dateFromField.timestampAsDate,
  this.dateTo = this.dateToField.timestampAsDate;
  $.log.debug('(DateColumnUserFilter#_onDisplayTextChanged) dateFrom=' + this.dateFrom + ' dateTo=' + this.dateTo);
  this.triggerFilterFieldsChanged(event);
};
