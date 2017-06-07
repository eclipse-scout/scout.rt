/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.SmartField2 = function() {
  scout.SmartField2.parent.call(this);

  this.searching = false;
  this.popup = null;
  this.lookupCall = null;
  this.codeType = null;
  this._pendingLookup = null;
  this._pendingSetSearching = null;
  this._lookupInProgress = false;
  this._tabPrevented = null;
  this.lookupRow = null;
  this.browseHierarchy = false;
  this.browseMaxRowCount = scout.SmartField2.DEFAULT_BROWSE_MAX_COUNT;
  this.browseAutoExpandAll = true;
  this.browseLoadIncremental = true;
  this.activeFilterEnabled = false;
  this.activeFilter = null;
  this.activeFilterLabels = [];
  this.columnDescriptors = null;
  this.displayStyle = scout.SmartField2.DisplayStyle.DEFAULT;
  this._userWasTyping = false; // used to detect whether the last thing the user did was typing (a proposal) or something else, like selecting a proposal row
  this._acceptInputEnabled = true; // used to prevent multiple execution of blur/acceptInput
};
scout.inherits(scout.SmartField2, scout.ValueField);

scout.SmartField2.DisplayStyle = {
  DEFAULT: 'default',
  DROPDOWN: 'dropdown'
};

scout.SmartField2.ErrorCode = {
  NOT_UNIQUE: 1,
  NO_RESULTS: 2
};

scout.SmartField2.DEBOUNCE_DELAY = 200;

scout.SmartField2.DEFAULT_BROWSE_MAX_COUNT = 100;

/**
 * @see IContentAssistField#getActiveFilterLabels() - should have the same order.
 */
scout.SmartField2.ACTIVE_FILTER_VALUES = ['UNDEFINED', 'FALSE', 'TRUE'];

/**
 * @override
 */
scout.SmartField2.prototype._init = function(model) {
  scout.SmartField2.parent.prototype._init.call(this, model);

  this.activeFilterLables = [
    this.session.text('ui.All'),
    this.session.text('ui.Inactive'),
    this.session.text('ui.Active')];
};

/**
 * Initializes lookup call and code type before calling set value.
 * This cannot be done in _init because the value field would call _setValue first
 */
scout.SmartField2.prototype._initValue = function(value) {
  this._setLookupCall(this.lookupCall);
  this._setCodeType(this.codeType);
  scout.SmartField2.parent.prototype._initValue.call(this, value);
};

/**
 * @override Widget.js
 */
scout.SmartField2.prototype._createKeyStrokeContext = function() {
  return new scout.InputFieldKeyStrokeContext();
};

scout.SmartField2.prototype._initKeyStrokeContext = function() {
  scout.SmartField2.parent.prototype._initKeyStrokeContext.call(this);

  this.keyStrokeContext.registerKeyStroke(new scout.SmartField2CancelKeyStroke(this));
  this.keyStrokeContext.registerKeyStroke(new scout.SmartField2ToggleKeyStroke(this));
};

scout.SmartField2.prototype._render = function() {
  this.addContainer(this.$parent, this.cssClassName(), new scout.SmartFieldLayout(this));
  this.addLabel();

  var fieldFunc = this.isDropdown() ? scout.fields.makeInputDiv : scout.fields.makeInputOrDiv;
  var $field = fieldFunc(this)
    .on('mousedown', this._onFieldMousedown.bind(this));

  if (!this.touch) {
    $field
      .blur(this._onFieldBlur.bind(this))
      .keyup(this._onFieldKeyup.bind(this))
      .keydown(this._onFieldKeydown.bind(this));
  }
  this.addField($field);

  if (!this.embedded) {
    this.addMandatoryIndicator();
  }
  this.addIcon();
  this.addStatus();
};

scout.SmartField2.prototype.cssClassName = function() {
  var prefix = this.displayStyle;
  if (this.displayStyle === scout.SmartField2.DisplayStyle.DEFAULT) {
    prefix = 'smart';
  }
  return prefix + '-field';
};

