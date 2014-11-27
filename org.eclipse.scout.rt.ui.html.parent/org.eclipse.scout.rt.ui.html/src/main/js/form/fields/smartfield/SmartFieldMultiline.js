scout.SmartFieldMultiline = function(lookupStrategy) {
  scout.SmartFieldMultiline.parent.call(this, lookupStrategy);
  this.options;
  this._$multilineField;
};
scout.inherits(scout.SmartFieldMultiline, scout.AbstractSmartField);

scout.SmartFieldMultiline.prototype._render = function($parent) {
  this.addContainer($parent, 'smart-field');
  this.addLabel();
  this.addMandatoryIndicator();
  var $fieldContainer = $('<div>'),
    $field = scout.fields.new$TextField().
      addClass('multiline').
      blur(this._onFieldBlur.bind(this)).
      click(this._onClick.bind(this)).
      keyup(this._onKeyup.bind(this)).
      keydown(this._onKeydown.bind(this)).
      appendTo($fieldContainer);
  this.addField($field, $fieldContainer);
  this.addIcon($fieldContainer);
  this._$multilineField = $.makeDiv('multiline-field', '<br/><br/>').
    appendTo($fieldContainer);
  this.addStatus();
};

//@override ValueField.js
scout.SmartFieldMultiline.prototype._renderDisplayText = function(displayText) {
  var tmp = this._splitValue(displayText);
  this.$field.val(tmp.firstLine);
  this._$multilineField.html(tmp.multiLines);
};

// @override AbstractSmartField.js
scout.SmartFieldMultiline.prototype._getInputBounds = function() {
  var fieldBounds = scout.graphics.getBounds(this.$fieldContainer),
    textFieldBounds = scout.graphics.getBounds(this.$field);
  fieldBounds.height = textFieldBounds.height;
  return fieldBounds;
};

scout.SmartFieldMultiline.prototype._splitValue = function(value) {
  var tmp, firstLine = '', multiLines = '<br/><br/>';
  if (value) {
    tmp = value.split("\n");
    firstLine = tmp.shift();
    multiLines = tmp.join('<br/>');
  }
  return {
    firstLine: firstLine,
    multiLines: multiLines
  };
};

// @override AbstractSmartField.js
scout.SmartFieldMultiline.prototype._applyOption = function(option) {
  var tmp = this._splitValue(option);
  scout.SmartFieldMultiline.parent.prototype._applyOption.call(this, tmp.firstLine);
  this._$multilineField.html(tmp.multiLines);
};
