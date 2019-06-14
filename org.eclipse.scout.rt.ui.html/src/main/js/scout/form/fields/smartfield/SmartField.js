/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.SmartField = function() {
  scout.SmartField.parent.call(this);

  this.popup = null;
  this.lookupCall = null;
  this.codeType = null;
  this._pendingLookup = null;
  this._pendingOpenPopup = false;
  this._tabPrevented = null;
  this.lookupRow = null;
  this.browseHierarchy = false;
  this.browseMaxRowCount = scout.SmartField.DEFAULT_BROWSE_MAX_COUNT;
  this.browseAutoExpandAll = true;
  this.browseLoadIncremental = false;
  this.searchRequired = false;
  this.activeFilterEnabled = false;
  this.activeFilter = null;
  this.activeFilterLabels = [];
  this.columnDescriptors = null;
  this.displayStyle = scout.SmartField.DisplayStyle.DEFAULT;
  this.touchMode = false;
  this.embedded = false;
  this._userWasTyping = false; // used to detect whether the last thing the user did was typing (a proposal) or something else, like selecting a proposal row
  this._acceptInputEnabled = true; // used to prevent multiple execution of blur/acceptInput
  this._acceptInputDeferred = $.Deferred();
  this._notUnique = false; // used to store the error state 'not unique' which must not be showed while typing, but when the field loses focus
  this._lastSearchText = null;
  this.lookupStatus = null;
  this._currentLookupCall = null; // should only be accessed on the original widget since the adapter accesses it
  this.lookupSeqNo = 0; // used to detect if the proposal chooser contains the results of the latest lookup, or an out-dated result.
  // only when the result is up-to-date, we can use the selected lookup row

  this._addCloneProperties(['lookupRow', 'codeType', 'lookupCall', 'activeFilter', 'activeFilterEnabled', 'activeFilterLabels',
    'browseHierarchy', 'browseMaxRowCount', 'browseAutoExpandAll', 'browseLoadIncremental', 'searchRequired', 'columnDescriptors',
    'displayStyle'
  ]);
};
scout.inherits(scout.SmartField, scout.ValueField);

scout.SmartField.DisplayStyle = {
  DEFAULT: 'default',
  DROPDOWN: 'dropdown'
};

scout.SmartField.ErrorCode = {
  NOT_UNIQUE: 1,
  NO_RESULTS: 2,
  NO_DATA: 3,
  SEARCH_REQUIRED: 4
};

scout.SmartField.DEBOUNCE_DELAY = 200;

scout.SmartField.DEFAULT_BROWSE_MAX_COUNT = 100;

/**
 * @see IContentAssistField#getActiveFilterLabels() - should have the same order.
 */
scout.SmartField.ACTIVE_FILTER_VALUES = ['UNDEFINED', 'FALSE', 'TRUE'];

scout.SmartField.prototype._init = function(model) {
  scout.SmartField.parent.prototype._init.call(this, model);

  if (this.activeFilterLabels.length === 0) {
    this.activeFilterLabels = [
      this.session.text('ui.All'),
      this.session.text('ui.Inactive'),
      this.session.text('ui.Active')
    ];
  }

  scout.fields.initTouch(this, model);
};

/**
 * Initializes lookup call and code type before calling set value.
 * This cannot be done in _init because the value field would call _setValue first
 */
scout.SmartField.prototype._initValue = function(value) {
  this._setLookupCall(this.lookupCall);
  this._setCodeType(this.codeType);
  this._setLookupRow(this.lookupRow);
  scout.SmartField.parent.prototype._initValue.call(this, value);
};

scout.SmartField.prototype._createKeyStrokeContext = function() {
  return new scout.InputFieldKeyStrokeContext();
};

scout.SmartField.prototype._initKeyStrokeContext = function() {
  scout.SmartField.parent.prototype._initKeyStrokeContext.call(this);

  this.keyStrokeContext.registerKeyStroke(new scout.SmartFieldCancelKeyStroke(this));
};

scout.SmartField.prototype._render = function() {
  this.addContainer(this.$parent, 'has-icon ' + this.cssClassName(), new scout.SmartFieldLayout(this));
  this.addLabel();

  var fieldFunc = this.isDropdown() ? scout.fields.makeInputDiv : scout.fields.makeInputOrDiv;
  var $field = fieldFunc.call(scout.fields, this)
    .on('mousedown', this._onFieldMouseDown.bind(this));

  if (!this.touchMode) {
    $field
      .keyup(this._onFieldKeyUp.bind(this))
      .keydown(this._onFieldKeyDown.bind(this))
      .on('input', this._onFieldInput.bind(this));
  }
  this.addField($field);

  if (!this.embedded) {
    this.addMandatoryIndicator();
  }
  this.addIcon();
  this.$icon.addClass('needsclick');
  this.addStatus();
};

scout.SmartField.prototype._renderGridData = function() {
  scout.SmartField.parent.prototype._renderGridData.call(this);
  this.updateInnerAlignment({
    useHorizontalAlignment: (this.browseHierarchy ? false : true)
  });
};

scout.SmartField.prototype._renderGridDataHints = function() {
  scout.SmartField.parent.prototype._renderGridDataHints.call(this);
  this.updateInnerAlignment({
    useHorizontalAlignment: (this.browseHierarchy ? false : true)
  });
};

scout.SmartField.prototype.cssClassName = function() {
  var prefix = this.displayStyle;
  if (this.displayStyle === scout.SmartField.DisplayStyle.DEFAULT) {
    prefix = 'smart';
  }
  return prefix + '-field';
};

scout.SmartField.prototype._readSearchText = function() {
  var fieldText = this._readDisplayText(),
    displayText = scout.nvl(this.displayText, ''),
    textLines = displayText.split('\n');

  if (textLines.length === 1 || scout.strings.empty(fieldText)) {
    return fieldText;
  }
  textLines.shift(); // remove first line
  scout.arrays.insert(textLines, fieldText, 0);
  return scout.strings.join('\n', textLines);
};

scout.SmartField.prototype._readDisplayText = function() {
  return scout.fields.valOrText(this.$field);
};

scout.SmartField.prototype._renderDisplayText = function() {
  var displayText = scout.nvl(this.displayText, ''),
    textLines = displayText.split('\n');
  if (textLines.length) {
    displayText = textLines[0];
  }
  scout.fields.valOrText(this.$field, displayText);
  scout.SmartField.parent.prototype._renderDisplayText.call(this);
};

/**
 * Accepts the selected lookup row and sets its id as value.
 * This function is called on blur, by a keystroke or programmatically at any time.
 *
 * @param [sync] optional boolean value (default: false), when set to true acceptInput is not allowed to start an asynchronous lookup for text search
 */
