scout.RadioButtonGroupKeyStrokeAdapter = function(radioButtonGroup) {
  scout.RadioButtonGroupKeyStrokeAdapter.parent.call(this, radioButtonGroup);
  this.registerKeyStroke(new scout.RadioButtonGroupLeftKeyStroke(radioButtonGroup));
  this.registerKeyStroke(new scout.RadioButtonGroupRightKeyStroke(radioButtonGroup));
};
scout.inherits(scout.RadioButtonGroupKeyStrokeAdapter, scout.FormFieldKeyStrokeAdapter);

scout.RadioButtonGroupLeftKeyStroke = function(field) {
  scout.RadioButtonGroupLeftKeyStroke.parent.call(this);
  this.keyStroke = 'LEFT';
  this.drawHint = false;
  this._field = field;
  this.initKeyStrokeParts();
};
scout.inherits(scout.RadioButtonGroupLeftKeyStroke, scout.KeyStroke);

scout.RadioButtonGroupLeftKeyStroke.prototype.handle = function(event) {
  var fieldBefore, $radioButton = $(event.target),
    selectedKey = $radioButton.attr('value');
  for (var key in this._field._radioButtonMap) {
    var radioButton = this._field._radioButtonMap[key];
    if (fieldBefore && key === selectedKey) {
      radioButton._renderTabbable(false);
      fieldBefore._renderTabbable(true);
      fieldBefore.$field.focus();
      fieldBefore._toggleChecked();
      break;
    }
    if (radioButton.enabled) {
      fieldBefore = radioButton;
    }
  }
};

scout.RadioButtonGroupRightKeyStroke = function(field) {
  scout.RadioButtonGroupRightKeyStroke.parent.call(this);
  this.keyStroke = 'RIGHT';
  this.drawHint = false;
  this._field = field;
  this.initKeyStrokeParts();
};
scout.inherits(scout.RadioButtonGroupRightKeyStroke, scout.KeyStroke);

scout.RadioButtonGroupRightKeyStroke.prototype.handle = function(event) {
  var fieldBefore, $radioButton = $(event.target),
    selectedKey = $radioButton.attr('value');
  for (var key in this._field._radioButtonMap) {
    var radioButton = this._field._radioButtonMap[key];
    if (fieldBefore && radioButton.enabled) {
      radioButton._renderTabbable(true);
      radioButton.$field.focus();
      radioButton._toggleChecked();
      fieldBefore._renderTabbable(false);
      break;
    }
    if (key === selectedKey && radioButton.enabled) {
      fieldBefore = this._field._radioButtonMap[key];
    }
  }
};
