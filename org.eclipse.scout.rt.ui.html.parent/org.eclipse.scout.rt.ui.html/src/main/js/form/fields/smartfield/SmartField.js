scout.SmartField = function() {
  scout.SmartField.parent.call(this);
  this._$popup;
  this.options;
  this._selectedOption = -1;
  this._oldVal;
};
scout.inherits(scout.SmartField, scout.ValueField);

scout.SmartField.prototype._render = function($parent) {
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

// navigate in options
// TODO AWE: (smartfield) scrolling intelligenter machen (erst scrollen, wenn man an die boundaries stösst).
scout.SmartField.prototype._onKeydown = function(e) {
  if (e.which == 33 || e.which == 34 || e.which == 38 || e.which == 40) {

    // ensure popup is opened for following operations
    if (!this._$popup) {
      setTimeout(function() {
        this._openPopup();
      }.bind(this));
      return;
    }

    var $options = this._$popup.children('.options').children(':visible');
    var pos = this._selectedOption;
    if (e.which == 33) { pos-=10; }
    if (e.which == 34) { pos+=10; }
    if (e.which == 38) { pos--; }
    if (e.which == 40) { pos++; }
    if (pos < 0) {
      pos = 0;
    } else {
      if (pos >= $options.length) {
        pos = $options.length - 1;
      }
    }
    if (pos != this.selectedOption) {
      this._selectOption($options, pos);
    }
  }
};

scout.SmartField.prototype._selectOption = function($options, pos) {
  if (this._selectedOption >= 0 && this._selectedOption < $options.length) {
    $($options[this._selectedOption]).removeClass('selected');
  }
  var $selectedOption = $($options[pos]);
  $selectedOption.addClass('selected');
  var h = this._$popup.height();
  var hPerOption = 19;
  var top = pos * hPerOption;
  this._$popup.children('.options').scrollTop(top);
  $.log.info('_selectedOption=' + this._selectedOption + ' pos='+pos + ' top=' + top + ' text=' +  $selectedOption.html());
  this._selectedOption = pos;
};

scout.SmartField.prototype._onKeyup = function(e) {

  // escape
  if (e.which == 27) { // TODO AWE (smartfield) key-constanten von C.GU verwenden
    this.$field.blur();
    return;
  }

  // enter
  if (e.which == 13) {
    if (this._selectedOption > -1) {
      var value = $(this._$popup.children('.options').children(':visible').get(this._selectedOption)).html();
      this.$field.val(value);
      this.$field.get(0).select();
      this._closePopup();
    }
    return;
  }

  // TODO AWE: (smartfield) das geht sicher noch schöner --> check preventDefault
  if (e.which == 33 || e.which == 34 || e.which == 38 || e.which == 40) {
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
  this._filterOptions(this.$field.val());
};


scout.SmartField.prototype._openPopup = function() {
  var numOptions = this.options.length,
    fieldBounds = scout.HtmlComponent.getBounds(this.$field),
    popupHeight = Math.min(10, numOptions) * 19 + 19 + 3, // TODO AWE: (smartfield) layout dynamisch
    popupBounds = new scout.Rectangle(fieldBounds.x, fieldBounds.y + fieldBounds.height, fieldBounds.width, popupHeight);
  this._$popup = $('<div>').
    addClass('smart-field-popup').
    append($('<div>').addClass('options')).
    append($('<div>').addClass('status').text(this._getStatusText(numOptions))).
    appendTo(this.$container);
  scout.HtmlComponent.setBounds(this._$popup, popupBounds);

  // layout options and status div
  scout.HtmlComponent.setSize(this._$popup.children('.options'), fieldBounds.width - 4, popupHeight - 19 - 3);

  // add options
  // try to find selected value in options
  var i, option,
    $optionsDiv = this._$popup.children('.options'),
    val = this.$field.val(),
    selectedPos = -1;
  for (i=0; i<numOptions; i++) {
    option = this.options[i];
    $('<div>').
      on('mousedown', this._onOptionMousedown.bind(this)).
      appendTo($optionsDiv).
      html(option);
    if (option === val) {
      selectedPos = i;
    }
  }

  if (selectedPos > -1) {
    this._selectOption(this._$popup.children('.options').children(), selectedPos);
  }
};

scout.SmartField.prototype._onOptionMousedown = function(e) {
  var selectedText = $(e.target).html();
  $.log.info('option selected ' + selectedText);
  this.$field.val(selectedText);
  this._closePopup();
};


scout.SmartField.prototype._onFocus = function() {
  this._oldVal = this.$field.val();
  $.log.debug('_onFocus. set oldVal=' + this._oldVal);
  if (!this._$popup) {
    this._openPopup();
  }
};

// TODO AWE: (smartfield) wenn das popup offen ist und man wegtabbt bekommt das nächste field noch nicht den fokus
scout.SmartField.prototype._onBlur = function() {
  $.log.debug("_onBlur");
  this._closePopup();
};

scout.SmartField.prototype._closePopup = function() {
  if (this._$popup) {
    this._selectedOption = -1;
    this._$popup.remove();
    this._$popup = null;
  }
};

scout.SmartField.prototype._getStatusText = function(numOptions) {
  if (numOptions === 0) {
    return 'Keine Übereinstimmung';
  } else if (numOptions === 1) {
    return '1 Option';
  } else {
    return numOptions + ' Optionen';
  }
};

scout.SmartField.prototype._filterOptions = function(text) {
  var val = this.$field.val();
  if (this._oldVal === val) {
    $.log.debug('value of field has not changed - do not filter (oldVal=' + this._oldVal + ')');
    return;
  }
  this._selectedOption = -1;
  var statusText, match, numVisibleOptions = 0,
    showAll = !text || '*' === text,
    regexp = new RegExp(text, 'i');
  this._$popup.children('.options').children().each(function() {
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
  this._$popup.children('.status').text(this._getStatusText(numVisibleOptions));
  this._oldVal = val;
  $.log.debug('updated oldVal=' + this._oldVal);
};

