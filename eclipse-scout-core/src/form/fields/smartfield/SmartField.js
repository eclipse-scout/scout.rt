/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays, Device, fields, FormField, InputFieldKeyStrokeContext, keys, LookupCall, objects, QueryBy, scout, SimpleLoadingSupport, SmartFieldCancelKeyStroke, SmartFieldLayout, Status, strings, ValueField} from '../../../index';
import $ from 'jquery';

export default class SmartField extends ValueField {

  constructor() {
    super();

    this.popup = null;
    this.lookupCall = null;
    this.codeType = null;
    this._pendingLookup = null;
    this._pendingOpenPopup = false;
    this._tabPrevented = null;
    this.lookupRow = null;
    this.browseHierarchy = false;
    this.browseMaxRowCount = SmartField.DEFAULT_BROWSE_MAX_COUNT; // a positive number, _not_ null or undefined!
    this.browseAutoExpandAll = true;
    this.browseLoadIncremental = false;
    this.searchRequired = false;
    this.activeFilterEnabled = false;
    this.activeFilter = null;
    this.activeFilterLabels = [];
    this.columnDescriptors = null;
    this.displayStyle = SmartField.DisplayStyle.DEFAULT;
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
    this.initActiveFilter = null;
    this.disabledCopyOverlay = true;
    this.maxLength = 500;
    this.maxLengthHandler = scout.create('MaxLengthHandler', {target: this});

    this._addCloneProperties(['lookupRow', 'codeType', 'lookupCall', 'activeFilter', 'activeFilterEnabled', 'activeFilterLabels',
      'browseHierarchy', 'browseMaxRowCount', 'browseAutoExpandAll', 'browseLoadIncremental', 'searchRequired', 'columnDescriptors',
      'displayStyle'
    ]);
  }

  static DisplayStyle = {
    DEFAULT: 'default',
    DROPDOWN: 'dropdown'
  };

  static ErrorCode = {
    NOT_UNIQUE: 1,
    NO_RESULTS: 2,
    NO_DATA: 3,
    SEARCH_REQUIRED: 4
  };

  static DEBOUNCE_DELAY = 200;

  static DEFAULT_BROWSE_MAX_COUNT = 100;

  /**
   * @see "IContentAssistField#getActiveFilterLabels()" - should have the same order.
   */
  static ACTIVE_FILTER_VALUES = ['UNDEFINED', 'FALSE', 'TRUE'];

  _init(model) {
    super._init(model);

    if (this.activeFilterLabels.length === 0) {
      this.activeFilterLabels = [
        this.session.text('ui.All'),
        this.session.text('ui.Inactive'),
        this.session.text('ui.Active')
      ];
    }

    fields.initTouch(this, model);
  }

  /**
   * Initializes lookup call and code type before calling set value.
   * This cannot be done in _init because the value field would call _setValue first
   */
  _initValue(value) {
    this._setLookupCall(this.lookupCall);
    this._setCodeType(this.codeType);
    this._setLookupRow(this.lookupRow);
    super._initValue(value);
  }

  markAsSaved() {
    super.markAsSaved();
    this.setInitActiveFilter(this.activeFilter);
  }

  resetValue() {
    super.resetValue();
    this.setActiveFilter(this.initActiveFilter);
  }

  _createKeyStrokeContext() {
    return new InputFieldKeyStrokeContext();
  }

  _initKeyStrokeContext() {
    super._initKeyStrokeContext();
    this.keyStrokeContext.registerKeyStroke(new SmartFieldCancelKeyStroke(this));
  }

  _render() {
    this.addContainer(this.$parent, 'has-icon ' + this.cssClassName(), new SmartFieldLayout(this));
    this.addLabel();

    let fieldFunc = this.isDropdown() ? fields.makeInputDiv : fields.makeInputOrDiv;
    let $field = fieldFunc.call(scout.fields, this)
      .on('mousedown', this._onFieldMouseDown.bind(this));

    if (!this.touchMode) {
      $field
        .keyup(this._onFieldKeyUp.bind(this))
        .keydown(this._onFieldKeyDown.bind(this))
        .on('input', this._onFieldInput.bind(this));
    }
    this.addField($field);
    this.maxLengthHandler.install($field);

    if (!this.embedded) {
      this.addMandatoryIndicator();
    }
    this.addIcon();
    this.addStatus();
  }

  _renderProperties() {
    super._renderProperties();
    this._renderMaxLength();
  }

  _renderGridData() {
    super._renderGridData();
    this.updateInnerAlignment({
      useHorizontalAlignment: !this.browseHierarchy
    });
  }

  _renderGridDataHints() {
    super._renderGridDataHints();
    this.updateInnerAlignment({
      useHorizontalAlignment: !this.browseHierarchy
    });
  }

  cssClassName() {
    let prefix = this.displayStyle;
    if (this.displayStyle === SmartField.DisplayStyle.DEFAULT) {
      prefix = 'smart';
    }
    return prefix + '-field';
  }

  _readSearchText() {
    let fieldText = this._readDisplayText(),
      displayText = scout.nvl(this.displayText, ''),
      textLines = displayText.split('\n');

    if (textLines.length === 1 || strings.empty(fieldText)) {
      return fieldText;
    }
    textLines.shift(); // remove first line
    arrays.insert(textLines, fieldText, 0);
    return strings.join('\n', textLines);
  }

  _readDisplayText() {
    return fields.valOrText(this.$field);
  }

  _renderDisplayText() {
    let displayText = this._prepareDisplayText();
    fields.valOrText(this.$field, displayText);
    super._renderDisplayText();
  }

