scout.RadioButtonGroupLeftKeyStroke = function(radioButtonGroup) {
  scout.RadioButtonGroupLeftKeyStroke.parent.call(this);
  this.field = radioButtonGroup;
  this.which = [scout.keys.LEFT];
  this.renderingHints.render = false;
};
scout.inherits(scout.RadioButtonGroupLeftKeyStroke, scout.KeyStroke);

scout.RadioButtonGroupLeftKeyStroke.prototype.handle = function(event) {
  var fieldBefore,
    selectedKey = $(event.target).attr('value');
  for (var key in this.field._radioButtonMap) {
    var radioButton = this.field._radioButtonMap[key];
    if (fieldBefore && key === selectedKey) {
      radioButton._renderTabbable(false);
      fieldBefore._renderTabbable(true);
      this.field.session.focusManager.requestFocus(fieldBefore.$field);
      fieldBefore._toggleChecked();
      break;
    }
    if (radioButton.enabled) {
      fieldBefore = radioButton;
    }
  }
};
