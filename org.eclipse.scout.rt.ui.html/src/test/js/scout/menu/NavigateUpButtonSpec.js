describe("NavigateUpButton", function() {

  var session, outline, menu, node = {};

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    outline = {session: session};
    menu = new scout.NavigateUpButton(outline, node);
  });

  it("_toggleDetail is always true", function() {
    expect(menu._toggleDetail()).toBe(true);
  });

  it("_isDetail returns true or false depending on the state of the detail-form and detail-table", function() {
    // false when both detailForm and detailTable are visible
    node.detailForm = {};
    node.detailFormVisible = true;
    node.detailFormVisibleByUi = true;
    node.detailTable = {};
    node.detailTableVisible = true;
    expect(menu._isDetail()).toBe(false);

    // false when detailForm is absent, even when if detailFormVisible=true
    delete node.detailForm;
    expect(menu._isDetail()).toBe(false);
    node.detailForm = {};

    // false when detailTable is absent, even when if detailTableVisible=true
    delete node.detailTable;
    expect(menu._isDetail()).toBe(false);
    node.detailTable = {};

    // true when detailForm is hidden by UI
    node.detailFormVisibleByUi = false;
    expect(menu._isDetail()).toBe(true);
    node.detailFormVisibleByUi = true;

    // false when property says to
    node.detailFormVisible = false;
    expect(menu._isDetail()).toBe(false);
    node.detailFormVisible = true;
    node.detailTableVisible = false;
    expect(menu._isDetail()).toBe(false);
  });

  describe("_buttonEnabled", function() {

    it("is true when current node has a parent or...", function() {
      node.parentNode = {};
      outline.defaultDetailForm = undefined;
      expect(menu._buttonEnabled()).toBe(true);
    });

    it("is true when current node is a top-level node and outline a default detail-form or...", function() {
      node.parentNode = undefined;
      outline.defaultDetailForm = {};
      expect(menu._buttonEnabled()).toBe(true);
    });

    it("is false otherwise", function() {
      node.parentNode = undefined;
      outline.defaultDetailForm = undefined;
      expect(menu._buttonEnabled()).toBe(false);
    });

  });

  describe("_drill", function() {

    beforeEach(function() {
      outline.setNodesSelected = function(node) {};
      outline.setNodeExpanded = function(node, expanded) {};
    });

    it("drills up to parent node, sets the selection on the tree", function() {
      node.parentNode = {};
      spyOn(outline, 'setNodesSelected');
      spyOn(outline, 'setNodeExpanded');
      menu._drill();
      expect(outline.navigateUpInProgress).toBe(true);
      expect(outline.setNodesSelected).toHaveBeenCalledWith(node.parentNode);
      expect(outline.setNodeExpanded).toHaveBeenCalledWith(node.parentNode, false);
    });

    it("shows default detail-form by removing selection from tree", function() {
      node.parentNode = undefined;
      menu.drill;
      spyOn(outline, 'setNodesSelected');
      menu._drill();
      expect(outline.setNodesSelected).toHaveBeenCalledWith([]);
    });

  });

});