scout.SmartField2.prototype._readDisplayText = function() {
  return scout.fields.valOrText(this, this.$field);
};

scout.SmartField2.prototype._renderDisplayText = function() {
  scout.fields.valOrText(this, this.$field, this.displayText);
};

/**
 * Accepts the selected lookup row and sets its id as value.
 * This function is called on blur, by a keystroke or programmatically at any time.
 */
scout.SmartField2.prototype.acceptInput = function() {
  if (!this._acceptInputEnabled) {
    return;
  }

  var
    searchText = this._readDisplayText(),
    selectedLookupRow = this.popup ? this.popup.getSelectedLookupRow() : null;

  // in case the user has typed something after he has selected a lookup row
  // --> ignore the selection.
  if (this._userWasTyping) {
    selectedLookupRow = null;
  }

  // abort pending lookups
  if (this._pendingLookup) {
    clearTimeout(this._pendingLookup);
  }

  // Do nothing when search text is equals to the text of the current lookup row
  if (this.lookupRow && this.lookupRow.text === searchText && !selectedLookupRow) {
    $.log.debug('(SmartField2#acceptInput) unchanged. Close popup');
    this._inputAccepted();
    return;
  }

  // 1.) when search text is empty and no lookup-row is selected, simply set the value to null
  if (scout.strings.empty(searchText) && !selectedLookupRow) {
    $.log.debug('(SmartField2#acceptInput) empty. Set lookup-row to null, close popup');
    this.setLookupRow(null);
    this._inputAccepted();
    return;
  }

  // 2.) proposal chooser is open -> use the selected row as value
  if (selectedLookupRow) {
    $.log.debug('(SmartField2#acceptInput) lookup-row selected. Set lookup-row, close popup lookupRow=', selectedLookupRow.toString());
    this.setLookupRow(selectedLookupRow);
    this._inputAccepted();
    return;
  }

  // 3.) proposal chooser is not open -> try to accept the current display text
  // this causes a lookup which may fail and open a new proposal chooser (property
  // change for 'result'). Or in case the text is empty, just set the value to null
  $.log.debug('(SmartField2#acceptInput) getByText() searchText=', searchText);
  this._executeLookup(this.lookupCall.getByText.bind(this.lookupCall, searchText))
    .done(this._acceptInputDone.bind(this));
};

scout.SmartField2.prototype._inputAccepted = function() {
  this._userWasTyping = false;
  this.closePopup();

  // focus next tabbable
  if (this._tabPrevented) {
    var $tabElements = this.entryPoint().find(':tabbable'),
      direction = this._tabPrevented.shiftKey ? -1 : 1,
      fieldIndex = $tabElements.index(this.$field),
      nextIndex = fieldIndex + direction;

    if (nextIndex < 0) {
      nextIndex = $tabElements.length - 1;
    } else if (nextIndex >= $tabElements.length) {
      nextIndex = 0;
    }
    $.log.debug('(SmartField2#_inputAccepted) tab-index=' + fieldIndex + ' next tab-index=' + nextIndex);
    this._acceptInputEnabled = false;
    $tabElements.eq(nextIndex).focus();
    this._acceptInputEnabled = true;
    this._tabPrevented = null;
  }
};

scout.SmartField2.prototype._acceptInputDone = function(result) {
  this._userWasTyping = false;
  this._extendResult(result);

  // when there's exactly one result, we accept that lookup row
  if (result.numLookupRows === 1) {
    var lookupRow = result.singleMatch;
    if (this._isLookupRowActive(lookupRow)) {
      this.setLookupRow(lookupRow);
      this._inputAccepted();
    } else {
      this.setErrorStatus(scout.Status.error({
        message: this.session.text('SmartFieldInactiveRow', result.searchText)
      }));
    }
    return;
  }

  this._acceptInputFail(result);
};

/**
 * Extends the properties 'singleMatch' and 'numLookupRows' on the given result object.
 * The implementation is different depending on the browseHierarchy property.
 */
