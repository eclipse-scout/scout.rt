/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  arrays, CellEditorPopup, CellEditorRenderedOptions, CodeLookupCall, ColumnDescriptor, Device, EnumObject, fields, FormField, InitModelOf, InputFieldKeyStrokeContext, keys, KeyStrokeContext, LoadingSupport, LookupCall, LookupCallOrModel,
  LookupResult, LookupRow, MaxLengthHandler, objects, ProposalChooserActiveFilterSelectedEvent, ProposalChooserLookupRowSelectedEvent, QueryBy, scout, SimpleLoadingSupport, SmartFieldCancelKeyStroke, SmartFieldEventMap, SmartFieldLayout,
  SmartFieldModel, SmartFieldPopup, SmartFieldTouchPopup, Status, strings, TreeProposalChooser, ValueField
} from '../../../index';
import $ from 'jquery';

export class SmartField<TValue> extends ValueField<TValue> implements SmartFieldModel<TValue> {
  declare model: SmartFieldModel<TValue>;
  declare eventMap: SmartFieldEventMap<TValue>;
  declare self: SmartField<any>;

  popup: SmartFieldTouchPopup<TValue> | SmartFieldPopup<TValue>;
  lookupCall: LookupCall<TValue>;
  codeType: string;
  lookupRow: LookupRow<TValue>;
  browseHierarchy: boolean;
  browseMaxRowCount: number;
  browseAutoExpandAll: boolean;
  browseLoadIncremental: boolean;
  searchRequired: boolean;
  activeFilterEnabled: boolean;
  activeFilter: SmartFieldActiveFilter;
  activeFilterLabels: string[];
  columnDescriptors: ColumnDescriptor[];
  displayStyle: SmartFieldDisplayStyle;
  touchMode: boolean;
  embedded: boolean;
  lookupStatus: Status;
  /** used to detect if the proposal chooser contains the results of the latest lookup, or an out-dated result. */
  lookupSeqNo: number;
  /** only when the result is up-to-date, we can use the selected lookup row */
  initActiveFilter: SmartFieldActiveFilter;
  maxLength: number;
  maxLengthHandler: MaxLengthHandler;

  /**
   * should only be accessed on the original widget since the adapter accesses it
   * @internal
   */
  _currentLookupCall: LookupCall<TValue>;

  protected _pendingLookup: number;
  protected _pendingOpenPopup: boolean;
  protected _tabPrevented: { shiftKey: boolean };
  /** used to detect whether the last thing the user did was typing (a proposal) or something else, like selecting a proposal row */
  protected _userWasTyping: boolean;
  /** used to prevent multiple execution of blur/acceptInput */
  protected _acceptInputEnabled: boolean;
  protected _acceptInputDeferred: JQuery.Deferred<any>;
  /** used to store the error state 'not unique' which must not be showed while typing, but when the field loses focus */
  protected _notUnique: boolean;
  protected _lastSearchText: string;
  protected _cellEditorPopup: CellEditorPopup<TValue>;
  protected _lockLookupRow: boolean;

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
    this.browseMaxRowCount = SmartField.DEFAULT_BROWSE_MAX_COUNT;
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
    this._userWasTyping = false;
    this._acceptInputEnabled = true;
    this._acceptInputDeferred = $.Deferred();
    this._notUnique = false;
    this._lastSearchText = null;
    this.lookupStatus = null;
    this._currentLookupCall = null;
    this.lookupSeqNo = 0;
    this.initActiveFilter = null;
    this.disabledCopyOverlay = true;
    this.maxLength = 500;
    this.maxLengthHandler = scout.create(MaxLengthHandler, {
      target: this
    });

