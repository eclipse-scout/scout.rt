/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  aria, Event, InitModelOf, InputFieldKeyStrokeContext, keys, KeyStrokeContext, keyStrokeModifier, Outline, Page, PropertyChangeEvent, SearchOutlineDownKeyStroke, SearchOutlineEventMap, SearchOutlineLayout, SearchOutlineModel,
  SearchOutlineUpKeyStroke, SearchState, TreeAllChildNodesDeletedEvent, TreeNodesDeletedEvent, TreeNodesInsertedEvent
} from '../../index';
import $ from 'jquery';

export class SearchOutline extends Outline implements SearchOutlineModel {
  declare model: SearchOutlineModel;
  declare eventMap: SearchOutlineEventMap;
  declare self: SearchOutline;

  searchQuery: string;
  maxSearchFieldLength = 60;
  minSearchTokenLength = 2;

  searchFieldKeyStrokeContext: KeyStrokeContext;
  searchStatus: string;

  $searchPanel: JQuery;
  $clearIcon: JQuery;
  $searchStatus: JQuery;
  $queryField: JQuery<HTMLInputElement>;

  protected _searchQueryValid = true;
  protected _searchStates = new Set<SearchState>();
  protected _searchStateResultCountChangedHandler = this._onSearchStateResultCountChanged.bind(this);
  protected _searchStateLimitedChangedHandler = this._onSearchStateLimitedChanged.bind(this);
  protected _searchStatePendingChangedHandler = this._onSearchStatePendingChanged.bind(this);
  protected _searchStateDestroyHandler = this._onSearchStateDestroy.bind(this);

  protected override _init(model: InitModelOf<this>) {
    super._init(model);

    this.updateSearchStates();
    this.on('nodesInserted nodesDeleted allChildNodesDeleted', event => {
      if (!(event as unknown as TreeNodesInsertedEvent<SearchOutline> | TreeNodesDeletedEvent<SearchOutline> | TreeAllChildNodesDeletedEvent<SearchOutline>).parentNode) {
        this.updateSearchStates();
      }
    });

    this.search();
  }

  protected override _initKeyStrokeContext() {
    super._initKeyStrokeContext();

    this.searchFieldKeyStrokeContext = this._createKeyStrokeContextForSearchField();
    this.searchFieldKeyStrokeContext.registerKeyStroke(new SearchOutlineDownKeyStroke(this));
    this.searchFieldKeyStrokeContext.registerKeyStroke(new SearchOutlineUpKeyStroke(this));
  }

  protected _createKeyStrokeContextForSearchField(): KeyStrokeContext {
    let keyStrokeContext = new InputFieldKeyStrokeContext();
    keyStrokeContext.$scopeTarget = () => this.$searchPanel;
    keyStrokeContext.$bindTarget = () => this.$queryField;
    keyStrokeContext.registerStopPropagationKeys(keyStrokeModifier.NONE, [keys.ENTER, keys.BACKSPACE]);
    return keyStrokeContext;
  }

  protected override _render() {
    super._render();

    // Override layout
    this.htmlComp.setLayout(new SearchOutlineLayout(this));

    this.$container.addClass('search-outline');
    this.$searchPanel = this.$container.prependDiv('search-outline-panel');
    this.$queryField = this.$searchPanel.appendElement('<input>', 'search-outline-field')
      .on('input', this._createOnQueryFieldInputFunction().bind(this))
      .on('keypress', this._onQueryFieldKeyPress.bind(this)) as JQuery<HTMLInputElement>;
    this.$clearIcon = this.$searchPanel.appendSpan('clear-icon unfocusable action text-field-icon')
      .on('mousedown', this._onClearIconMouseDown.bind(this));

    this.$searchStatus = this.$searchPanel.appendDiv('search-outline-status')
      .on('mousedown', this._onTitleMouseDown.bind(this));
    this.session.keyStrokeManager.installKeyStrokeContext(this.searchFieldKeyStrokeContext);
  }

  protected override _remove() {
    this.session.keyStrokeManager.uninstallKeyStrokeContext(this.searchFieldKeyStrokeContext);
    this.$searchPanel.remove();
    super._remove();
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderSearchQuery();
    this._renderSearchStatus();
  }