scout.SmartField2.prototype._extendResult = function(result) {
  result.singleMatch = null;

  if (this.browseHierarchy) {
    // tree (hierarchical)
    var proposalChooser = scout.create('TreeProposalChooser2', {
      parent: this,
      smartField: this
    });
    proposalChooser.setLookupResult(result);
    var leafs = proposalChooser.findLeafs();
    result.numLookupRows = leafs.length;
    if (result.numLookupRows === 1) {
      result.singleMatch = leafs[0].lookupRow;
    }
  } else {
    // table
    result.numLookupRows = result.lookupRows.length;
    if (result.numLookupRows === 1) {
      result.singleMatch = result.lookupRows[0];
    }
  }
};

scout.SmartField2.prototype._acceptInputFail = function(result) {
  var searchText = result.searchText;

  // in any other case something went wrong
  if (result.numLookupRows === 0) {
    this.closePopup();
    this.setValue(null);
    this.setDisplayText(searchText);
    this.setErrorStatus(scout.Status.error({
      message: this.session.text('SmartFieldCannotComplete', searchText),
      code: scout.SmartField2.ErrorCode.NO_RESULTS
    }));
    return;
  }

  if (result.numLookupRows > 1) {
    this.setValue(null);
    this.setDisplayText(searchText);
    this.setErrorStatus(scout.Status.error({
      message: this.session.text('SmartFieldNotUnique', searchText),
      code: scout.SmartField2.ErrorCode.NOT_UNIQUE
    }));
    if (this.isPopupOpen()) {
      this.popup.setLookupResult(result);
    } else {
      this._lookupByTextOrAllDone(result);
    }
    if (this.isPopupOpen()) {
      this.popup.selectFirstLookupRow();
    }
    return;
  }
};

scout.SmartField2.prototype.lookupByRec = function(rec) {
  $.log.debug('(SmartField2#lookupByRec) rec=', rec);
  return this._executeLookup(this.lookupCall.getByRec.bind(this.lookupCall, rec))
    .then(function(result) {

      // Since this function is only used for hierarchical trees we
      // can simply set the appendResult flag always to true here
      result.appendResult = true;
      if (this.isPopupOpen()) {
        this.popup.setLookupResult(result);
      }
    }.bind(this));
};

/**
 * Validates the given lookup row is enabled and matches the current activeFilter settings.
 *
 * @returns {boolean}
 */
scout.SmartField2.prototype._isLookupRowActive = function(lookupRow) {
  if (!lookupRow.enabled) {
    return false;
  }
  if (!this.activeFilterEnabled) {
    return true;
  }
  if (this.activeFilter === 'TRUE') {
    return lookupRow.active;
  }
  if (this.activeFilter === 'FALSE') {
    return !lookupRow.active;
  }
  return true;
};

scout.SmartField2.prototype._renderEnabled = function() {
  scout.SmartField2.parent.prototype._renderEnabled.call(this);

  this.$field.setTabbable(this.enabledComputed);
};

scout.SmartField2.prototype._setLookupCall = function(lookupCall) {
  if (typeof lookupCall === 'string') {
    lookupCall = scout.create(lookupCall, {
      session: this.session
    });
  }
  this._setProperty('lookupCall', lookupCall);

  // FIXME [awe] 7.0 - SF2: im AbstractContentAssistField.java setCodeTypeClass hat es so eine Logik die schaut, ob das smart field
  // selber hierarchy setzt, falls nicht wird das field auf den wert von codeType.hierarchy gesetzt
};

scout.SmartField2.prototype._setCodeType = function(codeType) {
  this._setProperty('codeType', codeType);
  if (!codeType) {
    return;
  }
  var lookupCall = scout.create('CodeLookupCall', {
    session: this.session,
    codeType: codeType
  });
  this.setProperty('lookupCall', lookupCall);
};