  _prepareDisplayText() {
    let displayText = scout.nvl(this.displayText, ''),
      textLines = displayText.split('\n');
    if (textLines.length) {
      displayText = textLines[0];
    }
    return displayText;
  }

  /**
   * Accepts the selected lookup row and sets its id as value.
   * This function is called on blur, by a keystroke or programmatically at any time.
   *
   * @param [sync] optional boolean value (default: false), when set to true acceptInput is not allowed to start an asynchronous lookup for text search
   */
  acceptInput(sync) {
    if (!this._acceptInputEnabled) {
      $.log.isTraceEnabled() && $.log.trace('(SmartField#acceptInput) Skipped acceptInput because _acceptInputEnabled=false');
      return this._acceptInputDeferred.promise();
    }

    // Use a timeout to prevent multiple execution within the same user action
    this._acceptInputEnabled = false;
    setTimeout(() => {
      this._acceptInputEnabled = true;
    });

    let
      searchText = this._readSearchText(),
      searchTextEmpty = strings.empty(searchText),
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
  }

  /**
   * This function is used to determine if the currently selected lookup row can be
   * used when acceptInput is called. Basically we don't want to use the row in case
   * the result is out-dated.
   */
  _getSelectedLookupRow(searchTextChanged) {
    // don't use selected lookup row if...
    if (!this.isPopupOpen() || // 1. popup has been closed
      (searchTextChanged && this._userWasTyping)) { // 2. search text has changed and user was typing
      return null;
    }
    // 3. if the result row is from an out-dated result
    return this.lookupSeqNo === this.popup.lookupResult.seqNo ?
      this.popup.getSelectedLookupRow() : null;
  }

  _checkSearchTextChanged(searchText) {
    if (this.isDropdown() || !this._userWasTyping) {
      return false; // search text cannot change in drop-down fields
    }

    // check if search text has changed since the last search, when it has changed
    // we cannot use the currently selected lookup row, because these proposals are
    // out-dated.
    return !this._searchTextEquals(searchText, this._lastSearchText);
  }

  _searchTextEquals(searchText, lastSearchText) {
    let a = strings.nullIfEmpty(this._firstTextLine(searchText));
    let b = strings.nullIfEmpty(lastSearchText);
    return strings.equalsIgnoreCase(a, b);
  }

  _clearPendingLookup() {
    if (this._pendingLookup) {
      clearTimeout(this._pendingLookup);
      this._pendingLookup = null;
    }
  }