  protected override _renderTitle() {
    super._renderTitle();
    // Move before search panel
    if (this.titleVisible) {
      this.$title.insertBefore(this.$searchPanel);
      aria.linkElementWithLabel(this.$queryField, this.$title);
    }
  }

  setSearchQuery(searchQuery: string) {
    this.setProperty('searchQuery', searchQuery);
  }

  protected _setSearchQuery(searchQuery: string) {
    this._setProperty('searchQuery', searchQuery);
    this.search();
  }

  protected _renderSearchQuery() {
    this.$queryField.val(this.searchQuery);
    this._updateHasText();
  }

  protected _updateHasText() {
    this.$queryField.toggleClass('has-text', !!this.$queryField.val());
  }

  setSearchStatus(searchStatus: string) {
    this.setProperty('searchStatus', searchStatus);
  }

  protected _updateSearchStatus() {
    if (!this._searchQueryValid) {
      return;
    }
    if (this.pending) {
      this.setSearchStatus(this.session.text('ui.SearchInProgressFor', this.searchQuery));
      return;
    }
    const resultCount = this.resultCount;
    this.setSearchStatus(this.session.text('ui.NumSearchResults', this.limited ? resultCount + '+' : resultCount, this.searchQuery));
  }

  protected _renderSearchStatus() {
    let animate = this.rendered;

    if (this.searchStatus && !this.$searchStatus.isVisible()) {
      if (animate) {
        this.$searchStatus.slideDown({
          duration: 200,
          progress: this.revalidateLayout.bind(this)
        });
      } else {
        this.$searchStatus.show();
      }
    } else if (!this.searchStatus && this.$searchStatus.isVisible()) {
      if (animate) {
        this.$searchStatus.slideUp({
          duration: 200,
          progress: this.revalidateLayout.bind(this)
        });
      } else {
        this.$searchStatus.hide();
      }
    }
    this.$searchStatus.textOrNbsp(this.searchStatus);
    this.$searchPanel.toggleClass('has-status', !!this.searchStatus);
  }

  setMaxSearchFieldLength(maxSearchFieldLength: number) {
    if (maxSearchFieldLength <= 0) {
      maxSearchFieldLength = 200;
    }
    this.setProperty('maxSearchFieldLength', maxSearchFieldLength);
  }

  setMinSearchTokenLength(minSearchTokenLength: number) {
    if (minSearchTokenLength <= 0) {
      minSearchTokenLength = 2;
    }
    this.setProperty('minSearchTokenLength', minSearchTokenLength);
  }

  search() {
    this._searchStates.forEach(searchState => searchState.setPending(true));

    this._validateSearchQuery();
    if (!this._searchQueryValid) {
      this.resetSearch();
      return;
    }

    this._updateSearchStatus();
    this._triggerSearch();
  }

  protected _validateSearchQuery() {
    if (!this.searchQuery?.length) {
      this.setSearchStatus(null);
      this._searchQueryValid = false;
      return;
    }

    if (this.searchQuery.length > this.maxSearchFieldLength) {
      this.setSearchStatus(this.session.text('ui.SearchTermTooLong'));
      this._searchQueryValid = false;
      return;
    }

    // remove wildcards and split into tokens (i.e. non-empty strings without spaces)
    const tokens = this.searchQuery.replaceAll(/\*/g, '').split(' ').filter(Boolean);

    // at least one token must hav min length
    for (const token of tokens) {
      if (token.length >= this.minSearchTokenLength) {
        this._searchQueryValid = true;
        return;
      }
    }
    this.setSearchStatus(this.session.text('ui.SearchTermTooShort'));
    this._searchQueryValid = false;
  }

  protected _triggerSearch() {
    this.trigger('search');
  }

  resetSearch() {
    this._triggerResetSearch();
  }

  protected _triggerResetSearch() {
    this.trigger('resetSearch');
  }

  protected _createOnQueryFieldInputFunction(): (event: JQuery.TriggeredEvent) => void {
    return event => {
      this._updateHasText();
      // debounced update search query
      $.debounce(() => this.setSearchQuery(this.$queryField.val()))();
    };
  }

