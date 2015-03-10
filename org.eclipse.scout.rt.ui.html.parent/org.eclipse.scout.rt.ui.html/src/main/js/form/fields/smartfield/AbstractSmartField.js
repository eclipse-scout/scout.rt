scout.AbstractSmartField = function() {
  scout.AbstractSmartField.parent.call(this);
  this._$popup;
  this._$optionsDiv;
  this.options;
  this._selectedOption = -1;
  this._oldVal;
  this._addAdapterProperties(['proposalChooser']);
};
scout.inherits(scout.AbstractSmartField, scout.ValueField);

scout.AbstractSmartField.prototype.init = function(model, session) {
  scout.AbstractSmartField.parent.prototype.init.call(this, model, session);
};

scout.AbstractSmartField.prototype._render = function($parent) {
  var cssClass = this.proposal ? 'proposal-field' : 'smart-field';
  this.addContainer($parent, cssClass);
  this.addLabel();
  this.addField(scout.fields.new$TextField()
    .blur(this._onFieldBlur.bind(this))
    .click(this._onClick.bind(this))
    .keyup(this._onKeyUp.bind(this))
    .keydown(this._onKeyDown.bind(this)));
  this.addMandatoryIndicator();
  this.addIcon();
  this.addStatus();
};

scout.AbstractSmartField.prototype._renderProperties = function() {
  scout.AbstractSmartField.parent.prototype._renderProperties.call(this);
  this._renderProposalChooser();
};


scout.AbstractSmartField.prototype._renderProposalChooser = function() {
  if (!this._$popup) {
    // We always expect the popup to be open at this point
    return;
  }
  // FIXME AWE: können wir besser auf diesen PC reagieren (spezifisches event?)
  $.log.info('_renderProposalChooser proposalChooser=' + this.proposalChooser);
  if (this.proposalChooser) {
    this.proposalChooser.render(this._$popup);
    this._resizePopup();
  }
  else {
    this._closePopup(false);
  }
};

scout.AbstractSmartField.prototype._removeProposalChooser = function() {
  $.log.info('_removeProposalChooser proposalChooser=' + this.proposalChooser);
  this._closePopup(false);
};

scout.AbstractSmartField.prototype._updateScrollbar = function() {
  scout.scrollbars.update(this._$optionsDiv);
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
scout.AbstractSmartField.prototype._onKeyDown = function(e) {
  if (e.which === scout.keys.TAB) {
    if (this._selectedOption > -1) {
      // FIXME AWE: (smart-field) apply selected proposal on TAB
      this._applyOption(value);
      this._closePopup();
    }
    return;
  }

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
  scout.scrollbars.scrollTo(this._$optionsDiv, $selectedOption);
};

scout.AbstractSmartField.prototype._onKeyUp = function(e) {
  // escape
  if (e.which === scout.keys.ESC) {
    this.$field.blur();
    return;
  }

  // enter
  if (e.which === scout.keys.ENTER) {
    if (this._selectedOption > -1) {
      // FIXME AWE: (smart-field) apply selected proposal on ENTER
      this._applyOption(value);
      this._closePopup();
    }
    return;
  }

  // Pop-ups shouldn't open when smart-fields are focused by tabbing through them
  if (e.which === scout.keys.TAB ||
    e.which === scout.keys.SHIFT ||
    this._isNavigationKey(e)) {
    return;
  }

  // ensure pop-up is opened for following operations
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
  var val = this._searchText();
  if (this._oldVal === val) {
    $.log.debug('value of field has not changed - do not filter (oldVal=' + this._oldVal + ')');
    return;
  }
  this._selectedOption = -1;
  this.session.send(this.id, 'openProposal', {
    searchText: this._searchText(),
    selectCurrentValue: false});

  this._oldVal = val;
  $.log.debug('updated oldVal=' + this._oldVal);
};

scout.AbstractSmartField.prototype._onPopupMousedown = function(event) {
  // Make sure field blur won't be triggered -> pop-up must not be closed on mouse down
  event.preventDefault();
};

/**
 * Returns the bounds of the text-input element. Subclasses may override this method when their
 * text-field is not === this.$field.
 */
scout.AbstractSmartField.prototype._getInputBounds = function() {
  return scout.graphics.offsetBounds(this.$field);
};

scout.AbstractSmartField.prototype._onFieldBlur = function() {
  scout.AbstractSmartField.parent.prototype._onFieldBlur.call(this);
  $.log.debug("_onBlur");
  this._closePopup();
};

scout.AbstractSmartField.prototype._closePopup = function(notifyServer) {
  if (this._$popup) {
    notifyServer = notifyServer === undefined ? true : notifyServer;
    if (notifyServer) {
      this.session.send(this.id, 'closeProposal');
    }
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
      text = this.session.text('NoOptions');
    } else if (vararg === 1) {
      text = this.session.text('OneOption');
    } else {
      text = this.session.text('NumOptions', vararg);
    }
  } else {
    text = vararg;
  }
  this._$popup.children('.status').text(text);
};

scout.AbstractSmartField.prototype._searchText = function() {
  var displayText = this.$field.val();
  return scout.strings.hasText(displayText) ? displayText : '*';
};

// FIXME AWE: an dieser stelle müssten wir auch die screen-boundaries berücksichtigen
// und entscheiden, ob das popup gegen unten oder gegen oben geöffnet werden soll.
scout.AbstractSmartField.prototype._openPopup = function() {
  if (this._$popup) {
    return false;
  } else {
    // FIXME AWE: (smart-field) compare with Swing client when searchText is '*' and when it's == the
    // display text. Asteriks means "browse all".
    this.session.send(this.id, 'openProposal', {
      searchText: '*', // this._searchText(),
      selectCurrentValue: false});

    this._$popup = $.makeDiv('smart-field-popup')
      .on('mousedown', this._onPopupMousedown.bind(this))
      .appendTo($('body'));

    var htmlPopup = new scout.HtmlComponent(this._$popup, this.session),
      fieldBounds = this._getInputBounds();
    htmlPopup.validateRoot = true;
    htmlPopup.setLayout(new scout.PopupLayout(htmlPopup, this._resizePopup.bind(this)));
    htmlPopup.setBounds(new scout.Rectangle(
      fieldBounds.x,
      fieldBounds.y + fieldBounds.height,
      fieldBounds.width,
      150));

    return true;
  }
};

/**
 * This method is called when the PopupLayout is invalidated which happens typically
 * when the proposal table adds or removes rows.
 */
scout.AbstractSmartField.prototype._resizePopup = function() {
  var htmlPopup = scout.HtmlComponent.get(this._$popup),
    prefSize = htmlPopup.getPreferredSize(),
    inputBounds = this._getInputBounds();
  prefSize.width = Math.max(inputBounds.width, prefSize.width);
  prefSize.height = Math.min(400, prefSize.height);
  $.log.info('_resizePopup prefSize=' + prefSize);
  htmlPopup.setSize(prefSize);
};