scout.SmartField2.prototype._formatValue = function(value) {
  if (scout.objects.isNullOrUndefined(value)) {
    return '';
  }

  // we already have a lookup row - Note: in Scout Classic (remote case)
  // we always end here and don't need to perform a getByKey lookup.
  if (this.lookupRow) {
    return this.lookupRow.text;
  }

  // we must do a lookup first to get the display text
  return this._executeLookup(this.lookupCall.getByKey.bind(this.lookupCall, value))
    .then(function(lookupRow) {
      return lookupRow ? lookupRow.text : '';
    });
};

/**
 * @param {boolean} [browse] whether or not the lookup call should execute getAll() or getByText() with the current display text
 */
scout.SmartField2.prototype.openPopup = function(browse) {
  $.log.info('SmartField2#openPopup browse=' + browse + ' popup=' + this.popup);
  // Reset scheduled focus next tabbable when user clicks on the smartfield
  // while a lookup is resolved.
  this._tabPrevented = null;

  // already open // FIXME [awe] 7.0 - SF2: check when this if applies
  if (this.isPopupOpen()) {
    return;
  }

  // In case the field is invalid, we always want to start a lookup with the current display text
  // unless the error was 'no results' because in that case it would be pointless to search for that text
  if (this.errorStatus && !this._hasUiError(scout.SmartField2.ErrorCode.NO_RESULTS)) {
    browse = false;
  }

  var promise;
  if (browse) {
    promise = this._executeLookup(this.lookupCall.getAll.bind(this.lookupCall));
    $.log.debug('(SmartField2#openPopup) getAll()');
  } else {
    var searchText = this._readDisplayText();
    promise = this._executeLookup(this.lookupCall.getByText.bind(this.lookupCall, searchText));
    $.log.debug('(SmartField2#openPopup) getByText() searchText=', searchText);
  }
  promise.done(function(result) {
    result.browse = browse;
    this._lookupByTextOrAllDone(result);
  }.bind(this));
};

scout.SmartField2.prototype._hasUiError = function(codes) {
  if (!this.errorStatus) {
    return false;
  }

  if (codes) {
    codes = scout.arrays.ensure(codes);
  } else {
    codes = [scout.SmartField2.ErrorCode.NO_RESULTS, scout.SmartField2.ErrorCode.NOT_UNIQUE];
  }

  // collect codes from the status hierarchy
  var statusList = scout.Status.asFlatList(this.errorStatus);
  var foundCodes = statusList.reduce(function(list, status) {
    if (status.code && list.indexOf(status.code) === -1) {
      list.push(status.code);
    }
    return list;
  }, []);

  // if one of the requested codes exist in the list of found codes
  return codes.some(function(code) {
    return foundCodes.indexOf(code) > -1;
  });
};

scout.SmartField2.prototype._lookupByTextOrAllDone = function(result) {

  // In cases where the user has tabbed to the next field, while results for the previous
  // smartfield are still loading: don't show the proposal popup.
  if (!this.isFocused()) {
    this.closePopup();
    return;
  }

  // Remove error codes set from UI
  if (this._hasUiError()) {
    this.setErrorStatus(null);
  }

  var numLookupRows = result.lookupRows.length;
  if (numLookupRows === 0 &&!result.noData) {
    this.closePopup(); // FIXME [awe] 7.0 - SF2: also set displayText and value=null here?
    this.setErrorStatus(scout.Status.error({
      message: this.session.text('SmartFieldCannotComplete', result.searchText),
      code: scout.SmartField2.ErrorCode.NO_RESULTS
    }));
    return;
  }

  var popupStatus = null;
  if (numLookupRows > this.browseMaxRowCount) {
    popupStatus = scout.Status.info({
      message: this.session.text('SmartFieldMoreThanXRows', this.browseMaxRowCount)
    });
  }

  // We don't want to set an error status on the field for the 'no data' case
  // Only show the message as status in the proposal chooser popup
  if (result.noData) {
    popupStatus = scout.Status.info({
      message: this.session.text('SmartFieldNoDataFound')
    });
  }

  // Render popup, if not yet rendered and set results
  if (this.popup) {
    this.popup.setLookupResult(result);
    this.popup.setStatus(popupStatus);
  } else {
    this._renderPopup(result, popupStatus);
  }
};

