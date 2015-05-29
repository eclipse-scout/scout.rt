scout.AbstractSmartField = function() {
  scout.AbstractSmartField.parent.call(this);

  this.BROWSE_ALL = '';
  this.DEBOUNCE_DELAY = 200;

  this._$popup;
  this._$optionsDiv;
  this.options;
  this._oldSearchText;
  this._browseOnce;
  this._addAdapterProperties(['proposalChooser']);
  this._mouseDownListener=this._onMouseDown.bind(this);
  /**
   * This flag is required in 'proposal' mode because in that case,
   * we do not open the $popup immediately. With the flag we can
   * avoid multiple openProposal request.
   */
  this._proposalRequested;
};
scout.inherits(scout.AbstractSmartField, scout.ValueField);

scout.AbstractSmartField.prototype.init = function(model, session) {
  scout.AbstractSmartField.parent.prototype.init.call(this, model, session);
};

/**
 * @override
 */
scout.AbstractSmartField.prototype._createKeyStrokeAdapter = function() {
  // FIXME AWE: (smart-field) andere key-strokes implementieren
  // keyup(controller) in KeystrokeManager#installAdapter ergänzen
  // Idee: nur registrieren, wenn keystroke ein handleKeyUp hat, event object ergänzen
  // oder zusätzlichen parameter um zw. keyup / keydown zu unterscheiden?
  return new scout.SmartFieldKeyStrokeAdapter(this);
};

scout.AbstractSmartField.prototype._render = function($parent) {
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
};

