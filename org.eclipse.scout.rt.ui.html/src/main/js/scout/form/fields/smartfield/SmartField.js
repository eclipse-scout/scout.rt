// FIXME AWE: (smart-field) anderer status-text wenn Suche nach "*" und keine Ergebnisse gefunden
// --> Keine Daten vorhanden

// FIXME AWE: (smart-field) Lupe-Icon durch Loading-Icon austauschen während Laden von SmartField
scout.SmartField = function() {
  scout.SmartField.parent.call(this);

  this.DEBOUNCE_DELAY = 200;

  this._addAdapterProperties(['proposalChooser']);
  this.options;
  /**
   * This property is used to prevent unnecessary requests to the server.
   */
  this._oldSearchText;
  this._popup;
  this._requestedProposal = false;
  this._tabPrevented = false;
};
scout.inherits(scout.SmartField, scout.ValueField);

/**
 * @override
 */
scout.SmartField.prototype._createKeyStrokeAdapter = function() {
  // FIXME AWE/NBU: (smart-field) andere key-strokes implementieren
  // keyup(controller) in KeyStrokeUtil#installAdapter ergänzen
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

  if (this.cellEditor && this.cellEditor.openFieldPopupOnCellEdit) {
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


scout.SmartField.prototype._syncDisplayText = function(displayText) {
  this._oldSearchText = displayText;
  this.displayText = displayText;
};

/**
 * When popup is not rendered at this point, we render the popup.
 */
scout.SmartField.prototype._renderProposalChooser = function() {
  $.log.debug('(SmartField#_renderProposalChooser) proposalChooser=' + this.proposalChooser);
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
  $.log.trace('(SmartField#_removeProposalChooser) proposalChooser=' + this.proposalChooser);
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
    this._openProposal(true);
  }
};

scout.SmartField.prototype._onIconClick = function(event) {
  scout.SmartField.parent.prototype._onIconClick.call(this, event);
  if (!this._popup.rendered) {
    this._openProposal(true);
  }
};

scout.SmartField.prototype._isPreventDefaultTabHandling = function() {
  var doPrevent = !!this.proposalChooser;
  $.log.trace('(SmartField#_isPreventDefaultTabHandling) must prevent default when TAB was pressed = ' + doPrevent);
  return doPrevent;
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

  // We must prevent default focus handling
  if (e.which === scout.keys.TAB) {
    if (this._isPreventDefaultTabHandling()) {
      e.preventDefault();
      this._tabPrevented = true;
      this._acceptProposal();
      return;
    }
  }

  // We must not deal with TAB key here, because
  // this is already handled by _onFieldBlur()
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
      // Since this is the keyDown handler we cannot access the typed text here
      // But when the user presses the down arrow, we can open the proposal
      // chooser immediately. Also we can start a search with text that was already
      // in the text field.
      this._openProposal(true);
    }
  }
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

  // The typed character is not available until the keyUp event happens
  // That's why we must deal with that event here (and not in keyDown)
  // We don't use _searchText() here because we always want the text the
  // user has typed.
  if (this._popup.rendered) {
    this._proposalTyped();
  } else {
    this._openProposal(false);
  }
};


scout.SmartField.prototype._onFocus = function(e) {
  this._oldSearchText = this._readDisplayText();
};

scout.SmartField.prototype._proposalTyped = function() {
  var searchText = this._readDisplayText();
  $.log.trace('(SmartField#_proposalTyped) searchText=' + searchText + ' displayText=' + this.displayText);
  if (searchText === this.displayText) {
    return;
  }
  this.displayText = searchText;

  // debounce send
  clearTimeout(this._sendTimeoutId);
  this._sendTimeoutId = setTimeout(function() {
    $.log.debug('(SmartField#_proposalTyped) send searchText=' + searchText);
    this.remoteHandler(this.id, 'proposalTyped', {
      searchText: searchText
    });
  }.bind(this), this.DEBOUNCE_DELAY);
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
  this._requestedProposal = false;
  this._acceptProposal(true);
};