scout.SmartField.prototype.acceptInput = function(sync) {
  if (!this._acceptInputEnabled) {
    $.log.isTraceEnabled() && $.log.trace('(SmartField#acceptInput) Skipped acceptInput because _acceptInputEnabled=false');
    return this._acceptInputDeferred.promise();
  }

  // Use a timeout to prevent multiple execution within the same user action
  this._acceptInputEnabled = false;
  setTimeout(function() {
    this._acceptInputEnabled = true;
  }.bind(this));

  var
    searchText = this._readSearchText(),
    searchTextEmpty = scout.strings.empty(searchText),
    searchTextChanged = this._checkSearchTextChanged(searchText),
    selectedLookupRow = this._getSelectedLookupRow(searchTextChanged);

  this._setProperty('displayText', searchText);
  this._acceptInputDeferred = $.Deferred();
  this._flushLookupStatus();
  this._clearPendingLookup();

  if (this.touchMode) {
    $.log.isDebugEnabled() && $.log.debug('(SmartField#acceptInput) Always send acceptInput for touch field');
    this._inputAccepted();
    return;
  }

  return this._acceptInput(sync, searchText, searchTextEmpty, searchTextChanged, selectedLookupRow);
};

/**
 * This function is used to determine if the currently selected lookup row can be
 * used when acceptInput is called. Basically we don't want to use the row in case
 * the result is out-dated.
 */
scout.SmartField.prototype._getSelectedLookupRow = function(searchTextChanged) {
  // don't use selected lookup row if...
  if (!this.isPopupOpen() || // 1. popup has been closed
    (searchTextChanged && this._userWasTyping)) { // 2. search text has changed and user was typing
    return null;
  }
  // 3. if the result row is from an out-dated result
  return this.lookupSeqNo === this.popup.lookupResult.seqNo ?
    this.popup.getSelectedLookupRow() : null;
};

scout.SmartField.prototype._checkSearchTextChanged = function(searchText) {
  if (this.isDropdown() || !this._userWasTyping) {
    return false; // search text cannot change in drop-down fields
  }

  // check if search text has changed since the last search, when it has changed
  // we cannot use the currently selected lookup row, because these proposals are
  // out-dated.
  return !this._searchTextEquals(searchText, this._lastSearchText);
};

scout.SmartField.prototype._searchTextEquals = function(searchText, lastSearchText) {
  var a = scout.strings.nullIfEmpty(this._firstTextLine(searchText));
  var b = scout.strings.nullIfEmpty(lastSearchText);
  return scout.strings.equalsIgnoreCase(a, b);
};

scout.SmartField.prototype._clearPendingLookup = function() {
  if (this._pendingLookup) {
    clearTimeout(this._pendingLookup);
    this._pendingLookup = null;
  }
};

/**
 * This function is intended to be overridden. Proposal field has another behavior than the smart field.
 *
 * @param [sync] optional boolean value (default: false), when set to true acceptInput is not allowed to start an asynchronous lookup for text search
 */
scout.SmartField.prototype._acceptInput = function(sync, searchText, searchTextEmpty, searchTextChanged, selectedLookupRow) {
  // Don't show the not-unique error when the search-text becomes empty while typing (see ticket #229775)
  if (this._notUnique && !searchTextEmpty) {
    this._setNotUniqueError(searchText);
  }

  // Do nothing when search text is equals to the text of the current lookup row
  if (!selectedLookupRow && this.lookupRow) {
    var lookupRowText = scout.strings.nvl(this.lookupRow.text);
    if (lookupRowText === searchText) {
      $.log.isDebugEnabled() && $.log.debug('(SmartField#_acceptInput) unchanged: text is equals. Close popup');
      this._clearLookupStatus();
      this._inputAccepted(false);
      return;
    }
  }

  // Do nothing when we don't have a current lookup row and search text is empty
  // trigger event when search text has changed. This is required for the case where
  // a field is cleared, and the remote model must be updated (value=null)
  if (!selectedLookupRow && !this.lookupRow && searchTextEmpty) {
    $.log.isDebugEnabled() && $.log.debug('(SmartField#_acceptInput) unchanged: text is empty. Close popup');
    this._clearLookupStatus();
    if (this.errorStatus && this.errorStatus.code === scout.SmartField.ErrorCode.NO_RESULTS) {
      // clear the error status from previous search which did not find any results. This error status is no longer valid as we accept the null content here.
      this.clearErrorStatus();
    }
    this._inputAccepted(searchTextChanged || this._userWasTyping);
    return;
  }

  // 1.) when search text is empty and no lookup-row is selected, simply set the value to null
  // Note: here we assume that a current lookup row is set.
  if (!selectedLookupRow && searchTextEmpty) {
    $.log.isDebugEnabled() && $.log.debug('(SmartField#_acceptInput) empty. Set lookup-row to null, close popup');
    this._clearLookupStatus();
    this.setLookupRow(null);
    this._inputAccepted();
    return;
  }

  // 2.) proposal chooser is open -> use the selected row as value
  if (selectedLookupRow) {
    $.log.isDebugEnabled() && $.log.debug('(SmartField#_acceptInput) lookup-row selected. Set lookup-row, close popup lookupRow=', selectedLookupRow.toString());
    this._clearLookupStatus();
    this.setLookupRow(selectedLookupRow);
    this._inputAccepted();
    return;
  }

  // 3.) proposal chooser is not open -> try to accept the current display text
  // this causes a lookup which may fail and open a new proposal chooser (property
  // change for 'result').
  if (searchTextChanged || this._userWasTyping) {
    this._acceptByText(sync, this._firstTextLine(searchText));
  } else if (!this._hasUiError()) {
    this._inputAccepted(false);
  } else if (this._hasNotUniqueError() && this.popup) {
    // popup has been opened (again) with errorStatus NOT_UNIQUE, and search text is still the same
    this.popup.selectFirstLookupRow();
  } else {
    // even though there's nothing todo, someone could wait for our promise to be resolved
    this._acceptInputDeferred.resolve();
  }

  return this._acceptInputDeferred.promise();
};

/**
 * Required for multiline smart-field. Only use first line of search text for accept by text.
 * Note: for the regular lookup by text, we use the readDisplayText() function which always
 * returns a single line. But in acceptInput we need the full search text (=display text + additional
 * lines) in order to check whether or not the display text has changed, compared to the current
 * lookup row. That's why we must extract the first line here.
 */
scout.SmartField.prototype._firstTextLine = function(text) {
  if (scout.strings.empty(text)) {
    return text;
  }
  return text.split('\n')[0];
};

