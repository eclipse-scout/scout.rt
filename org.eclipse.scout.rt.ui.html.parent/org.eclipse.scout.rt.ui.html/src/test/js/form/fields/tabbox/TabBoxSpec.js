/* global FormSpecHelper */
describe("TabBox", function() {
  var session;
  var helper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = new scout.Session($('#sandbox'), '1.1');
    helper = new FormSpecHelper(session);
  });

  function createTabBox(tabItems) {
    var model = helper.createFieldModel('TabBox');

    model.tabItems = [];
    for (var i=0; i < tabItems.length; i++) {
      model.tabItems.push(tabItems[i].id);
    }
    model.selectedTab = 0;
    model.parent = session.rootAdapter.id;

    return createAdapter(model, session, tabItems);
  }

  describe("render", function() {
    var field;

    beforeEach(function() {
      var groupBox = helper.createFieldModel('TabItem');
      field = createTabBox([groupBox]);
    });

    it("does NOT call layout for the selected tab on initialization", function() {
      spyOn(session.layoutValidator, 'invalidateTree').and.callThrough();
      field.render(session.$entryPoint);
      expect(session.layoutValidator.invalidateTree).not.toHaveBeenCalled();
    });

  });

});
