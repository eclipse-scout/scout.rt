scout.SmartFieldMultiline = function(lookupStrategy) {
  scout.SmartFieldMultiline.parent.call(this, lookupStrategy);
  this.options;
  this._$multilineField;
};
scout.inherits(scout.SmartFieldMultiline, scout.AbstractSmartField);

scout.SmartFieldMultiline.prototype._render = function($parent) {
  var $field, htmlComp;

  this.addContainer($parent, 'smart-field');
  this.addLabel();
  this.addMandatoryIndicator();
  this.addFieldContainer($('<div>'));
  htmlComp = new scout.HtmlComponent(this.$fieldContainer, this.session);
  htmlComp.setLayout(new scout.SmartFieldMultilineLayout());

  $field = scout.fields.new$TextField().
      addClass('multiline').
      blur(this._onFieldBlur.bind(this)).
      click(this._onClick.bind(this)).
      keyup(this._onKeyUp.bind(this)).
      keydown(this._onKeyDown.bind(this)).
      appendTo(this.$fieldContainer);
  this.addField($field);
  this.addIcon(this.$fieldContainer);
  this._$multilineField = $.makeDiv('multiline-field')
    .appendTo(this.$fieldContainer);

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
  var fieldBounds = scout.graphics.offsetBounds(this.$fieldContainer),
    textFieldBounds = scout.graphics.offsetBounds(this.$field);
  fieldBounds.height = textFieldBounds.height;
  return fieldBounds;
};

scout.SmartFieldMultiline.prototype._splitValue = function(value) {
  var firstLine = '', multiLines = '';
  if (value) {
    var tmp = value.split("\n");
    firstLine = tmp.shift();
    multiLines = tmp.join('<br/>');
  }
  return {
    firstLine: firstLine,
    multiLines: multiLines
  };
};