/**
 * This function is intended to be overridden. Proposal field has another behavior than the smart field.
 *
 * @param sync when set to true it's not allowed to start an asynchronous lookup to search by text, the
 *     current search text is discarded. The flag is set to true in case we click on another field, where
 *     we must make sure the order of (browser) events is not changed by the lookup that would return _after_
 *     the events for the clicked field are handled.
 */
scout.SmartField.prototype._acceptByText = function(sync, searchText) {
  sync = scout.nvl(sync, false);
  $.log.isDebugEnabled() && $.log.debug('(SmartField#_acceptByText) sync=' + sync + ' searchText=', searchText);

  if (sync) {
    this._acceptByTextSync(searchText);
  } else {
    this._acceptByTextAsync(searchText);
  }
};

scout.SmartField.prototype._acceptByTextSync = function(searchText) {
  this._lastSearchText = null;
  this._inputAccepted();
  if (!this._hasUiError()) {
    this.resetDisplayText();
  }
};

scout.SmartField.prototype._acceptByTextAsync = function(searchText) {
  this._lastSearchText = searchText;
  this._executeLookup(this.lookupCall.cloneForText(searchText), true)
    .done(this._acceptByTextDone.bind(this))
    .done(this._triggerLookupCallDone.bind(this));
  this._triggerAcceptByText(searchText);
};

scout.SmartField.prototype._inputAccepted = function(triggerEvent, acceptByLookupRow) {
  triggerEvent = scout.nvl(triggerEvent, true);
  acceptByLookupRow = scout.nvl(acceptByLookupRow, true);
  // don't close when shown in touch popup (also called when clear() is executed)
  if (!this.embedded) {
    this.closePopup();
  }
  this._userWasTyping = false;
  if (triggerEvent) {
    this._triggerAcceptInput(acceptByLookupRow);
  }
  this._focusNextTabbable();
  this._acceptInputDeferred.resolve();
};

scout.SmartField.prototype._focusNextTabbable = function() {
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
    $.log.isDebugEnabled() && $.log.debug('(SmartField#_inputAccepted) tab-index=' + fieldIndex + ' next tab-index=' + nextIndex);
    var $nextElement = $tabElements.eq(nextIndex).focus();
    if (scout.objects.isFunction($nextElement[0].select)) {
      $nextElement[0].select();
    }
    this._tabPrevented = null;
  }
};

scout.SmartField.prototype._acceptByTextDone = function(result) {
  this._userWasTyping = false;
  this._extendResult(result);
  this._notUnique = result.numLookupRows > 1;

  // when there's exactly one result, we accept that lookup row
  if (result.uniqueMatch) {
    var lookupRow = result.uniqueMatch;
    if (this._isLookupRowActive(lookupRow)) {
      this.setLookupRow(lookupRow);
      this._inputAccepted();
    } else {
      this.setErrorStatus(scout.Status.error({
        message: this.session.text('SmartFieldInactiveRow', result.text)
      }));
    }
    return;
  }

  this._acceptInputFail(result);
};

/**
 * Extends the properties 'uniqueMatch' and 'numLookupRows' on the given result object.
 * The implementation is different depending on the browseHierarchy property.
 */
scout.SmartField.prototype._extendResult = function(result) {
  result.seqNo = this.lookupSeqNo;
  result.uniqueMatch = null;

  // Set query type on result, e.g. 'byAll'
  var propertyName = 'by' + scout.strings.toUpperCaseFirstLetter(result.queryBy.toLowerCase());
  result[propertyName] = true;

  if (this.browseHierarchy) {
    // tree (hierarchical)
    var proposalChooser = scout.create('TreeProposalChooser', {
      parent: this,
      smartField: this
    });
    proposalChooser.setLookupResult(result);
    var leafs = proposalChooser.findLeafs();
    result.numLookupRows = leafs.length;
    if (result.numLookupRows === 1) {
      result.uniqueMatch = leafs[0].lookupRow;
    }
  } else {
    // table
    result.numLookupRows = result.lookupRows.length;
    if (result.numLookupRows === 1) {
      result.uniqueMatch = result.lookupRows[0];
    }
  }

  result.empty = (result.numLookupRows === 0);
};

scout.SmartField.prototype._acceptInputFail = function(result) {
  var searchText = result.text;

  // in any other case something went wrong
  if (result.empty) {
    if (!this.embedded) {
      this.closePopup();
    }
    this.setValue(null);
    this.setDisplayText(searchText);
    this.setErrorStatus(scout.Status.error({
      message: this.session.text('SmartFieldCannotComplete', searchText),
      code: scout.SmartField.ErrorCode.NO_RESULTS
    }));
  }

  if (result.numLookupRows > 1) {
    this.setValue(null);
    this.setDisplayText(searchText);
    this._setNotUniqueError(searchText);
    if (this.isPopupOpen()) {
      this.popup.setLookupResult(result);
    } else {
      this._lookupByTextOrAllDone(result);
    }
    // check again if popup is open yet (might have been opened by _lookupByTextOrAllDone)
    if (this.isPopupOpen()) {
      this.popup.selectFirstLookupRow();
    }
  }

  this._acceptInputDeferred.resolve();
  this._triggerAcceptInputFail();
};

scout.SmartField.prototype.lookupByRec = function(rec) {
  $.log.isDebugEnabled() && $.log.debug('(SmartField#lookupByRec) rec=', rec);
  this._lastSearchText = null;
  return this._executeLookup(this.lookupCall.cloneForRec(rec))
    .then(function(result) {

      // Since this function is only used for hierarchical trees we
      // can simply set the appendResult flag always to true here
      result.appendResult = true;
      result.rec = rec;

      if (this.isPopupOpen()) {
        this.popup.setLookupResult(result);
      }
    }.bind(this))
    .then(this._triggerLookupCallDone.bind(this));
};

/**
 * Validates the given lookup row is enabled and matches the current activeFilter settings.
 *
 * @returns {boolean}
 */
