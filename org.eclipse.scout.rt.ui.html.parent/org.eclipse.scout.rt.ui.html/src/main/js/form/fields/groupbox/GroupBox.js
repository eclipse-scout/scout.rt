// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.GroupBox = function() {
  scout.GroupBox.parent.call(this);
  this._$label;
  this._gridLayout;
};
scout.inherits(scout.GroupBox, scout.FormField);

scout.GroupBox.prototype.init = function(model, session) {
  scout.GroupBox.parent.prototype.init.call(this, model, session);
  this.formFields = this.session.getOrCreateModelAdapters(model.formFields, this);
};

scout.GroupBox.prototype._render = function($parent) {
  this.$container = $parent.appendDiv(undefined, 'form-field group-box');
  this.$container.attr('id', 'Scout-' + this.id);
  this.$container.data('gridData', this.gridData);
  this.$container.data('columns', this.gridColumnCount);
  this._gridLayout = new scout.GridLayout(this.$container);
  this.$container.data('gridLayout', this._gridLayout);

  if (this.label) {
    // TODO AWE: das geht wohl auch hübscher mit :after
    var span = '<span class="group-box-title-border"></span>';
    this._$label = this.$container.appendDiv(undefined, 'group-box-title', this.label + span);
  }

  var i, formField;
  for (i = 0; i < this.formFields.length; i++) {
    formField = this.formFields[i];
    if (!this._isSystemButton(formField)) { // do not render system buttons on group box
      formField.attach(this.$container);
    }
  }
};

scout.GroupBox.prototype._isSystemButton = function(formField) {
  return formField instanceof scout.Button &&
    formField.systemType != scout.Button.SYSTEM_TYPE.NONE;
};

scout.GroupBox.prototype.getFormFields = function() {
  return this.formFields;
};

scout.GroupBox.prototype.getSystemButtons = function() {
  var i, formField, systemButtons = [];
  for (i = 0; i < this.formFields.length; i++) {
    if (this._isSystemButton(this.formFields[i])) {
      systemButtons.push(this.formFields[i]);
    }
  }
  return systemButtons;
};

scout.GroupBox.prototype.updateLayout = function(force) {
  if (this.$container) {
    this._gridLayout.updateLayout(force);
  } else {
    // TODO AWE: (C.GU) schauen woher dieses zweite form kommt (id=19)
    console.error('groupBox ' + this.id + ' has this.$container=null. Cannot updateLayout()');
  }
};

scout.GroupBox.prototype.layout = function() {
  this._gridLayout.layout();
};

scout.GroupBox.prototype._onModelBorderVisibleChanged = function(borderVisible) {
  // NOP
};

scout.GroupBox.prototype.dispose = function() {
  scout.GroupBox.parent.prototype.dispose.call(this);
  var i, formField;
  for (i = 0; i < this.formFields.length; i++) {
    formField = this.session.getModelAdapter(this.formFields[i].id);
    if (formField) {
      formField.dispose();
    }
  }
};

scout.GroupBox.prototype.onModelPropertyChange = function(event) {
  if (event.hasOwnProperty('borderVisible')) {
    this._onModelBorderVisibleChanged(event.borderVisible);
  }
};
