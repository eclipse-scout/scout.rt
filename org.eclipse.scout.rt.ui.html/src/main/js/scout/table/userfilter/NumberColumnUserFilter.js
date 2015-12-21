scout.NumberColumnUserFilter = function() {
  scout.NumberColumnUserFilter.parent.call(this);

  this.numberFrom;
  this.numberFromField;
  this.numberTo;
  this.numberToField;
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
 * @override ColumnUserFilter.js
 */
scout.NumberColumnUserFilter.prototype.fieldsFilterActive = function() {
  return scout.objects.isNumber(this.numberFrom) || scout.objects.isNumber(this.numberTo);
};

/**
 * @override ColumnUserFilter.js
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
 * @implements ColumnUserFilter.js
 */
scout.NumberColumnUserFilter.prototype.filterFieldsTitle = function() {
  return this.session.text('ui.NumberRange');
};

/**
 * @override ColumnUserFilter.js
 */
scout.NumberColumnUserFilter.prototype.addFilterFields = function(groupBox) {
  this.numberFromField = groupBox.addFilterField('NumberField', 'ui.from', 0);
  this.numberFromField.displayText = _toNumberString(this.numberFrom);
  this.numberFromField.on('displayTextChanged', this._onDisplayTextChanged.bind(this));

  this.numberToField = groupBox.addFilterField('NumberField', 'ui.to', 1);
  this.numberToField.displayText = _toNumberString(this.numberTo);
  this.numberToField.on('displayTextChanged', this._onDisplayTextChanged.bind(this));

  function _toNumberString(number) {
    return scout.objects.isNumber(number) ? number.toString() : '';
  }
};

scout.NumberColumnUserFilter.prototype._onDisplayTextChanged = function(event) {
  // FIXME AWE: (filter) discuss with C.GU... unser NumberField.js kann keinen value (numeric) liefern, richtig?
  // Das field sollte etwas wie getValue() haben das eine fixfertige number liefert anstatt der konvertierung hier
  this.numberFrom = this._toNumber(this.numberFromField.displayText),
  this.numberTo = this._toNumber(this.numberToField.displayText);
  $.log.debug('(NumberColumnUserFilter#_onDisplayTextChanged) numberFrom=' + this.numberFrom + ' numberTo=' + this.numberTo);
  this.triggerFilterFieldsChanged(event);
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
