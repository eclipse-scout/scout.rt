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
  this.addFieldContainer($('<div>'));
  htmlComp = new scout.HtmlComponent(this.$fieldContainer, this.session);
  htmlComp.setLayout(new scout.SmartFieldMultilineLayout());

  $field = scout.fields.inputOrDiv(this)
    .addClass('multiline')
    .click(this._onClick.bind(this))
    .appendTo(this.$fieldContainer);
  if (!this.touch) {
    $field
      .blur(this._onFieldBlur.bind(this))
      .focus(this._onFocus.bind(this))
      .keyup(this._onKeyUp.bind(this))
      .keydown(this._onKeyDown.bind(this));
  }
  this.addField($field);
  this._$multilineField = $.makeDiv('multiline-field')
    .appendTo(this.$fieldContainer);

  if (!this.embedded) {
    this.addMandatoryIndicator();
  }
  this.addIcon(this.$fieldContainer);
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
  scout.fields.valOrText(this, this.$field, firstLine);
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
    firstLine = scout.fields.valOrText(this, this.$field),
    newDisplayText = [firstLine],
    oldDisplayText = this.displayText.split('\n');
  for (i = 1; i < oldDisplayText.length; i++) {
    newDisplayText.push(oldDisplayText[i]);
  }
  return newDisplayText.join('\n');
};

