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
};

scout.FilterFieldsGroupBox.prototype._addFromToNumberFields = function() {

};

scout.FilterFieldsGroupBox.prototype._addFromToDateFields = function() {

};

scout.FilterFieldsGroupBox.prototype._addFreeTextField = function() {
  var freeTextField = scout.create('StringField', {
    parent: this,
    session: this.session,
    labelVisible: false,
    statusVisible: false,
    maxLength: 100
  });
  this.controls.push(freeTextField);
};

