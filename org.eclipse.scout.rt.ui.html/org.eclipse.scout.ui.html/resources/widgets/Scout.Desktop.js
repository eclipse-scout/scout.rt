// SCOUT GUI 0.2
// (c) Copyright 2013-2014, BSI Business Systems Integration AG
//
// desktop: contains viewButtons, main_tree, (empty) bench, tools
//

Scout.Desktop = function (scout, $parent, data) {
  this.handleUpdate = function handleUpdate(event) {
    if(event.outline !== undefined) {
      this.tree.outlineId = event.outline.id;
      this.tree.clearNodes();
      this.tree.addNodes(event.outline.pages);
    }
    else if(event.nodesAdded !== undefined) {
      var nodes = event.nodesAdded;
     //TODO work with e.getCommonParentNode()
      //TODO move to outline/tree.js?
      for(var i in nodes) {
        var $parentNode = this.tree.$div.find("#"+ nodes[i].parentNodeId);
        var tmpNodes = new Array();
        tmpNodes[0]=nodes[i];
        this.tree.addNodes(tmpNodes,$parentNode);
      }
    }
  };
  scout.widgetMap[data.id] = this;

  // create all 4 containers
  this.viewButtonBar = new Scout.Desktop.ViewButtonBar(scout, $parent, data.viewButtons);
//  var tool = new Scout.Desktop.Tool(scout, $parent, data.tools);

  this.tree = new Scout.Desktop.Tree(scout, $parent, data.outline);
  this.bench = new Scout.Desktop.Bench(scout, $parent);

  // show node
//  var nodes = scout.syncAjax('drilldown', widget.start);
//  tree.addNodes(nodes);
};