scout.SmartField.prototype._isLookupRowActive = function(lookupRow) {
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

scout.SmartField.prototype._renderEnabled = function() {
  scout.SmartField.parent.prototype._renderEnabled.call(this);
  this.$field.setTabbable(this.enabledComputed);
};

scout.SmartField.prototype.setLookupCall = function(lookupCall) {
  this.setProperty('lookupCall', lookupCall);
};

scout.SmartField.prototype._setLookupCall = function(lookupCall) {
  this._setProperty('lookupCall', scout.LookupCall.ensure(lookupCall, this.session));
};

scout.SmartField.prototype._setCodeType = function(codeType) {
  this.setProperty('codeType', codeType);
};

scout.SmartField.prototype._setCodeType = function(codeType) {
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

scout.SmartField.prototype._formatValue = function(value) {
  if (scout.objects.isNullOrUndefined(value)) {
    return '';
  }

  // we already have a lookup row - Note: in Scout Classic (remote case)
  // we always end here and don't need to perform a getByKey lookup.
  if (this.lookupRow) {
    return this._formatLookupRow(this.lookupRow);
  }

  // we must do a lookup first to get the display text
  // Note: this has a side-effect as it sets the property lookupRow on the smart field
  this._lastSearchText = null;
  return this._executeLookup(this.lookupCall.cloneForKey(value), true)
    .then(this._lookupByKeyDone.bind(this))
    .then(this._triggerLookupCallDone.bind(this));
};

scout.SmartField.prototype._lookupByKeyDone = function(result) {
  this._notUnique = false;
  var lookupRow = scout.LookupCall.firstLookupRow(result);
  this.setLookupRow(lookupRow);
  return this._formatLookupRow(lookupRow);
};

/**
 * This function is called when we need to format a display text from a given lookup
 * row. By default the property 'text' is used for that purpose. Override this function
 * if you need to format different properties from the lookupRow.
 */
scout.SmartField.prototype._formatLookupRow = function(lookupRow) {
  return lookupRow ? lookupRow.text : '';
};

/**
 * @param {boolean} [browse] whether or not the lookup call should execute getAll() or getByText() with the current display text.
 *     if browse is undefined, browse is set to true automatically if search text is empty
 * @returns {Promise}
 */
scout.SmartField.prototype.openPopup = function(browse) {
  var searchText = this._readDisplayText();
  $.log.isInfoEnabled() && $.log.info('SmartField#openPopup browse=' + browse + ' searchText=' + searchText + ' popup=' + this.popup + ' pendingOpenPopup=' + this._pendingOpenPopup);

  // Reset scheduled focus next tabbable when user clicks on the smartfield while a lookup is resolved.
  this._tabPrevented = null;
  this._pendingOpenPopup = true;

  if (scout.strings.empty(searchText)) {
    // if search text is empty - always do 'browse', no matter what the error code is
    browse = true;
  } else if (this.errorStatus) {
    // In case the search yields a not-unique error, we always want to start a lookup
    // with the current display text in every other case we better do browse again
    browse = !this._hasNotUniqueError() && !this.searchRequired;
  }

  return this._lookupByTextOrAll(browse, searchText);
};

scout.SmartField.prototype._hasUiError = function(codes) {
  var status = this._errorStatus();

  if (!status) {
    return false;
  }

  if (codes) {
    codes = scout.arrays.ensure(codes);
  } else {
    codes = [scout.SmartField.ErrorCode.NO_RESULTS, scout.SmartField.ErrorCode.NOT_UNIQUE];
  }

  // collect codes from the status hierarchy
  var statusList = scout.Status.asFlatList(status);
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

/**
 * @returns {Promise}
 */
scout.SmartField.prototype._lookupByTextOrAll = function(browse, searchText) {
  // default values
  searchText = scout.nvl(searchText, this._readDisplayText());
  browse = scout.nvl(browse, scout.strings.empty(searchText));

  // never do a text-lookup if field has dropdown style
  if (this.isDropdown()) {
    browse = true;
  }

  // this avoids unnecessary lookup-calls when a keyboard event has triggered
  // the lookupByTextOrAll function but the search-text has not changed #226643.
  if (!browse) {
    var lastSearchText = null;
    if (this._lastSearchText) {
      lastSearchText = this._lastSearchText;
    } else {
      lastSearchText = this._getLastSearchText();
    }
    if (this._searchTextEquals(searchText, lastSearchText)) {
      this._pendingOpenPopup = false;
      return;
    }
  }

  this._clearPendingLookup();

  var deferred = $.Deferred();
  var doneHandler = function(result) {
    this._lookupByTextOrAllDone(result);
    deferred.resolve(result);
  }.bind(this);

  // execute lookup byAll immediately
  if (browse) {
    $.log.isDebugEnabled() && $.log.debug('(SmartField#_lookupByTextOrAll) lookup byAll (seachText empty)');
    this._lastSearchText = null;
    if (this.searchRequired) {
      doneHandler({
        queryBy: scout.QueryBy.TEXT,
        lookupRows: []
      });
      this.setLookupStatus(scout.Status.warning({
        message: this.session.text('TooManyRows'),
        code: scout.SmartField.ErrorCode.SEARCH_REQUIRED
      }));
    } else {
      this._executeLookup(this.lookupCall.cloneForAll(), true)
        .done(doneHandler)
        .done(this._triggerLookupCallDone.bind(this));
    }
  } else {
    // execute lookup byText with a debounce/delay
    this._pendingLookup = setTimeout(function() {
      $.log.isDebugEnabled() && $.log.debug('(SmartField#_lookupByTextOrAll) lookup byText searchText=' + searchText);
      this._lastSearchText = searchText;
      this._executeLookup(this.lookupCall.cloneForText(searchText), true)
        .done(doneHandler)
        .done(this._triggerLookupCallDone.bind(this));
    }.bind(this), scout.SmartField.DEBOUNCE_DELAY);
  }

  return deferred.promise();
};

/**
 * Returns the text used to store the 'last search-text'. The implementation differs between SmartField and ProposalField.
 */
scout.SmartField.prototype._getLastSearchText = function() {
  return scout.objects.optProperty(this.lookupRow, 'text');
};

scout.SmartField.prototype._lookupByTextOrAllDone = function(result) {
  this._extendResult(result);
  this._notUnique = !result.byAll && result.numLookupRows > 1;

  if (this._handleException(result)) {
    return;
  }

  // In cases where the user has tabbed to the next field, while results for the previous
  // smart-field are still loading: don't show the proposal popup. In the case of a cell-editor
  // it's also possible that the smart-field is not rendered anymore when the lookup is done
  if (!this.rendered ||
    !this.isFocused() && !this.isTouchable()) {
    this.closePopup();
    return;
  }

  // 'No data' case
  if (result.empty && result.byAll) {
    // When active filter is enabled we must always show the popup, because the user
    // must be able to switch the filter properties. Otherwise a user could set the filter
    // to 'inactive', and receives an empty result for that query, the popup is closed
    // and the user can not switch the filter back to 'active' again because the filter
    // control is not visible.
    if (this.activeFilterEnabled) {
      this._ensurePopup(result);
    } else {
      this.closePopup();
    }

    this.setLookupStatus(scout.Status.warning({
      message: this.session.text('SmartFieldNoDataFound'),
      code: scout.SmartField.ErrorCode.NO_DATA
    }));
    return;
  }

  if (result.empty) {
    this._handleEmptyResult();
    this.setLookupStatus(scout.Status.warning({
      message: this.session.text('SmartFieldCannotComplete', result.text),
      code: scout.SmartField.ErrorCode.NO_RESULTS
    }));
    return;
  }

  var popupStatus = null;
  if (result.numLookupRows > this.browseMaxRowCount) {
    // Info: we limit the lookup rows here, but this is more a last line of defense
    // limit should be always performed on the server, so we don't have to transfer
    // unnecessary lookup rows over the slow network. Make sure your Scout lookup call
    // or REST service impl. respects the max. row count property.
    result.lookupRows = result.lookupRows.slice(0, this.browseMaxRowCount);
    popupStatus = scout.Status.info({
      message: this.session.text('SmartFieldMoreThanXRows', this.browseMaxRowCount)
    });
  }

  // Render popup, if not yet rendered and set results
  this._ensurePopup(result, popupStatus);
};

scout.SmartField.prototype._ensurePopup = function(result, status) {
  if (this.popup) {
    this.popup.setLookupResult(result);
    this.popup.setStatus(status);
  } else {
    this._renderPopup(result, status);
  }
};

scout.SmartField.prototype._handleException = function(result) {
  // Oops! Something went wrong while the lookup has been processed.
  if (result.exception) {
    this.setErrorStatus(scout.Status.error({
      message: result.exception
    }));
    this.closePopup();
    return true;
  }
  return false;
};

scout.SmartField.prototype._handleEmptyResult = function() {
  if (this.touchMode || this.activeFilterEnabled) {
    // In mobile mode we always open the popup, event if we don't have a result
    // Otherwise it would be impossible to enter text in a proposal field with
    // an empty proposal list. The same goes for activeFilterEnabled state -
    // a filter can lead to an empty result (for instance when there are no
    // inactive proposals), and it's hard to switch to another filter value
    // when the popup does not show up at all.
    var emptyResult = {
      lookupRows: []
    };
    this._ensurePopup(emptyResult);
  } else if (this.embedded) {
    this.popup.clearLookupRows();
  } else {
    this.closePopup();
  }
};

scout.SmartField.prototype._renderPopup = function(result, status) {
  // On touch devices the field does not get the focus.
  // But it should look focused when the popup is open.
  this.$field.addClass('focused');
  this.$container.addClass('popup-open');

  var useTouch = this.touchMode && !this.isDropdown();
  var popupType = useTouch ? 'SmartFieldTouchPopup' : 'SmartFieldPopup';
  this._pendingOpenPopup = false;
  this.popup = scout.create(popupType, {
    parent: this,
    $anchor: this.$field,
    boundToAnchor: !useTouch,
    closeOnAnchorMouseDown: false,
    field: this,
    lookupResult: result,
    status: status
  });

  this.popup.open();

  /* This variable is required to route events to the right field:
   * - in normal mode popup events should be processed by the normal smart-field
   * - in touch mode, the field flagged with the 'touch' property should process no
   *   events at all, instead the field flagged with the 'embedded' property should
   *   process these events.
   *
   * (*1) because the lookup is processed by the field flagged with 'touch' we must
   *      set the activeFilter on that field too, because the java-model on the server
   *      is stateful. The java field always passes the activeFilter property to the
   *      lookup call.
   */
  var fieldForPopup = useTouch ? this.popup._field : this;
  this.popup.on('lookupRowSelected', fieldForPopup._onLookupRowSelected.bind(fieldForPopup));
  this.popup.on('activeFilterSelected', this._onActiveFilterSelected.bind(this)); // intentionally use this instead of fieldForPopup *1
  this.popup.one('remove', function() {
    this.popup = null;
    if (this.rendered) {
      this.$container.removeClass('popup-open');
      this.$field.removeClass('focused');
      this._renderErrorStatus();
    }
  }.bind(this));
};

scout.SmartField.prototype.closePopup = function() {
  this._pendingOpenPopup = false;
  if (this.popup) {
    this.popup.close();
  }
};

/**
 * Calls acceptInput if mouse down happens outside of the field or popup
 * @override
 */
scout.SmartField.prototype.aboutToBlurByMouseDown = function(target) {
  if (this.touchMode) {
    return false;
  }
  var eventOnField = this.$field.isOrHas(target) || this.$icon.isOrHas(target) || (this.$clearIcon && this.$clearIcon.isOrHas(target));
  var eventOnPopup = this.popup && this.popup.$container.isOrHas(target);
  var eventOnTooltip = this._tooltip() && this._tooltip().rendered && this._tooltip().$container.isOrHas(target);
  if (!eventOnField && !eventOnPopup && !eventOnTooltip) {
    this.acceptInput(true); // event outside this value field
  }
};

scout.SmartField.prototype._onFieldMouseDown = function(event) {
  $.log.isDebugEnabled() && $.log.debug('(SmartField#_onFieldMouseDown)');
  this.activate(true);
};

scout.SmartField.prototype.activate = function(onField) {
  if (!this.enabledComputed || !this.rendered) {
    return;
  }
  if (!this.isDropdown() && !scout.fields.handleOnClick(this)) {
    return;
  }
  // Don't focus on desktop devices when click is on field #217192
  // Also required for touch case where field is a DIV and not an INPUT field
  if (!onField || scout.device.supportsTouch()) {
    this.$field.focus();
  }
  this.togglePopup();
};

scout.SmartField.prototype._onIconMouseDown = function(event) {
  $.log.isDebugEnabled() && $.log.debug('(SmartField#_onIconMouseDown)');
  if (!this.enabledComputed) {
    return;
  }
  event.preventDefault();
  this.$field.focus();
  if (!this.embedded) {
    if (this.isDropdown()) {
      this.togglePopup();
    } else if (!this.popup) {
      this.openPopup(!this.searchRequired);
    }
  }
};

scout.SmartField.prototype._onClearIconMouseDown = function(event) {
  $.log.isDebugEnabled() && $.log.debug('(SmartField#_onClearIconMouseDown)');
  if (!this.enabledComputed) {
    return;
  }
  event.preventDefault();
  this.$field.focus();
  this.clear();
};

scout.SmartField.prototype._clear = function() {
  // don't tab next field when user clicks on clear icon (acceptInput is called later)
  this._tabPrevented = null;
  // the state of these two flags is important. See #_checkSearchTextChanged
  this._lastSearchText = this._readDisplayText();
  this._userWasTyping = true;
  scout.fields.valOrText(this.$field, '');
  if (this.touchMode) {
    // There is actually no "x" the user can press in touch mode, but if the developer calls clear() manually, it should work too.
    // Because accept input works differently in touch mode we need to explicitly set the value to null
    this.setValue(null);
  }
  if (this.isPopupOpen()) {
    // When cleared, browse by all again, need to do it in setTimeout because sending acceptInput and lookupAll at the same time does not seem to work
    setTimeout(this._lookupByTextOrAll.bind(this, true));
  }
  this._updateHasText();
};

scout.SmartField.prototype.togglePopup = function() {
  $.log.isInfoEnabled() && $.log.info('(SmartField#togglePopup) popupOpen=', this.isPopupOpen());
  if (this.isPopupOpen()) {
    this.closePopup();
  } else {
    this.openPopup(!this.searchRequired);
  }
};

scout.SmartField.prototype._onFieldBlur = function(event) {
  this.setFocused(false);
  this.setLoading(false);
  if (this.isTouchable()) {
    return;
  }
  this.acceptInput(false);
  this.closePopup();
};

/**
 * @returns true if the field is either 'embedded' or in 'touchMode'.
 */
scout.SmartField.prototype.isTouchable = function() {
  return this.embedded || this.touchMode;
};

scout.SmartField.prototype._onFieldKeyUp = function(event) {
  // Escape
  if (event.which === scout.keys.ESC) {
    return;
  }

  // Pop-ups shouldn't open when one of the following keys is pressed
  var w = event.which;
  var isPaste = ((event.ctrlKey || event.metaKey) && w === scout.keys.V) || (event.shiftKey && w === scout.keys.INSERT);
  var isCut = ((event.ctrlKey || event.metaKey) && w === scout.keys.X) || (event.shiftKey && w === scout.keys.DELETE);

  if (!isPaste && !isCut && (
      event.ctrlKey ||
      event.altKey ||
      event.metaKey ||
      w === scout.keys.ENTER ||
      w === scout.keys.TAB ||
      w === scout.keys.SHIFT ||
      w === scout.keys.CTRL ||
      w === scout.keys.ALT ||
      w === scout.keys.HOME ||
      w === scout.keys.END ||
      w === scout.keys.LEFT ||
      w === scout.keys.RIGHT ||
      w === scout.keys.WIN_LEFT ||
      w === scout.keys.WIN_RIGHT ||
      w === scout.keys.SELECT ||
      w === scout.keys.NUM_LOCK ||
      w === scout.keys.CAPS_LOCK ||
      w === scout.keys.SCROLL_LOCK ||
      w === scout.keys.PAUSE ||
      w === scout.keys.PRINT_SCREEN ||
      this._isNavigationKey(event) ||
      this._isFunctionKey(event)
    )) {
    return;
  }

  // The typed character is not available until the keyUp event happens
  // That's why we must deal with that event here (and not in keyDown)
  // We don't use _displayText() here because we always want the text the
  // user has typed.
  if (this._pendingOpenPopup || this.isPopupOpen()) {
    if (!this.isDropdown()) {
      this._lookupByTextOrAll();
    }
  } else if (!this._pendingOpenPopup) {
    this.openPopup();
  }
};

scout.SmartField.prototype.isPopupOpen = function() {
  return !!(this.popup && !this.popup.removalPending);
};

scout.SmartField.prototype._onFieldKeyDown = function(event) {
  this._updateUserWasTyping(event);

  // We must prevent default focus handling
  if (event.which === scout.keys.TAB) {
    if (this.mode === scout.FormField.Mode.DEFAULT) {
      event.preventDefault(); // prevent browser default TAB behavior
      event.stopPropagation(); // prevent FocusContext#._onKeyDown
      $.log.isDebugEnabled() && $.log.debug('(SmartField#_onFieldKeyDown) set _tabPrevented');
      this._tabPrevented = {
        shiftKey: event.shiftKey
      };
    }
    this.acceptInput();
    return;
  }

  if (event.which === scout.keys.ENTER) {
    this._handleEnterKey(event);
    return;
  }

  // For dropdowns, not only navigation keys trigger the popup (see code below).
  // However, there are still some exceptions that should be ignored:
  var w = event.which;
  if (this.isDropdown() && (
      event.ctrlKey ||
      event.altKey ||
      event.metaKey ||
      w === scout.keys.ESC ||
      w === scout.keys.SHIFT ||
      w === scout.keys.CTRL ||
      w === scout.keys.ALT ||
      w === scout.keys.WIN_LEFT ||
      w === scout.keys.WIN_RIGHT ||
      w === scout.keys.SELECT ||
      w === scout.keys.NUM_LOCK ||
      w === scout.keys.CAPS_LOCK ||
      w === scout.keys.SCROLL_LOCK ||
      w === scout.keys.PAUSE ||
      w === scout.keys.PRINT_SCREEN ||
      this._isFunctionKey(event)
    )) {
    return;
  }

  // If field has dropdown style, we open the popup immediately
  // because we must not wait until text has been typed
  if (this._isNavigationKey(event) || this.isDropdown()) {
    if (this.isPopupOpen()) {
      this.popup.delegateKeyEvent(event);
    } else if (!this._pendingOpenPopup) {
      this.openPopup(!this.searchRequired);
    }
    event.stopPropagation(); // key has been handled (popup open). do not allow propagation to other listeners because this could remove tooltips
  }
};

scout.SmartField.prototype._onFieldInput = function() {
  this._updateHasText();
};

scout.SmartField.prototype._updateUserWasTyping = function(event) {
  var w = event.which;
  var isPaste = ((event.ctrlKey || event.metaKey) && w === scout.keys.V) || (event.shiftKey && w === scout.keys.INSERT);
  var isCut = ((event.ctrlKey || event.metaKey) && w === scout.keys.X) || (event.shiftKey && w === scout.keys.DELETE);

  if (!isPaste && !isCut && (
      event.ctrlKey ||
      event.altKey ||
      event.metaKey ||
      w === scout.keys.ESC ||
      w === scout.keys.TAB ||
      w === scout.keys.SHIFT ||
      w === scout.keys.CTRL ||
      w === scout.keys.ALT ||
      w === scout.keys.HOME ||
      w === scout.keys.END ||
      w === scout.keys.LEFT ||
      w === scout.keys.RIGHT ||
      w === scout.keys.WIN_LEFT ||
      w === scout.keys.WIN_RIGHT ||
      w === scout.keys.SELECT ||
      w === scout.keys.NUM_LOCK ||
      w === scout.keys.CAPS_LOCK ||
      w === scout.keys.SCROLL_LOCK ||
      w === scout.keys.PAUSE ||
      w === scout.keys.PRINT_SCREEN ||
      this._isFunctionKey(event)
    )) {
    // neutral, don't change flag
    return;
  }

  this._userWasTyping = !(this._isNavigationKey(event) || w === scout.keys.ENTER);
};

scout.SmartField.prototype._isNavigationKey = function(event) {
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

scout.SmartField.prototype._handleEnterKey = function(event) {
  if (this.isPopupOpen()) {
    this.popup.selectLookupRow();
    event.stopPropagation();
  }
};

scout.SmartField.prototype._isFunctionKey = function(event) {
  return event.which >= scout.keys.F1 && event.which <= scout.keys.F12;
};

scout.SmartField.prototype._onLookupRowSelected = function(event) {
  // When a row has been selected in the proposal chooser, cancel all
  // pending and running lookup-calls. This avoids situations where the
  // lookup-call returns with results after the user has pressed the
  // enter key in order to select a result (see ticket #229775).
  this._clearPendingLookup();

  var currentLookupCall = this.original()._currentLookupCall;

  if (currentLookupCall) {
    currentLookupCall.abort();
    this.original()._currentLookupCall = null;
    this.setLoading(false);
  }

  this.setLookupRow(event.lookupRow);
  this._inputAccepted();
  this.closePopup();
};

scout.SmartField.prototype._onActiveFilterSelected = function(event) {
  this.setActiveFilter(event.activeFilter);
  this._lookupByTextOrAll(!this.searchRequired);
};

scout.SmartField.prototype.setBrowseMaxRowCount = function(browseMaxRowCount) {
  this.setProperty('browseMaxRowCount', browseMaxRowCount);
};

scout.SmartField.prototype.setBrowseAutoExpandAll = function(browseAutoExpandAll) {
  this.setProperty('browseAutoExpandAll', browseAutoExpandAll);
};

scout.SmartField.prototype.setBrowseLoadIncremental = function(browseLoadIncremental) {
  this.setProperty('browseLoadIncremental', browseLoadIncremental);
  if (this.lookupCall) {
    // change template here. Will be used on the next clone
    this.lookupCall.setLoadIncremental(browseLoadIncremental);
  }
};

scout.SmartField.prototype.setActiveFilter = function(activeFilter) {
  this.setProperty('activeFilter', this.activeFilterEnabled ? activeFilter : null);
};

scout.SmartField.prototype.setActiveFilterEnabled = function(activeFilterEnabled) {
  this.setProperty('activeFilterEnabled', activeFilterEnabled);
};

scout.SmartField.prototype.setSearchRequired = function(searchRequired) {
  this.setProperty('searchRequired', searchRequired);
};

/**
 * A wrapper function around lookup calls used to display the state in the UI.
 */
scout.SmartField.prototype._executeLookup = function(lookupCall, abortExisting) {
  this.lookupSeqNo++;
  this.setLoading(true);

  var currentLookupCall = this.original()._currentLookupCall;

  if (abortExisting && currentLookupCall) {
    currentLookupCall.abort();
  }
  this.original()._currentLookupCall = lookupCall;
  this.trigger('prepareLookupCall', {
    lookupCall: lookupCall
  });

  return lookupCall
    .execute()
    .always(function() {
      this.original()._currentLookupCall = null;
      this.setLoading(false);
      this._clearLookupStatus();
      this._clearNoResultsErrorStatus();
    }.bind(this));
};

/**
 * Reset error status NO_RESULTS when a lookup is performed, otherwise it would interfere with the
 * temporary lookupStatus and we'd see an out-dated error-status message while the user is typing.
 */
scout.SmartField.prototype._clearNoResultsErrorStatus = function() {
  if (this.isTouchable()) {
    return;
  }
  if (this._userWasTyping && this.errorStatus && this.errorStatus.code === scout.SmartField.ErrorCode.NO_RESULTS) {
    this.setErrorStatus(null);
  }
};

/**
 * Returns true if the smart-field lookup returns a lot of rows. In that case
 * the proposal chooser must create a table with virtual scrolling, which means
 * only the rows visible in the UI are rendered in the DOM. By default we render
 * all rows, since this avoids problems with layout-invalidation with rows
 * that have a bitmap-image (PNG) which is loaded asynchronously.
 */
scout.SmartField.prototype.virtual = function() {
  return this.browseMaxRowCount > scout.SmartField.DEFAULT_BROWSE_MAX_COUNT;
};

scout.SmartField.prototype.isDropdown = function() {
  return this.displayStyle === scout.SmartField.DisplayStyle.DROPDOWN;
};

scout.SmartField.prototype._setLookupRow = function(lookupRow) {
  // remove css classes from old lookup-row
  if (this.lookupRow) {
    this.removeCssClass(this.lookupRow.cssClass);
  }

  this._setProperty('lookupRow', lookupRow);

  // add css classes from new lookup-row
  if (lookupRow) {
    this.addCssClass(lookupRow.cssClass);
  }
};

scout.SmartField.prototype.setLookupRow = function(lookupRow) {
  if (this.lookupRow === lookupRow) {
    return;
  }
  this._notUnique = false;
  this.clearErrorStatus();
  this._setLookupRow(lookupRow);
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

scout.SmartField.prototype.setDisplayText = function(displayText) {
  scout.SmartField.parent.prototype.setDisplayText.call(this, displayText);
  this._userWasTyping = false;
};

scout.SmartField.prototype.resetDisplayText = function() {
  var returned = this.formatValue(this.value);
  if (returned && $.isFunction(returned.promise)) {
    // Promise is returned -> set display text later
    returned
      .done(this._setAndRenderDisplayText.bind(this))
      .fail(function() {
        $.log.isInfoEnabled() && $.log.info('Could not resolve display text for value: ' + this.value);
      }.bind(this));
  } else {
    this._setAndRenderDisplayText(returned);
  }
};

/**
 * This method is very similar to setDisplayText(), but does _not_ check for equality with
 * the current value. The property is always set and (if the field is rendered) the given
 * display text is always rendered. This is important when resetting the display text,
 * because the visible text in the input field may differ from the "displayText" property
 * value. If setDisplayText() was used, the visible text would not always be reset.
 */
scout.SmartField.prototype._setAndRenderDisplayText = function(displayText) {
  this._setProperty('displayText', displayText);
  if (this.rendered) {
    this._renderDisplayText();
  }
};

scout.SmartField.prototype._getValueFromLookupRow = function(lookupRow) {
  return lookupRow.key;
};

scout.SmartField.prototype._setValue = function(value) {
  // set the cached lookup row to null. Keep in mind that the lookup row is set async in a timeout
  // must of the time. Thus we must remove the reference to the old lookup row as early as possible
  if (!this._lockLookupRow) {
    if (scout.objects.isNullOrUndefined(value)) {
      // when value is set to null, we must also reset the cached lookup row
      this._setLookupRow(null);
    } else {
      // when a value is set, we only keep the cached lookup row when the key of the lookup row is equals to the value
      if (this._checkResetLookupRow(value)) {
        this._setLookupRow(null);
      }
    }
  }
  scout.SmartField.parent.prototype._setValue.call(this, value);
  this._notUnique = false;
};

/**
 * Sub-classes like the proposal field may override this function to implement a different behavior.
 */
scout.SmartField.prototype._checkResetLookupRow = function(value) {
  return this.lookupRow && this.lookupRow.key !== value;
};

/**
 * This function may be overridden to return another value than this.value.
 * For instance the proposal field does'nt use the value but the key from the
 * lookup row for comparison.
 *
 * @returns the value used to find the selected element in a proposal chooser.
 */
scout.SmartField.prototype.getValueForSelection = function() {
  return this._showSelection() ? this.value : null;
};

scout.SmartField.prototype._showSelection = function() {
  if (scout.objects.isNullOrUndefined(this.value) ||
    scout.objects.isNullOrUndefined(this.lookupRow)) {
    return false;
  }

  var text;
  if (this.rendered) {
    // check if text matches (deal with multi-line)
    text = this._readDisplayText();
    var additionalLines = this.additionalLines();
    if (additionalLines) {
      text = [text].concat(additionalLines).join('\n');
    }
  } else {
    text = this.displayText;
  }

  return text === this.lookupRow.text;
};

/**
 * override to ensure dropdown fields and touch mode smart fields does not have a clear icon.
 */
scout.SmartField.prototype.isClearable = function() {
  return scout.SmartField.parent.prototype.isClearable.call(this) && !this.isDropdown() && !this.touchMode;
};

scout.SmartField.prototype._triggerLookupCallDone = function(result) {
  this.trigger('lookupCallDone', {
    result: result
  });
  return result;
};

scout.SmartField.prototype._triggerAcceptInputFail = function() {
  this._triggerAcceptInput(false, true);
};

scout.SmartField.prototype._triggerAcceptInput = function(acceptByLookupRow, failure) {
  this.trigger('acceptInput', {
    displayText: this.displayText,
    errorStatus: this.errorStatus,
    value: this.value,
    lookupRow: this.lookupRow,
    acceptByLookupRow: scout.nvl(acceptByLookupRow, true),
    failure: scout.nvl(failure, false)
  });
};

scout.SmartField.prototype._triggerAcceptByText = function(searchText) {
  this.trigger('acceptByText', {
    searchText: searchText,
    errorStatus: this.errorStatus
  });
};

/**
 * Function invoked if being rendered within a cell-editor (mode='scout.FormField.Mode.CELLEDITOR'), and once the editor finished its rendering.
 */
scout.SmartField.prototype.onCellEditorRendered = function(options) {
  if (options.openFieldPopup) {
    this._cellEditorPopup = options.cellEditorPopup;
    this.openPopup(!this.searchRequired);
  }
};

scout.SmartField.prototype.additionalLines = function() {
  var text = scout.nvl(this.displayText, ''),
    textLines = text.split('\n');
  if (textLines.length > 1) {
    textLines.shift();
    return textLines;
  } else {
    return null;
  }
};

scout.SmartField.prototype._createLoadingSupport = function() {
  return new scout.SimpleLoadingSupport({
    widget: this,
    loadingIndicatorDelay: 400 // ms
  });
};

/**
 * @override FormField.js
 */
scout.SmartField.prototype._isInitialShowStatus = function() {
  if (this.touchMode && (this._pendingOpenPopup || this.isPopupOpen())) {
    // Do not display a tooltip if the touch popup is open, the tooltip will be displayed there
    return false;
  }
  return scout.SmartField.parent.prototype._isInitialShowStatus.call(this);
};

/**
 * In touch mode, we must close the cell editor popup explicitly, because the touch-popup and its glasspane
 * prevents the cell editor popup from receiving mouse down events.
 */
scout.SmartField.prototype.acceptInputFromField = function(otherField) {
  this._copyValuesFromField(otherField);

  if (this._cellEditorPopup) {
    // this will call acceptInput on the touch smart-field (== this)
    this._cellEditorPopup.completeEdit();
    this._cellEditorPopup = null;
  } else {
    this.acceptInput();
  }
};

/**
 * This function is overridden by ProposalField because it has a different behavior than the smart-field.
 */
scout.SmartField.prototype._copyValuesFromField = function(otherField) {
  if (this.lookupRow !== otherField.lookupRow) {
    this.setLookupRow(otherField.lookupRow);
  }
  this.setErrorStatus(otherField.errorStatus);
  this.setDisplayText(otherField.displayText);
};

scout.SmartField.prototype._setNotUniqueError = function(searchText) {
  this.setErrorStatus(scout.Status.error({
    message: this.session.text('SmartFieldNotUnique', searchText),
    code: scout.SmartField.ErrorCode.NOT_UNIQUE
  }));
};

scout.SmartField.prototype._hasNotUniqueError = function(searchText) {
  return this._notUnique || this._hasUiError(scout.SmartField.ErrorCode.NOT_UNIQUE);
};

scout.SmartField.prototype._errorStatus = function() {
  return this.lookupStatus || this.errorStatus;
};

scout.SmartField.prototype.setLookupStatus = function(lookupStatus) {
  this.setProperty('lookupStatus', lookupStatus);
  if (this.rendered) {
    this._renderErrorStatus();
  }
};

scout.SmartField.prototype.clearErrorStatus = function() {
  this.setErrorStatus(null);
  this._clearLookupStatus();
};

scout.SmartField.prototype._clearLookupStatus = function() {
  this.setLookupStatus(null);
};

/**
 * Checks if there is a lookup status that needs to be set as error status
 * before we leave the smart-field. The lookup status is set to null, because
 * it is a temporary state that is only important while the user executes a lookup.
 */
scout.SmartField.prototype._flushLookupStatus = function() {
  if (!this.lookupStatus) {
    return;
  }

  if (this.lookupStatus.code === scout.SmartField.ErrorCode.NO_RESULTS ||
    this.lookupStatus.code === scout.SmartField.ErrorCode.NOT_UNIQUE) {
    var errorStatus = this.lookupStatus.clone();
    errorStatus.severity = scout.Status.Severity.ERROR;
    this.setErrorStatus(errorStatus);
  }

  this._clearLookupStatus();
};

scout.SmartField.prototype.requestInput = function() {
  if (this.enabledComputed && this.rendered) {
    this.focus();
    this.openPopup(!this.searchRequired);
  }
};
