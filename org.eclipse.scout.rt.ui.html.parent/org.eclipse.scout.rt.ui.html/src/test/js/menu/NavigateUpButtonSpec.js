describe("NavigateUpButton", function() {

  var session, outline, menu, node = {};

  beforeEach(function() {
    session = new scout.Session($('#sandbox'), '1.1');
    outline = {session: session};
    menu = new scout.NavigateUpButton(outline, node);
  });

  it("_toggleDetail is always true", function() {
    expect(menu._toggleDetail()).toBe(true);
  });

  it("_isDetail is true when detail-form is not visible", function() {
    node.detailFormVisible = true;
    expect(menu._isDetail()).toBe(false);
    node.detailFormVisible = false;
    expect(menu._isDetail()).toBe(true);
  });

  describe("_menuEnabled", function() {

    it("is true when current node has a parent or...", function() {
      node.parentNode = {};
      outline.defaultDetailForm = undefined;
      expect(menu._menuEnabled()).toBe(true);
    });

    it("is true when current node is a top-level node and outline a default detail-form or...", function() {
      node.parentNode = undefined;
      outline.defaultDetailForm = {};
      expect(menu._menuEnabled()).toBe(true);
    });

    it("is false otherwise", function() {
      node.parentNode = undefined;
      outline.defaultDetailForm = undefined;
      expect(menu._menuEnabled()).toBe(false);
    });

  });

  describe("_drill", function() {

    beforeEach(function() {
      outline.setNodesSelected = function(node) {};
      outline.setNodeExpanded = function(node, $node, expanded) {};
    });

    it("drills up to parent node, sets the selection on the tree", function() {
      node.parentNode = {};
      spyOn(outline, 'setNodesSelected');
      spyOn(outline, 'setNodeExpanded');
      menu._drill();
      expect(outline._navigateUp).toBe(true);
      expect(outline.setNodesSelected).toHaveBeenCalledWith(node.parentNode);
      expect(outline.setNodeExpanded).toHaveBeenCalledWith(node.parentNode, undefined, false);
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
