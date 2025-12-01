/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {App, Event, objects, OutlineAdapter, Page, RemoteEvent, SearchOutline, SearchOutlineModel, SearchPage, SearchState} from '../../index';

export class SearchOutlineAdapter extends OutlineAdapter {
  declare widget: SearchOutline;

  protected _searchStatesModel: Record<string, string>;
  protected _searchStates = new Map<string, SearchState>();

  constructor() {
    super();
    this._addRemoteProperties(['searchQuery']);
  }

  override _postCreateWidget() {
    super._postCreateWidget();

    this._updateSearchStates(this._searchStatesModel);
  }

  protected override _initProperties(model: SearchOutlineModel & { searchStates?: Record<string, string>; requestFocusQueryField?: boolean }) {
    if (model.searchStates !== undefined) {
      // searchStates are only relevant for the adapter and not for the SearchOutline
      this._searchStatesModel = model.searchStates;
      delete model.searchStates;
    }
    if (model.requestFocusQueryField !== undefined) {
      // ignore pseudo property initially (to prevent the function SearchOutlineAdapter#requestFocusQueryField() to be replaced)
      delete model.requestFocusQueryField;
    }
  }

  protected _syncRequestFocusQueryField() {
    this.widget.focusQueryField();
  }

  protected _syncSearchStates(searchStates: Record<string, string>) {
    // do not write searchState to SearchOutline as searchStates are only relevant for the adapter
    this._updateSearchStates(searchStates);
  }

  protected _updateSearchStates(searchStates: Record<string, string>) {
    const removedPageIds = new Set(this._searchStates.keys());

    for (const [pageId, searchStateId] of Object.entries(searchStates || {})) {
      const searchState = this.session.getOrCreateWidget(searchStateId, this.widget) as SearchState;
      if (!searchState) {
        continue;
      }

      removedPageIds.delete(pageId);

      const page: SearchPage = this.widget.nodeById(pageId);
      if (page) {
        page.searchState = searchState;
      }

      this._searchStates.set(pageId, searchState);
    }

    for (const pageId of removedPageIds) {
      this._searchStates.delete(pageId);

      const page: SearchPage = this.widget.nodeById(pageId);
      if (page) {
        delete page.searchState;
      }
    }

    this.widget.updateSearchStates();
  }

  protected _onWidgetSearch(event: Event<SearchOutline>) {
    this._send('search', null, {showBusyIndicator: false});
  }

  protected _onWidgetResetSearch(event: Event<SearchOutline>) {
    this._send('resetSearch', null, {showBusyIndicator: false});
  }

  protected override _onWidgetEvent(event: Event<SearchOutline>) {
    if (event.type === 'search') {
      this._onWidgetSearch(event);
    } else if (event.type === 'resetSearch') {
      this._onWidgetResetSearch(event);
    } else {
      super._onWidgetEvent(event);
    }
  }

  protected _sendSearchQuery(searchQuery: string) {
    this._send('property', {searchQuery}, {showBusyIndicator: false});
  }

  override onModelAction(event: RemoteEvent) {
    if (event.type === 'search') {
      this._onSearch();
    } else {
      super.onModelAction(event);
    }
  }

  protected _onSearch() {
    this.widget.search();
  }

  /**
   * Static method to modify the prototype of SearchOutline.
   */
  static modifySearchOutlinePrototype() {
    if (!App.get().remote) {
      return;
    }

    // Currently the _initTreeNodeInternal function of the SearchOutline is OutlineAdapter._initTreeNodeInternalRemote and _initTreeNodeInternalOrig is Outline._initTreeNodeInternal (both are inherited from the prototype of the Outline).
    // Remembering the orig function will result in OutlineAdapter._initTreeNodeInternalRemote being _initTreeNodeInternalOrig on the prototype of SearchOutline.
    // When this function is called on a SearchOutline it will call 'this._initTreeNodeInternalOrig' which is the function itself and therefore, a stack overflow error is thrown.
    // And as there is nothing to remember (i.e. the SearchOutline has no own implementation of _initTreeNodeInternal) this flag can simply be set to false.
    objects.replacePrototypeFunction(SearchOutline, '_initTreeNodeInternal', SearchOutlineAdapter._initTreeNodeInternalRemote, false);
  }

  protected static override _initTreeNodeInternalRemote(this: SearchOutline & { modelAdapter: SearchOutlineAdapter; _initTreeNodeInternalOrig }, page: SearchPage, parentNode: Page) {
    // _initTreeNodeInternal was replaced without remembering the orig function which was OutlineAdapter._initTreeNodeInternalRemote.
    // Therefore, call this function directly and not via _initTreeNodeInternalOrig.
    OutlineAdapter._initTreeNodeInternalRemote.call(this, page, parentNode);

    if (!this.modelAdapter) {
      return;
    }

    const searchState = this.modelAdapter._searchStates.get(page.id);
    if (searchState) {
      page.searchState = searchState;
    }
  }
}

App.addListener('bootstrap', SearchOutlineAdapter.modifySearchOutlinePrototype);
