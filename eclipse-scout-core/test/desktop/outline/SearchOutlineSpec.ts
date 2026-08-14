/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Column, Event, InitModelOf, Outline, PageWithTable, scout, SearchOutline, SearchPage, SearchState, Session, Table} from '../../../src/index';

describe('SearchOutline', () => {
  let session: Session;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    session.textMap.add('ui.NumSearchResults', '{0} search results for "{1}"');
    session.textMap.add('ui.SearchInProgressFor', 'Searching for "{0}"...');
    session.textMap.add('ui.SearchTermTooLong', 'The search term is too long.');
    session.textMap.add('ui.SearchTermTooShort', 'The search term is too short.');
  });

  describe('nodesInserted', () => {

    it('updates searchStates', () => {
      const outline = createSearchOutline();

      expect(outline._searchStates).toEqual(new Set());

      const searchPage0 = createSearchPage(outline, {text: 'SearchPage0'});
      outline.insertNode(searchPage0);

      expect(outline._searchStates).toEqual(new Set([searchPage0.searchState]));

      const searchPage1 = createSearchPage(outline, {text: 'SearchPage1'});
      const searchPage2 = createSearchPage(outline, {text: 'SearchPage2'});
      outline.insertNodes([searchPage1, searchPage2]);

      expect(outline._searchStates).toEqual(new Set([searchPage0.searchState, searchPage1.searchState, searchPage2.searchState]));
    });

    it('does not retrigger search when search query is absent', () => {
      const outline = createSearchOutline();

      const events: Event<SearchOutline>[] = [];
      outline.on('search resetSearch', event => events.push(event));

      expect(outline.pending).toBe(false);
      expect(outline.searchQuery).toBe(undefined);
      expect(outline.searchStatus).toBe(null);

      const searchPage0 = createSearchPage(outline, {text: 'SearchPage0'});
      outline.insertNode(searchPage0);

      expect(searchPage0.searchState.pending).toBe(true);
      expect(outline.pending).toBe(true);
      expect(outline.searchStatus).toBe(null);
      expect(events.map(event => event.type)).toEqual([]);

      const searchPage1 = createSearchPage(outline, {text: 'SearchPage1'});
      const searchPage2 = createSearchPage(outline, {text: 'SearchPage2'});
      outline.insertNodes([searchPage1, searchPage2]);

      expect(searchPage0.searchState.pending).toBe(true);
      expect(searchPage1.searchState.pending).toBe(true);
      expect(searchPage2.searchState.pending).toBe(true);
      expect(outline.pending).toBe(true);
      expect(outline.searchStatus).toBe(null);
      expect(events.map(event => event.type)).toEqual([]);

      outline.setSearchQuery('f o o');

      expect(searchPage0.searchState.pending).toBe(true);
      expect(searchPage1.searchState.pending).toBe(true);
      expect(searchPage2.searchState.pending).toBe(true);
      expect(outline.pending).toBe(true);
      expect(outline.searchStatus).toBe('The search term is too short.');
      expect(events.map(event => event.type)).toEqual(['resetSearch']);
    });

    it('retriggers search when search query is present', () => {
      const outline = createSearchOutline();
      outline.on('search', event => {
        for (let searchState of outline.nodes.map((node: SearchPage) => node.searchState)) {
          if (outline.searchQuery) {
            searchState.setPending(false);
            searchState.setResultCount(111);
            searchState.setLimited(false);
          } else {
            searchState.setPending(true);
          }
        }
      });

      expect(outline.searchQuery).toBe(undefined);
      expect(outline.searchStatus).toBe(null);
      expect(outline.pending).toBe(false);

      const events: Event<SearchOutline>[] = [];
      outline.on('search resetSearch', event => events.push(event));

      // -----

      const searchPage0 = createSearchPage(outline, {text: 'SearchPage0'});
      outline.insertNode(searchPage0);

      expect(searchPage0.searchState.pending).toBe(true);
      expect(outline.pending).toBe(true);
      expect(outline.searchStatus).toBe(null);
      expect(events.map(event => event.type)).toEqual([]);

      // -----

      outline.setSearchQuery('foo');

      expect(searchPage0.searchState.pending).toBe(false);
      expect(outline.pending).toBe(false);
      expect(outline.searchStatus).toBe('111 search results for "foo"');
      expect(events.map(event => event.type)).toEqual(['search']);

      // -----

      const searchPage1 = createSearchPage(outline, {text: 'SearchPage1'});
      const searchPage2 = createSearchPage(outline, {text: 'SearchPage2'});
      outline.insertNodes([searchPage1, searchPage2]);

      expect(searchPage0.searchState.pending).toBe(false);
      expect(searchPage1.searchState.pending).toBe(false);
      expect(searchPage2.searchState.pending).toBe(false);
      expect(outline.pending).toBe(false);
      expect(outline.searchStatus).toBe('333 search results for "foo"');
      expect(events.map(event => event.type)).toEqual(['search', 'search']);
    });
  });

  describe('nodesDeleted', () => {

    it('updates searchStates', () => {
      const outline = createSearchOutline();
      const searchPage0 = createSearchPage(outline, {text: 'SearchPage0'});
      const searchPage1 = createSearchPage(outline, {text: 'SearchPage1'});
      const searchPage2 = createSearchPage(outline, {text: 'SearchPage2'});
      outline.insertNodes([searchPage0, searchPage1, searchPage2]);

      expect(outline._searchStates).toEqual(new Set([searchPage0.searchState, searchPage1.searchState, searchPage2.searchState]));

      outline.deleteNode(searchPage0);

      expect(outline._searchStates).toEqual(new Set([searchPage1.searchState, searchPage2.searchState]));

      outline.deleteNodes([searchPage1, searchPage2]);

      expect(outline._searchStates).toEqual(new Set());
    });

    it('does not retrigger search when search query is present', () => {
      const outline = createSearchOutline();
      outline.on('search', event => {
        for (let searchState of outline.nodes.map((node: SearchPage) => node.searchState)) {
          if (outline.searchQuery) {
            searchState.setPending(false);
            searchState.setResultCount(111);
            searchState.setLimited(false);
          } else {
            searchState.setPending(true);
          }
        }
      });

      const searchPage0 = createSearchPage(outline, {text: 'SearchPage0'});
      const searchPage1 = createSearchPage(outline, {text: 'SearchPage1'});
      const searchPage2 = createSearchPage(outline, {text: 'SearchPage2'});
      outline.insertNodes([searchPage0, searchPage1, searchPage2]);
      outline.setSearchQuery('foo');

      spyOn(outline, 'setSearchStates').and.callThrough();

      const events: Event<SearchOutline>[] = [];
      outline.on('search resetSearch', event => events.push(event));

      // -----

      expect(outline.setSearchStates).toHaveBeenCalledTimes(0);
      expect(outline.pending).toBe(false);
      expect(outline.searchStatus).toBe('333 search results for "foo"');

      // -----

      outline.deleteNode(searchPage1);

      expect(outline.setSearchStates).toHaveBeenCalledTimes(1);
      expect(outline.pending).toBe(false);
      expect(outline.searchStatus).toBe('222 search results for "foo"');
      expect(events.map(event => event.type)).toEqual([]);
    });
  });

  describe('allChildNodesDeleted', () => {

    it('updates searchStates', () => {
      const outline = createSearchOutline();
      const searchPage0 = createSearchPage(outline, {text: 'SearchPage0'});
      const searchPage1 = createSearchPage(outline, {text: 'SearchPage1'});
      const searchPage2 = createSearchPage(outline, {text: 'SearchPage2'});
      outline.insertNodes([searchPage0, searchPage1, searchPage2]);

      expect(outline._searchStates).toEqual(new Set([searchPage0.searchState, searchPage1.searchState, searchPage2.searchState]));

      outline.deleteAllChildNodes();

      expect(outline._searchStates).toEqual(new Set());
    });
  });

  describe('setSearchQuery', () => {

    it('triggers search', () => {
      const outline = createSearchOutline();

      spyOn(outline, 'search').and.callFake(() => {
      });
      expect(outline.search).toHaveBeenCalledTimes(0);

      outline.setSearchQuery('foo');
      expect(outline.search).toHaveBeenCalledTimes(1);

      outline.setSearchQuery('foo');
      expect(outline.search).toHaveBeenCalledTimes(1);

      outline.setSearchQuery('Bar');
      expect(outline.search).toHaveBeenCalledTimes(2);
    });
  });

  describe('_updateSearchStatus', () => {

    it('builds searchStatus from searchStates', () => {
      const outline = createSearchOutline();
      const searchPage0 = createSearchPage(outline, {text: 'SearchPage0'});
      const searchPage1 = createSearchPage(outline, {text: 'SearchPage1'});
      outline.insertNodes([searchPage0, searchPage1]);
      outline.setSearchQuery('foo');

      expect(outline.searchStatus).toBe('Searching for "foo"...');

      searchPage0.searchState.setResultCount(42);
      searchPage0.searchState.setPending(false);

      // still searching as page 1 is pending
      expect(outline.searchStatus).toBe('Searching for "foo"...');

      searchPage1.searchState.setResultCount(13);
      searchPage1.searchState.setPending(false);

      expect(outline.searchStatus).toBe('55 search results for "foo"');

      searchPage0.searchState.setLimited(true);

      expect(outline.searchStatus).toBe('55+ search results for "foo"');
    });
  });

  it('sets the initial focus to the query field', () => {
    const outline = createSearchOutline();
    outline.render();
    session.focusManager.validateFocus();
    expect(outline.$queryField).toBeFocused();
  });

  describe('search', () => {

    it('sets all searchStates pending', () => {
      const outline = createSearchOutline();
      const searchPage0 = createSearchPage(outline, {text: 'SearchPage0'});
      const searchPage1 = createSearchPage(outline, {text: 'SearchPage1'});
      outline.insertNodes([searchPage0, searchPage1]);

      searchPage0.searchState.setPending(false);
      searchPage1.searchState.setPending(false);

      outline.search();

      expect(searchPage0.searchState.pending).toBeTrue();
      expect(searchPage1.searchState.pending).toBeTrue();
    });

    it('validates searchQuery', () => {
      const outline = createSearchOutline();

      spyOn(outline, '_validateSearchQuery').and.callFake(() => {
      });

      expect(outline._validateSearchQuery).toHaveBeenCalledTimes(0);

      outline.search();

      expect(outline._validateSearchQuery).toHaveBeenCalledTimes(1);
    });

    it('resets search if searchQuery is invalid', () => {
      const outline = createSearchOutline();

      let searchQueryValid = true;
      spyOn(outline, '_validateSearchQuery').and.callFake(() => {
        outline._searchQueryValid = searchQueryValid;
      });
      spyOn(outline, 'resetSearch').and.callFake(() => {
      });

      expect(outline.resetSearch).toHaveBeenCalledTimes(0);

      outline.search();
      expect(outline.resetSearch).toHaveBeenCalledTimes(0);

      searchQueryValid = false;
      outline.search();
      expect(outline.resetSearch).toHaveBeenCalledTimes(1);
    });

    it('updates search', () => {
      const outline = createSearchOutline();
      outline.setSearchQuery('foo');

      spyOn(outline, '_updateSearchStatus').and.callFake(() => {
      });

      expect(outline._updateSearchStatus).toHaveBeenCalledTimes(0);

      outline.search();

      expect(outline._updateSearchStatus).toHaveBeenCalledTimes(1);
    });

    it('triggers search event', () => {
      const outline = createSearchOutline();
      outline.setSearchQuery('foo');

      let searchTriggeredCount = 0;
      outline.on('search', e => searchTriggeredCount++);

      expect(searchTriggeredCount).toBe(0);

      outline.search();

      expect(searchTriggeredCount).toBe(1);
    });
  });

  describe('_validateSearchQuery', () => {

    it('clears searchStatus if no query is set', () => {
      const outline = createSearchOutline();
      outline.setSearchStatus('Some');
      outline._searchQueryValid = true;

      outline._validateSearchQuery();

      expect(outline.searchStatus).toBeNull();
      expect(outline._searchQueryValid).toBeFalse();
    });

    it('validates max length of searchQuery', () => {
      const outline = createSearchOutline();
      outline.setMaxSearchFieldLength(10);

      outline.setSearchQuery('foo');
      outline.setSearchStatus(null);

      outline._validateSearchQuery();
      expect(outline.searchStatus).toBeNull();

      outline.setSearchQuery('lorem ipsum dolor');
      outline.setSearchStatus(null);

      outline._validateSearchQuery();
      expect(outline.searchStatus).toBe('The search term is too long.');
    });

    it('validates min length of search tokens', () => {
      const outline = createSearchOutline();
      outline.setMinSearchTokenLength(4);

      outline.setSearchQuery('lorem ipsum dolor');
      outline.setSearchStatus(null);

      outline._validateSearchQuery();
      expect(outline.searchStatus).toBeNull();

      outline.setSearchQuery('foo bar');
      outline.setSearchStatus(null);

      outline._validateSearchQuery();
      expect(outline.searchStatus).toBe('The search term is too short.');

      outline.setSearchQuery('*f*o*o* *b*a*r*');
      outline.setSearchStatus(null);

      outline._validateSearchQuery();
      expect(outline.searchStatus).toBe('The search term is too short.');
    });
  });

  describe('resetSearch', () => {

    it('triggers resetSearch event', () => {
      const outline = createSearchOutline();
      outline.setSearchQuery('foo');

      let resetSearchTriggeredCount = 0;
      outline.on('resetSearch', e => resetSearchTriggeredCount++);

      expect(resetSearchTriggeredCount).toBe(0);

      outline.resetSearch();

      expect(resetSearchTriggeredCount).toBe(1);
    });
  });

  describe('updateSearchStates', () => {

    it('collects all searchStates of top level nodes', () => {
      const outline = createSearchOutline();
      const searchPage0 = createSearchPage(outline, {text: 'SearchPage0'});
      const searchPage1 = createSearchPage(outline, {text: 'SearchPage1'});
      const searchPage10 = createSearchPage(outline, {text: 'SearchPage10'});

      outline.insertNodes([searchPage0, searchPage1]);
      outline.insertNode(searchPage10, searchPage1);

      spyOn(outline, 'setSearchStates').and.callThrough();

      outline.updateSearchStates();

      expect(outline.setSearchStates).toHaveBeenCalledOnceWith(new Set([searchPage0.searchState, searchPage1.searchState]));
    });
  });

  describe('setSearchStates', () => {

    it('triggers add searchState and remove searchState', () => {
      const outline = createSearchOutline();
      const searchPage0 = createSearchPage(outline, {text: 'SearchPage0'});
      const searchPage1 = createSearchPage(outline, {text: 'SearchPage1'});
      const searchPage2 = createSearchPage(outline, {text: 'SearchPage2'});

      outline.setSearchStates(new Set([searchPage0.searchState, searchPage1.searchState]));

      spyOn(outline._searchStates, 'add').and.callThrough();
      spyOn(outline._searchStates, 'delete').and.callThrough();

      outline.setSearchStates(new Set([searchPage1.searchState, searchPage2.searchState]));

      expect(outline._searchStates.add).toHaveBeenCalledOnceWith(searchPage2.searchState);
      expect(outline._searchStates.delete).toHaveBeenCalledOnceWith(searchPage0.searchState);
    });
  });

  describe('add searchState', () => {

    it('installs listeners', () => {
      const outline = createSearchOutline();
      const searchPage = createSearchPage(outline, {text: 'SearchPage'});

      expect(searchPage.searchState.events.count('propertyChange:resultCount propertyChange:limited propertyChange:pending', outline._searchStateChangeHandler)).toBe(0);
      expect(searchPage.searchState.events.count('destroy', outline._searchStateDestroyHandler)).toBe(0);

      outline.setSearchStates(new Set([searchPage.searchState]));

      expect(searchPage.searchState.events.count('propertyChange:resultCount propertyChange:limited propertyChange:pending', outline._searchStateChangeHandler)).toBe(1);
      expect(searchPage.searchState.events.count('destroy', outline._searchStateDestroyHandler)).toBe(1);
    });
  });

  describe('remove searchState', () => {

    it('uninstalls listeners', () => {
      const outline = createSearchOutline();
      const searchPage = createSearchPage(outline, {text: 'SearchPage'});
      outline.setSearchStates(new Set([searchPage.searchState]));

      expect(searchPage.searchState.events.count('propertyChange:resultCount propertyChange:limited propertyChange:pending', outline._searchStateChangeHandler)).toBe(1);
      expect(searchPage.searchState.events.count('destroy', outline._searchStateDestroyHandler)).toBe(1);

      outline.setSearchStates(new Set());

      expect(searchPage.searchState.events.count('propertyChange:resultCount propertyChange:limited propertyChange:pending', outline._searchStateChangeHandler)).toBe(0);
      expect(searchPage.searchState.events.count('destroy', outline._searchStateDestroyHandler)).toBe(0);
    });
  });

  describe('_onSearchStateResultCountChanged', () => {

    it('updates searchStatus', () => {
      const outline = createSearchOutline();
      const searchPage = createSearchPage(outline, {text: 'SearchPage'});
      outline.setSearchStates(new Set([searchPage.searchState]));

      spyOn(outline, '_updateSearchStatus').and.callFake(() => {
      });

      expect(outline._updateSearchStatus).toHaveBeenCalledTimes(0);

      searchPage.searchState.setResultCount(42);

      expect(outline._updateSearchStatus).toHaveBeenCalledTimes(1);

      searchPage.searchState.setResultCount(42);

      expect(outline._updateSearchStatus).toHaveBeenCalledTimes(1);

      searchPage.searchState.setResultCount(13);

      expect(outline._updateSearchStatus).toHaveBeenCalledTimes(2);
    });
  });

  describe('_onSearchStateLimitedChanged', () => {

    it('updates searchStatus', () => {
      const outline = createSearchOutline();
      const searchPage = createSearchPage(outline, {text: 'SearchPage'});
      outline.setSearchStates(new Set([searchPage.searchState]));

      spyOn(outline, '_updateSearchStatus').and.callFake(() => {
      });

      expect(outline._updateSearchStatus).toHaveBeenCalledTimes(0);

      searchPage.searchState.setLimited(true);

      expect(outline._updateSearchStatus).toHaveBeenCalledTimes(1);

      searchPage.searchState.setLimited(true);

      expect(outline._updateSearchStatus).toHaveBeenCalledTimes(1);

      searchPage.searchState.setLimited(false);

      expect(outline._updateSearchStatus).toHaveBeenCalledTimes(2);
    });
  });

  describe('_onSearchStatePendingChanged', () => {

    it('updates searchStatus', () => {
      const outline = createSearchOutline();
      const searchPage = createSearchPage(outline, {text: 'SearchPage'});
      outline.setSearchStates(new Set([searchPage.searchState]));

      spyOn(outline, '_updateSearchStatus').and.callFake(() => {
      });

      expect(outline._updateSearchStatus).toHaveBeenCalledTimes(0);

      searchPage.searchState.setPending(false);

      expect(outline._updateSearchStatus).toHaveBeenCalledTimes(1);

      searchPage.searchState.setPending(false);

      expect(outline._updateSearchStatus).toHaveBeenCalledTimes(1);

      searchPage.searchState.setPending(true);

      expect(outline._updateSearchStatus).toHaveBeenCalledTimes(2);
    });
  });

  describe('_onSearchStateDestroy', () => {

    it('removes searchState', () => {
      const outline = createSearchOutline();
      const searchPage = createSearchPage(outline, {text: 'SearchPage'});
      outline.setSearchStates(new Set([searchPage.searchState]));

      expect(outline._searchStates).toEqual(new Set([searchPage.searchState]));

      searchPage.searchState.destroy();

      expect(outline._searchStates).toEqual(new Set());
    });
  });

  describe('resultCount', () => {

    it('aggregates resultCount of all searchStates', () => {
      const outline = createSearchOutline();
      const searchPage0 = createSearchPage(outline, {text: 'SearchPage0'});
      const searchPage1 = createSearchPage(outline, {text: 'SearchPage1'});
      const searchPage2 = createSearchPage(outline, {text: 'SearchPage1'});
      outline.insertNodes([searchPage0, searchPage1, searchPage2]);

      searchPage0.searchState.setResultCount(7);
      searchPage1.searchState.setResultCount(13);
      searchPage2.searchState.setResultCount(42);

      expect(outline.resultCount).toBe(62);

      searchPage0.searchState.setResultCount(0);

      expect(outline.resultCount).toBe(55);

      searchPage1.searchState.setResultCount(100);

      expect(outline.resultCount).toBe(142);
    });
  });

  describe('limited', () => {

    it('aggregates resultCount of all searchStates', () => {
      const outline = createSearchOutline();
      const searchPage0 = createSearchPage(outline, {text: 'SearchPage0'});
      const searchPage1 = createSearchPage(outline, {text: 'SearchPage1'});
      const searchPage2 = createSearchPage(outline, {text: 'SearchPage1'});
      outline.insertNodes([searchPage0, searchPage1, searchPage2]);

      searchPage0.searchState.setLimited(true);
      searchPage1.searchState.setLimited(true);
      searchPage2.searchState.setLimited(false);

      expect(outline.limited).toBeTrue();

      searchPage0.searchState.setLimited(false);

      expect(outline.limited).toBeTrue();

      searchPage1.searchState.setLimited(false);

      expect(outline.limited).toBeFalse();

      searchPage2.searchState.setLimited(true);

      expect(outline.limited).toBeTrue();
    });
  });

  describe('pending', () => {

    it('aggregates resultCount of all searchStates', () => {
      const outline = createSearchOutline();
      const searchPage0 = createSearchPage(outline, {text: 'SearchPage0'});
      const searchPage1 = createSearchPage(outline, {text: 'SearchPage1'});
      const searchPage2 = createSearchPage(outline, {text: 'SearchPage1'});
      outline.insertNodes([searchPage0, searchPage1, searchPage2]);

      searchPage0.searchState.setPending(true);
      searchPage1.searchState.setPending(true);
      searchPage2.searchState.setPending(false);

      expect(outline.pending).toBeTrue();

      searchPage0.searchState.setPending(false);

      expect(outline.pending).toBeTrue();

      searchPage1.searchState.setPending(false);

      expect(outline.pending).toBeFalse();

      searchPage2.searchState.setPending(true);

      expect(outline.pending).toBeTrue();
    });
  });

  function createSearchOutline(model?: Partial<InitModelOf<SearchOutline>>): SpecSearchOutline {
    return scout.create(SpecSearchOutline, {
      parent: session.desktop,
      ...model
    });
  }

  function createSearchPage(outline: Outline, model?: Partial<InitModelOf<PageWithTable>>): PageWithTable & SearchPage {
    return scout.create(PageWithTable, {
      parent: outline,
      detailTable: {
        objectType: Table,
        columns: [{objectType: Column}]
      },
      searchState: scout.create(SearchState, {parent: outline}),
      ...model
    });
  }

  class SpecSearchOutline extends SearchOutline {

    declare _searchQueryValid: boolean;
    declare _searchStates: Set<SearchState>;
    declare _searchStateChangeHandler: typeof this._onSearchStateChanged;
    declare _searchStateDestroyHandler: typeof this._onSearchStateDestroy;

    override _updateSearchStatus() {
      super._updateSearchStatus();
    }

    override _validateSearchQuery() {
      super._validateSearchQuery();
    }
  }
});
