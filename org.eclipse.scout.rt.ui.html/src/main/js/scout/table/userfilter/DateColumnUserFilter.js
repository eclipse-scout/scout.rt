scout.DateColumnUserFilter = function() {
  scout.DateColumnUserFilter.parent.call(this);

  this.dateFrom;
  this.dateTo;
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
 * @implements ColumnUserFilter.js
 */
scout.DateColumnUserFilter.prototype.fieldsFilterActive = function() {
  return this.dateFrom || this.dateTo;
};

/**
 * @implements ColumnUserFilter.js
 */
scout.DateColumnUserFilter.prototype.acceptByFields = function(key, normKey) {
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
scout.DateColumnUserFilter.prototype.updateFilterFields = function(event) {
  $.log.debug('(DateColumnUserFilter#updateFilterFields) from=' + event.from + ' to=' + event.to);
  this.dateFrom = event.from,
  this.dateTo = event.to;
};
