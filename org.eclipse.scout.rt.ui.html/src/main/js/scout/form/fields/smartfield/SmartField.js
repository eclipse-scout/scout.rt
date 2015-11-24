/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
// FIXME AWE: (smart-field) anderer status-text wenn Suche nach "*" und keine Ergebnisse gefunden
// --> Keine Daten vorhanden

// FIXME AWE: (smart-field) Do not open popup when Ctrl or Alt key is pressed (e.g. Ctrl + 1)

// FIXME AWE: (smart-field) Lupe-Icon durch Loading-Icon austauschen während Laden von SmartField

/**
 * Three smart-field modes:
 *
 * [default] when no flag is set, used for Desktop applications
 *     smart-field opens a popup for proposal-chooser when user
 *     clicks into input-field
 * [touch] smart-field is only a DIV that shows the display-text
 *     when user clicks on the DIV it opens a popup that has an
 *     embedded smart-field and a proposal-chooser.
 * [embedded] used in the popup opened by the touch smart-field.
 *     this type of smart-field does not react on focus / blur
 *     events.
 */
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
 * @override Widget.js
 */
scout.SmartField.prototype._createKeyStrokeContext = function() {
  return new scout.InputFieldKeyStrokeContext();
};

/**
 * @override FormField.js
 */
scout.SmartField.prototype._init = function(model) {
  scout.SmartField.parent.prototype._init.call(this, model);
  scout.fields.initTouch(this, model);
  this._popup = model.popup;
};

scout.SmartField.prototype.addPopup = function() {
  if (!this._popup) {
    var popupType = this.touch ? scout.SmartFieldTouchPopup : scout.SmartFieldPopup;
    this._popup = scout.create(popupType, {
      parent: this,
      $anchor: this.$field,
      field: this
    });
  }
};

scout.SmartField.prototype._render = function($parent) {
  var cssClass = this.proposal ? 'proposal-field' : 'smart-field';
  this.addContainer($parent, cssClass, new scout.SmartFieldLayout(this));
  this.addLabel();

  var $field = scout.fields.makeInputOrDiv(this)
    .click(this._onClick.bind(this));
  if (!this.touch) {
    $field
      .blur(this._onFieldBlur.bind(this))
      .focus(this._onFocus.bind(this))
      .keyup(this._onKeyUp.bind(this))
      .keydown(this._onKeyDown.bind(this));
  }
  this.addField($field);

  if (!this.embedded) {
    this.addMandatoryIndicator();
  }
  this.addIcon();
  this.addStatus();
  this.addPopup();
};

scout.SmartField.prototype._remove = function() {
  scout.SmartField.parent.prototype._remove.call(this);
  if (this._popup) {
    this._popup = null;
  }
};

/**
 * Method invoked if being rendered within a cell-editor (mode='scout.FormField.MODE_CELLEDITOR'), and once the editor finished its rendering.
 */