scout.SmartField2.prototype._renderPopup = function(result, status) {
  // On touch devices the field does not get the focus.
  // But it should look focused when the popup is open.
  this.$field.addClass('focused');
  this.$container.addClass('popup-open');

  this.popup = scout.create('SmartField2Popup', {
    parent: this,
    $anchor: this.$field,
    boundToAnchor: true,
    closeOnAnchorMousedown: false,
    field: this,
    lookupResult: result,
    status: status
  });

  this.popup.open();
  this.popup.on('lookupRowSelected', this._onLookupRowSelected.bind(this));
  this.popup.on('activeFilterSelected', this._onActiveFilterSelected.bind(this));
  this.popup.on('remove', function() {
    this.popup = null;
    if (this.rendered) {
      this.$container.removeClass('popup-open');
      this.$field.removeClass('focused');
    }
  }.bind(this));
};

scout.SmartField2.prototype.isFocused = function() {
  return this.rendered && scout.focusUtils.isActiveElement(this.$field);
};

scout.SmartField2.prototype.closePopup = function() {
  if (this.popup) {
    this.popup.close();
  }
};

/**
 * Calls acceptInput if mouse down happens outside of the field or popup
 * @override
 */
scout.SmartField2.prototype.aboutToBlurByMouseDown = function(target) {
  var eventOnField = this.$field.isOrHas(target);
  var eventOnPopup = this.popup && this.popup.$container.isOrHas(target);
  if (!eventOnField && !eventOnPopup) {
    this.acceptInput(); // event outside this value field
  }
};

scout.SmartField2.prototype._onFieldMousedown = function(event) {
  $.log.debug('(SmartField2#_onFieldMousedown)');
  if (!this.enabledComputed) {
    return;
  }
  this.togglePopup();
};

scout.SmartField2.prototype._onIconMousedown = function(event) {
  $.log.debug('(SmartField2#_onIconMousedown)');
  if (!this.enabledComputed) {
    return;
  }
  this.$field.focus();
  this.togglePopup();
};

scout.SmartField2.prototype.togglePopup = function() {
  $.log.info('(SmartField2#togglePopup) popupOpen=', this.isPopupOpen());
  if (this.isPopupOpen()) {
    this.closePopup();
  } else {
    this.openPopup(true);
  }
};

scout.SmartField2.prototype._onFieldBlur = function(event) {
  scout.SmartField2.parent.prototype._onFieldBlur.call(this, event);
  this.setSearching(false);
};

scout.SmartField2.prototype._onFieldKeyup = function(event) {
  // Escape
  if (event.which === scout.keys.ESCAPE) {
    event.stopPropagation();
    return;
  }

  // Enter
  if (event.which === scout.keys.ENTER) {
    event.stopPropagation();
    return;
  }

  // Pop-ups shouldn't open when one of the following keys is pressed
  var w = event.which;
  var pasteShortcut = event.ctrlKey && w === scout.keys.V;

  if (
    !pasteShortcut && (
      event.ctrlKey || event.altKey ||
      w === scout.keys.TAB ||
      w === scout.keys.SHIFT ||
      w === scout.keys.CTRL ||
      w === scout.keys.HOME ||
      w === scout.keys.END ||
      w === scout.keys.LEFT ||
      w === scout.keys.RIGHT ||
      this._isNavigationKey(event) ||
      this._isFunctionKey(event)
    )) {
    return;
  }

  // The typed character is not available until the keyUp event happens
  // That's why we must deal with that event here (and not in keyDown)
  // We don't use _displayText() here because we always want the text the
  // user has typed.
  if (this.isPopupOpen()) {
    this._lookupByText();
  } else {
    $.log.debug('(SmartField2#_onFieldKeyup)');
    this.openPopup(false);
  }
};

/**
 * @override Widget.js
 */
scout.SmartField.prototype._createKeyStrokeContext = function() {
  return new scout.InputFieldKeyStrokeContext();
};

