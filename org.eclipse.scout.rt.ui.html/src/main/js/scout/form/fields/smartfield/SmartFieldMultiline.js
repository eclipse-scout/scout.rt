scout.SmartFieldMultiline = function() {
  scout.SmartFieldMultiline.parent.call(this);
  this.options;
  this._$multilineField;
};
scout.inherits(scout.SmartFieldMultiline, scout.SmartField);

scout.SmartFieldMultiline.prototype._render = function($parent) {
  var $field, htmlComp;

  this.addContainer($parent, 'smart-field', new scout.SmartFieldLayout(this));
  this.addLabel();
  this.addMandatoryIndicator();
  this.addFieldContainer($('<div>'));
  htmlComp = new scout.HtmlComponent(this.$fieldContainer, this.session);
  htmlComp.setLayout(new scout.SmartFieldMultilineLayout());

  $field = scout.fields.new$TextField().
      addClass('multiline').
      blur(this._onFieldBlur.bind(this)).
      click(this._onClick.bind(this)).
      focus(this._onFocus.bind(this)).
      keyup(this._onKeyUp.bind(this)).
      keydown(this._onKeyDown.bind(this)).
      appendTo(this.$fieldContainer);
  this.addField($field);
  this.addIcon(this.$fieldContainer);
  this._$multilineField = $.makeDiv('multiline-field')
    .appendTo(this.$fieldContainer);

  this.addStatus();
  this.addPopup();
};

/**
 * @override ValueField.js
 */
scout.SmartFieldMultiline.prototype._renderDisplayText = function(displayText) {
  var tmp = displayText.split('\n'),
    firstLine = tmp.shift(),
    additionalLines = tmp.join('<br/>');
  this.$field.val(firstLine);
  this._$multilineField.html(additionalLines);
};

/**
 * @override SmartField.js
 */
scout.SmartFieldMultiline.prototype._getInputBounds = function() {
  var fieldBounds = scout.graphics.offsetBounds(this.$fieldContainer),
    textFieldBounds = scout.graphics.offsetBounds(this.$field);
  fieldBounds.height = textFieldBounds.height;
  return fieldBounds;
};

/**
 * Concatenates the text in the input-field and from the other additional lines in model.displayText
 * (which are displayed in the text-box below the input-field).
 *
 * @override SmartField.js
 */
scout.SmartFieldMultiline.prototype._readSearchText = function() {
  var i,
    firstLine = this.$field.val(),
    newDisplayText = [firstLine],
    oldDisplayText = this.displayText.split('\n');
  for (i = 1; i < oldDisplayText.length; i++) {
    newDisplayText.push(oldDisplayText[i]);
  }
  return newDisplayText.join('\n');
};

