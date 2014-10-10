scout.AbstractSmartField = function() {
  scout.AbstractSmartField.parent.call(this);
  this._$popup;
  this._$viewport;
  this.options;
  this._selectedOption = -1;
  this._oldVal;
};
scout.inherits(scout.AbstractSmartField, scout.ValueField);

scout.AbstractSmartField.prototype._render = function($parent) {
  this.addContainer($parent, 'smart-field');
  this.addLabel();
  this.addMandatoryIndicator();

  this.$field = $('<input>').
    attr('type', 'text').
    addClass('field').
    disableSpellcheck().
    blur(this._onFieldBlur.bind(this)).
    appendTo(this.$container).
    // TODO AWE: (smartfield) event-handling mit C.GU besprechen, siehe auch ValueField.js
    // --> 1. auf ValueField eine attachXyzEvent() Methode machen
    //     2. _onXyzEvent überschreiben
    focus(this._onFocus.bind(this)).
    blur(this._onBlur.bind(this)).
    keyup(this._onKeyup.bind(this)).
    keydown(this._onKeydown.bind(this));

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
  scout.Scrollbar2.update(this._$viewport);
};

scout.AbstractSmartField.prototype._isNavigationKey = function(e) {
  return e.which === scout.keys.PAGE_UP ||
    e.which === scout.keys.PAGE_DOWN ||
    e.which === scout.keys.UP ||
    e.which === scout.keys.DOWN;
};

// navigate in options
// TODO AWE: (smartfield) scrolling intelligenter machen (erst scrollen, wenn man an die boundaries stösst).
scout.AbstractSmartField.prototype._onKeydown = function(e) {
  if (this._isNavigationKey(e)) {
    // ensure popup is opened for following operations
    if (!this._$popup) {
      setTimeout(function() {
        this._openPopup();
      }.bind(this));
      return;
    }

    var pos = this._selectedOption,
        $options = this._get$Options(true);
    switch (e.which) {
      case scout.keys.PAGE_UP: pos -= 10; break;
      case scout.keys.PAGE_DOWN: pos += 10; break;
      case scout.keys.UP: pos--; break;
      case scout.keys.DOWN: pos++; break;
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

  var $selectedOption = $($options[pos]),
    scrollDir = 'n/a',
    scrollTop = this._$viewport.scrollTop(),
    viewportH = this._$viewport.height(),
    optionH = scout.HtmlComponent.getSize($selectedOption).height,
    optionY = $selectedOption.position().top;

  $selectedOption.addClass('selected');

  if (optionY < 0) {
    scrollDir = 'Up';
    this._$viewport.scrollTop(scrollTop + optionY);
  } else if (optionY + optionH > viewportH) {
    scrollDir = 'Down';
    this._$viewport.scrollTop(scrollTop + optionY + optionH - viewportH);
  }

  $.log.debug('_selectedOption=' + this._selectedOption + ' pos=' + pos + ' scrollDir=' + scrollDir + ' text=' +  $selectedOption.text());
  this._selectedOption = pos;
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
      var value = $(this._get$Options(true).get(this._selectedOption)).html();
      this.$field.val(value);
      this.$field.get(0).select();
      this._closePopup();
    }
    return;
  }

  // TODO AWE: (smartfield) das geht sicher noch schöner --> check preventDefault/stopPropagation
  if (this._isNavigationKey(e)) {
    return;
  }

  // ensure popup is opened for following operations
  if (!this._$popup) {
    setTimeout(function() {
      this._openPopup();
    }.bind(this));
    return;
  }

  // filter options
  this._filterOptions();
};

scout.AbstractSmartField.prototype._filterOptions = function() {
  var val = this.$field.val();
  if (this._oldVal === val) {
    $.log.debug('value of field has not changed - do not filter (oldVal=' + this._oldVal + ')');
    return;
  }
  this._selectedOption = -1;
  this._filterOptionsImpl(val);
  this._oldVal = val;
  $.log.debug('updated oldVal=' + this._oldVal);
};

/**
 * @param numOptions the height of the popup is 'numOptions * height per option in px'
 * @param vararg same as in #_setStatusText(vararg)
 */
scout.AbstractSmartField.prototype._showPopup = function(numOptions, vararg) {
  var fieldBounds = scout.HtmlComponent.getBounds(this.$field),
    popupHeight = numOptions * 24 + 24 + 3, // TODO AWE: (smartfield) popup-layout dynamischer
    popupBounds = new scout.Rectangle(fieldBounds.x, fieldBounds.y + fieldBounds.height, fieldBounds.width, popupHeight);
  this._$popup = $('<div>').
    addClass('smart-field-popup').
    append($('<div>').addClass('options')).
    append($('<div>').addClass('status')).
    appendTo(this.$container);
  scout.HtmlComponent.setBounds(this._$popup, popupBounds);
  // layout options and status-div
  var $optionsDiv = this._get$OptionsDiv();
  this._$viewport = scout.Scrollbar2.install($optionsDiv, {invertColors:true});
  scout.HtmlComponent.setSize($optionsDiv, fieldBounds.width - 4, popupHeight - 24 - 3);
  this._setStatusText(vararg);
};

/**
 * Adds the given options to the DOM, tries to select the selected option by comparing to the value of the text-field.
 */
scout.AbstractSmartField.prototype._renderOptions = function(options) {
  var i, option, selectedPos = -1,
    val = this.$field.val();
  for (i=0; i<options.length; i++) {
    option = options[i];
    $('<div>').
      on('mousedown', this._onOptionMousedown.bind(this)).
      appendTo(this._$viewport).
      html(option);
    if (option === val) {
      selectedPos = i;
    }
  }
  if (selectedPos > -1) {
    this._selectOption(this._get$Options(true), selectedPos);
  }
};

scout.AbstractSmartField.prototype._emptyOptions = function(options) {
  this._$viewport.empty();
};

scout.AbstractSmartField.prototype._onOptionMousedown = function(e) {
  var selectedText = $(e.target).html();
  $.log.info('option selected ' + selectedText);
  this.$field.val(selectedText);
  this._closePopup();
};

scout.AbstractSmartField.prototype._onFocus = function() {
  this._oldVal = this.$field.val();
  $.log.debug('_onFocus. set oldVal=' + this._oldVal);
  if (!this._$popup) {
    this._openPopup();
  }
};

scout.AbstractSmartField.prototype._onBlur = function() {
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

