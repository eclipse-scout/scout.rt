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
  this._addField('NumberField', 'ui.from', 0);
  this._addField('NumberField', 'ui.to', 1);
};

scout.FilterFieldsGroupBox.prototype._addFromToDateFields = function() {
  this._addField('DateField', 'ui.from', 0);
  this._addField('DateField', 'ui.to', 1);
};

scout.FilterFieldsGroupBox.prototype._addField = function(objectType, text, gridY) {
  this.fields.push(scout.create(objectType, {
    parent: this,
    label: this.session.text(text),
    statusVisible: false,
    labelWidthInPixel: 50,
    maxLength: 100,
    gridData: {
      y: gridY
    }
  }));
};

scout.FilterFieldsGroupBox.prototype._addFreeTextField = function() {
  var freeTextField = scout.create('StringField', {
    parent: this,
    labelVisible: false,
    statusVisible: false,
    maxLength: 100,
    displayText: this.filter.freeText
  });
  freeTextField.on('displayTextChanged', this._updateFilter.bind(this));
  this.fields.push(freeTextField);
};

scout.FilterFieldsGroupBox.prototype._updateFilter = function(event) {
  // FIXME AWE: (filter) other filter types
  this.trigger('filterUpdated', {
    filterType: 'text',
    text: event.displayText
  });
};
