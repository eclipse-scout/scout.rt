// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.GroupBox = function() {
  scout.GroupBox.parent.call(this);
  this._$label;
  this._gridLayout;
};
scout.inherits(scout.GroupBox, scout.FormField);

scout.GroupBox.prototype._render = function($parent) {
  this.$container = $parent.appendDiv(undefined, 'form-field group-box');
  this.$container.attr('id', 'Scout-' + this.model.id);
  this.$container.data('gridData', this.model.gridData);
  this.$container.data('columns', this.model.gridColumnCount);
  this._gridLayout = new scout.GridLayout(this.$container);
  this.$container.data('gridLayout', this._gridLayout);

  if (this.model.label) {
    // TODO AWE: das geht wohl auch hübscher mit :after
    var span = '<span class="group-box-title-border"></span>';
    this._$label = this.$container.appendDiv(undefined, 'group-box-title', this.model.label + span);
  }

  this._formFields = [];
  if (this.model.formFields) {
    var i, formFieldModel, formField;
    for (i = 0; i < this.model.formFields.length; i++) {
      formFieldModel = this.model.formFields[i];
      formField = this.session.widgetMap[formFieldModel.id];
      if (!formField) {
        formField = this.session.objectFactory.create(formFieldModel);
      }
      if (!this._isSystemButton(formField)) { // do not render system buttons on group box
        formField.attach(this.$container);
      }
      this._formFields[i] = formField;
    }
  }
};


scout.GroupBox.prototype._isSystemButton = function(formField) {
  return formField instanceof scout.Button &&
         formField.getSystemType() != scout.Button.SYSTEM_TYPE.NONE;
};

scout.GroupBox.prototype.getFormFields = function() {
  return this._formFields;
};

scout.GroupBox.prototype.getSystemButtons = function() {
  var i, formField, systemButtons = [];
  for (i=0; i<this._formFields.length; i++) {
    if (this._isSystemButton(this._formFields[i])) {
      systemButtons.push(this._formFields[i]);
    }
  }
  return systemButtons;
};

scout.GroupBox.prototype.updateLayout = function(force) {
  if (this.$container) {
    this._gridLayout.updateLayout(force);
  } else {
    // TODO AWE: (C.GU) schauen woher dieses zweite form kommt (id=19)
    console.error('groupBox ' + this.model.id + ' has this.$container=null. Cannot updateLayout()');
  }
};

scout.GroupBox.prototype.layout = function() {
  this._gridLayout.layout();
};

scout.GroupBox.prototype._onModelBorderVisibleChanged = function(borderVisible) {
  // NOP
};

scout.GroupBox.prototype.onModelPropertyChange = function(event) {
  if (event.hasOwnProperty('borderVisible')) {
    this._onModelBorderVisibleChanged(event.borderVisible);
  }
};
