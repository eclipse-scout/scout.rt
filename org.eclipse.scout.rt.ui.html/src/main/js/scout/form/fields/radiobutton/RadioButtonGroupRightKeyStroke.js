scout.RadioButtonGroupRightKeyStroke = function(radioButtonGroup) {
  scout.RadioButtonGroupRightKeyStroke.parent.call(this);
  this.field = radioButtonGroup;
  this.which = [scout.keys.RIGHT];
  this.renderingHints.render = false;
};
scout.inherits(scout.RadioButtonGroupRightKeyStroke, scout.KeyStroke);

scout.RadioButtonGroupRightKeyStroke.prototype.handle = function(event) {
  var fieldBefore,
    selectedKey = $(event.target).attr('value');
  for (var key in this.field._radioButtonMap) {
    var radioButton = this.field._radioButtonMap[key];
    if (fieldBefore && radioButton.enabled && radioButton.visible) {
      radioButton._renderTabbable(true);
      this.field.session.focusManager.requestFocus(radioButton.$field);
      radioButton._toggleChecked();
      fieldBefore._renderTabbable(false);
      break;
    }
    if (key === selectedKey && radioButton.enabled && radioButton.visible) {
      fieldBefore = this.field._radioButtonMap[key];
    }
  }
};
