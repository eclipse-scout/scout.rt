scout.SmartField = function() {
  scout.SmartField.parent.call(this);
  this._$popup;
  this.options;
  this._selectedOption = -1;
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
    keyup(this._onKeyup.bind(this));

  this.addIcon();
  this.addStatus();
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
      var value = $(this._$popup.children(':visible').get(this._selectedOption)).html();
      this.$field.val(value);
      this._closePopup();
      this.$field.select();
    }
    return;
  }

  // ensure popup is opened for following operations
  if (!this._$popup) {
    setTimeout(function() {
      this._openPopup();
    }.bind(this));
    return;
  }

  // navigate in options
  // TODO AWE: (smartfield) support page-up/down 33/34
  if (e.which == 38 || e.which == 40) {
    var $options = this._$popup.children(':visible');
    var pos = this._selectedOption;
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
      if (this._selectedOption >= 0 && this._selectedOption < $options.length) {
        $($options[this._selectedOption]).removeClass('selected');
      }
      $($options[pos]).addClass('selected');
      $.log.info('_selectedOption=' + this._selectedOption + ' pos='+pos);
      this._selectedOption = pos;
    }
    return;
  }

  // filter options
  this._filterOptions(this.$field.val());
};

scout.SmartField.prototype._openPopup = function() {
  var fieldBounds = scout.HtmlComponent.getBounds(this.$field);
  var popupBounds = new scout.Rectangle(fieldBounds.x, fieldBounds.y + fieldBounds.height, fieldBounds.width, 100);
  this._$popup = $('<div>').
    addClass('smart-field-popup').
    appendTo(this.$container);
  scout.HtmlComponent.setBounds(this._$popup, popupBounds);

  // add options
  var i, option;
  for (i=0; i<this.options.length; i++) {
    option = this.options[i];
    $('<div>').
      on('mousedown', this._onOptionMousedown.bind(this)).
      appendTo(this._$popup).
      html(option);
  }
};

scout.SmartField.prototype._onOptionMousedown = function(e) {
  var selectedText = $(e.target).html();
  $.log.info('option selected ' + selectedText);
  this.$field.val(selectedText);
  this._closePopup();
};


scout.SmartField.prototype._onFocus = function() {
  $.log.debug("_onFocus");
  if (!this._$popup) {
    this._openPopup();
  }
};

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

scout.SmartField.prototype._filterOptions = function(text) {
  this._selectedOption = -1;
  var regexp = new RegExp(text, 'i');
  this._$popup.children().each(function() {
    if (!text || '*' === text) {
      $(this).setVisible(true);
    } else {
      // TODO AWE: (smartfield) implement clever client-side match algorithm
      $.log.debug('regexp='+regexp + ' html='+$(this).html());
      $(this).setVisible($(this).html().match(regexp));
    }
  });
};

