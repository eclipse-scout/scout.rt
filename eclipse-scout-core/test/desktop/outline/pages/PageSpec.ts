/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, Form, GroupBox, Menu, MenuOwner, Outline, Page, PageDetailMenuContributor, PageParamDo, PageWithNodes, PageWithTable, ParentTablePageMenuContributor, scout, Table, TableRow, Widget} from '../../../../src';
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

  it('detailTable and detailForm are destroyed when page is destroyed', () => {
    session.desktop.setBenchVisible(true);
    session.desktop.setOutline(outline);

    let parentPage = scout.create(PageWithNodes, {
      parent: outline,
      childNodes: [
        {
          objectType: Page,
          detailForm: {
            objectType: Form,
            rootGroupBox: {
              objectType: GroupBox
            }
          },
          detailFormVisible: true,
          detailTable: {
            objectType: Table
          },
          detailTableVisible: true
        }
      ]
    });
    outline.insertNode(parentPage);
    outline.expandNode(parentPage);
    let page = parentPage.childNodes[0];
    outline.selectNode(page);

    expect(page.detailTable).toBeInstanceOf(Table);
    expect(page.detailForm).toBeInstanceOf(Widget);
    let detailTable = page.detailTable;
    let detailForm = page.detailForm;

    outline.deleteNode(page);
    expect(page.destroyed).toBe(true);
    expect(detailForm.destroyed).toBe(true);
    expect(detailTable.destroyed).toBe(true);
    expect(page.detailForm).toBe(null);
    expect(page.detailTable).toBe(null);
  });

  it('detailTable and detailForm are enhanced with parent table page menus', () => {
    const parentPage = createAndInsertPage({
      objectType: Table,
      menus: [
        menuHelper.createModel('EmptySpaceMenu'),
        menuHelper.createModel('SingleSelectionMenu', [Table.MenuType.SingleSelection]),
        menuHelper.createModel('MultiSelectionMenu', [Table.MenuType.MultiSelection]),
        menuHelper.createModel('SingleMultiSelectionMenu', [Table.MenuType.SingleSelection, Table.MenuType.MultiSelection]),
        {
          ...menuHelper.createModel('SingleSelectionMenuWithChildMenus', [Table.MenuType.SingleSelection]),
          childActions: [
            menuHelper.createModel('ChildSingleSelectionMenu', [Table.MenuType.SingleSelection]),
            menuHelper.createModel('ChildMultiSelectionMenu', [Table.MenuType.MultiSelection])
          ]
        }
      ]
    }, null);
    parentPage._createChildPage = row => scout.create(Page, {
      parent: outline,
      detailTable: {
        objectType: Table,
        menus: [menuHelper.createModel('DetailTableSingleSelectionMenu', [Table.MenuType.SingleSelection])]
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
      menuTypes: m.menuTypes,
      childActions: arrays.ensure(m.childActions).map(child => extractTextAndMenuTypes(child))
    });
    const expectedParentTablePageMenus = [
      {
        text: 'SingleSelectionMenu',
        menuTypes: [],
        childActions: []
      }, {
        text: 'SingleMultiSelectionMenu',
        menuTypes: [],
        childActions: []
      }, {
        text: 'SingleSelectionMenuWithChildMenus',
        menuTypes: [],
        childActions: [{
          text: 'ChildSingleSelectionMenu',
          menuTypes: [],
          childActions: []
        }]
      }
    ];

    expect(detailFormRootGroupBox.menus.map(extractTextAndMenuTypes)).toEqual([...expectedParentTablePageMenus, {
      text: 'DetailFormMenu',
      menuTypes: [],
      childActions: []
    }]);

    expect(detailTable.menus.map(extractTextAndMenuTypes)).toEqual([...expectedParentTablePageMenus, {
      text: 'DetailTableSingleSelectionMenu',
      menuTypes: [Table.MenuType.SingleSelection],
      childActions: []
    }]);

    detailTable.setMenus([menuHelper.createModel('DetailTableMultiSelectionMenu', [Table.MenuType.MultiSelection])]);
    expect(detailTable.menus.map(extractTextAndMenuTypes)).toEqual([...expectedParentTablePageMenus, {
      text: 'DetailTableMultiSelectionMenu',
      menuTypes: [Table.MenuType.MultiSelection],
      childActions: []
    }]);
  });

  describe('computeTextForRow', () => {

    it('considers summary columns', () => {
      const page = scout.create(Page, {parent: outline});
      const table = tableHelper.createTable(tableHelper.createModel(
        tableHelper.createModelColumns(5),
        $.extend(true, [], tableHelper.createModelRows(5, 2), [{cells: ['a', 'b', 'c', 'd', 'e']}, {cells: ['1', '2', '3', '4', '5']}])
      ));
      const [rowAbc, row123] = table.rows;

      expect(page.computeTextForRow(rowAbc)).toBe('a');
      expect(page.computeTextForRow(row123)).toBe('1');

      table.columns[1].setSummary(true);
      expect(page.computeTextForRow(rowAbc)).toBe('b');
      expect(page.computeTextForRow(row123)).toBe('2');

      table.columns[4].setSummary(true);
      expect(page.computeTextForRow(rowAbc)).toBe('b e');
      expect(page.computeTextForRow(row123)).toBe('2 5');

      table.columns[0].setSummary(true);
      table.columns[1].setSummary(false);
      table.columns[3].setSummary(true);
      expect(page.computeTextForRow(rowAbc)).toBe('a d e');
      expect(page.computeTextForRow(row123)).toBe('1 4 5');
    });
  });

  describe('uuid', () => {

    it('uuidPath for remote page includes parent', () => {
      outline.classId = 'outline-class-id'; // outline contains only own classId without any parents (see AbstractOutline.classId)
      const page = scout.create(Page, {
        parent: outline,
        classId: 'page-class-id' // page contains only own classId without any parents (see AbstractPage.classId)
      });
      expect(page.uuidPath()).toBe('page-class-id|outline-class-id');
      expect(outline.uuidPath()).toBe('outline-class-id');
    });

    it('uuidPath for local page includes parent', () => {
      outline.uuid = 'outline-uuid'; // outline contains only own uuid without any parents
      const page = scout.create(Page, {
        parent: outline,
        uuid: 'page-uuid' // page contains only own uuid without any parents
      });
      expect(page.uuidPath()).toBe('page-uuid|outline-uuid');
      expect(outline.uuidPath()).toBe('outline-uuid');
    });

    it('ObjectUuidBuilder.buildId returns id without parent for local and remote case', () => {
      outline.classId = 'outline-class-id';
      const remotePage = scout.create(Page, {
        parent: outline,
        classId: 'page-class-id'
      });

      expect(remotePage.getObjectUuidBuilder().buildId()).toBe('page-class-id');
      expect(outline.getObjectUuidBuilder().buildId()).toBe('outline-class-id');

      outline.classId = null;
      outline.uuid = 'outline-uuid';
      const localPage = scout.create(Page, {
        parent: outline,
        uuid: 'page-uuid'
      });

      expect(localPage.getObjectUuidBuilder().buildId()).toBe('page-uuid');
      expect(outline.getObjectUuidBuilder().buildId()).toBe('outline-uuid');
    });
  });

  describe('pageDetailMenuContributors', () => {
    let pageWithDetailFormAndTableModel;

    class MenuContributor extends PageDetailMenuContributor {
      override contribute(originalMenus: Menu[], detailContent: MenuOwner) {
        return [...originalMenus, scout.create(Menu, {
          parent: detailContent,
          text: 'contributed-menu'
        })];
      }
    }

    beforeEach(() => {
      pageWithDetailFormAndTableModel = {
        parent: outline,
        detailTable: {
          objectType: Table,
          menus: [{objectType: Menu, text: 'table-menu'}]
        },
        detailForm: {
          objectType: Form,
          rootGroupBox: {
            objectType: GroupBox,
            menus: [{objectType: Menu, text: 'form-menu'}]
          }
        }
      };
    });

    it('can be extended by the model', () => {
      let page = scout.create(Page, {
        parent: outline
      });
      expect(page.detailMenuContributors.length).toBe(1);
      expect(page.detailMenuContributors[0]).toBeInstanceOf(ParentTablePageMenuContributor);

      let page2 = scout.create(Page, {
        parent: outline,
        detailMenuContributors: [MenuContributor]
      });
      expect(page2.detailMenuContributors.length).toBe(2);
      expect(page2.detailMenuContributors[0]).toBeInstanceOf(ParentTablePageMenuContributor);
      expect(page2.detailMenuContributors[1]).toBeInstanceOf(MenuContributor);
    });

    it('can contribute new menus for detail table and detail form', () => {
      let page = scout.create(Page, {
        ...pageWithDetailFormAndTableModel,
        detailMenuContributors: [MenuContributor]
      });
      outline.insertNode(page);
      outline.selectNode(page);
      expect(page.detailForm.rootGroupBox.menus.map(menu => menu.text)).toEqual(['form-menu', 'contributed-menu']);
    });

    it('don\'t contribute new menus multiple times', () => {
      let page = scout.create(Page, {
        ...pageWithDetailFormAndTableModel,
        detailMenuContributors: [MenuContributor]
      });
      outline.insertNode(page);
      outline.selectNode(page);
      expect(page.detailForm.rootGroupBox.menus.map(menu => menu.text)).toEqual(['form-menu', 'contributed-menu']);
      expect(page.detailTable.menus.map(menu => menu.text)).toEqual(['table-menu', 'contributed-menu']);

      // Contributors are called again when menus of the detail content change, or when the detail content itself changes
      page.detailTable.setMenus([{objectType: Menu, text: 'new-table-menu'}]);
      expect(page.detailTable.menus.map(menu => menu.text)).toEqual(['new-table-menu', 'contributed-menu']);
      expect(page.detailForm.rootGroupBox.menus.map(menu => menu.text)).toEqual(['form-menu', 'contributed-menu']);

      page.detailForm.rootGroupBox.setMenus([{objectType: Menu, text: 'new-form-menu'}]);
      expect(page.detailTable.menus.map(menu => menu.text)).toEqual(['new-table-menu', 'contributed-menu']);
      expect(page.detailForm.rootGroupBox.menus.map(menu => menu.text)).toEqual(['new-form-menu', 'contributed-menu']);

      let newTable = scout.create(Table, {parent: outline});
      page.setDetailTable(newTable);
      expect(page.detailTable.menus.map(menu => menu.text)).toEqual(['contributed-menu']);
      expect(page.detailForm.rootGroupBox.menus.map(menu => menu.text)).toEqual(['new-form-menu', 'contributed-menu']);

      let newForm = scout.create(Form, {parent: outline, rootGroupBox: {objectType: GroupBox}});
      page.setDetailForm(newForm);
      expect(page.detailTable.menus.map(menu => menu.text)).toEqual(['contributed-menu']);
      expect(page.detailForm.rootGroupBox.menus.map(menu => menu.text)).toEqual(['contributed-menu']);
    });

    it('can contribute new menus by cloning existing menus including their child menus', () => {
      class OutlineMenuContributor extends PageDetailMenuContributor {
        override contribute(originalMenus: Menu[], detailContent: MenuOwner) {
          return [...this._cloneMenus(detailContent.findParent(Outline).menus, detailContent), ...originalMenus];
        }
      }

      let page = scout.create(Page, {
        ...pageWithDetailFormAndTableModel,
        detailMenuContributors: [OutlineMenuContributor]
      });
      outline.setMenus([{objectType: Menu, text: 'outline-menu', childActions: [{objectType: Menu, text: 'child'}]}]);
      outline.insertNode(page);
      outline.selectNode(page);
      expect(page.detailForm.rootGroupBox.menus.map(menu => menu.text)).toEqual(['outline-menu', 'form-menu']);
      expect(page.detailForm.rootGroupBox.menus[0].cloneOf).toBe(outline.menus[0]);
      expect(page.detailForm.rootGroupBox.menus[0].childActions[0].cloneOf).toBe(outline.menus[0].childActions[0]);
      expect(page.detailTable.menus.map(menu => menu.text)).toEqual(['outline-menu', 'table-menu']);
      expect(page.detailTable.menus[0].cloneOf).toBe(outline.menus[0]);
      expect(page.detailTable.menus[0].childActions[0].cloneOf).toBe(outline.menus[0].childActions[0]);
    });
  });

  describe('pageParam', () => {
    class SimplePage extends PageWithNodes {
    }

    class PageWithParam extends PageWithNodes {
      declare pageParam: MyPageParamDo;
    }

    class MyPageParamDo extends PageParamDo {
      prop: string;
    }

    it('is mandatory when creating a page if declared explicitly', () => {
      // No param required
      scout.create(SimplePage, {
        parent: outline
      });

      // Page param is missing
      // @ts-expect-error
      scout.create(PageWithParam, {
        parent: outline
      });

      // Wrong param type is used
      scout.create(PageWithParam, {
        parent: outline,
        // @ts-expect-error
        pageParam: scout.create(PageParamDo)
      });

      // Everything ok
      let page = scout.create(PageWithParam, {
        parent: outline,
        pageParam: scout.create(MyPageParamDo, {prop: 'hi'})
      });
      expect(page.pageParam).toBeInstanceOf(MyPageParamDo);
    });
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

    override _createChildPage(row: TableRow): Page {
      return super._createChildPage(row);
    }
  }
});
