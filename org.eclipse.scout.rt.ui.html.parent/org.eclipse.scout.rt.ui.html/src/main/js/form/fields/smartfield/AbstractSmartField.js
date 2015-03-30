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

/**
 * @override
 */
scout.AbstractSmartField.prototype._registerKeyStrokeAdapter = function() {
  this.keyStrokeAdapter = new scout.SmartFieldKeyStrokeAdapter(this);
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
  $.log.info('_renderProposalChooser proposalChooser=' + this.proposalChooser);
  if (this.proposalChooser) {
    this.proposalChooser.render(this._$popup);
    this._resizePopup();
  }
};

/**
 * This method is called after a valid option has been selected in the proposal chooser.
 */
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

// FIXME AWE: (smart-field) hier das konzept von N.BU mit Actions, KeyStrokes verwenden (keyup, keydown)

// navigate in options
scout.AbstractSmartField.prototype._onKeyDown = function(event) {
  if (event.which === scout.keys.TAB) {
    this._acceptProposal();
    return;
  }

  // stop propagation if popup is open, so other key listeners aren't triggered (e.g. form close)
  if (event.which === scout.keys.ENTER ||
      event.which === scout.keys.ESCAPE) {
    if (this._$popup) {
      event.stopPropagation();
    }
    return;
  }

  if (this._isNavigationKey(event)) {
    // ensure popup is opened for following operations
    if (this._openPopup()) {
      return;
    }
    switch (event.which) {
      case scout.keys.PAGE_UP:
      case scout.keys.PAGE_DOWN:
      case scout.keys.UP:
      case scout.keys.DOWN:
        if (this.proposalChooser) {
          this.proposalChooser.delegateEvent(event);
        }
        break;
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
  // Escape
  if (e.which === scout.keys.ESC) {
    this._closePopup();
    e.stopPropagation();
    return;
  }

  // Enter
  if (e.which === scout.keys.ENTER) {
    this._acceptProposal();
    e.stopPropagation();
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

scout.AbstractSmartField.prototype._filterOptions = function() {
  var val = this._searchText();
  if (this._oldVal === val) {
    $.log.debug('value of field has not changed - do not filter (oldVal=' + this._oldVal + ')');
    return;
  }
  this._selectedOption = -1;
  this.session.send(this.id, 'proposalTyped', {
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
  // omit super call
  $.log.debug('AbstractSmartField#_onFieldBlur');
  this._closePopup();
};

/**
 * This method is called when the user presses the TAB key in the UI.
 * onFieldBlur is also executed, but won't do anything, since the $popup is already closed in the UI.
 */
scout.AbstractSmartField.prototype._acceptProposal = function() {
  $.log.debug('AbstractSmartField#_acceptProposal');
  this.session.send(this.id, 'acceptProposal', {searchText: this._searchText()});
  this._closePopup(false);
};

scout.AbstractSmartField.prototype._closePopup = function(notifyServer) {
  if (this._$popup) {
    notifyServer = notifyServer === undefined ? true : notifyServer;
    if (notifyServer) {
      this.session.send(this.id, 'cancelProposal');
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
  return this.$field.val();
};

// FIXME AWE: (smart-field) an dieser stelle müssten wir auch die screen-boundaries berücksichtigen
// und entscheiden, ob das popup gegen unten oder gegen oben geöffnet werden soll.
/**
 * This method opens a popup before we contact the server to load proposals. This means
 * at this point we cannot know what size the popup should have. We have to set a fixed
 * size and resize the popup later when proposals are available.
 */
scout.AbstractSmartField.prototype._openPopup = function() {
  if (this._$popup) {
    return false;
  } else {
    this.session.send(this.id, 'openProposal', {
      searchText: this._searchText(),
      selectCurrentValue: false});

    this._$popup = $.makeDiv('smart-field-popup')
      .on('mousedown', this._onPopupMousedown.bind(this))
      .appendTo($('body'));

    var htmlPopup = new scout.HtmlComponent(this._$popup, this.session),
      popupLayout = new scout.PopupLayout(htmlPopup),
      fieldBounds = this._getInputBounds();

    popupLayout.autoSize = false;
    popupLayout.adjustAutoSize = function(prefSize) {
      return new scout.Dimension(
          Math.max(fieldBounds.width, prefSize.width),
          Math.min(350, prefSize.height));
    };

    htmlPopup.validateRoot = true;
    htmlPopup.setLayout(popupLayout);
    htmlPopup.setBounds(new scout.Rectangle(
      fieldBounds.x,
      fieldBounds.y + fieldBounds.height,
      fieldBounds.width,
      scout.HtmlEnvironment.formRowHeight * 2));
    popupLayout.autoSize = true;

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
  $.log.debug('_resizePopup prefSize=' + prefSize);
  htmlPopup.setSize(prefSize);
};

/**
 * @override
 */
scout.AbstractSmartField.prototype.acceptDisplayText = function() {
  this._acceptProposal();
};
