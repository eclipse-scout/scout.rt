scout.SmartField = function() {
  scout.SmartField.parent.call(this);

  this.BROWSE_ALL = '';
  this.DEBOUNCE_DELAY = 200;

  this._$optionsDiv;
  this.options;
  this._oldSearchText;
  this._browseOnce;
  this._addAdapterProperties(['proposalChooser']);
  this._popup;
};
scout.inherits(scout.SmartField, scout.ValueField);

scout.SmartField.prototype._init = function(model, session) {
  scout.SmartField.parent.prototype._init.call(this, model, session);
};

/**
 * @override
 */
scout.SmartField.prototype._createKeyStrokeAdapter = function() {
  // FIXME AWE/NBU: (smart-field) andere key-strokes implementieren
  // keyup(controller) in KeystrokeManager#installAdapter ergänzen
  // Idee: nur registrieren, wenn keystroke ein handleKeyUp hat, event object ergänzen
  // oder zusätzlichen parameter um zw. keyup / keydown zu unterscheiden?
  return new scout.SmartFieldKeyStrokeAdapter(this);
};

scout.SmartField.prototype._render = function($parent) {
  var cssClass = this.proposal ? 'proposal-field' : 'smart-field';
  this.addContainer($parent, cssClass, new scout.SmartFieldLayout(this));
  this.addLabel();
  this.addField(scout.fields.new$TextField()
    .blur(this._onFieldBlur.bind(this))
    .click(this._onClick.bind(this))
    .focus(this._onFocus.bind(this))
    .keyup(this._onKeyUp.bind(this))
    .keydown(this._onKeyDown.bind(this)));

  this.addMandatoryIndicator();
  this.addIcon();
  this.addStatus();
  this.addSmartFieldPopup();

  if (this.cellEditor) {
    this._onClick();
  }
};

scout.SmartField.prototype.addSmartFieldPopup = function() {
  this._popup = new scout.SmartFieldPopup(this.session, {
    $anchor: this.$field,
    smartField: this
  });
  this.addChild(this._popup);
};

scout.SmartField.prototype._renderProperties = function() {
  scout.SmartField.parent.prototype._renderProperties.call(this);
  this._renderProposalChooser();
};

/**
 * When popup is not rendered at this point, we render the popup.
 */
