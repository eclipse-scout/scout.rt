/* global TableSpecHelper */
describe("CheckBoxColumn", function() {
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

  describe("table checkable column", function() {

    it("a checkbox column gets inserted if table.checkable=true", function() {
      var model = helper.createModelFixture(2);
      model.checkable = true;
      expect(model.columns.length).toBe(2);
      var table = helper.createTable(model);
      expect(table.columns.length).toBe(3);

      table.render(session.$entryPoint);
      expect(table.columns[0] instanceof scout.CheckBoxColumn).toBeTruthy();
    });

    it("no checkbox column gets inserted if table.checkable=false", function() {
      var model = helper.createModelFixture(2);
      model.checkable = false;
      expect(model.columns.length).toBe(2);
      var table = helper.createTable(model);
      expect(table.columns.length).toBe(2);

      table.render(session.$entryPoint);
      expect(table.columns[0] instanceof scout.CheckBoxColumn).toBeFalsy();
    });

    it("this.checkableColumn is set to the new column", function() {
      var model = helper.createModelFixture(2);
      model.checkable = true;
      var table = helper.createTable(model);
      table.render(session.$entryPoint);

      expect(table.checkableColumn).not.toBeUndefined();
      expect(table.checkableColumn).toBe(table.columns[0]);
    });

    it("displays the row.checked state as checkbox", function() {
      var model = helper.createModelFixture(2, 2);
      model.checkable = true;
      model.rows[0].checked = true;
      model.rows[1].checked = false;
      var table = helper.createTable(model);
      var $rows = table.$rows();

      table.render(session.$entryPoint);
      var checked = table.columns[0].$checkBox($rows.eq(0)).prop('checked');
      expect(checked).toBe(true);
      checked = table.columns[0].$checkBox($rows.eq(1)).prop('checked');
      expect(checked).toBe(false);
    });

  });

  describe("boolean column", function() {

    it("displays the cell value as checkbox", function() {
      var model = helper.createModelFixture(2, 2);
      model.columns[0].objectType = 'BooleanColumn';
      model.rows[0].cells[0].value = true;
      model.rows[1].cells[0].value = false;
      var table = helper.createTable(model);
      var $rows = table.$rows();

      table.render(session.$entryPoint);
      var checked = table.columns[0].$checkBox($rows.eq(0)).prop('checked');
      expect(checked).toBe(true);
      checked = table.columns[0].$checkBox($rows.eq(1)).prop('checked');
      expect(checked).toBe(false);
    });

  });

});
