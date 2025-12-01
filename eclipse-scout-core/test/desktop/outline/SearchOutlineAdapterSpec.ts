/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {scout, SearchOutline, SearchOutlineAdapter, SearchPage, SearchState} from '../../../src/index';
import {OutlineSpecHelper} from '../../../src/testing';

describe('SearchOutlineAdapter', () => {
  let session: SandboxSession;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  describe('onModelPropertyChange', () => {

    describe('requestFocusQueryField', () => {

      it('may be called multiple times', () => {
        let outline = scout.create(createSimpleModel(SearchOutline, session));
        linkWidgetAndAdapter(outline, 'SearchOutlineAdapter');
        outline.render();

        session.$entryPoint.focus();
        expect(document.activeElement).toBe(session.$entryPoint[0]);
        let event = createPropertyChangeEvent(outline, {
          requestFocusQueryField: null
        });
        outline.modelAdapter.onModelPropertyChange(event);
        expect(document.activeElement).toBe(outline.$queryField[0]);

        session.$entryPoint.focus();
        expect(document.activeElement).toBe(session.$entryPoint[0]);
        event = createPropertyChangeEvent(outline, {
          requestFocusQueryField: null
        });
        outline.modelAdapter.onModelPropertyChange(event);
        expect(document.activeElement).toBe(outline.$queryField[0]);
      });
    });
  });

  describe('searchStates', () => {

    it('are transferred to pages initially', () => {
      const helper = new OutlineSpecHelper(session);
      const adapter = scout.create(createSimpleModel(SearchOutlineAdapter, session));

      const model = helper.createModelFixture(2);
      model.nodes.forEach(node => {
        node.nodeType = 'table';
      });
      model.objectType = 'SearchOutline';

      registerAdapterData({id: 'searchState0', objectType: 'SearchState'}, session);
      registerAdapterData({id: 'searchState1', objectType: 'SearchState'}, session);

      model.searchStates = {};
      model.searchStates[model.nodes[0].id] = 'searchState0';
      model.searchStates[model.nodes[1].id] = 'searchState1';

      const outline = adapter.createWidget(model, session.desktop) as SpecSearchOutline;
      const [page0, page1] = outline.nodes as SearchPage[];

      expect(page0.searchState).toBeDefined();
      expect(page1.searchState).toBeDefined();
      expect(outline._searchStates).toEqual(new Set([page0.searchState, page1.searchState]));
    });

    it('are updated on pages when property changes', () => {
      const helper = new OutlineSpecHelper(session);
      const adapter = scout.create(createSimpleModel(SearchOutlineAdapter, session));

      const model = helper.createModelFixture(2);
      model.nodes.forEach(node => {
        node.nodeType = 'table';
      });
      model.objectType = 'SearchOutline';

      registerAdapterData({id: 'searchState0', objectType: 'SearchState'}, session);

      model.searchStates = {};
      model.searchStates[model.nodes[0].id] = 'searchState0';

      const outline = adapter.createWidget(model, session.desktop) as SpecSearchOutline;
      const [page0, page1] = outline.nodes as SearchPage[];

      expect(page0.searchState).toBeDefined();
      expect(page1.searchState).toBeUndefined();
      expect(outline._searchStates).toEqual(new Set([page0.searchState]));

      registerAdapterData({id: 'searchState1', objectType: 'SearchState'}, session);

      const searchStatesNew = {};
      searchStatesNew[model.nodes[1].id] = 'searchState1';
      adapter.onModelPropertyChange(createPropertyChangeEvent(outline, {searchStates: searchStatesNew}));

      expect(page0.searchState).toBeUndefined();
      expect(page1.searchState).toBeDefined();
      expect(outline._searchStates).toEqual(new Set([page1.searchState]));
    });
  });

  class SpecSearchOutline extends SearchOutline {

    declare _searchStates: Set<SearchState>;
  }
});