/**
 * Prevent TABbing to the next field when popup is open. Because popup removal is animated,
 * we allow TAB, if the popup is still there but flagged as "to be removed" with the
 * removalPending flag.
 */
scout.SmartField2.prototype._isPreventDefaultTabHandling = function(event) {
  var doPrevent = false;
  if (this.isPopupOpen() || this._lookupInProgress) {
    doPrevent = true;
  }
  $.log.trace('(SmartField2#_isPreventDefaultTabHandling) must prevent default when TAB was pressed = ' + doPrevent);
  return doPrevent;
};

scout.SmartField2.prototype.isPopupOpen = function() {
  return this.popup && !this.popup.removalPending;
};

scout.SmartField2.prototype._onFieldKeydown = function(event) {
  this._updateUserWasTyping(event);

  // We must prevent default focus handling
  if (event.which === scout.keys.TAB) {
    if (this.mode === scout.FormField.Mode.DEFAULT) {
      event.preventDefault();
      $.log.info('(SmartField2#_onFieldKeydown) set _tabPrevented');
      this._tabPrevented = {
        shiftKey: event.shiftKey
      };
    }
    this.acceptInput();
    return;
  }

  if (this._isNavigationKey(event)) {
    if (this.isPopupOpen()) {
      this.popup.delegateKeyEvent(event);
    } else {
      this.openPopup(true);
    }
  }
};

scout.SmartField2.prototype._updateUserWasTyping = function(event) {
  var w = event.which;
  if (w === scout.keys.TAB) {
    // neutral, don't change flag
  } else {
    this._userWasTyping = !(this._isNavigationKey(event) || w === scout.keys.ENTER);
  }
};

scout.SmartField2.prototype._isNavigationKey = function(event) {
  var navigationKeys = [
    scout.keys.PAGE_UP,
    scout.keys.PAGE_DOWN,
    scout.keys.UP,
    scout.keys.DOWN
  ];

  if (this.isDropdown()) {
    navigationKeys.push(scout.keys.HOME);
    navigationKeys.push(scout.keys.END);
  }

  return scout.isOneOf(event.which, navigationKeys);
};

scout.SmartField2.prototype._isFunctionKey = function(e) {
  return e.which >= scout.keys.F1 && e.which < scout.keys.F12;
};

scout.SmartField2.prototype._onLookupRowSelected = function(event) {
  this.setLookupRow(event.lookupRow);
  this.closePopup();
};

// FIXME [awe] 7.0 - SF2: discuss usage of activeFilter. With current impl. we cannot
// use the activeFilter in the lookup call because it belongs to the widget state.
scout.SmartField2.prototype._onActiveFilterSelected = function(event) {
  this.setActiveFilter(event.activeFilter);
  this._lookupByText();
};

scout.SmartField2.prototype.setSearching = function(searching) {
  this.setProperty('searching', searching);
};

scout.SmartField2.prototype._renderSearching = function() {
  this.$container.toggleClass('searching', this.searching);
};

scout.SmartField2.prototype.setBrowseAutoExpandAll = function(browseAutoExpandAll) {
  this.setProperty('browseAutoExpandAll', browseAutoExpandAll);
};

scout.SmartField2.prototype.setBrowseLoadIncremental = function(browseLoadIncremental) {
  this.setProperty('browseLoadIncremental', browseLoadIncremental);
  if (this.lookupCall) {
    this.lookupCall.setLoadIncremental(browseLoadIncremental); // FIXME [awe] 7.0 - SF2: discuss with C.GU, really a property for all lookups?
  }
};

scout.SmartField2.prototype.setActiveFilter = function(activeFilter) {
  this.setProperty('activeFilter', this.activeFilterEnabled ? activeFilter : null);
};

