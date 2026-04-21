/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Column, Desktop, Form, MenuBar, Outline, PageField, PageWithTable, scout, SearchFormTableControl, Table, TreeNodeModel} from '../../../../src';

describe('PageField', () => {
  let session: SandboxSession;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  class SearchForm extends Form {
  }

  class SearchFormPage extends PageWithTable {
    protected override _jsonModel(): TreeNodeModel {
      return {
        detailTable: {
          id: 'jswidgets.SamplePageWithTable.Table',
          objectType: Table,
          maxRowCount: 5,
          columns: [
            {
              id: 'MyColumn',
              objectType: Column
            }
          ],
          tableControls: [
            {
              id: 'SearchFormTableControl',
              objectType: SearchFormTableControl,
              form: {
                id: 'SearchForm',
                objectType: SearchForm
              }
            }
          ]
        }
      };
    }
  }

  describe('outline', () => {
    it('is automatically created if not specified', () => {
      let pageField = scout.create(PageField, {parent: session.desktop});
      expect(pageField.outline).toBeInstanceOf(Outline);
      expect(pageField.outline.nodes.length).toBe(0);

      let oldOutline = pageField.outline;
      pageField.setOutline(scout.create(Outline, {parent: session.desktop, nodes: [{objectType: SearchFormPage}]}));
      expect(pageField.outline).toBeInstanceOf(Outline);
      expect(pageField.outline.nodes.length).toBe(1);
      expect(pageField.outline).not.toBe(oldOutline);
      expect(oldOutline.destroyed).toBe(true);
    });

    it('hides navigate buttons', () => {
      let pageField = scout.create(PageField, {parent: session.desktop});
      expect(pageField.outline.navigateButtonsVisible).toBe(false);

      pageField.setOutline(scout.create(Outline, {parent: session.desktop}));
      expect(pageField.outline.navigateButtonsVisible).toBe(false);
    });

    it('ensures outline is never shown in compact mode', () => {
      session.desktop.displayStyle = Desktop.DisplayStyle.COMPACT;
      let pageField = scout.create(PageField, {parent: session.desktop});
      expect(pageField.outline.compact).toBe(false);
      expect(pageField.outline.embedDetailContent).toBe(false);

      pageField.setOutline(scout.create(Outline, {parent: session.desktop}));
      expect(pageField.outline.compact).toBe(false);
      expect(pageField.outline.embedDetailContent).toBe(false);
    });
  });

  describe('page', () => {
    it('can be a Page, object type or child model', () => {
      let pageField = scout.create(PageField, {parent: session.desktop});
      pageField.setPage(SearchFormPage);
      expect(pageField.page).toBeInstanceOf(SearchFormPage);

      let oldPage = pageField.page;
      pageField.setPage({objectType: SearchFormPage});
      expect(pageField.page).toBeInstanceOf(SearchFormPage);
      expect(pageField.page).not.toBe(oldPage);

      let page = scout.create(SearchFormPage, {parent: pageField.outline});
      pageField.setPage(page);
      expect(pageField.page).toBe(page);

      page = scout.create(SearchFormPage, {parent: scout.create(Outline, {parent: session.desktop})});
      expect(() => pageField.setPage(page)).toThrowError();
    });

    it('is the only page in the dummy outline and will be selected', () => {
      let pageField = scout.create(PageField, {parent: session.desktop, page: SearchFormPage});
      expect(pageField.outline.nodes).toEqual([pageField.page]);
      expect(pageField.outline.nodes[0].childNodes.length).toBe(0);
      expect(pageField.outline.selectedNodes).toEqual([pageField.page]);

      let oldPage = pageField.page;
      pageField.setPage(SearchFormPage);
      expect(oldPage.destroyed).toBe(true);
      expect(pageField.outline.nodes).toEqual([pageField.page]);
      expect(pageField.outline.nodes[0].childNodes.length).toBe(0);
      expect(pageField.outline.selectedNodes).toEqual([pageField.page]);
    });

    it('is set to null if node is deleted from outline', () => {
      let pageField = scout.create(PageField, {parent: session.desktop, page: SearchFormPage});
      expect(pageField.outline.nodes).toEqual([pageField.page]);

      pageField.outline.deleteAllNodes();
      expect(pageField.page).toBe(null);
    });

    it('is set if page is inserted into the outline', () => {
      let pageField = scout.create(PageField, {parent: session.desktop});
      expect(pageField.page).toBeUndefined();

      let page = scout.create(SearchFormPage, {parent: pageField.outline});
      pageField.outline.insertNode(page);
      expect(pageField.outline.nodes).toEqual([pageField.page]);
    });

    it('prevents insertion of multiple root nodes', () => {
      let pageField = scout.create(PageField, {parent: session.desktop});
      expect(pageField.page).toBeUndefined();

      let page = scout.create(SearchFormPage, {parent: pageField.outline});
      let page2 = scout.create(SearchFormPage, {parent: pageField.outline});
      expect(() => pageField.outline.insertNodes([page, page2])).toThrowError();

      pageField.setPage(page);
      let page3 = scout.create(SearchFormPage, {parent: pageField.outline});
      expect(() => pageField.outline.insertNode(page3)).toThrowError();
    });
  });

  describe('search form field', () => {
    it('shows search form of page', () => {
      let pageField = scout.create(PageField, {parent: session.desktop});
      expect(pageField.searchFormField.innerForm).toBe(null);

      pageField.setPage({objectType: SearchFormPage});
      expect(pageField.searchFormField.innerForm).toBe(pageField.page.detailTable.findTableControl(SearchFormTableControl).form);

      pageField.setPage(null);
      expect(pageField.searchFormField.innerForm).toBe(null);
    });

    it('is hidden if there is no search form', () => {
      let pageField = scout.create(PageField, {parent: session.desktop});
      expect(pageField.searchFormField.visible).toBe(false);

      pageField.setPage({objectType: SearchFormPage});
      expect(pageField.searchFormField.visible).toBe(true);

      pageField.setPage(null);
      expect(pageField.searchFormField.visible).toBe(false);
    });

    it('is updated if search form changes', () => {
      let pageField = scout.create(PageField, {parent: session.desktop});
      pageField.setPage({objectType: SearchFormPage});
      expect(pageField.searchFormField.innerForm).toBe(pageField.page.detailTable.findTableControl(SearchFormTableControl).form);
      expect(pageField.searchFormField.visible).toBe(true);

      pageField.page.detailTable.findTableControl(SearchFormTableControl).setForm(null);
      expect(pageField.searchFormField.innerForm).toBe(null);
      expect(pageField.searchFormField.visible).toBe(false);

      pageField.page.detailTable.findTableControl(SearchFormTableControl).setForm({objectType: SearchForm});
      expect(pageField.searchFormField.innerForm).toBe(pageField.page.detailTable.findTableControl(SearchFormTableControl).form);
      expect(pageField.searchFormField.visible).toBe(true);
    });

    it('is updated if detail table changes', () => {
      let pageField = scout.create(PageField, {parent: session.desktop});
      pageField.setPage({objectType: SearchFormPage});
      expect(pageField.searchFormField.innerForm).toBe(pageField.page.detailTable.findTableControl(SearchFormTableControl).form);
      expect(pageField.searchFormField.visible).toBe(true);

      pageField.page.setDetailTable(scout.create(Table, {parent: pageField.page.outline})); // table does not have table controls
      expect(pageField.searchFormField.innerForm).toBe(null);
      expect(pageField.searchFormField.visible).toBe(false);

      pageField.page.setDetailTable(scout.create(Table, {
        parent: pageField.page.outline,
        tableControls: [
          {
            objectType: SearchFormTableControl,
            form: {objectType: SearchForm}
          }
        ]
      }));
      expect(pageField.searchFormField.innerForm).toBe(pageField.page.detailTable.findTableControl(SearchFormTableControl).form);
      expect(pageField.searchFormField.visible).toBe(true);

      pageField.page.setDetailTable(null);
      expect(pageField.searchFormField.innerForm).toBe(null);
      expect(pageField.searchFormField.visible).toBe(false);
    });
  });

  describe('table field', () => {
    it('shows detail table of page', () => {
      let pageField = scout.create(PageField, {parent: session.desktop});
      expect(pageField.tableField.table).toBe(null);

      pageField.setPage({objectType: SearchFormPage});
      expect(pageField.tableField.table).toBe(pageField.page.detailTable);

      pageField.setPage(null);
      expect(pageField.tableField.table).toBe(null);
    });

    it('is hidden if there is no detail table', () => {
      let pageField = scout.create(PageField, {parent: session.desktop});
      expect(pageField.tableField.visible).toBe(false);

      pageField.setPage({objectType: SearchFormPage});
      expect(pageField.tableField.visible).toBe(true);

      pageField.setPage(null);
      expect(pageField.tableField.visible).toBe(false);
    });

    it('hides search form table control', () => {
      let pageField = scout.create(PageField, {parent: session.desktop, page: SearchFormPage});
      expect(pageField.tableField.table.findTableControl(SearchFormTableControl).visible).toBe(false);
    });

    it('moves table menubar to bottom', () => {
      let pageField = scout.create(PageField, {parent: session.desktop, page: SearchFormPage});
      expect(pageField.tableField.table.menuBar.position).toBe(MenuBar.Position.BOTTOM);
    });

    it('is updated if detail table changes', () => {
      let pageField = scout.create(PageField, {parent: session.desktop});
      pageField.setPage({objectType: SearchFormPage});
      expect(pageField.tableField.table).toBe(pageField.page.detailTable);
      expect(pageField.tableField.visible).toBe(true);

      pageField.page.setDetailTable(null);
      expect(pageField.tableField.table).toBe(null);
      expect(pageField.tableField.visible).toBe(false);

      pageField.page.setDetailTable(scout.create(Table, {parent: pageField.page.outline}));
      expect(pageField.tableField.table).toBe(pageField.page.detailTable);
      expect(pageField.tableField.visible).toBe(true);
      expect(pageField.tableField.table.menuBar.position).toBe(MenuBar.Position.BOTTOM);
    });
  });
});