  /**
   * This function is intended to be overridden. Proposal field has another behavior than the smart field.
   *
   * @param [sync] optional boolean value (default: false), when set to true acceptInput is not allowed to start an asynchronous lookup for text search
   */
  _acceptInput(sync, searchText, searchTextEmpty, searchTextChanged, selectedLookupRow) {

    let unchanged = false;
    if (this.removing) {
      // Rare case: _acceptInput may be called when the field is being removed. In that case
      // we do nothing and leave the lookupRow unchanged.
      unchanged = true;
    } else if (!selectedLookupRow && this.lookupRow) {
      // Do nothing when search text is equals to the text of the current lookup row
      let lookupRowText = strings.nvl(this.lookupRow.text);
      unchanged = lookupRowText === searchText;
    }

    if (unchanged) {
      $.log.isDebugEnabled() && $.log.debug('(SmartField#_acceptInput) unchanged: widget is removing or searchText is equals. Close popup');
      this._clearLookupStatus();
      this._inputAccepted(false);
      return;
    }

    // Don't show the not-unique error when the search-text becomes empty while typing (see ticket #229775)
    if (this._notUnique && !searchTextEmpty) {
      this._setNotUniqueError(searchText);
    }

    // Do nothing when we don't have a current lookup row and search text is empty
    // trigger event when search text has changed. This is required for the case where
    // a field is cleared, and the remote model must be updated (value=null)
    if (!selectedLookupRow && !this.lookupRow && searchTextEmpty) {
      $.log.isDebugEnabled() && $.log.debug('(SmartField#_acceptInput) unchanged: text is empty. Close popup');
      this._clearLookupStatus();
      if (this.errorStatus && this.errorStatus.code === SmartField.ErrorCode.NO_RESULTS) {
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
      // even though there's nothing to do, someone could wait for our promise to be resolved
      this._acceptInputDeferred.resolve();
    }

    return this._acceptInputDeferred.promise();
  }

  /**
   * Required for multiline smart-field. Only use first line of search text for accept by text.
   * Note: for the regular lookup by text, we use the readDisplayText() function which always
   * returns a single line. But in acceptInput we need the full search text (=display text + additional
   * lines) in order to check whether or not the display text has changed, compared to the current
   * lookup row. That's why we must extract the first line here.
   */
  _firstTextLine(text) {
    if (strings.empty(text)) {
      return text;
    }
    return text.split('\n')[0];
  }

  /**
   * This function is intended to be overridden. Proposal field has another behavior than the smart field.
   *
   * @param sync when set to true it's not allowed to start an asynchronous lookup to search by text, the
   *     current search text is discarded. The flag is set to true in case we click on another field, where
   *     we must make sure the order of (browser) events is not changed by the lookup that would return _after_
   *     the events for the clicked field are handled.
   */
  _acceptByText(sync, searchText) {
    sync = scout.nvl(sync, false);
    $.log.isDebugEnabled() && $.log.debug('(SmartField#_acceptByText) sync=' + sync + ' searchText=', searchText);

    if (sync) {
      this._acceptByTextSync(searchText);
    } else {
      this._acceptByTextAsync(searchText);
    }
  }

  _acceptByTextSync(searchText) {
    this._lastSearchText = null;
    this._inputAccepted();
    if (!this._hasUiError()) {
      this.resetDisplayText();
    }
  }

  _acceptByTextAsync(searchText) {
    this._lastSearchText = searchText;
    this._executeLookup(this.lookupCall.cloneForText(searchText), true)
      .done(this._acceptByTextDone.bind(this))
      .done(this._triggerLookupCallDone.bind(this));
    this._triggerAcceptByText(searchText);
  }

  _inputAccepted(triggerEvent, acceptByLookupRow) {
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
  }

  _focusNextTabbable() {
    if (this._tabPrevented) {
      let $tabElements = this.entryPoint().find(':tabbable'),
        direction = this._tabPrevented.shiftKey ? -1 : 1,
        fieldIndex = $tabElements.index(this.$field),
        nextIndex = fieldIndex + direction;

      if (nextIndex < 0) {
        nextIndex = $tabElements.length - 1;
      } else if (nextIndex >= $tabElements.length) {
        nextIndex = 0;
      }
      $.log.isDebugEnabled() && $.log.debug('(SmartField#_inputAccepted) tab-index=' + fieldIndex + ' next tab-index=' + nextIndex);
      let $nextElement = $tabElements.eq(nextIndex).focus();
      if (objects.isFunction($nextElement[0].select)) {
        $nextElement[0].select();
      }
      // This is normally done by FocusManager, but since propagation is stopped, we need to do it here as well
      $nextElement.addClass('keyboard-navigation');
      this._tabPrevented = null;
    }
  }

  _acceptByTextDone(result) {
    this._userWasTyping = false;
    this._extendResult(result);
    this._notUnique = result.numLookupRows > 1;

    // when there's exactly one result, we accept that lookup row
    if (result.uniqueMatch) {
      let lookupRow = result.uniqueMatch;
      if (this._isLookupRowActive(lookupRow)) {
        this.setLookupRow(lookupRow);
        this._inputAccepted();
      } else {
        this.setErrorStatus(Status.error({
          message: this.session.text('SmartFieldInactiveRow', result.text)
        }));
      }
      return;
    }

    this._acceptInputFail(result);
  }

  /**
   * Extends the properties 'uniqueMatch' and 'numLookupRows' on the given result object.
   * The implementation is different depending on the browseHierarchy property.
   */
  _extendResult(result) {
    result.seqNo = this.lookupSeqNo;
    result.uniqueMatch = null;

    // Set query type on result, e.g. 'byAll'
    let propertyName = 'by' + strings.toUpperCaseFirstLetter(result.queryBy.toLowerCase());
    result[propertyName] = true;

    if (this.browseHierarchy) {
      // tree (hierarchical)
      let proposalChooser = scout.create('TreeProposalChooser', {
        parent: this,
        smartField: this
      });
      proposalChooser.setLookupResult(result);
      let leafs = proposalChooser.findLeafs();
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
  }

  _acceptInputFail(result) {
    let searchText = result.text;

    // in any other case something went wrong
    if (result.empty) {
      if (!this.embedded) {
        this.closePopup();
      }
      this.setValue(null);
      this.setDisplayText(searchText);
      this.setErrorStatus(Status.error({
        message: this.session.text('SmartFieldCannotComplete', searchText),
        code: SmartField.ErrorCode.NO_RESULTS
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
  }

  lookupByRec(rec) {
    $.log.isDebugEnabled() && $.log.debug('(SmartField#lookupByRec) rec=', rec);
    this._lastSearchText = null;
    return this._executeLookup(this.lookupCall.cloneForRec(rec))
      .then(result => {

        // Since this function is only used for hierarchical trees we
        // can simply set the appendResult flag always to true here
        result.appendResult = true;
        result.rec = rec;

        if (this.isPopupOpen()) {
          this.popup.setLookupResult(result);
        }
      })
      .then(this._triggerLookupCallDone.bind(this));
  }

  /**
   * Validates the given lookup row is enabled and matches the current activeFilter settings.
   *
   * @returns {boolean}
   */
  _isLookupRowActive(lookupRow) {
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
  }

  _renderEnabled() {
    super._renderEnabled();
    this.$field.setTabbable(this.enabledComputed);
  }

  setMaxLength(maxLength) {
    this.setProperty('maxLength', maxLength);
  }

  _renderMaxLength() {
    this.maxLengthHandler.render();
  }

  setLookupCall(lookupCall) {
    this.setProperty('lookupCall', lookupCall);
  }

  _setLookupCall(lookupCall) {
    this._setProperty('lookupCall', LookupCall.ensure(lookupCall, this.session));
    this._syncBrowseMaxRowCountWithLookupCall();
  }

  _setCodeType(codeType) {
    this._setProperty('codeType', codeType);
    if (!codeType) {
      return;
    }
    let lookupCall = scout.create('CodeLookupCall', {
      session: this.session,
      codeType: codeType
    });
    this.setProperty('lookupCall', lookupCall);
  }

  _formatValue(value) {
    if (objects.isNullOrUndefined(value)) {
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
  }

  _lookupByKeyDone(result) {
    this._notUnique = false;
    let lookupRow = LookupCall.firstLookupRow(result);
    this.setLookupRow(lookupRow);
    return this._formatLookupRow(lookupRow);
  }

  /**
   * This function is called when we need to format a display text from a given lookup
   * row. By default the property 'text' is used for that purpose. Override this function
   * if you need to format different properties from the lookupRow.
   */
  _formatLookupRow(lookupRow) {
    return lookupRow ? lookupRow.text : '';
  }

  /**
   * @param {boolean} [browse] whether or not the lookup call should execute getAll() or getByText() with the current display text.
   *     if browse is undefined, browse is set to true automatically if search text is empty
   * @returns {Promise}
   */
  openPopup(browse) {
    // In case searchRequired is set to true, we always start a new search with the text from the field as query
    let searchText = this._readDisplayText(),
      searchAlways = this.searchRequired ? true : null;
    $.log.isInfoEnabled() && $.log.info('SmartField#openPopup browse=' + browse + ' searchText=' + searchText +
      ' popup=' + this.popup + ' pendingOpenPopup=' + this._pendingOpenPopup);

    // Reset scheduled focus next tabbable when user clicks on the smartfield while a lookup is resolved.
    this._tabPrevented = null;
    this._pendingOpenPopup = true;

    if (strings.empty(searchText)) {
      // if search text is empty - always do 'browse', no matter what the error code is
      browse = true;
    } else if (this.errorStatus) {
      // In case the search yields a not-unique error, we always want to start a lookup
      // with the current display text in every other case we better do browse again
      if (this._hasNotUniqueError()) {
        searchAlways = true;
        browse = false;
      } else if (!this.searchRequired) {
        browse = true;
      }
    }

    return this._lookupByTextOrAll(browse, searchText, searchAlways);
  }

  _hasUiError(codes) {
    let status = this._errorStatus();

    if (!status) {
      return false;
    }

    if (codes) {
      codes = arrays.ensure(codes);
    } else {
      codes = [SmartField.ErrorCode.NO_RESULTS, SmartField.ErrorCode.NOT_UNIQUE];
    }

    // collect codes from the status hierarchy
    let statusList = Status.asFlatList(status);
    let foundCodes = statusList.reduce((list, status) => {
      if (status.code && list.indexOf(status.code) === -1) {
        list.push(status.code);
      }
      return list;
    }, []);

    // if one of the requested codes exist in the list of found codes
    return codes.some(code => {
      return foundCodes.indexOf(code) > -1;
    });
  }

  /**
   * @param browse [boolean] optional, whether to perform a lookupByAll (=browse) or a lookupByText.
   *        By default the param is set to <code>true</code> if the search-text is not empty
   * @param searchText [String] optional, when not set the search-text from the smart-field is used
   * @param searchAlways [boolean] optional, only used when browse=false. When set to true the search
   *        is always performed, event when the search-text has not changed. By default the param is
   *        set to <code>false</code>.
   * @returns {Promise}
   */
  _lookupByTextOrAll(browse, searchText, searchAlways) {
    // default values
    searchText = scout.nvl(searchText, this._readDisplayText());
    browse = scout.nvl(browse, strings.empty(searchText));
    searchAlways = scout.nvl(searchAlways, false);

    // never do a text-lookup if field has dropdown style
    if (this.isDropdown()) {
      browse = true;
    }

    // this avoids unnecessary lookup-calls when a keyboard event has triggered
    // the lookupByTextOrAll function but the search-text has not changed #226643.
    if (!browse && !searchAlways) {
      let lastSearchText = null;
      if (this._lastSearchText) {
        lastSearchText = this._lastSearchText;
      } else {
        lastSearchText = this._getLastSearchText();
      }
      if (this._searchTextEquals(searchText, lastSearchText)) {
        this._pendingOpenPopup = false;
        $.log.debug('(SmartField#_lookupByTextOrAll) searchText is equals -> skip lookup');
        return;
      }
    }

    this._clearPendingLookup();

    let deferred = $.Deferred();
    let doneHandler = function(result) {
      this._lookupByTextOrAllDone(result);
      deferred.resolve(result);
    }.bind(this);

    // execute lookup byAll immediately
    if (browse) {
      $.log.isDebugEnabled() && $.log.debug('(SmartField#_lookupByTextOrAll) lookup byAll (searchText empty)');
      this._lastSearchText = null;
      if (this.searchRequired) {
        doneHandler({
          queryBy: QueryBy.TEXT,
          lookupRows: []
        });
        this.setLookupStatus(Status.warning({
          message: this.session.text('TooManyRows'),
          code: SmartField.ErrorCode.SEARCH_REQUIRED
        }));
      } else {
        this._executeLookup(this.lookupCall.cloneForAll(), true)
          .done(doneHandler)
          .done(this._triggerLookupCallDone.bind(this));
      }
    } else {
      // execute lookup byText with a debounce/delay
      this._pendingLookup = setTimeout(() => {
        $.log.isDebugEnabled() && $.log.debug('(SmartField#_lookupByTextOrAll) lookup byText searchText=' + searchText);
        this._lastSearchText = searchText;
        this._executeLookup(this.lookupCall.cloneForText(searchText), true)
          .done(doneHandler)
          .done(this._triggerLookupCallDone.bind(this));
      }, SmartField.DEBOUNCE_DELAY);
    }

    return deferred.promise();
  }

  /**
   * Returns the text used to store the 'last search-text'. The implementation differs between SmartField and ProposalField.
   */
  _getLastSearchText() {
    return objects.optProperty(this.lookupRow, 'text');
  }

  _lookupByTextOrAllDone(result) {
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

      this.setLookupStatus(Status.warning({
        message: this.session.text('SmartFieldNoDataFound'),
        code: SmartField.ErrorCode.NO_DATA
      }));
      return;
    }

    if (result.empty) {
      this._handleEmptyResult();
      this.setLookupStatus(Status.warning({
        message: this.session.text('SmartFieldCannotComplete', result.text),
        code: SmartField.ErrorCode.NO_RESULTS
      }));
      return;
    }

    let popupStatus = null;
    if (result.numLookupRows > this.browseMaxRowCount) {
      // Info: we limit the lookup rows here, but this is more a last line of defense
      // limit should be always performed on the server, so we don't have to transfer
      // unnecessary lookup rows over the slow network. Make sure your Scout lookup call
      // or REST service impl. respects the max. row count property.
      result.lookupRows = result.lookupRows.slice(0, this.browseMaxRowCount);
      popupStatus = Status.info({
        message: this.session.text('SmartFieldMoreThanXRows', this.browseMaxRowCount)
      });
    }

    // Render popup, if not yet rendered and set results
    this._ensurePopup(result, popupStatus);
  }

  _ensurePopup(result, status) {
    if (this.popup) {
      this.popup.setLookupResult(result);
      this.popup.setStatus(status);
    } else {
      this._renderPopup(result, status);
    }
  }

  _handleException(result) {
    // Oops! Something went wrong while the lookup has been processed.
    if (result.exception) {
      this.setErrorStatus(Status.error({
        message: result.exception
      }));
      this.closePopup();
      return true;
    }
    return false;
  }

  _handleEmptyResult() {
    if (this.touchMode || this.activeFilterEnabled) {
      // In mobile mode we always open the popup, event if we don't have a result
      // Otherwise it would be impossible to enter text in a proposal field with
      // an empty proposal list. The same goes for activeFilterEnabled state -
      // a filter can lead to an empty result (for instance when there are no
      // inactive proposals), and it's hard to switch to another filter value
      // when the popup does not show up at all.
      let emptyResult = {
        lookupRows: []
      };
      this._ensurePopup(emptyResult);
    } else if (this.embedded) {
      this.popup.clearLookupRows();
    } else {
      this.closePopup();
    }
  }

  _renderPopup(result, status) {
    // On touch devices the field does not get the focus.
    // But it should look focused when the popup is open.
    this.$field.addClass('focused');
    this.$container.addClass('popup-open');

    let useTouch = this.touchMode && !this.isDropdown();
    let popupType = useTouch ? 'SmartFieldTouchPopup' : 'SmartFieldPopup';
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
    this.popup.$container.css('--inactive-lookup-row-suffix-text', `'${this.session.text('InactiveState')}'`);

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
    let fieldForPopup = useTouch ? this.popup._field : this;
    this.popup.on('lookupRowSelected', fieldForPopup._onLookupRowSelected.bind(fieldForPopup));
    this.popup.on('activeFilterSelected', this._onActiveFilterSelected.bind(this)); // intentionally use this instead of fieldForPopup *1
    this.popup.one('remove', () => {
      this.popup = null;
      if (this.rendered) {
        this.$container.removeClass('popup-open');
        this.$field.removeClass('focused');
        this._renderErrorStatus();
      }
    });
  }

  closePopup() {
    this._pendingOpenPopup = false;
    if (this.popup) {
      this.popup.close();
    }
  }

  /**
   * Calls acceptInput if mouse down happens outside of the field or popup
   * @override
   */
  aboutToBlurByMouseDown(target) {
    if (this.touchMode) {
      return false;
    }
    if (fields.eventOutsideProposalField(this, target)) {
      this.acceptInput(true);
    }
  }

  _onFieldMouseDown(event) {
    $.log.isDebugEnabled() && $.log.debug('(SmartField#_onFieldMouseDown)');
    this.activate(true);
  }

  activate(onField) {
    if (!this.enabledComputed || !this.rendered) {
      return;
    }
    if (!this.isDropdown() && !fields.handleOnClick(this)) {
      if (this.popup && this.popup.removalPending) {
        // If smart field is activated while it is closing (during remove animation), wait for the animation to finish and activate it afterwards
        this.popup.one('remove', () => {
          if (this.rendered) {
            this.activate(onField);
          }
        });
      }
      return;
    }
    // Don't focus on desktop devices when click is on field #217192
    // Also required for touch case where field is a DIV and not an INPUT field
    if (!onField || Device.get().supportsOnlyTouch()) {
      this.$field.focus();
    }
    this.togglePopup();
  }

  _onIconMouseDown(event) {
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
  }

  _onClearIconMouseDown(event) {
    $.log.isDebugEnabled() && $.log.debug('(SmartField#_onClearIconMouseDown)');
    if (!this.enabledComputed) {
      return;
    }
    event.preventDefault();
    this.$field.focus();
    this.clear();
  }

  _clear() {
    // don't tab next field when user clicks on clear icon (acceptInput is called later)
    this._tabPrevented = null;
    // the state of these two flags is important. See #_checkSearchTextChanged
    this._lastSearchText = this._readDisplayText();
    this._userWasTyping = true;
    fields.valOrText(this.$field, '');
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
  }

  togglePopup() {
    $.log.isInfoEnabled() && $.log.info('(SmartField#togglePopup) popupOpen=', this.isPopupOpen());
    if (this.isPopupOpen()) {
      this.closePopup();
    } else {
      this.openPopup(!this.searchRequired);
    }
  }

  _onFieldBlur(event) {
    this.setFocused(false);
    this.setLoading(false);
    if (this.isTouchable()) {
      return;
    }
    this.acceptInput(false);
    this.closePopup();
  }

  /**
   * @returns {boolean} true if the field is either 'embedded' or in 'touchMode'.
   */
  isTouchable() {
    return this.embedded || this.touchMode;
  }

  _onFieldKeyUp(event) {
    // Escape
    if (event.which === keys.ESC) {
      return;
    }

    // Pop-ups shouldn't open when one of the following keys is pressed
    let w = event.which;
    let isPaste = ((event.ctrlKey || event.metaKey) && w === keys.V) || (event.shiftKey && w === keys.INSERT);
    let isCut = ((event.ctrlKey || event.metaKey) && w === keys.X) || (event.shiftKey && w === keys.DELETE);
    let isCutOrPaste = (isPaste || isCut) && !this.isDropdown();

    if (!isCutOrPaste && (
      event.ctrlKey ||
      event.altKey ||
      event.metaKey ||
      w === keys.ENTER ||
      w === keys.TAB ||
      w === keys.SHIFT ||
      w === keys.CTRL ||
      w === keys.ALT ||
      w === keys.HOME ||
      w === keys.END ||
      w === keys.LEFT ||
      w === keys.RIGHT ||
      w === keys.WIN_LEFT ||
      w === keys.WIN_RIGHT ||
      w === keys.SELECT ||
      w === keys.NUM_LOCK ||
      w === keys.CAPS_LOCK ||
      w === keys.SCROLL_LOCK ||
      w === keys.PAUSE ||
      w === keys.PRINT_SCREEN ||
      this._isNavigationKey(event) ||
      this._isFunctionKey(event)
    )) {
      return;
    }

    // The typed character is not available until the keyUp event happens
    // That's why we must deal with that event here (and not in keyDown)
    // We don't use _displayText() here because we always want the text the
    // user has typed.
    this._handleInput();
  }

  _handleInput() {
    if (this._pendingOpenPopup || this.isPopupOpen()) {
      if (!this.isDropdown()) {
        this._lookupByTextOrAll();
      }
    } else if (!this._pendingOpenPopup) {
      this.openPopup();
    }
  }

  isPopupOpen() {
    return !!(this.popup && !this.popup.removalPending);
  }

  _onFieldKeyDown(event) {
    this._updateUserWasTyping(event);

    // We must prevent default focus handling
    if (event.which === keys.TAB) {
      if (this.mode === FormField.Mode.DEFAULT) {
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

    if (event.which === keys.ENTER) {
      this._handleEnterKey(event);
      return;
    }

    // For dropdowns, not only navigation keys trigger the popup (see code below).
    // However, there are still some exceptions that should be ignored:
    let w = event.which;
    if (this.isDropdown() && (
      event.ctrlKey ||
      event.altKey ||
      event.metaKey ||
      w === keys.ESC ||
      w === keys.SHIFT ||
      w === keys.CTRL ||
      w === keys.ALT ||
      w === keys.WIN_LEFT ||
      w === keys.WIN_RIGHT ||
      w === keys.SELECT ||
      w === keys.NUM_LOCK ||
      w === keys.CAPS_LOCK ||
      w === keys.SCROLL_LOCK ||
      w === keys.PAUSE ||
      w === keys.PRINT_SCREEN ||
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
      event.preventDefault(); // prevent scrolling of container
    }
  }

  _onFieldInput() {
    this._updateHasText();
    // Handling for undo/redo events which can affect this field, even tough the focus is on another field
    // we must have the focus, because otherwise acceptInput would be skipped, which could cause the smart-field
    // to have an invalid displayText which does not reflect the current value. #246765
    if (!this._userWasTyping) {
      if (!this.isFocused()) {
        this.focus();
      }
      this._handleInput();
    }
  }

  _updateUserWasTyping(event) {
    let w = event.which;
    let isPaste = ((event.ctrlKey || event.metaKey) && w === keys.V) || (event.shiftKey && w === keys.INSERT);
    let isCut = ((event.ctrlKey || event.metaKey) && w === keys.X) || (event.shiftKey && w === keys.DELETE);
    let isCutOrPaste = (isPaste || isCut) && !this.isDropdown();

    if (!isCutOrPaste && (
      event.ctrlKey ||
      event.altKey ||
      event.metaKey ||
      w === keys.ESC ||
      w === keys.TAB ||
      w === keys.SHIFT ||
      w === keys.CTRL ||
      w === keys.ALT ||
      w === keys.HOME ||
      w === keys.END ||
      w === keys.LEFT ||
      w === keys.RIGHT ||
      w === keys.WIN_LEFT ||
      w === keys.WIN_RIGHT ||
      w === keys.SELECT ||
      w === keys.NUM_LOCK ||
      w === keys.CAPS_LOCK ||
      w === keys.SCROLL_LOCK ||
      w === keys.PAUSE ||
      w === keys.PRINT_SCREEN ||
      this._isFunctionKey(event)
    )) {
      // neutral, don't change flag
      return;
    }

    this._userWasTyping = !(this._isNavigationKey(event) || w === keys.ENTER);
  }

  _isNavigationKey(event) {
    let navigationKeys = [
      keys.PAGE_UP,
      keys.PAGE_DOWN,
      keys.UP,
      keys.DOWN
    ];

    if (this.isDropdown()) {
      navigationKeys.push(keys.HOME);
      navigationKeys.push(keys.END);
    }

    return scout.isOneOf(event.which, navigationKeys);
  }

  _handleEnterKey(event) {
    if (this.isPopupOpen()) {
      this.popup.selectLookupRow();
      event.stopPropagation();
    }
  }

  _isFunctionKey(event) {
    return event.which >= keys.F1 && event.which <= keys.F12;
  }

  _onLookupRowSelected(event) {
    // When a row has been selected in the proposal chooser, cancel all
    // pending and running lookup-calls. This avoids situations where the
    // lookup-call returns with results after the user has pressed the
    // enter key in order to select a result (see ticket #229775).
    this._clearPendingLookup();

    let currentLookupCall = this.original()._currentLookupCall;

    if (currentLookupCall) {
      currentLookupCall.abort();
      this.original()._currentLookupCall = null;
      this.setLoading(false);
    }

    this.setLookupRow(event.lookupRow);
    this._inputAccepted();
    this.closePopup();
  }

  /**
   * When the user changes the active-filter we must always perform a new search. When the user has typed a searchText
   * we must perform a lookupByText. When the searchText is empty or different from the text of the selected lookup-row
   * we are in browse mode where we use the default given by the 'searchRequired' property. See: #237229.
   */
  _onActiveFilterSelected(event) {
    this.setActiveFilter(event.activeFilter);
    let browse = !this.searchRequired;
    let searchText = this._readSearchText();
    if (this.lookupRow) {
      if (this.lookupRow.text !== searchText) {
        browse = false;
      }
    } else if (strings.hasText(searchText)) {
      browse = false;
    }
    this._lookupByTextOrAll(browse, searchText, true);
  }

  /**
   * @param {number} browseMaxRowCount - a positive number, _not_ null or undefined!
   */
  setBrowseMaxRowCount(browseMaxRowCount) {
    this.setProperty('browseMaxRowCount', browseMaxRowCount);
    this._syncBrowseMaxRowCountWithLookupCall();
  }

  _syncBrowseMaxRowCountWithLookupCall() {
    if (this.lookupCall) {
      // sync max rows with lookup call => request one more row to detect if there would be more rows than browseMaxRowCount.
      this.lookupCall.setMaxRowCount(this.browseMaxRowCount + 1);
    }
  }

  setBrowseAutoExpandAll(browseAutoExpandAll) {
    this.setProperty('browseAutoExpandAll', browseAutoExpandAll);
  }

  setBrowseLoadIncremental(browseLoadIncremental) {
    this.setProperty('browseLoadIncremental', browseLoadIncremental);
    if (this.lookupCall) {
      // change template here. Will be used on the next clone
      this.lookupCall.setLoadIncremental(browseLoadIncremental);
    }
  }

  setActiveFilter(activeFilter) {
    this.setProperty('activeFilter', this.activeFilterEnabled ? activeFilter : null);
  }

  setActiveFilterEnabled(activeFilterEnabled) {
    this.setProperty('activeFilterEnabled', activeFilterEnabled);
  }

  setInitActiveFilter(initActiveFilter) {
    this.setProperty('initActiveFilter', initActiveFilter);
  }

  setSearchRequired(searchRequired) {
    this.setProperty('searchRequired', searchRequired);
  }

  /**
   * A wrapper function around lookup calls used to display the state in the UI.
   */
  _executeLookup(lookupCall, abortExisting) {
    this.lookupSeqNo++;
    this.setLoading(true);

    let currentLookupCall = this.original()._currentLookupCall;

    if (abortExisting && currentLookupCall) {
      currentLookupCall.abort();
    }
    this.original()._currentLookupCall = lookupCall;
    this.trigger('prepareLookupCall', {
      lookupCall: lookupCall
    });

    return lookupCall
      .execute()
      .always(() => {
        this.original()._currentLookupCall = null;
        this.setLoading(false);
        this._clearLookupStatus();
        this._clearNoResultsErrorStatus();
      });
  }

  /**
   * Reset error status NO_RESULTS when a lookup is performed, otherwise it would interfere with the
   * temporary lookupStatus and we'd see an out-dated error-status message while the user is typing.
   */
  _clearNoResultsErrorStatus() {
    if (this.isTouchable()) {
      return;
    }
    if (this._userWasTyping && this.errorStatus && this.errorStatus.code === SmartField.ErrorCode.NO_RESULTS) {
      this.setErrorStatus(null);
    }
  }

  /**
   * Returns true if the smart-field lookup returns a lot of rows. In that case
   * the proposal chooser must create a table with virtual scrolling, which means
   * only the rows visible in the UI are rendered in the DOM. By default we render
   * all rows, since this avoids problems with layout-invalidation with rows
   * that have a bitmap-image (PNG) which is loaded asynchronously.
   */
  virtual() {
    return this.browseMaxRowCount > SmartField.DEFAULT_BROWSE_MAX_COUNT;
  }

  isDropdown() {
    return this.displayStyle === SmartField.DisplayStyle.DROPDOWN;
  }

  _setLookupRow(lookupRow) {
    // remove css classes from old lookup-row
    if (this.lookupRow) {
      this.removeCssClass(this.lookupRow.cssClass);
    }

    this._setProperty('lookupRow', lookupRow);

    // add css classes from new lookup-row
    if (lookupRow) {
      this.addCssClass(lookupRow.cssClass);
    }
  }

  setLookupRow(lookupRow) {
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
  }

  setDisplayText(displayText) {
    super.setDisplayText(displayText);
    this._userWasTyping = false;
  }

  resetDisplayText() {
    let returned = this.formatValue(this.value);
    if (returned && $.isFunction(returned.promise)) {
      // Promise is returned -> set display text later
      returned
        .done(this._setAndRenderDisplayText.bind(this))
        .fail(() => {
          $.log.isInfoEnabled() && $.log.info('Could not resolve display text for value: ' + this.value);
        });
    } else {
      this._setAndRenderDisplayText(returned);
    }
  }

  /**
   * This method is very similar to setDisplayText(), but does _not_ check for equality with
   * the current value. The property is always set and (if the field is rendered) the given
   * display text is always rendered. This is important when resetting the display text,
   * because the visible text in the input field may differ from the "displayText" property
   * value. If setDisplayText() was used, the visible text would not always be reset.
   */
  _setAndRenderDisplayText(displayText) {
    this._setProperty('displayText', displayText);
    if (this.rendered) {
      this._renderDisplayText();
    }
  }

  _getValueFromLookupRow(lookupRow) {
    return lookupRow.key;
  }

  _setValue(value) {
    // set the cached lookup row to null. Keep in mind that the lookup row is set async in a timeout
    // must of the time. Thus we must remove the reference to the old lookup row as early as possible
    if (!this._lockLookupRow) {
      if (objects.isNullOrUndefined(value)) {
        // when value is set to null, we must also reset the cached lookup row
        this._setLookupRow(null);
      } else {
        // when a value is set, we only keep the cached lookup row when the key of the lookup row is equals to the value
        if (this._checkResetLookupRow(value)) {
          this._setLookupRow(null);
        }
      }
    }
    super._setValue(value);
    this._notUnique = false;
  }

  /**
   * Sub-classes like the proposal field may override this function to implement a different behavior.
   */
  _checkResetLookupRow(value) {
    return this.lookupRow && this.lookupRow.key !== value;
  }

  /**
   * This function may be overridden to return another value than this.value.
   * For instance the proposal field does'nt use the value but the key from the
   * lookup row for comparison.
   *
   * @returns {*} the value used to find the selected element in a proposal chooser.
   */
  getValueForSelection() {
    return this._showSelection() ? this.value : null;
  }

  _showSelection() {
    if (objects.isNullOrUndefined(this.value) ||
      objects.isNullOrUndefined(this.lookupRow)) {
      return false;
    }

    let text;
    if (this.rendered) {
      // check if text matches (deal with multi-line)
      text = this._readDisplayText();
      let additionalLines = this.additionalLines();
      if (additionalLines) {
        text = [text].concat(additionalLines).join('\n');
      }
    } else {
      text = this.displayText;
    }

    return text === this.lookupRow.text;
  }

  /**
   * override to ensure dropdown fields and touch mode smart fields does not have a clear icon.
   */
  isClearable() {
    return super.isClearable() && !this.isDropdown() && !this.touchMode;
  }

  _triggerLookupCallDone(result) {
    this.trigger('lookupCallDone', {
      result: result
    });
    return result;
  }

  _triggerAcceptInputFail() {
    this._triggerAcceptInput(false, true);
  }

  _triggerAcceptInput(acceptByLookupRow, failure) {
    this.trigger('acceptInput', {
      displayText: this.displayText,
      errorStatus: this.errorStatus,
      value: this.value,
      lookupRow: this.lookupRow,
      acceptByLookupRow: scout.nvl(acceptByLookupRow, true),
      failure: scout.nvl(failure, false)
    });
  }

  _triggerAcceptByText(searchText) {
    this.trigger('acceptByText', {
      searchText: searchText,
      errorStatus: this.errorStatus
    });
  }

  /**
   * Function invoked if being rendered within a cell-editor (mode='scout.FormField.Mode.CELLEDITOR'), and once the editor finished its rendering.
   */
  onCellEditorRendered(options) {
    if (options.openFieldPopup) {
      this._cellEditorPopup = options.cellEditorPopup;
      this.openPopup(!this.searchRequired);
    }
  }

  additionalLines() {
    let text = scout.nvl(this.displayText, ''),
      textLines = text.split('\n');
    if (textLines.length > 1) {
      textLines.shift();
      return textLines;
    }
    return null;

  }

  _createLoadingSupport() {
    return new SimpleLoadingSupport({
      widget: this,
      loadingIndicatorDelay: 400 // ms
    });
  }

  /**
   * @override FormField.js
   */
  _isInitialShowStatus() {
    if (this.touchMode && (this._pendingOpenPopup || this.isPopupOpen())) {
      // Do not display a tooltip if the touch popup is open, the tooltip will be displayed there
      return false;
    }
    return super._isInitialShowStatus();
  }

  /**
   * In touch mode, we must close the cell editor popup explicitly, because the touch-popup and its glasspane
   * prevents the cell editor popup from receiving mouse down events.
   */
  acceptInputFromField(otherField) {
    this._copyValuesFromField(otherField);

    if (this._cellEditorPopup) {
      // this will call acceptInput on the touch smart-field (== this)
      this._cellEditorPopup.completeEdit();
      this._cellEditorPopup = null;
    } else {
      this.acceptInput();
    }
  }

  /**
   * This function is overridden by ProposalField because it has a different behavior than the smart-field.
   */
  _copyValuesFromField(otherField) {
    if (this.lookupRow !== otherField.lookupRow) {
      this.setLookupRow(otherField.lookupRow);
    }
    this.setErrorStatus(otherField.errorStatus);
    this.setDisplayText(otherField.displayText);
  }

  _setNotUniqueError(searchText) {
    this.setErrorStatus(Status.error({
      message: this.session.text('SmartFieldNotUnique', searchText),
      code: SmartField.ErrorCode.NOT_UNIQUE
    }));
  }

  _hasNotUniqueError(searchText) {
    return this._notUnique || this._hasUiError(SmartField.ErrorCode.NOT_UNIQUE);
  }

  _errorStatus() {
    return this.lookupStatus || this.errorStatus;
  }

  setLookupStatus(lookupStatus) {
    this.setProperty('lookupStatus', lookupStatus);
    if (this.rendered) {
      this._renderErrorStatus();
    }
  }

  clearErrorStatus() {
    this.setErrorStatus(null);
    this._clearLookupStatus();
  }

  _clearLookupStatus() {
    this.setLookupStatus(null);
  }

  /**
   * Checks if there is a lookup status that needs to be set as error status
   * before we leave the smart-field. The lookup status is set to null, because
   * it is a temporary state that is only important while the user executes a lookup.
   */
  _flushLookupStatus() {
    if (!this.lookupStatus) {
      return;
    }

    if (this.lookupStatus.code === SmartField.ErrorCode.NO_RESULTS ||
      this.lookupStatus.code === SmartField.ErrorCode.NOT_UNIQUE) {
      let errorStatus = this.lookupStatus.clone();
      errorStatus.severity = Status.Severity.ERROR;
      this.setErrorStatus(errorStatus);
    }

    this._clearLookupStatus();
  }

  requestInput() {
    if (this.enabledComputed && this.rendered) {
      this.focus();
      this.openPopup(!this.searchRequired);
    }
  }
}
