scout.FilterFieldsGroupBox = function() {
  scout.FilterFieldsGroupBox.parent.call(this);
  this.column;
};
scout.inherits(scout.FilterFieldsGroupBox, scout.GroupBox);

scout.FilterFieldsGroupBox.prototype._init = function(model) {
  scout.FilterFieldsGroupBox.parent.prototype._init.call(this, model);
  this.filter = model.filter;
  this.column = model.column;
  if (this.column.type === 'number') {
    this._addFromToNumberFields();
  } else if (this.column.type === 'date') {
    this._addFromToDateFields();
  } else {
    this._addFreeTextField();
  }
  this.cssClass = 'filter-fields';
};

scout.FilterFieldsGroupBox.prototype.groupText = function() {
  if (this.column.type === 'number') {
    return this.session.text('ui.NumberRange');
  } else if (this.column.type === 'date') {
    return this.session.text('ui.DateRange');
  } else {
    return this.session.text('ui.FreeText');
  }
};

/**
 * @override GroupBox.js
 */
scout.FilterFieldsGroupBox.prototype._render = function($parent) {
  scout.FilterFieldsGroupBox.parent.prototype._render.call(this, $parent);
  // remove mandatory indicator from free-text field
  if (this.column.type === 'text') {
    var stringField = this.fields[0];
    stringField.$mandatory.remove();
    stringField.$mandatory = null;
  }
};

scout.FilterFieldsGroupBox.prototype._addFromToNumberFields = function() {
  var fromField = this._addField('NumberField', 'ui.from', 0);
  fromField.displayText = this._toNumberString(this.filter.numberRange ? this.filter.numberRange.from : null);
  fromField.on('displayTextChanged', this._updateNumberFilter.bind(this));

  var toField = this._addField('NumberField', 'ui.to', 1);
  toField.displayText = this._toNumberString(this.filter.numberRange ? this.filter.numberRange.to : null);
  toField.on('displayTextChanged', this._updateNumberFilter.bind(this));
};

scout.FilterFieldsGroupBox.prototype._updateNumberFilter = function(event) {
  this.trigger('filterUpdated', {
    filterType: 'number',
    from: this.fields[0].displayText,
    to: this.fields[1].displayText
  });
};

scout.FilterFieldsGroupBox.prototype._toNumberString = function(number) {
  if (number === null || number === undefined) { // not for 0
    return '';
  } else {
    return number.toString();
  }
};

scout.FilterFieldsGroupBox.prototype._addFromToDateFields = function() {
  // FIXME AWE: (filter) throw away range object and use separate properties instead
  var fromField = this._addField('DateField', 'ui.from', 0);
  fromField.timestamp = this.filter.dateRange ? this.filter.dateRange.from : null;
  fromField.on('timestampChanged', this._updateDateFilter.bind(this));

  var toField = this._addField('DateField', 'ui.to', 1);
  toField.timestamp = this.filter.dateRange ? this.filter.dateRange.to : null;
  toField.on('timestampChanged', this._updateDateFilter.bind(this));
};

scout.FilterFieldsGroupBox.prototype._updateDateFilter = function(event) {
  this.trigger('filterUpdated', {
    filterType: 'date',
    from: this.fields[0].timestamp,
    to: this.fields[1].timestamp
  });
};

// FIXME AWE: (filter) es braucht wahrscheinlich auch eine validierung? z.B. from muss kleiner sein als to
scout.FilterFieldsGroupBox.prototype._addField = function(objectType, text, gridY) {
  var field = scout.create(objectType, {
    parent: this,
    label: this.session.text(text),
    statusVisible: false,
    labelWidthInPixel: 50,
    maxLength: 100,
    gridData: {
      y: gridY
    }
  });
  this.fields.push(field);
  return field;
};

scout.FilterFieldsGroupBox.prototype._addFreeTextField = function() {
  var freeTextField = scout.create('StringField', {
    parent: this,
    labelVisible: false,
    statusVisible: false,
    maxLength: 100,
    displayText: this.filter.freeText
  });
  freeTextField.on('displayTextChanged', this._updateTextFilter.bind(this));
  this.fields.push(freeTextField);
};

scout.FilterFieldsGroupBox.prototype._updateTextFilter = function(event) {
  this.trigger('filterUpdated', {
    filterType: 'text',
    text: event.displayText
  });
};
