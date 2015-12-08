scout.FilterFieldsGroupBox = function() {
  scout.FilterFieldsGroupBox.parent.call(this);
  this.column;
};
scout.inherits(scout.FilterFieldsGroupBox, scout.GroupBox);

scout.FilterFieldsGroupBox.prototype._init = function(model) {
  scout.FilterFieldsGroupBox.parent.prototype._init.call(this, model);
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
  this.fields.push(scout.create('NumberField', {
    parent: this,
    label: this.session.text('ui.from'),
    statusVisible: false,
    labelWidthInPixel: 50,
    maxLength: 100,
    gridData: {
      y: 0
    }
  }));
  this.fields.push(scout.create('NumberField', {
    parent: this,
    label: this.session.text('ui.to'),
    statusVisible: false,
    labelWidthInPixel: 50,
    maxLength: 100,
    gridData: {
      y: 1
    }
  }));
};

scout.FilterFieldsGroupBox.prototype._addFromToDateFields = function() {
  this.fields.push(scout.create('DateField', {
    parent: this,
    label: this.session.text('ui.from'),
    statusVisible: false,
    labelWidthInPixel: 50,
    maxLength: 100,
    gridData: {
      y: 0
    }
  }));
  this.fields.push(scout.create('DateField', {
    parent: this,
    label: this.session.text('ui.to'),
    statusVisible: false,
    labelWidthInPixel: 50,
    maxLength: 100,
    gridData: {
      y: 1
    }
  }));
};

scout.FilterFieldsGroupBox.prototype._addFreeTextField = function() {
  this.fields.push(scout.create('StringField', {
    parent: this,
    labelVisible: false,
    statusVisible: false,
    maxLength: 100
  }));
};

