/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {scout, Session, TableField, TableFieldAdapter} from '../../../../src/index';
import {FormSpecHelper, TableSpecHelper} from '../../../../src/testing/index';
import {FullModelOf, InitModelOf} from '../../../../src/scout';

describe('TableFieldAdapter', () => {
  let session: SandboxSession, helper: FormSpecHelper, tableHelper: TableSpecHelper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    tableHelper = new TableSpecHelper(session);
    helper = new FormSpecHelper(session);
    $.fx.off = true;
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(() => {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
    $.fx.off = false;
  });

  function createTableFieldWithTableModel() {
    let table = tableHelper.createModelFixture(2, 2);
    registerAdapterData(table, session);
    return createTableFieldModel({table: table.id});
  }

  function createTableFieldAdapter(model: InitModelOf<TableFieldAdapter>): TableFieldAdapter {
    return scout.create(TableFieldAdapter, $.extend({}, model));
  }

  function createTableFieldModel(tableModel: any): FullModelOf<TableField> & { session: Session; id: string; objectType: string } {
    return helper.createFieldModel('TableField', session.desktop, tableModel) as FullModelOf<TableField> & { session: Session; id: string; objectType: string };
  }

  describe('property table', () => {

    it('destroys the table and model adapter if value is changed to \'\'', () => {
      let model = createTableFieldWithTableModel();
      let adapter = createTableFieldAdapter(model);
      let tableField = adapter.createWidget(model, session.desktop) as TableField;
      let table = tableField.table;
      expect(session.getModelAdapter(table.id).widget).toBe(table);

      let message = {
        events: [createPropertyChangeEvent(tableField, {table: ''})]
      };
      session._processSuccessResponse(message);
      expect(tableField.table).toBeFalsy();
      expect(table.destroyed).toBe(true);
      expect(session.getModelAdapter(table.id)).toBeFalsy();
    });
  });
});