scout.SmartField.prototype._renderProposalChooser = function() {
  $.log.debug('_renderProposalChooser proposalChooser=' + this.proposalChooser);
  if (this.proposalChooser) {
    this._requestedProposal = false;
    this._renderPopup();
    this.proposalChooser.render(this._popup.$container);
    if (this.rendered) {
      // a.) render after a click (property change), form is completely laid out
      this._popup.resize();
    } else {
      // b.) render when HTML page is loaded, layout of form is not done yet
      //     we must acquire focus, because popup is only closed when field
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
scout.SmartField.prototype._removeProposalChooser = function() {
  $.log.debug('_removeProposalChooser proposalChooser=' + this.proposalChooser);
  this._closeProposal(false);
};

scout.SmartField.prototype._isNavigationKey = function(e) {
  return e.which === scout.keys.PAGE_UP ||
    e.which === scout.keys.PAGE_DOWN ||
    e.which === scout.keys.UP ||
    e.which === scout.keys.DOWN;
};

scout.SmartField.prototype._isFunctionKey = function(e) {
  return e.which >= scout.keys.F1 && e.which < scout.keys.F12;
};

scout.SmartField.prototype._onClick = function(e) {
  if (!this._popup.rendered) {
    this._openProposal(this.BROWSE_ALL, true);
  }
};

scout.SmartField.prototype._onIconClick = function(event) {
  scout.SmartField.parent.prototype._onIconClick.call(this, event);
  if (!this._popup.rendered) {
    this._openProposal(this.BROWSE_ALL, true);
  }
};

// navigate in options
scout.SmartField.prototype._onKeyDown = function(e) {
  if (e.which === scout.keys.ESCAPE) {
    if (this._popup.rendered) {
      e.stopPropagation();
    }
    this._closeProposal();
    return;
  }

  if (e.which === scout.keys.ENTER) {
    if (this._popup.rendered) {
      e.stopPropagation();
    }
    this._acceptProposal();
    return;
  }

  if (this._isNavigationKey(e)) {
    if (this.proposalChooser) {
      this.proposalChooser.delegateEvent(e);
    } else {
      this._openProposal(this.BROWSE_ALL, true);
    }
  }
};

scout.SmartField.prototype._onFocus = function(e) {
  this._browseOnce = true;
  this._oldSearchText = this._searchText();
};

scout.SmartField.prototype._onKeyUp = function(e) {
  // Escape
  if (e.which === scout.keys.ESCAPE) {
    e.stopPropagation();
    return;
  }

  // Enter
  if (e.which === scout.keys.ENTER) {
    e.stopPropagation();
    return;
  }

  // Pop-ups shouldn't open when one of the following keys is pressed
  if (e.which === scout.keys.TAB ||
    e.which === scout.keys.SHIFT ||
    e.which === scout.keys.HOME ||
    e.which === scout.keys.END ||
    e.which === scout.keys.LEFT ||
    e.which === scout.keys.RIGHT ||
    this._isNavigationKey(e) ||
    this._isFunctionKey(e)) {
    return;
  }

  // ensure pop-up is opened for following operations
  if (this._popup.rendered) {
    this._proposalTyped();
  } else if (this._browseOnce) {
    this._browseOnce = false;
    this._openProposal(this._searchText(), false);
  }
};

scout.SmartField.prototype._proposalTyped = function() {
  var searchText = this._searchText();
  if (this._oldSearchText === searchText) {
    $.log.debug('(SmartField#_proposalTyped) value of field has not changed - do not send proposalTyped (oldSearchText=' + this._oldSearchText + ')');
    return;
  }

  // debounce send
  clearTimeout(this._sendTimeoutId);
  this._sendTimeoutId = setTimeout(function() {
    $.log.debug('(SmartField#_proposalTyped) send searchText=' + searchText);
    this.session.send(this.id, 'proposalTyped', {
      searchText: searchText
    });
  }.bind(this), this.DEBOUNCE_DELAY);

  this._oldSearchText = searchText;
  $.log.debug('(SmartField#_proposalTyped) updated oldSearchText=' + this._oldSearchText);
};

/**
 * Returns the bounds of the text-input element. Subclasses may override this method when their
 * text-field is not === this.$field.
 */
scout.SmartField.prototype._fieldBounds = function() {
  return scout.graphics.offsetBounds(this.$field);
};

scout.SmartField.prototype._onFieldBlur = function() {
  // omit super call
  $.log.debug('(SmartField#_onFieldBlur)');
  this._browseOnce = false;
  this._requestedProposal = false;
  this._acceptProposal();
};

/**
 * This method is called when the user presses the TAB or ENTER key in the UI.
 * onFieldBlur is also executed, but won't do anything, since the popup is already closed in the UI.
 * In case the smart-field is a proposal-field we must send the current searchText to the server,
 * even when the popup is not opened (this happens when the user types something which is not in the
 * list of proposals). We must accept the user defined text in that case.
 */
scout.SmartField.prototype._acceptProposal = function() {
  // must clear pending "proposalTyped" events because nothing good happens
  // when proposalTyped arrives _after_ an "acceptProposal" event.
  clearTimeout(this._sendTimeoutId);
  this._sendTimeoutId = null;
  var searchText = this._searchText();
  this.displayText = searchText;
  $.log.debug('(SmartField#_acceptProposal) searchText=' + searchText);
  this.session.send(this.id, 'acceptProposal', {
    searchText: searchText
  });
  this._closeProposal(false);
};

scout.SmartField.prototype._closeProposal = function(notifyServer) {
  if (this._popup.rendered) {
    if (scout.helpers.nvl(notifyServer, true)) {
      this.session.send(this.id, 'cancelProposal');
    }
    this._popup.close();
  }
};

scout.SmartField.prototype._searchText = function() {
  return this.$field.val();
};

// FIXME AWE: (smart-field) anderer status-text wenn Suche nach "*" und keine Ergebnisse gefunden
// --> Keine Daten vorhanden

/**
 * This method opens a popup before we contact the server to load proposals. This means
 * at this point we cannot know what size the popup should have. We have to set a fixed
 * size and resize the popup later when proposals are available.
 */
scout.SmartField.prototype._openProposal = function(searchText, selectCurrentValue) {
  // A proposal-field (PF) has a slightly different behavior than a smart-field (SF):
  // When the typed proposal doesn't match a proposal from the list, the popup
  // is closed. The smart-field would stay open in that case. The SF also opens the
  // popup _before_ we send a request to the server (-> more responsive UI)
  $(document).on('mousedown', this._mouseDownListener);
  if (!this.proposal) {
    // FIXME AWE: das hier ausbauen und Lupe-Icon durch Loading-Icon austauschen während laden
    // this._renderPopup();
  }
  if (!this._requestedProposal) {
    this._requestedProposal = true;
    this.session.send(this.id, 'openProposal', {
      searchText: searchText,
      selectCurrentValue: selectCurrentValue
    });
  }
};

scout.SmartField.prototype._renderPopup = function() {
  if (this._popup.rendered) {
    return;
  }
  this._popup.render();
};

/**
 * @override
 */
scout.SmartField.prototype.displayTextChanged = function() {
  this._acceptProposal();
};