scout.SmartField.prototype.onCellEditorRendered = function(options) {
  if (options.openFieldPopup) {
    this._onClick();
  }
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
 * @override ValueField.js
 */
scout.SmartField.prototype._renderDisplayText = function(displayText) {
  scout.fields.valOrText(this, this.$field, displayText);
};

/**
 * Sync method is required because when proposal-chooser has never been rendered and the search-string
 * does not return any proposals in a proposal field, neither _renderProposalChooser nor _removeProposalChooser
 * is called and thus the _requestedProposal flag would never be resetted.
 */
scout.SmartField.prototype._syncProposalChooser = function(proposalChooser) {
  $.log.debug('(SmartField#_syncProposalChooser) set _requestedProposal to false');
  this.proposalChooser = proposalChooser;
  this._requestedProposal = false;
};

/**
 * When popup is not rendered at this point, we render the popup.
 */
scout.SmartField.prototype._renderProposalChooser = function() {
  $.log.debug('(SmartField#_renderProposalChooser) proposalChooser=' + this.proposalChooser + ' touch=' + this.touch);
  if (!this.proposalChooser || this.touch) {
    return;
  }
  this._renderPopup();
  this._popup._renderProposalChooser(this.proposalChooser);
};

/**
 * This method is called after a valid option has been selected in the proposal chooser.
 */
scout.SmartField.prototype._removeProposalChooser = function() {
  $.log.trace('(SmartField#_removeProposalChooser) proposalChooser=' + this.proposalChooser);
  if (this.touch) {
    return;
  }
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

scout.SmartField.prototype._onClick = function(event) {
  if (scout.fields.handleOnClick(this)) {
    if (this.touch) {
      this._popup.open();
    } else {
      this._openProposal(true);
    }
  }
};

scout.SmartField.prototype._onIconClick = function(event) {
  if (scout.fields.handleOnClick(this)) {
    if (this.touch) {
      this._popup.open();
    } else {
      scout.SmartField.parent.prototype._onIconClick.call(this, event);
      this._openProposal(true);
    }
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
  this._oldSearchText = this._readSearchText();
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
    this._send('proposalTyped', {
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
  $.log.debug('(SmartField#_onFieldBlur) tabPrevented=' + this._tabPrevented);
  this._requestedProposal = false;

  if (this.embedded) {
    return;
  }

  // When we have prevented TAB handling in keyDown handler, we have already sent an acceptProposal event.
  // At this time the proposalChooser was open, and thus the proposalChooserOpen flag was set to true
  // which means the Java-client uses the selected row from the proposalChooser and _not_ the display text.
  // After the request, when we set the focus with _focusNextTabbable(), we must _not_ send acceptProposal
  // again. It is not only unnecessary but would even cause errors, because at this time the proposalChooser
  // is closed and for the Java-client the request would look like, it should perform a lookup after the
  // user has typed something into the SmartField.
  if (this._tabPrevented) {
    this._tabPrevented = false;
  } else {
    this._acceptProposal(true);
  }
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

  forceClose = scout.nvl(forceClose, false);
  var proposalChooserOpen = !!this.proposalChooser,
    searchText = this._readSearchText();

  $.log.debug('(SmartField#_acceptProposal) searchText=' + searchText + ' proposalChooserOpen=' + proposalChooserOpen + ' forceClose=' + forceClose);
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
    $.log.debug('(SmartField) request done proposalChooser=' + this.proposalChooser);
    if (this._tabPrevented && !this.proposalChooser) {
      this._focusNextTabbable();
    }
  }.bind(this));
};

/**
 * Override this method to return the search-text for this smart-field.
 * The implementation is different for single- and multi-line smart-fields.
 */
scout.SmartField.prototype._readSearchText = function() {
  return this._readDisplayText();
};

scout.SmartField.prototype._sendAcceptProposal = function(searchText, chooser, forceClose) {
  this.displayText = searchText;
  this._oldSearchText = searchText;
  this._send('acceptProposal', {
    searchText: searchText,
    chooser: chooser,
    forceClose: forceClose
  });
};

// FIXME AWE/DWI: check if we can find next tabbable in the current focus-context (FocusManager)
scout.SmartField.prototype._focusNextTabbable = function() {
  var $tabElements = this.entryPoint().find(':tabbable');
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
    if (scout.nvl(notifyServer, true)) {
      this._sendCancelProposal();
    }
    this._popup.close();
  }
};

scout.SmartField.prototype._sendCancelProposal = function() {
  this._send('cancelProposal');
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
    this._send('openProposal', {
      searchText: searchText,
      selectCurrentValue: selectCurrentValue
    });
  }
};

scout.SmartField.prototype._renderPopup = function() {
  if (this._popup.rendered) {
    return;
  }
  this._popup.open();
};

/**
 * @override ValueField.js
 */
scout.SmartField.prototype.acceptInput = function(whileTyping) {
  if (this.mode !== scout.FormField.MODE_CELLEDITOR && !this.embedded) {
    this._acceptProposal(true);
  }
};

/**
 * @override ValueField.js
 */
scout.SmartField.prototype.aboutToBlurByMouseDown = function(target) {
  var eventOnField = this.$field.isOrHas(target);
  var eventOnPopup = this._popup.rendered && this._popup.$container.isOrHas(target);

  if (!eventOnField && !eventOnPopup) {
    this.acceptInput(); // event outside this field.
  }
};
