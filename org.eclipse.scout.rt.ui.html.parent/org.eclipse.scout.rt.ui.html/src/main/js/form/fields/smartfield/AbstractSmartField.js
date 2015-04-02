scout.AbstractSmartField = function() {
  scout.AbstractSmartField.parent.call(this);
  this._$popup;
  this._$optionsDiv;
  this.options;
  this._selectedOption = -1;
  this._oldSearchText;
  this._addAdapterProperties(['proposalChooser']);
};
scout.inherits(scout.AbstractSmartField, scout.ValueField);

scout.AbstractSmartField.prototype.init = function(model, session) {
  scout.AbstractSmartField.parent.prototype.init.call(this, model, session);
};

/**
 * @override
 */
scout.AbstractSmartField.prototype._createKeyStrokeAdapter = function() {
  // FIXME AWE: (smart-field) andere keystrokes implementieren
  return new scout.SmartFieldKeyStrokeAdapter(this);
};

scout.AbstractSmartField.prototype._render = function($parent) {
  var cssClass = this.proposal ? 'proposal-field' : 'smart-field';
  this.addContainer($parent, cssClass, new scout.SmartFieldLayout(this));
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

/**
 * When popup is not rendered at this point, we render the popup.
 */
scout.AbstractSmartField.prototype._renderProposalChooser = function() {
  $.log.debug('_renderProposalChooser proposalChooser=' + this.proposalChooser);
  if (this.proposalChooser) {
    this._openPopup(false, true);
    this.proposalChooser.render(this._$popup);
    if (this.rendered) {
      // a.) render after a click (property change), form is completely laid out
      this._resizePopup();
    } else {
      // b.) render when HTML page is loaded, layout of form is not done yet
      //     we must aquire the foucs, because popup is only closed when field
      //     loses focus.
      if (document.activeElement !== this.$field) {
        this.$field.focus();
      }
    }
  }
};

/**
 * This method is called after a valid option has been selected in the proposal chooser.
 */
scout.AbstractSmartField.prototype._removeProposalChooser = function() {
  $.log.debug('_removeProposalChooser proposalChooser=' + this.proposalChooser);
  this._closePopup(false);
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
    if (this.proposalChooser) {
      this.proposalChooser.delegateEvent(event);
    }
  }
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

  // update text in field
  this._proposalTyped();
};

scout.AbstractSmartField.prototype._proposalTyped = function() {
  var searchText = this._searchText();
  if (this._oldSearchText === searchText) {
    $.log.debug('value of field has not changed - do not filter (oldSearchText=' + this._oldSearchText + ')');
    return;
  }
  this._selectedOption = -1;
  this.session.send(this.id, 'proposalTyped', {
    searchText: searchText,
    selectCurrentValue: false});
  this._oldSearchText = searchText;
  $.log.debug('updated oldSearchText=' + this._oldSearchText);
};

scout.AbstractSmartField.prototype._onPopupMousedown = function(event) {
  // Make sure field blur won't be triggered -> pop-up must not be closed on mouse down
  event.preventDefault();
};

/**
 * Returns the bounds of the text-input element. Subclasses may override this method when their
 * text-field is not === this.$field.
 */
scout.AbstractSmartField.prototype._fieldBounds = function() {
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
scout.AbstractSmartField.prototype._openPopup = function(notifyServer, mustRender) {
  if (this._$popup) {
    return false;
  } else {
    notifyServer = notifyServer === undefined ? true : notifyServer;
    if (notifyServer) {
      this.session.send(this.id, 'openProposal', {
        searchText: this._searchText(),
        selectCurrentValue: false});
    }

    // A proposal-field (PF) has a slightly different behavior than a smart-field (SF):
    // When the typed proposal doesn't match a proposal from the list, the popup
    // is closed. The smart-field would stay open in that case. The SF also opens the
    // popup _before_ we send a request to the server (-> more responsive UI)
    if (this.proposal && !mustRender) {
      return false;
    }

    this._$popup = $.makeDiv('smart-field-popup')
      .on('mousedown', this._onPopupMousedown.bind(this))
      .appendTo($('body'));

    var htmlPopup = new scout.HtmlComponent(this._$popup, this.session),
      popupLayout = new scout.PopupLayout(htmlPopup);
    htmlPopup.validateRoot = true;
    popupLayout.autoSize = true;
    popupLayout.adjustAutoSize = function(prefSize) {
      // must re-evaluate _fieldBounds() for each call, since smart-field is not laid out at this point.
      return this._popupSize(this._fieldBounds(), prefSize);
    }.bind(this);
    htmlPopup.setLayout(popupLayout);
    return true;
  }
};

scout.AbstractSmartField.prototype._popupSize = function(fieldBounds, prefSize) {
  return new scout.Dimension(
    Math.max(fieldBounds.width, prefSize.width),
    Math.min(350, prefSize.height));
};

scout.AbstractSmartField.prototype._popupBounds = function(fieldBounds, prefSize) {
  var popupSize = this._popupSize(fieldBounds, prefSize);
  return new scout.Rectangle(
      fieldBounds.x,
      fieldBounds.y + fieldBounds.height,
      popupSize.width,
      popupSize.height);
};

scout.AbstractSmartField.prototype._resizePopup = function() {
  var htmlPopup = scout.HtmlComponent.get(this._$popup),
    popupLayout = htmlPopup.layoutManager,
    prefSize = htmlPopup.getPreferredSize(),
    bounds = this._popupBounds(this._fieldBounds(), prefSize);
  $.log.debug('_resizePopup bounds=' + bounds + ' prefSize=' + prefSize);
  htmlPopup.setBounds(bounds);
};

/**
 * @override
 */
scout.AbstractSmartField.prototype.acceptDisplayText = function() {
  this._acceptProposal();
};
