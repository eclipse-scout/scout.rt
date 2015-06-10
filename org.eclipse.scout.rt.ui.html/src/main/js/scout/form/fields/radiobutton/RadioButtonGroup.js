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
  var env = scout.HtmlEnvironment,
    htmlBodyContainer;

  this.addContainer($parent, 'radiobutton-group');

  this._$body = this.$container.appendDiv('radiobutton-group-body');
  htmlBodyContainer = new scout.HtmlComponent(this._$body, this.session);
  htmlBodyContainer.setLayout(new scout.LogicalGridLayout(env.formColumnGap, env.formRowGap));

  for (var i = 0; i < this.formFields.length; i++) {
    this.formFields[i].render(this._$body);
    if (this.formFields[i] instanceof scout.RadioButton) {
      this.formFields[i].$field.attr('name', this.id);
      this._radioButtonMap[this.formFields[i].radioValue] = this.formFields[i];
    }
  }

  this.addLabel();
  this.addMandatoryIndicator();
  this.addField(this._$body);
  this.addStatus();
};

scout.RadioButtonGroup.prototype._renderEnabled = function(){
  scout.RadioButtonGroup.parent.prototype._renderEnabled.call(this);
  this._provideTabIndex();
};

scout.RadioButtonGroup.prototype._provideTabIndex = function(){
  var tabSet;
  for (var i = 0; i < this.formFields.length; i++) {
    if (this.formFields[i] instanceof scout.RadioButton) {
      if(this.formFields[i].enabled && this.enabled && !tabSet){
        this.formFields[i]._renderTabbable(true);
        tabSet=this.formFields[i];
      }
      else if(tabSet && this.enabled && this.formFields[i].enabled && this.formFields[i].selected){
        tabSet._renderTabbable(false);
        this.formFields[i]._renderTabbable(true);
        tabSet=this.formFields[i];
      }
      else{
        this.formFields[i]._renderTabbable(false);
      }
    }
  }
};

scout.RadioButtonGroup.prototype._createKeyStrokeAdapter = function(){
  return new scout.RadioButtonGroupKeyStrokeAdapter(this);
};
