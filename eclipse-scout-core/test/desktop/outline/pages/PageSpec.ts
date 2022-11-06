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
import {Form, Outline, Page, PageWithTable, scout, Table, Widget} from '../../../../src';
import {OutlineSpecHelper} from '../../../../src/testing';
import {ChildModelOf} from '../../../../src/scout';

describe('Page', () => {

  let session: SandboxSession;
  let outline: Outline;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
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

  function createAndInsertPage(detailTable: ChildModelOf<Table>, detailForm: ChildModelOf<Form>) {
    let page = new PageWithLazyCreationCounter();
    page.on('propertyChange:detailForm', e => e.source.numFormCreated++);
    page.on('propertyChange:detailTable', e => e.source.numTableCreated++);
    let pageModel = {
      parent: outline,
      detailTable: detailTable,
      detailForm: detailForm
    };
    page.init(pageModel);
    outline.insertNodes([page], null);
    return page;
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
