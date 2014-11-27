scout.AbstractSmartField = function(lookupStrategy) {
  scout.AbstractSmartField.parent.call(this);
  this._$popup;
  this._$viewport;
  this.options;
  this._selectedOption = -1;
  this._oldVal;
  this._lookupStrategy = lookupStrategy;

  lookupStrategy._smartField = this;
};
scout.inherits(scout.AbstractSmartField, scout.ValueField);

scout.AbstractSmartField.prototype._render = function($parent) {
  this.addContainer($parent, 'smart-field');
  this.addLabel();
  this.addField(scout.fields.new$TextField()
    .blur(this._onFieldBlur.bind(this))
    .click(this._onClick.bind(this))
    .keyup(this._onKeyup.bind(this))
    .keydown(this._onKeydown.bind(this)));
  this.addMandatoryIndicator();
  this.addIcon();
  this.addStatus();
};

/**
 * @param visible [optional] when true, returns only visible options from the DOM, otherwise return all options regardless of their visible state.
 */
scout.AbstractSmartField.prototype._get$Options = function(visible) {
  var filter = visible === true ? ':visible' : undefined;
  return this._$viewport.children(filter);
};

scout.AbstractSmartField.prototype._get$OptionsDiv = function() {
  return this._$popup.children('.options');
};

scout.AbstractSmartField.prototype._updateScrollbar = function() {
  scout.scrollbars.update(this._$viewport);
};

scout.AbstractSmartField.prototype._isNavigationKey = function(e) {
  return e.which === scout.keys.PAGE_UP ||
    e.which === scout.keys.PAGE_DOWN ||
    e.which === scout.keys.UP ||
    e.which === scout.keys.DOWN;
};

scout.AbstractSmartField.prototype._onClick = function(e) {
  if (!this._$popup) {
    this._openPopup();
  }
};

scout.AbstractSmartField.prototype._onIconClick = function(event) {
  scout.AbstractSmartField.parent.prototype._onIconClick.call(this, event);
  if (!this._$popup) {
    this._openPopup();
  }
};

// navigate in options
scout.AbstractSmartField.prototype._onKeydown = function(e) {
  if (this._isNavigationKey(e)) {
    // ensure popup is opened for following operations
    if (this._openPopup()) {
      return;
    }

    var pos = this._selectedOption,
      $options = this._get$Options(true);
    switch (e.which) {
      case scout.keys.PAGE_UP:
        pos -= 10;
        break;
      case scout.keys.PAGE_DOWN:
        pos += 10;
        break;
      case scout.keys.UP:
        pos--;
        break;
      case scout.keys.DOWN:
        pos++;
        break;
    }
    pos = Math.min(Math.max(0, pos), $options.length - 1);
    if (pos !== this._selectedOption) {
      this._selectOption($options, pos);
    }
  }
};

scout.AbstractSmartField.prototype._selectOption = function($options, pos) {
  if (this._selectedOption >= 0 && this._selectedOption < $options.length) {
    $($options[this._selectedOption]).removeClass('selected');
  }
  var $selectedOption = $($options[pos]);
  $selectedOption.addClass('selected');
  this._selectedOption = pos;
  scout.scrollbars.scrollTo(this._$viewport, $selectedOption);
};

scout.AbstractSmartField.prototype._onKeyup = function(e) {
  // escape
  if (e.which === scout.keys.ESC) {
    this.$field.blur();
    return;
  }

  // enter
  if (e.which === scout.keys.ENTER) {
    if (this._selectedOption > -1) {
      var value = $(this._get$Options(true).get(this._selectedOption)).data('option');
      this._applyOption(value);
      this._closePopup();
    }
    return;
  }

  if (e.which === scout.keys.TAB ||
    e.which === scout.keys.SHIFT ||
    this._isNavigationKey(e)) {
    return;
  }

  // ensure popup is opened for following operations
  if (this._openPopup()) {
    return;
  }

  // filter options
  this._filterOptions();
};

/**
 * Applies the given option on the text-field. Subclasses may override this method
 * to implement a different apply-behavior (for multiline fields for instance).
 * @param option
 */
scout.AbstractSmartField.prototype._applyOption = function(option) {
  this.$field
    .val(option)
    .get(0).select();
};

scout.AbstractSmartField.prototype._filterOptions = function() {
  var val = this.$field.val();
  if (this._oldVal === val) {
    $.log.debug('value of field has not changed - do not filter (oldVal=' + this._oldVal + ')');
    return;
  }
  this._selectedOption = -1;
  this._lookupStrategy.filterOptions(val);
  this._oldVal = val;
  $.log.debug('updated oldVal=' + this._oldVal);
  this._updateScrollbar();
};

