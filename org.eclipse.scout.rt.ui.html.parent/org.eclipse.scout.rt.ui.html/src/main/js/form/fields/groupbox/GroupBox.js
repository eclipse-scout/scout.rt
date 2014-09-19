// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.GroupBox = function() {
  scout.GroupBox.parent.call(this);
  this.formFields = [];
  this._addAdapterProperties('formFields');
  this.$body;
  this.$buttonBar;

  this.controls = [];
  this.systemButtons = [];
  this.customButtons = [];
  this.processButtons = [];
};
scout.inherits(scout.GroupBox, scout.CompositeField);

scout.GroupBox.prototype._render = function($parent) {
  var root = this.parent.objectType == 'Form';
  var cssClass = root ? 'root-group-box' : 'group-box';
  this.$container = $parent.
    appendDiv('', cssClass).
    addClass('form-field').
    attr('id', 'GroupBox-' + this.id);

  var htmlContainer = new scout.HtmlComponent(this.$container);
  htmlContainer.setLayout(new scout.GroupBoxLayout());
  if (!root) {
    htmlContainer.layoutData = new scout.LogicalGridData(this);
  }

  if (this.label) {
    this.$label = $('<span>').html(this.label);
    this.$container.
      appendDiv('', 'group-box-title').
      append(this.$label);
  }

  this.$body = this.$container.appendDiv('', 'group-box-body');
  var env = scout.HtmlEnvironment;
  var htmlBody = new scout.HtmlComponent(this.$body);
  htmlBody.setLayout(new scout.LogicalGridLayout(env.formColumnGap, env.formRowGap));

  this._createFieldArraysByType();
  for (var i=0; i<this.controls.length; i++) {
    this.controls[i].render(this.$body);
  }

  if (this.processButtons.length > 0) {
    var buttonBar = new scout.GroupBoxButtonBar(this.processButtons);
    buttonBar.render(this.$container);
  }
};

scout.GroupBox.prototype._createFieldArraysByType = function() {
  var i, field;
  for (i = 0; i < this.formFields.length; i++) {
    field = this.formFields[i];
    if (field instanceof scout.Button) {
      if (field.processButton) {
        this.processButtons.push(field);
        if (field.systemType != scout.Button.SYSTEM_TYPE.NONE) {
          this.systemButtons.push(field);
        }
        else {
          this.customButtons.push(field);
        }
      }
      else {
        this.controls.push(field);
      }
    }
    else {
      this.controls.push(field);
    }
  }
};

/**
 * @override CompositeField.js
 */
scout.GroupBox.prototype.getFields = function() {
  return this.controls;
};

scout.GroupBox.prototype._setBorderVisible = function(borderVisible) {
  // NOP
};
