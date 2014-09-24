// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.GroupBox = function() {
  scout.GroupBox.parent.call(this);
  this.formFields = [];
  this._addAdapterProperties('formFields');
  this._$body;
  this._$groupBoxTitle;

  this.controls = [];
  this.systemButtons = [];
  this.customButtons = [];
  this.processButtons = [];
};
scout.inherits(scout.GroupBox, scout.CompositeField);

scout.GroupBox.prototype._render = function($parent) {
  this.$container = $parent.
    appendDiv('', this.mainBox ? 'root-group-box' : 'group-box').
    addClass('form-field').
    attr('id', 'GroupBox-' + this.id);

  var htmlCont = new scout.HtmlComponent(this.$container);
  htmlCont.setLayout(new scout.GroupBoxLayout());
  if (!this.mainBox) {
    htmlCont.layoutData = new scout.LogicalGridData(this);
  }

  this.$label = $('<span>').html(this.label);
  this._$groupBoxTitle = this.$container.
    appendDiv('', 'group-box-title').
    append(this.$label);

  this._$body = this.$container.appendDiv('', 'group-box-body');
  var env = scout.HtmlEnvironment;
  var htmlBody = new scout.HtmlComponent(this._$body);
  htmlBody.setLayout(new scout.LogicalGridLayout(env.formColumnGap, env.formRowGap));

  this._createFieldArraysByType();
  for (var i=0; i<this.controls.length; i++) {
    this.controls[i].render(this._$body);
  }

  if (this.processButtons.length > 0) {
    var buttonBar = new scout.GroupBoxButtonBar(this.processButtons);
    buttonBar.render(this.$container);
  }
};

scout.GroupBox.prototype._createFieldArraysByType = function() {
  this.controls = [];
  this.systemButtons = [];
  this.customButtons = [];
  this.processButtons = [];

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

/**
 * @override FormField.js
 */
scout.GroupBox.prototype._renderBorderVisible = function(visible) {
  // NOP
};

/**
 * @override FormField.js
 */
scout.GroupBox.prototype._renderLabelVisible = function(visible) {
  // TODO AWE: (concept) discuss with C.GU -> auf dem GUI server korrigieren oder im Browser UI?
  this._$groupBoxTitle.setVisible(visible && this.label && !this.mainBox);
};