/**
 * @param numOptions the height of the popup is 'numOptions * height per option in px'
 * @param vararg same as in #_setStatusText(vararg)
 */
scout.AbstractSmartField.prototype._showPopup = function(numOptions, vararg) {
  var fieldBounds = this._getInputBounds(),
    optionHeight = scout.HtmlEnvironment.formRowHeight,
    popupHeight = (numOptions + 1) * optionHeight,
    popupBounds = new scout.Rectangle(fieldBounds.x, fieldBounds.y + fieldBounds.height, fieldBounds.width, popupHeight);

  this._$popup = $.makeDiv('smart-field-popup')
    .on('mousedown', this._onPopupMousedown.bind(this))
    .append($.makeDIV('options'))
    .append($.makeDIV('status'))
    .appendTo(this.$container);
  scout.graphics.setBounds(this._$popup, popupBounds);
  // layout options and status-div
  var $optionsDiv = this._get$OptionsDiv();
  this._$viewport = scout.scrollbars.install($optionsDiv, {
    invertColors: true
  });
  scout.graphics.setSize($optionsDiv, fieldBounds.width - 2, popupHeight - optionHeight);
  this._setStatusText(vararg);
};

scout.AbstractSmartField.prototype._onPopupMousedown = function(event) {
  //Make sure field blur won't be triggered -> popup must not be closed on mouse down
  event.preventDefault();
};

/**
 * Returns the bounds of the text-input element. Subclasses may override this method when their
 * text-field is not === this.$field.
 * @returns
 */
scout.AbstractSmartField.prototype._getInputBounds = function() {
  return scout.graphics.getBounds(this.$field);
};

/**
 * Adds the given options to the DOM, tries to select the selected option by comparing to the value of the text-field.
 */
scout.AbstractSmartField.prototype._renderOptions = function(options) {
  var i, option, htmlOption, selectedPos = -1,
    val = this.$field.val();

  for (i = 0; i < options.length; i++) {
    option = options[i];
    htmlOption = option.replace(/\n/gi, "<br/>");
    $('<p>')
      .on('click', this._onOptionClick.bind(this))
      .appendTo(this._$viewport)
      .data('option', option) // stores the original text as received from the server
    .html(htmlOption);
    if (option === val) {
      selectedPos = i;
    }
  }
  if (selectedPos > -1) {
    this._selectOption(this._get$Options(true), selectedPos);
  }
  this._updateScrollbar();
};

scout.AbstractSmartField.prototype._emptyOptions = function(options) {
  this._$viewport.empty();
  this._updateScrollbar();
};

scout.AbstractSmartField.prototype._onOptionClick = function(e) {
  var selectedOption = $(e.target).data('option');
  $.log.info('option selected ' + selectedOption);
  this._applyOption(selectedOption);
  this._closePopup();
};

scout.AbstractSmartField.prototype._onFieldBlur = function() {
  scout.AbstractSmartField.parent.prototype._onFieldBlur.call(this);
  $.log.debug("_onBlur");
  this._closePopup();
};

scout.AbstractSmartField.prototype._closePopup = function() {
  if (this._$popup) {
    this._selectedOption = -1;
    this._$popup.remove();
    this._$popup = null;
  }
};

/**
 * @param vararg if vararg is numeric, a text is generated according to the given number of options and set in the status-bar<br/>
 *   if vararg is text, the given text is set in the status-bar
 */
scout.AbstractSmartField.prototype._setStatusText = function(vararg) {
  var text;
  if ($.isNumeric(vararg)) {
    if (vararg === 0) {
      text = scout.texts.get('noOptions');
    } else if (vararg === 1) {
      text = scout.texts.get('oneOption');
    } else {
      text = scout.texts.get('options', vararg);
    }
  } else {
    text = vararg;
  }
  this._$popup.children('.status').text(text);
};

scout.AbstractSmartField.prototype._openPopup = function() {
  this._lookupStrategy.openPopup();
};

scout.AbstractSmartField.prototype._onOptionsLoaded = function(options) {
  this._lookupStrategy.onOptionsLoaded(options);
};

scout.AbstractSmartField.prototype.onModelAction = function(event) {
  if (event.type === 'optionsLoaded') {
    this._onOptionsLoaded(event.options);
  } else {
    scout.AbstractSmartField.parent.prototype.onModelAction.call(this, event);
  }
};
