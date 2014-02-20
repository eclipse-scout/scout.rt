// SCOUT GUI 0.2
// (c) Copyright 2013-2014, BSI Business Systems Integration AG
//
// desktop: contains viewButtons, main_tree, (empty) bench, tools
//

Scout.Desktop = function (scout, $parent, data) {

  scout.widgetMap[data.id] = this;

  // create all 4 containers
  this.viewButtonBar = new Scout.DesktopViewButtonBar(scout, $parent, data.viewButtons);
//  var tool = new Scout.DesktopTool(scout, $parent, data.tools);

  this.tree = new Scout.DesktopTree(scout, $parent, data.outline);
  this.bench = new Scout.DesktopBench(scout, $parent);

  // show node
//  var nodes = scout.syncAjax('drilldown', widget.start);
//  tree.addNodes(nodes);

  this.onModelPropertyChange = function onModelPropertyChange(event) {
  };

  this.onModelAction = function onModelAction(event) {
    if(event.type_=="outlineChanged") {
      this.tree.outlineId = event.outline.id;
      this.tree.clearNodes();
      this.tree.addNodes(event.outline.pages);
      return;
    }
    if(event.type_=="nodesAdded") {
      //TODO work with e.getCommonParentNode()
      //TODO move to outline/tree.js?
      var nodes = event.nodes;
      for(var i in nodes) {
        var $parentNode = this.tree.$div.find("#"+ nodes[i].parentNodeId);
        var tmpNodes = new Array();
        tmpNodes[0]=nodes[i];
        this.tree.addNodes(tmpNodes,$parentNode);
      }
      return;
    }
  };

};