scout.AbstractSmartField.prototype._onMouseDown=function(event){
  var $target = $(event.target);
  // close the popup only if the click happened outside of the popup
  if (this._$popup.has($target).length === 0 && this.$container.has($target).length === 0) {
    this._closeProposal(true);
  }
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
    this._requestedProposal = false;
    this._renderPopup();
    this.proposalChooser.render(this._$popup);
    if (this.rendered) {
      // a.) render after a click (property change), form is completely laid out
      this._resizePopup();
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

scout.AbstractSmartField.prototype._resizePopup = function() {
  var htmlPopup = scout.HtmlComponent.get(this._$popup),
    popupLayout = htmlPopup.layoutManager,
    prefSize = htmlPopup.getPreferredSize(),
    bounds = this._popupBounds(this._fieldBounds(), prefSize);
  $.log.debug('_resizePopup bounds=' + bounds + ' prefSize=' + prefSize);
  // Invalidate is required, when the $popup is already opened and the proposal chooser is rendered later
  // when the popup size is the same as before, the proposal chooser would not layout its children (like
  // the table) without invalidate.
  htmlPopup.invalidate();
  htmlPopup.setBounds(bounds);
};

/**
 * This method is called after a valid option has been selected in the proposal chooser.
 */
scout.AbstractSmartField.prototype._removeProposalChooser = function() {
  $.log.debug('_removeProposalChooser proposalChooser=' + this.proposalChooser);
  this._closeProposal(false);
};

scout.AbstractSmartField.prototype._isNavigationKey = function(e) {
  return e.which === scout.keys.PAGE_UP ||
    e.which === scout.keys.PAGE_DOWN ||
    e.which === scout.keys.UP ||
    e.which === scout.keys.DOWN;
};

scout.AbstractSmartField.prototype._isFunctionKey = function(e) {
  return e.which >= scout.keys.F1 && e.which<scout.keys.F12;
};

scout.AbstractSmartField.prototype._onClick = function(e) {
  if (!this._$popup) {
    this._openProposal(this.BROWSE_ALL, true);
  }
};

scout.AbstractSmartField.prototype._onIconClick = function(event) {
  scout.AbstractSmartField.parent.prototype._onIconClick.call(this, event);
  if (!this._$popup) {
    this._openProposal(this.BROWSE_ALL, true);
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
    if (this.proposalChooser) {
      this.proposalChooser.delegateEvent(event);
    } else {
      this._openProposal(this.BROWSE_ALL, true);
    }
  }
};

scout.AbstractSmartField.prototype._onFocus = function(e) {
  this._browseOnce = true;
  this._oldSearchText = this._searchText();
};

scout.AbstractSmartField.prototype._onKeyUp = function(e) {
  // Escape
  if (e.which === scout.keys.ESCAPE) {
    this._closeProposal();
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
    e.which === scout.keys.HOME ||
    e.which === scout.keys.END ||
    e.which === scout.keys.LEFT ||
    e.which === scout.keys.RIGHT ||
    this._isNavigationKey(e)) {
    return;
  }

  // ensure pop-up is opened for following operations
  if (this._$popup || this._proposalRequested) {
    this._proposalTyped();
  } else if (this._browseOnce && !this._isFunctionKey(e)) {
    this._browseOnce = false;
    this._openProposal(this._searchText(), false);
  }
};

scout.AbstractSmartField.prototype._proposalTyped = function() {
  var searchText = this._searchText();
  if (this._oldSearchText === searchText) {
    $.log.debug('value of field has not changed - do not send proposalTyped (oldSearchText=' + this._oldSearchText + ')');
    return;
  }

  // debounce send
  clearTimeout(this._sendTimeoutId);
  this._sendTimeoutId = setTimeout(function() {
    this.session.send(this.id, 'proposalTyped', {searchText: searchText});
  }.bind(this), this.DEBOUNCE_DELAY);

  this._oldSearchText = searchText;
  $.log.debug('updated oldSearchText=' + this._oldSearchText);
};

scout.AbstractSmartField.prototype._onPopupMousedown = function(event) {
  // Make sure field blur won't be triggered (using preventDefault).
  // Also makes sure event does not get propagated (and handled by another mouse down handler, e.g. the one from CellEditorPopup.js)
  return false;
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
  this._browseOnce = false;
  this._requestedProposal = false;
  this._closeProposal();
};

/**
 * This method is called when the user presses the TAB or ENTER key in the UI.
 * onFieldBlur is also executed, but won't do anything, since the $popup is already closed in the UI.
 * In case the smart-field is a proposal-field we must send the current searchText to the server,
 * even when the popup is not opened (this happens when the user types something which is not in the
 * list of proposals). We must accept the user defined text in that case.
 */
scout.AbstractSmartField.prototype._acceptProposal = function() {
  if (!this.proposal && !this._$popup) {
    $.log.debug('proposal popup is not opened. do not acceptProposal');
    return;
  }
  var searchText = this._searchText();
  $.log.debug('AbstractSmartField#_acceptProposal searchText=' + searchText);
  this.session.send(this.id, 'acceptProposal', {searchText: searchText});
  this._closeProposal(false);
};

scout.AbstractSmartField.prototype._closeProposal = function(notifyServer) {
  $(document).off('mousedown', this._mouseDownListener);
  if (this._$popup) {
    notifyServer = scout.objects.whenUndefined(notifyServer, true);
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
scout.AbstractSmartField.prototype._openProposal = function(searchText, selectCurrentValue) {
  // A proposal-field (PF) has a slightly different behavior than a smart-field (SF):
  // When the typed proposal doesn't match a proposal from the list, the popup
  // is closed. The smart-field would stay open in that case. The SF also opens the
  // popup _before_ we send a request to the server (-> more responsive UI)
  $(document).on('mousedown', this._mouseDownListener);
  if (!this.proposal) {
    this._renderPopup();
  }
  if (!this._requestedProposal) {
    this._requestedProposal = true;
    this.session.send(this.id, 'openProposal', {
      searchText: searchText,
      selectCurrentValue: selectCurrentValue});
  }
};

scout.AbstractSmartField.prototype._renderPopup = function() {
  if (!this._$popup) {
    this._$popup = $.makeDiv('smart-field-popup')
      .on('mousedown', this._onPopupMousedown.bind(this))
      .appendTo($('body'));

    var htmlPopup = new scout.HtmlComponent(this._$popup, this.session),
      popupLayout = new scout.PopupLayout(htmlPopup),
      fieldBounds = this._fieldBounds(),
      initialPopupSize = new scout.Dimension(0, scout.HtmlEnvironment.formRowHeight);
    htmlPopup.validateRoot = true;
    popupLayout.adjustAutoSize = function(prefSize) {
      // must re-evaluate _fieldBounds() for each call, since smart-field is not laid out at this point.
      return this._popupSize(this._fieldBounds(), prefSize);
    }.bind(this);
    htmlPopup.setLayout(popupLayout);
    popupLayout.autoSize = false;
    htmlPopup.setBounds(this._popupBounds(fieldBounds, initialPopupSize));
    popupLayout.autoSize = true;
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

/**
 * @override
 */
scout.AbstractSmartField.prototype.acceptDisplayText = function() {
  this._acceptProposal();
};
