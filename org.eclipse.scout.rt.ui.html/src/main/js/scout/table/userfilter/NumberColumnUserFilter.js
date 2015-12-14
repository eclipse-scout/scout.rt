scout.NumberColumnUserFilter = function() {
  scout.NumberColumnUserFilter.parent.call(this);

  this.numberFrom;
  this.numberTo;
};
scout.inherits(scout.NumberColumnUserFilter, scout.ColumnUserFilter);

/**
 * @override ColumnUserFilter.js
 */
scout.NumberColumnUserFilter.prototype.createAddFilterEventData = function() {
  var data = scout.NumberColumnUserFilter.parent.prototype.createAddFilterEventData.call(this);
  data.numberFrom = this.numberFrom;
  data.numberTo = this.numberTo;
  return data;
};

/**
 * @implements ColumnUserFilter.js
 */
scout.NumberColumnUserFilter.prototype.fieldsFilterActive = function() {
  return scout.objects.isNumber(this.numberFrom) || scout.objects.isNumber(this.numberTo);
};

/**
 * @implements ColumnUserFilter.js
 */
scout.NumberColumnUserFilter.prototype.acceptByFields = function(key, normKey) {
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
 * @implements ColumnUserFilter.js
 */
scout.NumberColumnUserFilter.prototype.updateFilterFields = function(event) {
  $.log.debug('(NumberColumnUserFilter#updateFilterFields) from=' + event.from + ' to=' + event.to);
  // FIXME AWE: (filter) discuss with C.GU... unser NumberField.js kann keinen value (numeric) liefern, richtig?
  // Das field sollte etwas wie getValue() haben das eine fixfertige number liefert anstatt der konvertierung hier
  this.numberFrom = this._toNumber(event.from),
  this.numberTo = this._toNumber(event.to);
};

scout.NumberColumnUserFilter.prototype._toNumber = function(numberString) {
  if (!numberString) {
    return null;
  }
  // clean number string (only digits remain) // FIXME AWE (filter) improv. doesnt work for fractions, use float?
  numberString = numberString.replace(/\D/g, '');

  var number = parseInt(numberString, 10);
  if (isNaN(number)) {
    return null;
  }

  return number;
};
