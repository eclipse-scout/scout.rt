describe("NavigateDownButton", function() {

  var session, outline, menu, node = {};

  beforeEach(function() {
    session = new scout.Session($('#sandbox'), '1.1');
    outline = {session: session};
    menu = new scout.NavigateDownButton(outline, node);
  });

  it("_toggleDetail is always false", function() {
    expect(menu._toggleDetail()).toBe(false);
  });

  it("_isDetail is true when detail-form is visible", function() {
    node.detailFormVisible = true;
    expect(menu._isDetail()).toBe(true);
    node.detailFormVisible = false;
    expect(menu._isDetail()).toBe(false);
  });

  it("_menuEnabled is true when node is not a leaf", function() {
    node.leaf = true;
    expect(menu._menuEnabled()).toBe(false);
    node.leaf = false;
    expect(menu._menuEnabled()).toBe(true);
  });

  it("_drill drills down to first selected row in the detail table", function() {
    var drillNode = {};
    node.detailTable = {
      selectedRowIds: ['123'],
      rowById: function(rowIds) {
        return {
          nodeId: '123'
        };
      }
    };
    outline._nodeMap = {'123': drillNode};
    outline.setNodesSelected = function(node) {};
    outline.setNodeExpanded = function(node, $node, expanded) {};

    spyOn(outline, 'setNodesSelected');
    spyOn(outline, 'setNodeExpanded');
    menu._drill();
    expect(outline.setNodesSelected).toHaveBeenCalledWith(drillNode);
    expect(outline.setNodeExpanded).toHaveBeenCalledWith(drillNode, undefined, false);
  });

});
