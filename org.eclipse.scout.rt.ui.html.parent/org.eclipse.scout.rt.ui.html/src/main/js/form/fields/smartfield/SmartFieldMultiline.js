// TODO AWE: (smartfield) multiline oder remote muss zu einem behavior werden
// weil single/multi und local/remote in beliebigen kombinationen kommen können
scout.SmartFieldMultiline = function() {
  scout.SmartFieldMultiline.parent.call(this);
  this.options;
  this._$textField;
  this._$multilineField;
};
scout.inherits(scout.SmartFieldMultiline, scout.AbstractSmartField);

scout.SmartFieldMultiline.prototype._render = function($parent) {
  this.addContainer($parent, 'smart-field');
  this.addLabel();
  this.addMandatoryIndicator();

  this.$field = $.makeDIV('field')
    .appendTo(this.$container);

  this._$textField = $('<input>').
    attr('type', 'text').
    addClass('text-field').
    disableSpellcheck().
    blur(this._onFieldBlur.bind(this)).
    keyup(this._onKeyup.bind(this)).
    keydown(this._onKeydown.bind(this)).
    appendTo(this.$field);

  this._$multilineField = $.makeDIV('multiline-field', 'Täfernstrasse 16a<br/>CH 5405 Baden-Dättwil').
    appendTo(this.$field);

  this.addIcon(this.$field);
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

// @Override AbstractSmartField.js
scout.SmartFieldMultiline.prototype._get$Input = function() {
  return this._$textField;
};

//@Override AbstractSmartField.js
scout.SmartFieldMultiline.prototype._getInputBounds = function() {
  var fieldBounds = scout.HtmlComponent.getBounds(this.$field),
    textFieldBounds = scout.HtmlComponent.getBounds(this._$textField);
  fieldBounds.height = textFieldBounds.height;
  return fieldBounds;
};

//@Override AbstractSmartField.js
scout.SmartFieldMultiline.prototype._applyOption = function(option) {
  var tmp = option.split("\n"),
    firstLine = tmp.shift(),
    multiLines = tmp.join('<br/>');
  scout.SmartFieldMultiline.parent.prototype._applyOption.call(this, firstLine);
  this._$multilineField.html(multiLines);
};
