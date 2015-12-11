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
  var from, to;
  if (this.filter.numberRange) {
    from = this.filter.numberRange.from;
    to = this.filter.numberRange.to;
  }
  this._addField('NumberField', 'ui.from', 0, this._toNumberString(from), this._updateNumberFilter);
  this._addField('NumberField', 'ui.to', 1, this._toNumberString(to), this._updateNumberFilter);
};

scout.FilterFieldsGroupBox.prototype._updateNumberFilter = function(event) {
  this.trigger('filterUpdated', {
    filterType: 'number',
    from: this.fields[0].displayText,
    to: this.fields[1].displayText
  });
};

scout.FilterFieldsGroupBox.prototype._addFromToDateFields = function() {
  this._addField('DateField', 'ui.from', 0, this.filter.dateRange.from, this._updateDateFilter);
  this._addField('DateField', 'ui.to', 1, this.filter.dateRange.to, this._updateDateFilter);
};

scout.FilterFieldsGroupBox.prototype._toNumberString = function(number) {
  if (number === null || number === undefined) { // not for 0
    return '';
  } else {
    return number.toString();
  }
};

scout.FilterFieldsGroupBox.prototype._updateDateFilter = function(event) {
  this.trigger('filterUpdated', {
    filterType: 'date',
    from: this.fields[0].displayText,
    to: this.fields[1].displayText
  });
};

// FIXME AWE: (filter) es braucht wahrscheinlich auch eine validierung? z.B. from muss kleiner sein als to
scout.FilterFieldsGroupBox.prototype._addField = function(objectType, text, gridY, displayText, onDisplayTextChanged) {
  var field = scout.create(objectType, {
    parent: this,
    label: this.session.text(text),
    displayText: displayText, // FIXME AWE: (filter) use setValue() here
    statusVisible: false,
    labelWidthInPixel: 50,
    maxLength: 100,
    gridData: {
      y: gridY
    }
  });
  field.on('displayTextChanged', onDisplayTextChanged.bind(this));
  this.fields.push(field);
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
