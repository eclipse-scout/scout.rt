// TODO AWE: (smartfield) multiline oder remote muss zu einem behavior werden
// weil single/multi und local/remote in beliebigen kombinationen kommen können
scout.SmartFieldMultiline = function() {
  scout.SmartFieldMultiline.parent.call(this);
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
  this._$multilineField = $.makeDIV('multiline-field', '<br/><br/>').
    appendTo($fieldContainer);
  this.addStatus();
};

// FIXME AWE: (smartfield) remove copy&paste when todo in header is solved
scout.SmartFieldMultiline.prototype._filterOptionsImpl = function(query) {
  var statusText, match, numVisibleOptions = 0,
    showAll = !query || '*' === query,
    regexp = new RegExp(query, 'im');
  this._get$Options().each(function() {
    if (showAll) {
      $(this).setVisible(true);
    } else {
      match = $(this).html().match(regexp);
      if (match) { numVisibleOptions++; }
      $.log.debug('regexp='+regexp + ' html='+$(this).html());
      $(this).setVisible(match);
    }
  });
  if (showAll) { numVisibleOptions = this.options.length; }
  this._setStatusText(numVisibleOptions);
};

scout.SmartFieldMultiline.prototype._openPopup = function() {
  var numOptions = this.options.length;
  this._showPopup(Math.min(10, numOptions), numOptions);
  this._renderOptions(this.options);
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
  var tmp = value.split("\n");
  return {
    firstLine: tmp.shift(),
    multiLines: tmp.join('<br/>')
  };
};

// @override AbstractSmartField.js
scout.SmartFieldMultiline.prototype._applyOption = function(option) {
  var tmp = this._splitValue(option);
  scout.SmartFieldMultiline.parent.prototype._applyOption.call(this, tmp.firstLine);
  this._$multilineField.html(tmp.multiLines);
};
