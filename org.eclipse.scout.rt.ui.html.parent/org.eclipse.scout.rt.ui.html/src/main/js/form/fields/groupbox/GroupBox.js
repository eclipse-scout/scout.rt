// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.GroupBox = function() {
  scout.GroupBox.parent.call(this);
  this.formFields = [];
  this._addAdapterProperties('formFields');
};
scout.inherits(scout.GroupBox, scout.FormField);

scout.GroupBox.prototype._render = function($parent) {
  var root = this.parent.objectType == 'Form';
  var cssClass = root ? 'root-group-box' : 'group-box';
  this.$container = $parent.
    appendDiv('', cssClass).
    attr('id', 'GroupBox-' + this.id);

  if (this.label) {
    this.$label = $('<span>').html(this.label);
    this.$container.
    appendDiv('', 'group-box-title').
    append(this.$label);
  }

  new scout.TableLayout().render(this.$container, this, this.getControlFields());
};

/**
 * Returns all fields (including system buttons).
 */
scout.GroupBox.prototype.getFormFields = function() {
  return this.formFields;
};

/**
 * Returns all fields that are a system button.
 */
scout.GroupBox.prototype.getSystemButtons = function() {
  return this._getFields(true);
};

/**
 * Returns all fields that are _not_ a system button.
 */
scout.GroupBox.prototype.getControlFields = function() {
  return this._getFields(false);
};

/**
 * Returns all fields that are (not) a system button, depending on the given boolean value.
 */
scout.GroupBox.prototype._getFields = function(systemButton) {
  var i, fields = [];
  for (i = 0; i < this.formFields.length; i++) {
    if (systemButton == this._isSystemButton(this.formFields[i])) {
      fields.push(this.formFields[i]);
    }
  }
  return fields;
};

scout.GroupBox.prototype._isSystemButton = function(formField) {
  return formField instanceof scout.Button &&
    formField.systemType != scout.Button.SYSTEM_TYPE.NONE;
};

scout.GroupBox.prototype._setBorderVisible = function(borderVisible) {
  // NOP
};
