// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.RadioButtonGroup = function() {
  scout.RadioButtonGroup.parent.call(this);
  this._addAdapterProperties('formFields');
  this.formFields = [];
  this._$body;
  this._radioButtonMap = {};
};

scout.inherits(scout.RadioButtonGroup, scout.ValueField);

scout.RadioButtonGroup.prototype._render = function($parent) {
  var htmlComp,
    env = scout.HtmlEnvironment,
    htmlBodyContainer;

  this.addContainer($parent, 'radiobutton-group');

  this._$body = this.$container.appendDiv('radiobutton-group-body');
  htmlBodyContainer = new scout.HtmlComponent(this._$body, this.session);
  htmlBodyContainer.setLayout(new scout.LogicalGridLayout(env.formColumnGap, env.formRowGap));

  for (var i = 0; i < this.formFields.length; i++) {
    this.formFields[i].render(this._$body);
    if (this.formFields[i].objectType === 'RadioButton') {
      this.formFields[i].$field.attr('name', this.id);
      this._radioButtonMap[this.formFields[i].radioValue] = this.formFields[i];
    }
  }

  this.addLabel();
  this.addMandatoryIndicator();
  this.addField(this._$body);
  this.addStatus();
};
