/* global TableSpecHelper */
describe("TableField", function() {
  var session;
  var helper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = new scout.Session($('#sandbox'), '1.1');
    helper = new TableSpecHelper(session);
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(function() {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  function createTableField(model) {
    var tableField = new scout.TableField();
    tableField.init(model, session);
    return tableField;
  }

  function createModel(id) {
    if (id === undefined) {
      id = createUniqueAdapterId();
    }
    var model =  {
      "id": id,
      "enabled": true,
      "visible": true
    };
    return model;
  }

  describe("property table", function() {
    var tableField, table;
    beforeEach(function() {
      var tableModel = helper.createModelFixture(2, 2);
      table = helper.createTable(tableModel);
      tableField = createTableField(createModel());
    });

    it("shows (renders) the table if the value is set", function() {
      tableField.render(session.$entryPoint);

      expect(table.rendered).toBe(false);
      var message = {
        events: [createPropertyChangeEvent(tableField, {table: table.id})]
      };
      session._processSuccessResponse(message);

      expect(table.rendered).toBe(true);
    });

    it("hides (removes) the table if value is changed to ''", function() {
      tableField.table = table;
      tableField.render(session.$entryPoint);

      expect(table.rendered).toBe(true);
      var message = {
        events: [createPropertyChangeEvent(tableField, {table: ''})]
      };
      session._processSuccessResponse(message);

      expect(table.rendered).toBe(false);
    });

    it("table gets class 'field' to make it work with the form field layout", function() {
      tableField.table = table;
      tableField.render(session.$entryPoint);

      expect(table.$container).toHaveClass('field');
    });

    it("table gets class 'field' to make it work with the form field layout (also when loaded by property change event)", function() {
      tableField.render(session.$entryPoint);

      expect(table.rendered).toBe(false);
      var message = {
          events: [createPropertyChangeEvent(tableField, {table: table.id})]
      };
      session._processSuccessResponse(message);

      expect(table.$container).toHaveClass('field');
    });
  });
});
