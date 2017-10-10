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

  this.focused = false;
  this.clearable = false;
  this.popup = null;
  this.lookupCall = null;
  this.codeType = null;
  this._pendingLookup = null;
  this._pendingOpenPopup = false;
  this._lookupInProgress = false;
  this._tabPrevented = null;
  this.lookupRow = null;
  this.browseHierarchy = false;
  this.browseMaxRowCount = scout.SmartField2.DEFAULT_BROWSE_MAX_COUNT;
  this.browseAutoExpandAll = true;
  this.browseLoadIncremental = false;
  this.activeFilterEnabled = false;
  this.activeFilter = null;
  this.activeFilterLabels = [];
  this.columnDescriptors = null;
  this.displayStyle = scout.SmartField2.DisplayStyle.DEFAULT;
  this._userWasTyping = false; // used to detect whether the last thing the user did was typing (a proposal) or something else, like selecting a proposal row
  this._acceptInputEnabled = true; // used to prevent multiple execution of blur/acceptInput
  this._acceptInputDeferred = $.Deferred();
  this._notUnique = false; // used to store the error state 'not unique' which must not be showed while typing, but when the field loses focus
  this.lookupStatus = null;

  this._addCloneProperties(['lookupRow', 'codeType', 'lookupCall', 'activeFilter', 'activeFilterEnabled', 'activeFilterLabels',
    'browseHierarchy', 'browseMaxRowCount', 'browseAutoExpandAll', 'browseLoadIncremental'
  ]);
};
scout.inherits(scout.SmartField2, scout.ValueField);

scout.SmartField2.DisplayStyle = {
  DEFAULT: 'default',
  DROPDOWN: 'dropdown'
};

scout.SmartField2.ErrorCode = {
  NOT_UNIQUE: 1,
  NO_RESULTS: 2,
  NO_DATA: 3
};

scout.SmartField2.DEBOUNCE_DELAY = 200;

scout.SmartField2.DEFAULT_BROWSE_MAX_COUNT = 100;

/**
 * @see IContentAssistField#getActiveFilterLabels() - should have the same order.
 */
scout.SmartField2.ACTIVE_FILTER_VALUES = ['UNDEFINED', 'FALSE', 'TRUE'];

