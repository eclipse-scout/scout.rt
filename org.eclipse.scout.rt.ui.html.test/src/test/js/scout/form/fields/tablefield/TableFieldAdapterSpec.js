/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
describe("TableFieldAdapter", function() {
  var session, helper, tableHelper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    tableHelper = new scout.TableSpecHelper(session);
    helper = new scout.FormSpecHelper(session);
    $.fx.off = true;
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(function() {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
    $.fx.off = false;
  });

  function createTableFieldWithTableModel() {
    var table = tableHelper.createModelFixture(2, 2);
    registerAdapterData(table, session);
    return createTableFieldModel({table: table.id});
  }

  function createTableFieldAdapter(model) {
    return scout.create('TableFieldAdapter', createAdapterModel(model));
  }

  function createTableFieldModel(tableModel) {
    return helper.createFieldModel('TableField', session.desktop, tableModel);
  }

  describe('property table', function() {

    it('destroys the table and model adapter if value is changed to \'\'', function() {
      var model = createTableFieldWithTableModel();
      var adapter = createTableFieldAdapter(model);
      var tableField = adapter.createWidget(model, session.desktop);
      var table = tableField.table;
      expect(session.getModelAdapter(table.id).widget).toBe(table);

      var message = {
        events: [createPropertyChangeEvent(tableField, {table: ''})]
      };
      session._processSuccessResponse(message);
      expect(tableField.table).toBeFalsy();
      expect(table.destroyed).toBe(true);
      expect(session.getModelAdapter(table.id)).toBeFalsy();
    });
  });
});