  protected _onClearIconMouseDown(event: JQuery.MouseDownEvent) {
    this.setSearchQuery('');
    // focus field if x is pressed when the field does not have the focus
    this.$queryField.focus();
    // stay in field when x is pressed
    event.preventDefault();
  }

  protected _onQueryFieldKeyPress(event: JQuery.KeyPressEvent) {
    if (event.which === keys.ENTER) {
      this._setSearchQuery(this.$queryField.val());
    }
  }

  focusQueryField() {
    this.validateFocus();
  }

  /**
   * Focus and select content AFTER the search outline was rendered (and therefore the query field filled).
   */
  override validateFocus() {
    if (!this.rendered) {
      return;
    }
    let elementToFocus = this.$queryField[0];
    if (this.session.focusManager.requestFocus(elementToFocus)) {
      elementToFocus.select();
    }
  }

  updateSearchStates() {
    const searchStates = new Set<SearchState>();

    for (const page of this.nodes as SearchPage[]) {
      if (page.searchState) {
        searchStates.add(page.searchState);
      }
    }

    this.setSearchStates(searchStates);
  }

  setSearchStates(searchStates: Set<SearchState>) {
    searchStates ||= new Set();

    const searchStatesToAdd = new Set<SearchState>();
    for (const searchState of searchStates) {
      if (this._searchStates.has(searchState)) {
        continue;
      }
      searchStatesToAdd.add(searchState);
    }

    const searchStatesToRemove = new Set<SearchState>();
    for (const searchState of this._searchStates) {
      if (searchStates.has(searchState)) {
        continue;
      }
      searchStatesToRemove.add(searchState);
    }

    searchStatesToAdd.forEach(searchState => this._addSearchState(searchState));
    searchStatesToRemove.forEach(searchState => this._removeSearchState(searchState));
  }

  protected _addSearchState(searchState: SearchState) {
    if (!searchState) {
      return;
    }

    if (this._searchStates.has(searchState)) {
      return;
    }
    this._searchStates.add(searchState);

    this._installSearchStateListeners(searchState);
    this._updateSearchStatus();
  }

  protected _removeSearchState(searchState: SearchState) {
    if (!searchState) {
      return;
    }

    if (!this._searchStates.has(searchState)) {
      return;
    }
    this._searchStates.delete(searchState);

    this._uninstallSearchStateListeners(searchState);
    this._updateSearchStatus();
  }

  protected _installSearchStateListeners(searchState: SearchState) {
    if (!searchState) {
      return;
    }
    searchState.on('propertyChange:resultCount', this._searchStateResultCountChangedHandler);
    searchState.on('propertyChange:limited', this._searchStateLimitedChangedHandler);
    searchState.on('propertyChange:pending', this._searchStatePendingChangedHandler);
    searchState.one('destroy', this._searchStateDestroyHandler);
  }

  protected _uninstallSearchStateListeners(searchState: SearchState) {
    if (!searchState) {
      return;
    }
    searchState.off('propertyChange:resultCount', this._searchStateResultCountChangedHandler);
    searchState.off('propertyChange:limited', this._searchStateLimitedChangedHandler);
    searchState.off('propertyChange:pending', this._searchStatePendingChangedHandler);
    searchState.off('destroy', this._searchStateDestroyHandler);
  }

  protected _onSearchStateResultCountChanged(event: PropertyChangeEvent<number>) {
    this._updateSearchStatus();
  }

  protected _onSearchStateLimitedChanged(event: PropertyChangeEvent<boolean>) {
    this._updateSearchStatus();
  }

  protected _onSearchStatePendingChanged(event: PropertyChangeEvent<boolean>) {
    this._updateSearchStatus();
  }

  protected _onSearchStateDestroy(event: Event<SearchState>) {
    this._removeSearchState(event.source);
  }

  get resultCount(): number {
    let resultCount = 0;
    for (const searchState of this._searchStates) {
      resultCount += searchState.resultCount;
    }
    return resultCount;
  }

  get limited(): boolean {
    for (const searchState of this._searchStates) {
      if (searchState.limited) {
        return true;
      }
    }
    return false;
  }

  get pending(): boolean {
    for (const searchState of this._searchStates) {
      if (searchState.pending) {
        return true;
      }
    }
    return false;
  }
}

export interface SearchPage extends Page {
  searchState?: SearchState;
}
