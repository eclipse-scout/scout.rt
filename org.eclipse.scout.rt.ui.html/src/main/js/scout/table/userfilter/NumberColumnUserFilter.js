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
scout.NumberColumnUserFilter.prototype.createAddFilterEventData = function() {
  var data = scout.NumberColumnUserFilter.parent.prototype.createAddFilterEventData.call(this);
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
  this.numberFromField = groupBox.addFilterField('NumberField', 'ui.from', 0);
  this.numberFromField.decimalFormat = this.column.decimalFormat;
  this.numberFromField.setDisplayText(this.numberFrom);
  this.numberFromField.on('displayTextChanged', this._onDisplayTextChanged.bind(this));

  this.numberToField = groupBox.addFilterField('NumberField', 'ui.to', 1);
  this.numberToField.decimalFormat = this.column.decimalFormat;
  this.numberToField.setDisplayText(this.numberTo);
  this.numberToField.on('displayTextChanged', this._onDisplayTextChanged.bind(this));
};

scout.NumberColumnUserFilter.prototype._onDisplayTextChanged = function(event) {
  this.numberFrom = this.numberFromField.parse();
  this.numberTo = this.numberToField.parse();
  $.log.debug('(NumberColumnUserFilter#_onDisplayTextChanged) numberFrom=' + this.numberFrom + ' numberTo=' + this.numberTo);
  this.triggerFilterFieldsChanged(event);
};
