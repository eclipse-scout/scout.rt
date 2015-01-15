// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.RadioButtonGroup = function() {
  scout.RadioButtonGroup.parent.call(this);
  this._addAdapterProperties('formFields');
  this.formFields = [];
  this._$body;
//  this._$groupBoxTitle;
};

scout.inherits(scout.RadioButtonGroup, scout.ValueField);

scout.RadioButtonGroup.prototype._renderValue = function($parent){
  //nop
}

scout.RadioButtonGroup.prototype._render = function($parent){
  var htmlComp;

  this.addContainer($parent, 'radiobutton-group');

  this._$body = this.$container.appendDiv('radiobutton-group-body');
  for (var i = 0; i < this.formFields.length; i++) {
    this.formFields[i].render(this._$body);
    this.formFields[i]._$radioButton.attr('name', this.id);
    if(this.value && this.value === this.formFields[i].radioValue){
      this.formFields[i]._$radioButton.checked = 'checked';
    }
  }

  htmlComp = new scout.HtmlComponent(this.$container, this.session);
  htmlComp.layoutData = new scout.LogicalGridData(this);
  htmlComp.setLayout(new scout.RadioButtonGroupLayout(this.formFields));

  this.addField(this._$body);
  this.addLabel();
  this.addMandatoryIndicator();
  this.addStatus();
};

scout.RadioButtonGroup.prototype._renderDisplayText = function(displayText) {
  //There is no display text for a RadioButtonGroup
};

scout.RadioButtonGroup.prototype._readDisplayText = function() {
  //There is no display text for a RadioButtonGroup
  return "";
};

//scout.GroupBox.prototype._renderProperties = function() {
//  scout.GroupBox.parent.prototype._renderProperties.call(this);
//  //TODO nbu implement
//};
//
//scout.GroupBox.prototype._render = function($parent) {
//  var htmlComp = this.addContainer($parent, this.mainBox ? 'root-group-box' : 'group-box', new scout.GroupBoxLayout());
//  if (this.mainBox) {
//    htmlComp.layoutData = null;
//  }
//
//  this.$label = $('<span>').html(this.label);
//  this._$groupBoxTitle = this.$container.
//    appendDiv('group-box-title').
//    append(this.$label);
//
//  this._$body = this.$container.appendDiv('group-box-body');
//  var env = scout.HtmlEnvironment;
//  var htmlBody = new scout.HtmlComponent(this._$body, this.session);
//  htmlBody.setLayout(new scout.LogicalGridLayout(env.formColumnGap, env.formRowGap));
//
//  this._createFieldArraysByType();
//  for (var i=0; i<this.controls.length; i++) {
//    this.controls[i].render(this._$body);
//  }
//
//  if (this.processButtons.length > 0) {
//    var buttonBar = new scout.GroupBoxButtonBar(this.processButtons);
//    buttonBar.render(this.$container);
//  }
//};
//
//
//
//scout.GroupBox.prototype._renderBorderVisible = function(borderVisible) {
//  if (borderVisible && this.borderDecoration === 'auto') {
//    borderVisible = this._computeBorderVisible();
//  }
//
//  if (!borderVisible) {
//    this._$body.addClass('margin-invisible');
//  }
//};
//
////Don't include in renderProperties, it is not necessary to execute it initially because renderBorderVisible is executed already
//scout.GroupBox.prototype._renderBorderDecoration = function() {
//  this._renderBorderVisible(this.borderVisible);
//};
//
///**
// *
// * @returns false if it is the mainbox. Or if the groupbox contains exactly one tablefield which has an invisible label
// */
//scout.GroupBox.prototype._computeBorderVisible = function() {
//  var fields = this.getFields();
//  if (this.mainBox) {
//    return false;
//  } else if (fields.length === 1 &&
//      fields[0].objectType === 'TableField' &&
//      !fields[0].labelVisible) {
//    fields[0].$container.addClass('single');
//    return false;
//  }
//  return true;
//};
//
///**
// * @override FormField.js
// */
//scout.GroupBox.prototype._renderLabelVisible = function(visible) {
//  // TODO AWE: (concept) discuss with C.GU -> auf dem GUI server korrigieren oder im Browser UI?
//  // --> kein hack für main-box, wenn die auf dem model ein label hat, hat es im UI auch eins
//  this._$groupBoxTitle.setVisible(visible && this.label && !this.mainBox);
//};