scout.SmartField2.prototype._lookupByText = function() {
  var searchText = this._readDisplayText();
  $.log.trace('(SmartField2#_lookupByText) searchText=' + searchText);

  // debounce lookup
  if (this._pendingLookup) {
    clearTimeout(this._pendingLookup);
  }

  this._pendingLookup = setTimeout(function() {
    $.log.debug('(SmartField2#_lookupByText) searchText=' + searchText);
    // this.lookupCall.setActiveFilter(this.activeFilter); // FIXME [awe] 7.0 - SF2: add on LookupCall
    this._executeLookup(this.lookupCall.getByText.bind(this.lookupCall, searchText))
      .done(this._lookupByTextOrAllDone.bind(this));
  }.bind(this), scout.SmartField2.DEBOUNCE_DELAY);
};

/**
 * A wrapper function around lookup calls used to set the _lookupInProgress flag, and display the state in the UI.
 */
scout.SmartField2.prototype._executeLookup = function(lookupFunc) {
  this._lookupInProgress = true;
  this.showLookupInProgress();
  return lookupFunc()
    .done(function() {
      this._lookupInProgress = false;
      this.hideLookupInProgress();
    }.bind(this));
};

scout.SmartField2.prototype.showLookupInProgress = function() {
  if (this._pendingSetSearching) {
    clearTimeout(this._pendingSetSearching);
  }
  this.setSearching(true); // always show searching immediately
};

scout.SmartField2.prototype.hideLookupInProgress = function() {
  if (this._pendingSetSearching) {
    clearTimeout(this._pendingSetSearching);
  }
  this._pendingSetSearching = setTimeout(function() {
    this.setSearching(false);
    this._pendingSetSearching = null;
  }.bind(this), 250);
};

/**
 * Returns true if the smart-field lookup returns a lot of rows. In that case
 * the proposal chooser must create a table with virtual scrolling, which means
 * only the rows visible in the UI are rendered in the DOM. By default we render
 * all rows, since this avoids problems with layout-invalidation with rows
 * that have a bitmap-image (PNG) which is loaded asynchronously.
 */
scout.SmartField2.prototype.virtual = function() {
  return this.browseMaxRowCount > scout.SmartField2.DEFAULT_BROWSE_MAX_COUNT;
};

scout.SmartField2.prototype.isDropdown = function() {
  return this.displayStyle === scout.SmartField2.DisplayStyle.DROPDOWN;
};

scout.SmartField2.prototype._setLookupRow = function(lookupRow) {
  this._setProperty('lookupRow', lookupRow);
};

scout.SmartField2.prototype.setLookupRow = function(lookupRow) {
  if (this.lookupRow === lookupRow) {
    return;
  }
  this._setLookupRow(lookupRow);
  this.setErrorStatus(null);
  // this flag is required so lookup row is not changed again, when _setValue is called
  this._lockLookupRow = true;
  if (lookupRow) {
    this.setValue(this._getValueFromLookupRow(lookupRow));
  } else {
    this.setValue(null);
  }
  this._lockLookupRow = false;

  // In case we have a value X set, start to type search text, and then choose the lookup
  // row from the proposal with exactly the same value X, setValue() does nothing because
  // the value has not changed (even though the display text has) thus _formatValue is
  // never called. That's why we always reset the display text to make sure the display
  // text is correct.
  this.resetDisplayText();
};

scout.SmartField2.prototype.resetDisplayText = function() {
  if (this.rendered) {
    this._renderDisplayText();
  }
};

scout.SmartField2.prototype._getValueFromLookupRow = function(lookupRow) {
  return lookupRow.key;
};

scout.SmartField2.prototype._setValue = function(value) {
  // set the cached lookup row to null. Keep in mind that the lookup row is set async in a timeout
  // must of the time. Thus we must remove the reference to the old lookup row as early as possible
  if (!this._lockLookupRow) {
    if (value) {
      // when a value is set, we only keep the cached lookup row when the key of the lookup row is equals to the value
      if (this.lookupRow && this.lookupRow.key !== value) {
        this._setLookupRow(null);
      }
    } else {
      // when value is set to null, we must also reset the cached lookup row
      this._setLookupRow(null);
    }
  }
  scout.SmartField2.parent.prototype._setValue.call(this, value);
};
