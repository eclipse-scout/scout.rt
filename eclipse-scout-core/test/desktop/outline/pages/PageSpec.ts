/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Form, GroupBox, Outline, Page, PageWithTable, scout, Table, Widget} from '../../../../src';
import {MenuSpecHelper, OutlineSpecHelper, TableSpecHelper} from '../../../../src/testing';
import {ChildModelOf} from '../../../../src/scout';

describe('Page', () => {

  let session: SandboxSession;
  let outline: Outline;
  let tableHelper: TableSpecHelper;
  let menuHelper: MenuSpecHelper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    tableHelper = new TableSpecHelper(session);
    menuHelper = new MenuSpecHelper(session);
    outline = new OutlineSpecHelper(session).createOutline();
  });

  it('detailTable and detailForm are created lazily on page activation when created as object', () => {
    // test with object
    let page = createAndInsertPage({
      objectType: Table
    }, {
      objectType: Form
    });
    expect(page.detailTable).toBeFalsy();
    expect(page.detailForm).toBeFalsy();
    expectListenersToBeExecuted(0, page); // not created yet
    outline.selectNode(page); // page is activated
    expect(page.detailTable).toBeInstanceOf(Table);
    expect(page.detailForm).toBeInstanceOf(Widget);
    expectListenersToBeExecuted(2, page); // both listeners executed
  });

  it('detailTable and detailForm are initialized when passed as widget', () => {
    // if form or table is directly provided as widget: it is available right from the start
    let page = createAndInsertPage(
      scout.create(Table, {parent: outline}),
      scout.create(Form, {parent: outline}));
    expect(page.detailTable).toBeInstanceOf(Table);
    expect(page.detailForm).toBeInstanceOf(Widget);
    expectListenersToBeExecuted(2, page); // both listeners already executed without selecting the page
  });

  it('detailTable and detailForm are destroyed when overwritten', () => {
    let page = createAndInsertPage({
      objectType: Table
    }, {
      objectType: Form
    });
    outline.selectNode(page);
    expect(page.detailTable).toBeInstanceOf(Table);
    expect(page.detailForm).toBeInstanceOf(Widget);

    let oldForm = page.detailForm;
    let newForm = scout.create(Form, {parent: outline});
    page.setDetailForm(newForm);
    expect(oldForm.destroyed).toBe(true);
    expect(newForm.destroyed).toBe(false);

    let oldTable = page.detailTable;
    let newTable = scout.create(Table, {parent: outline});
    page.setDetailTable(newTable);
    expect(oldTable.destroyed).toBe(true);
    expect(newTable.destroyed).toBe(false);
  });

  it('detailTable and detailForm are enhanced with parent table page menus', () => {
    const parentPage = createAndInsertPage({
      objectType: Table,
      menus: [
        menuHelper.createModel('EmptySpaceMenu'),
        menuHelper.createModel('SingleSelectionMenu', null, [Table.MenuTypes.SingleSelection]),
        menuHelper.createModel('MultiSelectionMenu', null, [Table.MenuTypes.MultiSelection]),
        menuHelper.createModel('SingleMultiSelectionMenu', null, [Table.MenuTypes.SingleSelection, Table.MenuTypes.MultiSelection])
      ]
    }, null);
    parentPage.createChildPage = row => scout.create(Page, {
      parent: outline,
      detailTable: {
        objectType: Table,
        menus: [menuHelper.createModel('DetailTableSingleSelectionMenu', null, [Table.MenuTypes.SingleSelection])]
      },
      detailForm: {
        objectType: Form,
        rootGroupBox: {
          id: 'MainBox',
          objectType: GroupBox,
          menus: [menuHelper.createModel('DetailFormMenu')]
        }
      }
    });
    outline.selectNode(parentPage);

    parentPage.detailTable.insertRow(tableHelper.createModelRow('0', ['Foo']));

    const page = parentPage.childNodes[0];
    outline.selectNode(page);

    const detailFormRootGroupBox = page.detailForm.rootGroupBox;
    const detailTable = page.detailTable;
    const extractTextAndMenuTypes = m => ({
      text: m.text,
      menuTypes: m.menuTypes
    });
    const expectedParentTablePageMenus = [
      {
        text: 'SingleSelectionMenu',
        menuTypes: []
      }, {
        text: 'SingleMultiSelectionMenu',
        menuTypes: []
      }
    ];

    expect(detailFormRootGroupBox.menus.map(extractTextAndMenuTypes)).toEqual([...expectedParentTablePageMenus, {
      text: 'DetailFormMenu',
      menuTypes: []
    }]);

    expect(detailTable.menus.map(extractTextAndMenuTypes)).toEqual([...expectedParentTablePageMenus, {
      text: 'DetailTableSingleSelectionMenu',
      menuTypes: [Table.MenuTypes.SingleSelection]
    }]);

    detailTable.setMenus([menuHelper.createModel('DetailTableMultiSelectionMenu', null, [Table.MenuTypes.MultiSelection])]);
    expect(detailTable.menus.map(extractTextAndMenuTypes)).toEqual([...expectedParentTablePageMenus, {
      text: 'DetailTableMultiSelectionMenu',
      menuTypes: [Table.MenuTypes.MultiSelection]
    }]);
  });

  function createAndInsertPage(detailTable: ChildModelOf<Table>, detailForm: ChildModelOf<Form>, parentPage?: Page): PageWithLazyCreationCounter {
    const page = createPage(detailTable, detailForm);
    insertPage(page, parentPage);
    return page;
  }

  function createPage(detailTable: ChildModelOf<Table>, detailForm: ChildModelOf<Form>): PageWithLazyCreationCounter {
    const page = new PageWithLazyCreationCounter();
    page.on('propertyChange:detailForm', e => e.source.numFormCreated++);
    page.on('propertyChange:detailTable', e => e.source.numTableCreated++);
    const pageModel = {
      parent: outline,
      detailTable,
      detailForm
    };
    page.init(pageModel);
    return page;
  }

  function insertPage(page: Page, parentPage?: Page) {
    outline.insertNodes([page], parentPage);
  }

  function expectListenersToBeExecuted(expectation, page) {
    expect(page.numTableCreated).toBe(expectation);
    expect(page.numFormCreated).toBe(expectation);
  }

  class PageWithLazyCreationCounter extends PageWithTable {

    numTableCreated: number;
    numFormCreated: number;

    constructor() {
      super();
      this.numTableCreated = 0;
      this.numFormCreated = 0;
    }

    protected override _initDetailForm(form: Form) {
      super._initDetailForm(form);
      expect(form).toBeInstanceOf(Form);
      this.numFormCreated++;
    }

    protected override _initDetailTable(table: Table) {
      super._initDetailTable(table);
      expect(table).toBeInstanceOf(Table);
      this.numTableCreated++;
    }
  }
});