    this._addCloneProperties(['lookupRow', 'codeType', 'lookupCall', 'activeFilter', 'activeFilterEnabled', 'activeFilterLabels',
      'browseHierarchy', 'browseMaxRowCount', 'browseAutoExpandAll', 'browseLoadIncremental', 'searchRequired', 'columnDescriptors',
      'displayStyle'
    ]);
  }

  static DisplayStyle = {
    DEFAULT: 'default',
    DROPDOWN: 'dropdown'
  } as const;

  static ErrorCode = {
    NOT_UNIQUE: 1,
    NO_RESULTS: 2,
    NO_DATA: 3,
    SEARCH_REQUIRED: 4
  } as const;

  static DEBOUNCE_DELAY = 200;

  static DEFAULT_BROWSE_MAX_COUNT = 100;

  static ActiveFilter = {
    UNDEFINED: 'UNDEFINED',
    FALSE: 'FALSE',
    TRUE: 'TRUE'
  } as const;
  /**
   * @see "IContentAssistField#getActiveFilterLabels()" - should have the same order.
   */
  static ACTIVE_FILTER_VALUES = [
    SmartField.ActiveFilter.UNDEFINED,
    SmartField.ActiveFilter.FALSE,
    SmartField.ActiveFilter.TRUE] as const;

  protected override _init(model: InitModelOf<this>) {
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
  protected override _initValue(value: TValue) {
    this._setLookupCall(this.lookupCall);
    this._setCodeType(this.codeType);
    this._setLookupRow(this.lookupRow);
    super._initValue(value);
  }

  override markAsSaved() {
    super.markAsSaved();
    this.setInitActiveFilter(this.activeFilter);
  }

  override resetValue() {
    super.resetValue();
    this.setActiveFilter(this.initActiveFilter);
  }

  protected override _createKeyStrokeContext(): KeyStrokeContext {
    return new InputFieldKeyStrokeContext();
  }

  protected override _initKeyStrokeContext() {
    super._initKeyStrokeContext();
    this.keyStrokeContext.registerKeyStroke(new SmartFieldCancelKeyStroke(this));
  }

  protected override _render() {
    this.addContainer(this.$parent, 'has-icon ' + this.cssClassName(), new SmartFieldLayout(this));
    this.addLabel();

    let fieldFunc = this.isDropdown() ? fields.makeInputDiv : fields.makeInputOrDiv;
    let $field = (fieldFunc.call(fields, this) as JQuery<HTMLInputElement>)
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

  protected override _renderProperties() {
    super._renderProperties();
    this._renderMaxLength();
  }

  protected override _renderGridData() {
    super._renderGridData();
    this.updateInnerAlignment({
      useHorizontalAlignment: !this.browseHierarchy
    });
  }

  protected override _renderGridDataHints() {
    super._renderGridDataHints();
    this.updateInnerAlignment({
      useHorizontalAlignment: !this.browseHierarchy
    });
  }

  cssClassName(): string {
    let prefix: string = this.displayStyle;
    if (this.displayStyle === SmartField.DisplayStyle.DEFAULT) {
      prefix = 'smart';
    }
    return prefix + '-field';
  }

  protected _readSearchText(): string {
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

  override _readDisplayText(): string {
    return fields.valOrText(this.$field);
  }

  protected override _renderDisplayText() {
    let displayText = this._prepareDisplayText();
    fields.valOrText(this.$field, displayText);
    super._renderDisplayText();
  }

  protected _prepareDisplayText(): string {
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
   * @param sync optional boolean value (default: false), when set to true acceptInput is not allowed to start an asynchronous lookup for text search
   */
  override acceptInput(sync?: boolean): JQuery.Promise<void> | void {
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
  protected _getSelectedLookupRow(searchTextChanged: boolean): LookupRow<TValue> {
    // don't use selected lookup row if...
    if (!this.isPopupOpen() || // 1. popup has been closed
      (searchTextChanged && this._userWasTyping)) { // 2. search text has changed and user was typing
      return null;
    }
    // 3. if the result row is from an out-dated result
    return this.lookupSeqNo === this.popup.lookupResult.seqNo ?
      this.popup.getSelectedLookupRow() : null;
  }

  protected _checkSearchTextChanged(searchText: string): boolean {
    if (this.isDropdown() || !this._userWasTyping) {
      return false; // search text cannot change in drop-down fields
    }

    // check if search text has changed since the last search, when it has changed
    // we cannot use the currently selected lookup row, because these proposals are
    // out-dated.
    return !this._searchTextEquals(searchText, this._lastSearchText);
  }

  protected _searchTextEquals(searchText: string, lastSearchText: string): boolean {
    let a = strings.nullIfEmpty(this._firstTextLine(searchText));
    let b = strings.nullIfEmpty(lastSearchText);
    return strings.equalsIgnoreCase(a, b);
  }

  protected _clearPendingLookup() {
    if (this._pendingLookup) {
      clearTimeout(this._pendingLookup);
      this._pendingLookup = null;
    }
  }

  /**
   * This function is intended to be overridden. Proposal field has another behavior than the smart field.
   *
   * @param sync optional boolean value (default: false), when set to true acceptInput is not allowed to start an asynchronous lookup for text search
   */
  protected _acceptInput(sync: boolean, searchText: string, searchTextEmpty: boolean, searchTextChanged: boolean, selectedLookupRow: LookupRow<TValue>): JQuery.Promise<void> | void {
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
  protected _firstTextLine(text: string): string {
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
  protected _acceptByText(sync: boolean, searchText: string) {
    sync = scout.nvl(sync, false);
    $.log.isDebugEnabled() && $.log.debug('(SmartField#_acceptByText) sync=' + sync + ' searchText=', searchText);

    if (sync) {
      this._acceptByTextSync(searchText);
    } else {
      this._acceptByTextAsync(searchText);
    }
  }

  protected _acceptByTextSync(searchText: string) {
    this._lastSearchText = null;
    this._inputAccepted();
    if (!this._hasUiError()) {
      this.resetDisplayText();
    }
  }

  protected _acceptByTextAsync(searchText: string) {
    this._lastSearchText = searchText;
    this._executeLookup(this.lookupCall.cloneForText(searchText), true)
      .done(this._acceptByTextDone.bind(this))
      .done(this._triggerLookupCallDone.bind(this));
    this._triggerAcceptByText(searchText);
  }

  protected _inputAccepted(triggerEvent?: boolean, acceptByLookupRow?: boolean) {
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

  protected _focusNextTabbable() {
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
      let $nextElement = $tabElements.eq(nextIndex).focus() as JQuery<HTMLInputElement>;
      if (objects.isFunction($nextElement[0].select)) {
        $nextElement[0].select();
      }
      // This is normally done by FocusManager, but since propagation is stopped, we need to do it here as well
      $nextElement.addClass('keyboard-navigation');
      this._tabPrevented = null;
    }
  }

  protected _acceptByTextDone(result: SmartFieldLookupResult<TValue>) {
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
  protected _extendResult(result: SmartFieldLookupResult<TValue>) {
    result.seqNo = this.lookupSeqNo;
    result.uniqueMatch = null;

    // Set query type on result, e.g. 'byAll'
    let propertyName = 'by' + strings.toUpperCaseFirstLetter(result.queryBy.toLowerCase());
    result[propertyName] = true;

    if (this.browseHierarchy) {
      // tree (hierarchical)
      let proposalChooser = scout.create(TreeProposalChooser, {
        parent: this,
        smartField: this
      }) as TreeProposalChooser<TValue>;
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
  }

  protected _acceptInputFail(result: SmartFieldLookupResult<TValue>) {
    let searchText = result.text;

    // in any other case something went wrong
    if (!result.numLookupRows) {
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

  lookupByRec(rec: TValue): JQuery.Promise<SmartFieldLookupResult<TValue>> {
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

        return result;
      })
      .then(this._triggerLookupCallDone.bind(this));
  }

  /**
   * Validates the given lookup row is enabled and matches the current activeFilter settings.
   */
  protected _isLookupRowActive(lookupRow: LookupRow<TValue>): boolean {
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

  protected override _renderEnabled() {
    super._renderEnabled();
    this.$field.setTabbable(this.enabledComputed);
  }

  setMaxLength(maxLength: number) {
    this.setProperty('maxLength', maxLength);
  }

  protected _renderMaxLength() {
    this.maxLengthHandler.render();
  }

  setLookupCall(lookupCall: LookupCallOrModel<TValue>) {
    this.setProperty('lookupCall', lookupCall);
  }

  protected _setLookupCall(lookupCall: LookupCallOrModel<TValue>) {
    this._setProperty('lookupCall', LookupCall.ensure(lookupCall, this.session));
    this._syncBrowseMaxRowCountWithLookupCall();
  }

  protected _setCodeType(codeType: string) {
    this._setProperty('codeType', codeType);
    if (!codeType) {
      return;
    }
    let lookupCall = scout.create(CodeLookupCall, {
      session: this.session,
      codeType: codeType
    });
    this.setProperty('lookupCall', lookupCall);
  }

  protected override _formatValue(value: TValue): string | JQuery.Promise<string> {
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
      .then(result => {
        let formattedValue = this._lookupByKeyDone(result);
        return {formattedValue, result};
      })
      .then(({formattedValue, result}) => {
        this._triggerLookupCallDone(result);
        return formattedValue;
      });
  }

  protected _lookupByKeyDone(result: SmartFieldLookupResult<TValue>): string {
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
  protected _formatLookupRow(lookupRow: LookupRow<TValue>): string {
    return lookupRow ? lookupRow.text : '';
  }

  /**
   * @param browse whether or not the lookup call should execute getAll() or getByText() with the current display text.
   *     if browse is undefined, browse is set to true automatically if search text is empty
   */
  openPopup(browse?: boolean): JQuery.Promise<any> {
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

  protected _hasUiError(codes?: SmartFieldErrorCode | SmartFieldErrorCode[]): boolean {
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
   */
  protected _lookupByTextOrAll(browse?: boolean, searchText?: string, searchAlways?: boolean): JQuery.Promise<any> {
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
  protected _getLastSearchText(): string {
    return objects.optProperty(this.lookupRow, 'text');
  }

  protected _lookupByTextOrAllDone(result: SmartFieldLookupResult<TValue>) {
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

    let empty = !result.numLookupRows;
    // 'No data' case
    if (empty && result.byAll) {
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

    if (empty) {
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

  protected _ensurePopup(result: SmartFieldLookupResult<TValue>, status?: Status) {
    if (this.popup) {
      this.popup.setLookupResult(result);
      this.popup.setStatus(status);
    } else {
      this._renderPopup(result, status);
    }
  }

  protected _handleException(result: SmartFieldLookupResult<TValue>): boolean {
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

  protected _handleEmptyResult() {
    if (this.touchMode || this.activeFilterEnabled) {
      // In mobile mode we always open the popup, event if we don't have a result
      // Otherwise it would be impossible to enter text in a proposal field with
      // an empty proposal list. The same goes for activeFilterEnabled state -
      // a filter can lead to an empty result (for instance when there are no
      // inactive proposals), and it's hard to switch to another filter value
      // when the popup does not show up at all.
      let emptyResult = {
        lookupRows: []
      } as SmartFieldLookupResult<TValue>;
      this._ensurePopup(emptyResult);
    } else if (this.embedded) {
      (this.popup as SmartFieldTouchPopup<TValue>).clearLookupRows();
    } else {
      this.closePopup();
    }
  }

  protected _renderPopup(result: SmartFieldLookupResult<TValue>, status: Status) {
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
    let fieldForPopup = useTouch ? (this.popup as SmartFieldTouchPopup<TValue>)._field : this;
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
   * Calls acceptInput if mouse down happens outside the field or popup
   */
  override aboutToBlurByMouseDown(target: Element) {
    if (this.touchMode) {
      return;
    }
    if (fields.eventOutsideProposalField(this, target)) {
      this.acceptInput(true);
    }
  }

  protected _onFieldMouseDown(event: JQuery.MouseDownEvent) {
    $.log.isDebugEnabled() && $.log.debug('(SmartField#_onFieldMouseDown)');
    this.activate(true);
  }

  override activate(onField?: boolean) {
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

  protected override _onIconMouseDown(event: JQuery.MouseDownEvent) {
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

  protected override _onClearIconMouseDown(event: JQuery.MouseDownEvent) {
    $.log.isDebugEnabled() && $.log.debug('(SmartField#_onClearIconMouseDown)');
    if (!this.enabledComputed) {
      return;
    }
    event.preventDefault();
    this.$field.focus();
    this.clear();
  }

  protected override _clear() {
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

  protected override _onFieldBlur(event: JQuery.BlurEvent) {
    this.setFocused(false);
    this.setLoading(false);
    if (this.isTouchable()) {
      return;
    }
    this.acceptInput(false);
    this.closePopup();
  }

  /**
   * @returns true if the field is either 'embedded' or in 'touchMode'.
   */
  isTouchable(): boolean {
    return this.embedded || this.touchMode;
  }

  protected _onFieldKeyUp(event: JQuery.KeyUpEvent) {
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

  protected _handleInput() {
    if (this._pendingOpenPopup || this.isPopupOpen()) {
      if (!this.isDropdown()) {
        this._lookupByTextOrAll();
      }
    } else if (!this._pendingOpenPopup) {
      this.openPopup();
    }
  }

  isPopupOpen(): boolean {
    return !!(this.popup && !this.popup.removalPending);
  }

  protected _onFieldKeyDown(event: JQuery.KeyDownEvent) {
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

  protected _onFieldInput() {
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

  protected _updateUserWasTyping(event: JQuery.KeyDownEvent) {
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

  protected _isNavigationKey(event: JQuery.KeyDownEvent | JQuery.KeyUpEvent): boolean {
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

  protected _handleEnterKey(event: JQuery.KeyDownEvent) {
    if (this.isPopupOpen()) {
      this.popup.selectLookupRow();
      event.stopPropagation();
    }
  }

  protected _isFunctionKey(event: JQuery.KeyDownEvent | JQuery.KeyUpEvent): boolean {
    return event.which >= keys.F1 && event.which <= keys.F12;
  }

  protected _onLookupRowSelected(event: ProposalChooserLookupRowSelectedEvent<TValue>) {
    // When a row has been selected in the proposal chooser, cancel all
    // pending and running lookup-calls. This avoids situations where the
    // lookup-call returns with results after the user has pressed the
    // enter key in order to select a result (see ticket #229775).
    this._clearPendingLookup();

    let currentLookupCall = (this.original() as SmartField<TValue>)._currentLookupCall;

    if (currentLookupCall) {
      currentLookupCall.abort();
      (this.original() as SmartField<TValue>)._currentLookupCall = null;
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
  protected _onActiveFilterSelected(event: ProposalChooserActiveFilterSelectedEvent<TValue>) {
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
   * @param browseMaxRowCount - a positive number, _not_ null or undefined!
   */
  setBrowseMaxRowCount(browseMaxRowCount: number) {
    this.setProperty('browseMaxRowCount', browseMaxRowCount);
    this._syncBrowseMaxRowCountWithLookupCall();
  }

  protected _syncBrowseMaxRowCountWithLookupCall() {
    if (this.lookupCall) {
      // sync max rows with lookup call => request one more row to detect if there would be more rows than browseMaxRowCount.
      this.lookupCall.setMaxRowCount(this.browseMaxRowCount + 1);
    }
  }

  setBrowseAutoExpandAll(browseAutoExpandAll: boolean) {
    this.setProperty('browseAutoExpandAll', browseAutoExpandAll);
  }

  setBrowseLoadIncremental(browseLoadIncremental: boolean) {
    this.setProperty('browseLoadIncremental', browseLoadIncremental);
    if (this.lookupCall) {
      // change template here. Will be used on the next clone
      this.lookupCall.setLoadIncremental(browseLoadIncremental);
    }
  }

  setActiveFilter(activeFilter: SmartFieldActiveFilter) {
    this.setProperty('activeFilter', this.activeFilterEnabled ? activeFilter : null);
  }

  setActiveFilterEnabled(activeFilterEnabled: boolean) {
    this.setProperty('activeFilterEnabled', activeFilterEnabled);
  }

  setInitActiveFilter(initActiveFilter: SmartFieldActiveFilter) {
    this.setProperty('initActiveFilter', initActiveFilter);
  }

  setSearchRequired(searchRequired: boolean) {
    this.setProperty('searchRequired', searchRequired);
  }

  /**
   * A wrapper function around lookup calls used to display the state in the UI.
   */
  protected _executeLookup(lookupCall: LookupCall<TValue>, abortExisting?: boolean): JQuery.Promise<SmartFieldLookupResult<TValue>> {
    this.lookupSeqNo++;
    this.setLoading(true);

    let currentLookupCall = (this.original() as SmartField<TValue>)._currentLookupCall;

    if (abortExisting && currentLookupCall) {
      currentLookupCall.abort();
    }
    (this.original() as SmartField<TValue>)._currentLookupCall = lookupCall;
    this.trigger('prepareLookupCall', {
      lookupCall: lookupCall
    });

    return lookupCall
      .execute()
      .always(() => {
        (this.original() as SmartField<TValue>)._currentLookupCall = null;
        this.setLoading(false);
        this._clearLookupStatus();
        this._clearNoResultsErrorStatus();
      });
  }

  /**
   * Reset error status NO_RESULTS when a lookup is performed, otherwise it would interfere with the
   * temporary lookupStatus and we'd see an out-dated error-status message while the user is typing.
   */
  protected _clearNoResultsErrorStatus() {
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
  virtual(): boolean {
    return this.browseMaxRowCount > SmartField.DEFAULT_BROWSE_MAX_COUNT;
  }

  isDropdown(): boolean {
    return this.displayStyle === SmartField.DisplayStyle.DROPDOWN;
  }

  protected _setLookupRow(lookupRow: LookupRow<TValue>) {
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

  setLookupRow(lookupRow: LookupRow<TValue>) {
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

  override setDisplayText(displayText: string) {
    super.setDisplayText(displayText);
    this._userWasTyping = false;
  }

  resetDisplayText() {
    let returned = this.formatValue(this.value);
    if (returned && $.isFunction((returned as JQuery.Promise<string>).promise)) {
      // Promise is returned -> set display text later
      (returned as JQuery.Promise<string>)
        .done(this._setAndRenderDisplayText.bind(this))
        .fail(() => {
          $.log.isInfoEnabled() && $.log.info('Could not resolve display text for value: ' + this.value);
        });
    } else {
      this._setAndRenderDisplayText(returned as string);
    }
  }

  /**
   * This method is very similar to setDisplayText(), but does _not_ check for equality with
   * the current value. The property is always set and (if the field is rendered) the given
   * display text is always rendered. This is important when resetting the display text,
   * because the visible text in the input field may differ from the "displayText" property
   * value. If setDisplayText() was used, the visible text would not always be reset.
   */
  protected _setAndRenderDisplayText(displayText: string) {
    this._setProperty('displayText', displayText);
    if (this.rendered) {
      this._renderDisplayText();
    }
  }

  protected _getValueFromLookupRow(lookupRow: LookupRow<TValue>): TValue {
    return lookupRow.key;
  }

  protected override _setValue(value: TValue) {
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
  protected _checkResetLookupRow(value: TValue): boolean {
    return this.lookupRow && this.lookupRow.key !== value;
  }

  /**
   * This function may be overridden to return another value than this.value.
   * For instance the proposal field doesn't use the value but the key from the
   * lookup row for comparison.
   *
   * @returns the value used to find the selected element in a proposal chooser.
   */
  getValueForSelection(): TValue {
    return this._showSelection() ? this.value : null;
  }

  protected _showSelection(): boolean {
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
  override isClearable(): boolean {
    return super.isClearable() && !this.isDropdown() && !this.touchMode;
  }

  protected _triggerLookupCallDone(result: SmartFieldLookupResult<TValue>): SmartFieldLookupResult<TValue> {
    this.trigger('lookupCallDone', {
      result: result
    });
    return result;
  }

  protected _triggerAcceptInputFail() {
    this._triggerAcceptInput(false, true);
  }

  /** @internal */
  override _triggerAcceptInput(acceptByLookupRow?: boolean, failure?: boolean) {
    this.trigger('acceptInput', {
      displayText: this.displayText,
      errorStatus: this.errorStatus,
      value: this.value,
      lookupRow: this.lookupRow,
      acceptByLookupRow: scout.nvl(acceptByLookupRow, true),
      failure: scout.nvl(failure, false)
    });
  }

  protected _triggerAcceptByText(searchText: string) {
    this.trigger('acceptByText', {
      searchText: searchText,
      errorStatus: this.errorStatus
    });
  }

  /**
   * Function invoked if being rendered within a cell-editor (mode='scout.FormField.Mode.CELLEDITOR'), and once the editor finished its rendering.
   */
  onCellEditorRendered(options: CellEditorRenderedOptions<TValue>) {
    if (options.openFieldPopup) {
      this._cellEditorPopup = options.cellEditorPopup;
      this.openPopup(!this.searchRequired);
    }
  }

  additionalLines(): string[] {
    let text = scout.nvl(this.displayText, ''),
      textLines = text.split('\n');
    if (textLines.length > 1) {
      textLines.shift();
      return textLines;
    }
    return null;
  }

  protected override _createLoadingSupport(): LoadingSupport {
    return new SimpleLoadingSupport({
      widget: this,
      loadingIndicatorDelay: 400 // ms
    });
  }

  protected override _isInitialShowStatus(): boolean {
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
  acceptInputFromField(otherField: SmartField<TValue>) {
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
  protected _copyValuesFromField(otherField: SmartField<TValue>) {
    if (this.lookupRow !== otherField.lookupRow) {
      this.setLookupRow(otherField.lookupRow);
    }
    this.setErrorStatus(otherField.errorStatus);
    this.setDisplayText(otherField.displayText);
  }

  protected _setNotUniqueError(searchText: string) {
    this.setErrorStatus(Status.error({
      message: this.session.text('SmartFieldNotUnique', searchText),
      code: SmartField.ErrorCode.NOT_UNIQUE
    }));
  }

  protected _hasNotUniqueError(): boolean {
    return this._notUnique || this._hasUiError(SmartField.ErrorCode.NOT_UNIQUE);
  }

  protected override _errorStatus(): Status {
    return this.lookupStatus || this.errorStatus;
  }

  setLookupStatus(lookupStatus: Status) {
    let changed = this.setProperty('lookupStatus', lookupStatus);
    if (changed && this.rendered) {
      this._renderErrorStatus();
    }
  }

  override clearErrorStatus() {
    this.setErrorStatus(null);
    this._clearLookupStatus();
  }

  protected _clearLookupStatus() {
    this.setLookupStatus(null);
  }

  /**
   * Checks if there is a lookup status that needs to be set as error status
   * before we leave the smart-field. The lookup status is set to null, because
   * it is a temporary state that is only important while the user executes a lookup.
   */
  protected _flushLookupStatus() {
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

  override requestInput() {
    if (this.enabledComputed && this.rendered) {
      this.focus();
      this.openPopup(!this.searchRequired);
    }
  }
}

export type SmartFieldDisplayStyle = EnumObject<typeof SmartField.DisplayStyle>;
export type SmartFieldActiveFilter = EnumObject<typeof SmartField.ActiveFilter>;
export type SmartFieldErrorCode = EnumObject<typeof SmartField.ErrorCode>;

export interface SmartFieldLookupResult<Key> extends LookupResult<Key> {
  appendResult?: boolean;
  /**
   * Used to track if a result is outdated.
   */
  seqNo?: number;
  /**
   * Number of 'relevant' lookupRows, i.e. number of lookupRows for a non hierarchical lookupCall and number of leafs for a hierarchical one
   */
  numLookupRows?: number;
  /**
   * only 'relevant' lookupRow if numLookupRows === 1
   */
  uniqueMatch?: LookupRow<Key>;
}