/**
 * This method is called when the user presses the TAB or ENTER key in the UI, or when _onFieldBlur()
 * or acceptInput(). In case the field is a proposal-field we must send the current searchText
 * to the server, even when the popup is not opened (this happens when the user types something which
 * is not in the list of proposals). We must accept the user defined text in that case.
 */
scout.SmartField.prototype._acceptProposal = function(forceClose) {
  // must clear pending "proposalTyped" events because nothing good happens
  // when proposalTyped arrives _after_ an "acceptProposal" event.
  clearTimeout(this._sendTimeoutId);
  this._sendTimeoutId = null;

  forceClose = scout.helpers.nvl(forceClose, false);
  var proposalChooserOpen = !!this.proposalChooser,
    searchText = this._readDisplayText();

  $.log.debug('(SmartField#_acceptProposal) searchText=' + searchText + ' proposalChooserOpen=' + proposalChooserOpen);
  if (proposalChooserOpen) {
    // Always send accept proposal, when proposal chooser is opened
    // Because user wants to choose the selected proposal from the
    // proposal chooser by pressing TAB or ENTER.
    // The Java client will use the selected row as value when it
    // receives the acceptProposal event in that case.
    this._sendAcceptProposal(searchText, true, forceClose);
  } else {
    // When proposal chooser is closed, only send accept proposal
    // when search text has changed. Prevents unnecessary requests
    // to the server when the user tabs over the smartfield.
    if (searchText === this._oldSearchText) {
      return;
    }
    this._sendAcceptProposal(searchText, false, forceClose);
  }

  this.session.listen().done(function() {
    $.log.debug('SmartField request done proposalChooser=' + this.proposalChooser);
    if (this._tabPrevented) {
      this._tabPrevented = false;
      if (!this.proposalChooser) {
        this._focusNextTabbable();
      }
    }
  }.bind(this));
};


scout.SmartField.prototype._sendAcceptProposal = function(searchText, proposalChooserOpen, forceClose) {
  this.displayText = searchText;
  this._oldSearchText = searchText;
  this.remoteHandler(this.id, 'acceptProposal', {
    searchText: searchText,
    chooser: proposalChooserOpen,
    forceClose: forceClose
  });
};

// FIXME AWE/DWI: check if we can find next tabbable in the current focus-context (FocusManager)
scout.SmartField.prototype._focusNextTabbable = function() {
  var $tabElements = $(':tabbable');
  var nextIndex = 0;
  var fieldIndex = $tabElements.index(this.$field);
  if (fieldIndex + 1 < $tabElements.length) {
    nextIndex = fieldIndex + 1;
  }
  $.log.debug('SmartField tab-index=' + fieldIndex + ' next tab-index=' + nextIndex);
  $tabElements.eq(nextIndex).focus();
};

scout.SmartField.prototype._closeProposal = function(notifyServer) {
  if (this._popup.rendered) {
    if (scout.helpers.nvl(notifyServer, true)) {
      this.remoteHandler(this.id, 'cancelProposal');
    }
    this._popup.close();
  }
};

/**
 * This method opens a popup before we contact the server to load proposals. This means
 * at this point we cannot know what size the popup should have. We have to set a fixed
 * size and resize the popup later when proposals are available.
 *
 * When the smartfield is valid, we want to perform a "browse all" search (=empty string),
 * when the field is invalid, we want to perform a search with the current display-text.
 *
 * Other as in _proposalTyped we always open the proposal, even when the display text
 * has not changed.
 */
scout.SmartField.prototype._openProposal = function(browseAll) {
  var displayText = this._readDisplayText(),
    searchText = (browseAll && !this.errorStatus) ? '' : this._readDisplayText(),
    selectCurrentValue = browseAll;
  this.displayText = displayText;

  if (this._requestedProposal) {
    $.log.trace('(SmartField#_openProposal) already requested proposal -> do nothing');
  } else {
    this._requestedProposal = true;
    $.log.debug('(SmartField#_openProposal) send openProposal. searchText=' + searchText + ' selectCurrentValue=' + selectCurrentValue);
    this.remoteHandler(this.id, 'openProposal', {
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
scout.SmartField.prototype.acceptInput = function() {
  if (this.cellEditor) {
    return;
  }
  this._acceptProposal(true);
};