scout.SmartField2.prototype._init = function(model) {
  scout.SmartField2.parent.prototype._init.call(this, model);

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
scout.SmartField2.prototype._initValue = function(value) {
  this._setLookupCall(this.lookupCall);
  this._setCodeType(this.codeType);
  scout.SmartField2.parent.prototype._initValue.call(this, value);
};

scout.SmartField2.prototype._createKeyStrokeContext = function() {
  return new scout.InputFieldKeyStrokeContext();
};

scout.SmartField2.prototype._initKeyStrokeContext = function() {
  scout.SmartField2.parent.prototype._initKeyStrokeContext.call(this);

  this.keyStrokeContext.registerKeyStroke(new scout.SmartField2CancelKeyStroke(this));
};

scout.SmartField2.prototype._render = function() {
  this.addContainer(this.$parent, 'has-icon ' + this.cssClassName(), new scout.SmartField2Layout(this));
  this.addLabel();

  var fieldFunc = this.isDropdown() ? scout.fields.makeInputDiv : scout.fields.makeInputOrDiv;
  var $field = fieldFunc.call(scout.fields, this)
    .on('mousedown', this._onFieldMouseDown.bind(this));

  if (!this.touch) {
    $field
      .blur(this._onFieldBlur.bind(this))
      .focus(this._onFieldFocus.bind(this))
      .keyup(this._onFieldKeyUp.bind(this))
      .keydown(this._onFieldKeyDown.bind(this))
      .on('input', this._onInputChanged.bind(this));
  }
  this.addField($field);

  if (!this.embedded) {
    this.addMandatoryIndicator();
  }
  this.addIcon();
  this.$icon.addClass('needsclick');
  this.addClearIcon();
  this.addStatus();
};

scout.SmartField2.prototype._renderProperties = function() {
  scout.SmartField2.parent.prototype._renderProperties.call(this);
  this._renderClearable();
};

scout.SmartField2.prototype.cssClassName = function() {
  var prefix = this.displayStyle;
  if (this.displayStyle === scout.SmartField2.DisplayStyle.DEFAULT) {
    prefix = 'smart';
  }
  return prefix + '-field';
};

scout.SmartField2.prototype._readSearchText = function() {
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

scout.SmartField2.prototype._readDisplayText = function() {
  return scout.fields.valOrText(this.$field);
};

scout.SmartField2.prototype._renderDisplayText = function() {
  var displayText = scout.nvl(this.displayText, ''),
    textLines = displayText.split('\n');
  if (textLines.length) {
    displayText = textLines[0];
  }
  scout.fields.valOrText(this.$field, displayText);
  this._updateClearable();
};

/**
 * Accepts the selected lookup row and sets its id as value.
 * This function is called on blur, by a keystroke or programmatically at any time.
 */
scout.SmartField2.prototype.acceptInput = function() {
  if (!this._acceptInputEnabled) {
    $.log.trace('(SmartField2#acceptInput) Skipped acceptInput because _acceptInputEnabled=false');
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
    searchTextChanged = this._checkDisplayTextChanged(searchText),
    selectedLookupRow = this.isPopupOpen() ? this.popup.getSelectedLookupRow() : null;

  this._setProperty('displayText', searchText);
  this._acceptInputDeferred = $.Deferred();
  this._flushLookupStatus();

  // in case the user has typed something after he has selected a lookup row
  // --> ignore the selection.
  if (this._userWasTyping) {
    selectedLookupRow = null;
  }

  // abort pending lookups
  if (this._pendingLookup) {
    clearTimeout(this._pendingLookup);
    this._pendingLookup = null;
  }

  if (this.touch) {
    $.log.debug('(SmartField2#acceptInput) Always send acceptInput for touch field');
    this._inputAccepted();
    return;
  }

  return this._acceptInput(searchText, searchTextEmpty, searchTextChanged, selectedLookupRow);
};

/**
 * This function is intended to be overridden. Proposal field has another behavior than the smart field.
 */
scout.SmartField2.prototype._acceptInput = function(searchText, searchTextEmpty, searchTextChanged, selectedLookupRow) {
  if (this._notUnique) {
    this._setNotUniqueError(searchText);
  }

  // Do nothing when search text is equals to the text of the current lookup row
  if (!selectedLookupRow && this.lookupRow) {
    var lookupRowText = scout.strings.nvl(this.lookupRow.text);
    if (lookupRowText === searchText) {
      $.log.debug('(SmartField2#acceptInput) unchanged: text is equals. Close popup');
      this._inputAccepted(false);
      return;
    }
  }

  // Do nothing when we don't have a current lookup row and search text is empty
  // trigger event when search text has changed. This is required for the case where
  // a field is cleared, and the remote model must be updated (value=null)
  if (!selectedLookupRow && !this.lookupRow && searchTextEmpty) {
    $.log.debug('(SmartField2#acceptInput) unchanged: text is empty. Close popup');
    this.clearErrorStatus();
    this._inputAccepted(searchTextChanged);
    return;
  }

  // 1.) when search text is empty and no lookup-row is selected, simply set the value to null
  // Note: here we assume that a current lookup row is set.
  if (!selectedLookupRow && searchTextEmpty) {
    $.log.debug('(SmartField2#acceptInput) empty. Set lookup-row to null, close popup');
    this.clearErrorStatus();
    this.setLookupRow(null);
    this._inputAccepted();
    return;
  }

  // 2.) proposal chooser is open -> use the selected row as value
  if (selectedLookupRow) {
    $.log.debug('(SmartField2#acceptInput) lookup-row selected. Set lookup-row, close popup lookupRow=', selectedLookupRow.toString());
    this.clearErrorStatus();
    this.setLookupRow(selectedLookupRow);
    this._inputAccepted();
    return;
  }

  // 3.) proposal chooser is not open -> try to accept the current display text
  // this causes a lookup which may fail and open a new proposal chooser (property
  // change for 'result').
  if (searchTextChanged || this._userWasTyping) {
    this._acceptByText(this._firstTextLine(searchText));
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
scout.SmartField2.prototype._firstTextLine = function(text) {
  if (scout.strings.empty(text)) {
    return text;
  }
  return text.split('\n')[0];
};

/**
 * This function is intended to be overridden. Proposal field has another behavior than the smart field.
 */
scout.SmartField2.prototype._acceptByText = function(searchText) {
  $.log.debug('(SmartField2#_acceptByText) searchText=', searchText);
  this._executeLookup(this.lookupCall.getByText.bind(this.lookupCall, searchText))
    .done(this._acceptByTextDone.bind(this));
};

scout.SmartField2.prototype._inputAccepted = function(triggerEvent, acceptByLookupRow) {
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

scout.SmartField2.prototype._focusNextTabbable = function() {
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
    $tabElements.eq(nextIndex).focus();
    this._tabPrevented = null;
  }
};

scout.SmartField2.prototype._acceptByTextDone = function(result) {
  this._userWasTyping = false;
  this._extendResult(result);
  this._notUnique = result.numLookupRows > 1;

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

  result.empty = (result.numLookupRows === 0);
};

scout.SmartField2.prototype._acceptInputFail = function(result) {
  var searchText = result.searchText;

  // in any other case something went wrong
  if (result.empty) {
    if (!this.embedded) {
      this.closePopup();
    }
    this.setValue(null);
    this.setDisplayText(searchText);
    this.setErrorStatus(scout.Status.error({
      message: this.session.text('SmartFieldCannotComplete', searchText),
      code: scout.SmartField2.ErrorCode.NO_RESULTS
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

scout.SmartField2.prototype.lookupByRec = function(rec) {
  $.log.debug('(SmartField2#lookupByRec) rec=', rec);
  return this._executeLookup(this.lookupCall.getByRec.bind(this.lookupCall, rec))
    .then(function(result) {

      // Since this function is only used for hierarchical trees we
      // can simply set the appendResult flag always to true here
      result.appendResult = true;
      result.rec = rec;

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
  if (lookupCall instanceof scout.LookupCall) {
    // NOP - required to distinct instance from plain object (=model)
  } else if (scout.objects.isPlainObject(lookupCall)) {
    lookupCall.session = this.session;
    lookupCall = scout.create(lookupCall);
  } else if (typeof lookupCall === 'string') {
    lookupCall = scout.create(lookupCall, {
      session: this.session
    });
  }
  this._setProperty('lookupCall', lookupCall);
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
    return this._formatLookupRow(this.lookupRow);
  }

  // we must do a lookup first to get the display text
  // Note: this has a side-effect as it sets the property lookupRow on the smart field
  return this._executeLookup(this.lookupCall.getByKey.bind(this.lookupCall, value))
    .then(this._lookupByKeyDone.bind(this));
};

scout.SmartField2.prototype._lookupByKeyDone = function(lookupRow) {
  this._notUnique = false;
  this.setLookupRow(lookupRow);
  return this._formatLookupRow(lookupRow);
};

/**
 * This function is called when we need to format a display text from a given lookup
 * row. By default the property 'text' is used for that purpose. Override this function
 * if you need to format different properties from the lookupRow.
 */
scout.SmartField2.prototype._formatLookupRow = function(lookupRow) {
  return lookupRow ? lookupRow.text : '';
};

/**
 * @param {boolean} [browse] whether or not the lookup call should execute getAll() or getByText() with the current display text.
 *     if browse is undefined, browse is set to true automatically if search text is empty
 * @returns {Promise}
 */
scout.SmartField2.prototype.openPopup = function(browse) {
  var searchText = this._readDisplayText();
  $.log.info('SmartField2#openPopup browse=' + browse + ' searchText=' + searchText + ' popup=' + this.popup + ' pendingOpenPopup=' + this._pendingOpenPopup);

  // Reset scheduled focus next tabbable when user clicks on the smartfield while a lookup is resolved.
  this._tabPrevented = null;
  this._pendingOpenPopup = true;

  if (scout.strings.empty(searchText)) {
    // if search text is empty - always do 'browse', no matter what the error code is
    browse = true;
  } else if (this.errorStatus && !this._hasUiError(scout.SmartField2.ErrorCode.NO_RESULTS)) {
    // In case the field is invalid, we always want to start a lookup with the current display text
    // unless the error was 'no results' because in that case it would be pointless to search for that text
    browse = false;
  }

  return this._lookupByTextOrAll(browse, searchText);
};

scout.SmartField2.prototype._hasUiError = function(codes) {
  var status = this._errorStatus();

  if (!status) {
    return false;
  }

  if (codes) {
    codes = scout.arrays.ensure(codes);
  } else {
    codes = [scout.SmartField2.ErrorCode.NO_RESULTS, scout.SmartField2.ErrorCode.NOT_UNIQUE];
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
scout.SmartField2.prototype._lookupByTextOrAll = function(browse, searchText) {
  // default values
  searchText = scout.nvl(searchText, this._readDisplayText());
  browse = scout.nvl(browse, scout.strings.empty(searchText));

  // never do a text-lookup if field has dropdown style
  if (this.isDropdown()) {
    browse = true;
  }

  // clear pending lookup
  if (this._pendingLookup) {
    clearTimeout(this._pendingLookup);
    this._pendingLookup = null;
  }

  var deferred = $.Deferred();
  var doneHandler = function(result) {
    result.browse = browse;
    this._lookupByTextOrAllDone(result);
    deferred.resolve(result);
  }.bind(this);

  // execute lookup byAll immediately
  if (browse) {
    $.log.debug('(SmartField2#_lookupByTextOrAll) lookup byAll (seachText empty)');
    this._executeLookup(this.lookupCall.getAll.bind(this.lookupCall))
      .done(doneHandler);
  } else {
    // execute lookup byText with a debounce/delay
    this._pendingLookup = setTimeout(function() {
      $.log.debug('(SmartField2#_lookupByTextOrAll) lookup byText searchText=' + searchText);
      this._executeLookup(this.lookupCall.getByText.bind(this.lookupCall, searchText))
        .done(doneHandler);
    }.bind(this), scout.SmartField2.DEBOUNCE_DELAY);
  }

  return deferred.promise();
};

scout.SmartField2.prototype._lookupByTextOrAllDone = function(result) {
  this._extendResult(result);
  this._notUnique = !result.browse && result.numLookupRows > 1;

  // Oops! Something went wrong while the lookup has been processed.
  if (result.exception) {
    this.setErrorStatus(scout.Status.error({
      message: result.exception
    }));
    this.closePopup();
    return;
  }

  // In cases where the user has tabbed to the next field, while results for the previous
  // smart-field are still loading: don't show the proposal popup. In the case of a cell-editor
  // it's also possible that the smart-field is not rendered anymore when the lookup is done
  if (!this.rendered ||
    !this.isFocused() && !this.touch && !this.embedded) {
    this.closePopup();
    return;
  }

  // 'No data' case
  if (result.empty && result.browse) {
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

    this.setLookupStatus(scout.Status.warn({
      message: this.session.text('SmartFieldNoDataFound'),
      code: scout.SmartField2.ErrorCode.NO_DATA
    }));
    return;
  }

  if (result.empty) {
    this._handleEmptyResult();
    this.setLookupStatus(scout.Status.warn({
      message: this.session.text('SmartFieldCannotComplete', result.searchText),
      code: scout.SmartField2.ErrorCode.NO_RESULTS
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

scout.SmartField2.prototype._ensurePopup = function(result, status) {
  if (this.popup) {
    this.popup.setLookupResult(result);
    this.popup.setStatus(status);
  } else {
    this._renderPopup(result, status);
  }
};

scout.SmartField2.prototype._handleEmptyResult = function() {
  if (this.touch || this.activeFilterEnabled) {
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

scout.SmartField2.prototype._renderPopup = function(result, status) {
  // On touch devices the field does not get the focus.
  // But it should look focused when the popup is open.
  this.$field.addClass('focused');
  this.$container.addClass('popup-open');

  var useTouch = this.touch && !this.isDropdown();
  var popupType = useTouch ? 'SmartField2TouchPopup' : 'SmartField2Popup';
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
    }
  }.bind(this));
};

scout.SmartField2.prototype.isFocused = function() {
  return this.rendered && scout.focusUtils.isActiveElement(this.$field);
};

scout.SmartField2.prototype.closePopup = function() {
  this._pendingOpenPopup = false;
  if (this.popup) {
    this.popup.close();
  }
};

/**
 * Calls acceptInput if mouse down happens outside of the field or popup
 * @override
 */
scout.SmartField2.prototype.aboutToBlurByMouseDown = function(target) {
  var eventOnField = this.$field.isOrHas(target) || this.$icon.isOrHas(target) || this.$clearIcon.isOrHas(target);
  var eventOnPopup = this.popup && this.popup.$container.isOrHas(target);
  if (!eventOnField && !eventOnPopup) {
    this.acceptInput(); // event outside this value field
  }
};

scout.SmartField2.prototype._onFieldMouseDown = function(event) {
  $.log.debug('(SmartField2#_onFieldMouseDown)');
  if (!this.enabledComputed) {
    return;
  }
  if (!this.isDropdown() && !scout.fields.handleOnClick(this)) {
    return;
  }
  this.$field.focus(); // required for touch case where field is a DIV
  this.togglePopup();
};

scout.SmartField2.prototype._onIconMouseDown = function(event) {
  $.log.debug('(SmartField2#_onIconMouseDown)');
  if (!this.enabledComputed) {
    return;
  }

  event.preventDefault();

  var clearable = this.clearable;
  this.$field.focus();
  if (clearable) {
    this.clear();
    return;
  }

  if (!this.embedded) {
    if (this.isDropdown()) {
      this.togglePopup();
    } else if (!this.popup) {
      this.openPopup(true);
    }
  }
};

scout.SmartField2.prototype._onClearIconMouseDown = function(event) {
  $.log.debug('(SmartField2#_onClearIconMouseDown)');
  if (!this.enabledComputed) {
    return;
  }
  event.preventDefault();
  this.$field.focus();
  this.clear();
};

scout.SmartField2.prototype._clear = function() {
  this._userWasTyping = true;
  this.$field.val('');
  if (this.isPopupOpen()) {
    // When cleared, browse by all again, need to do it in setTimeout because sending acceptInput and lookupAll at the same time does not seem to work
    setTimeout(this._lookupByTextOrAll.bind(this, true));
  }
  this._updateClearable();
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
  this.setFocused(false);
  this.setLoading(false);
  if (this.embedded) {
    return;
  }
  scout.SmartField2.parent.prototype._onFieldBlur.call(this, event);
  this.closePopup();
};

scout.SmartField2.prototype._onFieldFocus = function(event) {
  this.setFocused(true);
};

scout.SmartField2.prototype._onInputChanged = function(event) {
  this._updateClearable();
};

scout.SmartField2.prototype._onFieldKeyUp = function(event) {
  // Escape
  if (event.which === scout.keys.ESC) {
    return;
  }

  // Pop-ups shouldn't open when one of the following keys is pressed
  var w = event.which;
  var pasteShortcut = event.ctrlKey && w === scout.keys.V;

  if (!pasteShortcut && (
      event.ctrlKey || event.altKey ||
      w === scout.keys.ENTER ||
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
  if (this._pendingOpenPopup || this.isPopupOpen()) {
    if (!this.isDropdown()) {
      this._lookupByTextOrAll();
    }
  } else if (!this._pendingOpenPopup) {
    this.openPopup();
  }
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
  return !!(this.popup && !this.popup.removalPending);
};

scout.SmartField2.prototype._onFieldKeyDown = function(event) {
  this._updateUserWasTyping(event);

  // We must prevent default focus handling
  if (event.which === scout.keys.TAB) {
    if (this.mode === scout.FormField.Mode.DEFAULT) {
      event.preventDefault(); // prevent browser default TAB behavior
      event.stopPropagation(); // prevent FocusContext#._onKeyDown
      $.log.debug('(SmartField2#_onFieldKeyDown) set _tabPrevented');
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

  // If field has dropdown style, we open the popup immediately
  // because we must not wait until text has been typed
  if (this._isNavigationKey(event) || this.isDropdown()) {
    if (this.isPopupOpen()) {
      this.popup.delegateKeyEvent(event);
    } else if (!this._pendingOpenPopup) {
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

scout.SmartField2.prototype._handleEnterKey = function(event) {
  if (this.isPopupOpen()) {
    this.popup.selectLookupRow();
    event.stopPropagation();
  }
};

scout.SmartField2.prototype._isFunctionKey = function(event) {
  return event.which >= scout.keys.F1 && event.which < scout.keys.F12;
};

scout.SmartField2.prototype._onLookupRowSelected = function(event) {
  this.setLookupRow(event.lookupRow);
  this._inputAccepted();
  this._updateClearable();
  this.closePopup();
};

scout.SmartField2.prototype._onActiveFilterSelected = function(event) {
  this.setActiveFilter(event.activeFilter);
  this._lookupByTextOrAll(true);
};

scout.SmartField2.prototype.setBrowseMaxRowCount = function(browseMaxRowCount) {
  this.setProperty('browseMaxRowCount', browseMaxRowCount);
};

scout.SmartField2.prototype.setBrowseAutoExpandAll = function(browseAutoExpandAll) {
  this.setProperty('browseAutoExpandAll', browseAutoExpandAll);
};

scout.SmartField2.prototype.setBrowseLoadIncremental = function(browseLoadIncremental) {
  this.setProperty('browseLoadIncremental', browseLoadIncremental);
  if (this.lookupCall) {
    this.lookupCall.setLoadIncremental(browseLoadIncremental);
  }
};

scout.SmartField2.prototype.setActiveFilter = function(activeFilter) {
  this.setProperty('activeFilter', this.activeFilterEnabled ? activeFilter : null);
};

scout.SmartField2.prototype.setActiveFilterEnabled = function(activeFilterEnabled) {
  this.setProperty('activeFilterEnabled', activeFilterEnabled);
};

/**
 * A wrapper function around lookup calls used to set the _lookupInProgress flag, and display the state in the UI.
 */
scout.SmartField2.prototype._executeLookup = function(lookupFunc) {
  this._lookupInProgress = true;
  this.setLoading(true);
  return lookupFunc()
    .always(function() {
      this._lookupInProgress = false;
      this.setLoading(false);
      this.clearErrorStatus();
    }.bind(this));
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

scout.SmartField2.prototype.resetDisplayText = function() {
  this._setProperty('displayText', this._formatValue(this.value));
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
    if (scout.objects.isNullOrUndefined(value)) {
      // when value is set to null, we must also reset the cached lookup row
      this._setLookupRow(null);
    } else {
      // when a value is set, we only keep the cached lookup row when the key of the lookup row is equals to the value
      if (this.lookupRow && this.lookupRow.key !== value) {
        this._setLookupRow(null);
      }
    }
  }
  scout.SmartField2.parent.prototype._setValue.call(this, value);
  this._notUnique = false;
};

/**
 * This function may be overridden to return another value than this.value.
 * For instance the proposal field does'nt use the value but the key from the
 * lookup row for comparison.
 *
 * @returns the value used to find the selected element in a proposal chooser.
 */
scout.SmartField2.prototype.getValueForSelection = function() {
  return this._showSelection() ? this.value : null;
};

scout.SmartField2.prototype._showSelection = function() {
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

scout.SmartField2.prototype.setFocused = function(focused) {
  this.setProperty('focused', focused);
};

scout.SmartField2.prototype._renderFocused = function() {
  this._updateClearable();
};

scout.SmartField2.prototype._updateClearable = function() {
  if (this.touch) {
    return;
  }
  if (!this.$field) {
    return;
  }
  if (this.isDropdown()) {
    this.setClearable(false);
    return;
  }
  var clearable = scout.strings.hasText(this._readDisplayText());
  if (!this.embedded) {
    clearable = clearable && this.focused;
  }
  this.setClearable(clearable);
};

scout.SmartField2.prototype._triggerAcceptInputFail = function() {
  this._triggerAcceptInput(false, true);
};

scout.SmartField2.prototype._triggerAcceptInput = function(acceptByLookupRow, failure) {
  this.trigger('acceptInput', {
    displayText: this.displayText,
    errorStatus: this.errorStatus,
    value: this.value,
    lookupRow: this.lookupRow,
    acceptByLookupRow: scout.nvl(acceptByLookupRow, true),
    failure: scout.nvl(failure, false)
  });
};

/**
 * Function invoked if being rendered within a cell-editor (mode='scout.FormField.Mode.CELLEDITOR'), and once the editor finished its rendering.
 */
scout.SmartField2.prototype.onCellEditorRendered = function(options) {
  if (options.openFieldPopup) {
    this._cellEditorPopup = options.cellEditorPopup;
    this.openPopup(true);
  }
};

scout.SmartField2.prototype.additionalLines = function() {
  var text = scout.nvl(this.displayText, ''),
    textLines = text.split('\n');
  if (textLines.length > 1) {
    textLines.shift();
    return textLines;
  } else {
    return null;
  }
};

scout.SmartField2.prototype._createLoadingSupport = function() {
  return new scout.SimpleLoadingSupport({
    widget: this,
    loadingIndicatorDelay: 400 // ms
  });
};

/**
 * @override FormField.js
 */
scout.SmartField2.prototype._showStatusMessage = function() {
  if (this.touch && (this._pendingOpenPopup || this.isPopupOpen())) {
    // Do not display a tooltip if the touch popup is open, the tooltip will be displayed there
    return;
  }
  scout.SmartField2.parent.prototype._showStatusMessage.call(this);
};

/**
 * In touch mode, we must close the cell editor popup explicitly, because the touch-popup and its glasspane
 * prevents the cell editor popup from receiving mouse down events.
 */
scout.SmartField2.prototype.acceptInputFromField = function(otherField) {
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
scout.SmartField2.prototype._copyValuesFromField = function(otherField) {
  if (this.lookupRow !== otherField.lookupRow) {
    this.setLookupRow(otherField.lookupRow);
  }
  this.setErrorStatus(otherField.errorStatus);
  this.setDisplayText(otherField.displayText);
};

scout.SmartField2.prototype._setNotUniqueError = function(searchText) {
  this.setErrorStatus(scout.Status.error({
    message: this.session.text('SmartFieldNotUnique', searchText),
    code: scout.SmartField2.ErrorCode.NOT_UNIQUE
  }));
};

scout.SmartField2.prototype._hasNotUniqueError = function(searchText) {
  return this._notUnique || this._hasUiError(scout.SmartField2.ErrorCode.NOT_UNIQUE);
};

scout.SmartField2.prototype._errorStatus = function() {
  return this.lookupStatus || this.errorStatus;
};

scout.SmartField2.prototype.setLookupStatus = function(lookupStatus) {
  this.setProperty('lookupStatus', lookupStatus);
  if (this.rendered) {
    this._renderErrorStatus();
  }
};

scout.SmartField2.prototype.clearErrorStatus = function() {
  this.setErrorStatus(null);
  this._clearLookupStatus();
};

scout.SmartField2.prototype._clearLookupStatus = function() {
  this.setLookupStatus(null);
};

/**
 * Checks if there is a lookup status that needs to be set as error status
 * before we leave the smart-field. The lookup status is set to null, because
 * it is a temporary state that is only important while the user executes a lookup.
 */
scout.SmartField2.prototype._flushLookupStatus = function() {
  if (!this.lookupStatus) {
    return;
  }

  if (this.lookupStatus.code === scout.SmartField2.ErrorCode.NO_RESULTS ||
    this.lookupStatus.code === scout.SmartField2.ErrorCode.NOT_UNIQUE) {
    var errorStatus = this.lookupStatus.clone();
    errorStatus.severity = scout.Status.Severity.ERROR;
    this.setErrorStatus(errorStatus);
  }

  this._clearLookupStatus();
};
