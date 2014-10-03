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
    keyup(this._onKeyup.bind(this)).
    keydown(this._onKeydown.bind(this));

  this.addIcon();
  this.addStatus();
};

// navigate in options
scout.SmartField.prototype._onKeydown = function(e) {
  if (e.which == 33 || e.which == 34 || e.which == 38 || e.which == 40) {

    // ensure popup is opened for following operations
    if (!this._$popup) {
      setTimeout(function() {
        this._openPopup();
      }.bind(this));
      return;
    }

    var $options = this._$popup.children(':visible');
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
      if (this._selectedOption >= 0 && this._selectedOption < $options.length) {
        $($options[this._selectedOption]).removeClass('selected');
      }
      var $selectedOption = $($options[pos]);
      $selectedOption.addClass('selected');
      var h = this._$popup.height();
      var hPerOption = 19; // TODO AWE: (smartfield) höhe aller optionen dynamisch ermitteln
      var top = pos * hPerOption;
      this._$popup.scrollTop(top);
      $.log.info('_selectedOption=' + this._selectedOption + ' pos='+pos + ' top=' + top + ' text=' +  $selectedOption.html());
      this._selectedOption = pos;
    }
  }
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

