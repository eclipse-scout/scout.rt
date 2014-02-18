// SCOUT GUI 0.2
// (c) Copyright 2013-2014, BSI Business Systems Integration AG
//
// desktop: contains viewButtons, main_tree, (empty) bench, tools
//

Scout.Desktop = function (scout, $parent, widget) {
  // create all 4 containers
  var viewButtonBar = new Scout.Desktop.ViewButtonBar(scout, $parent, widget.viewButtons);
//  var tool = new Scout.Desktop.ToolButton(scout, $parent, widget.tools);
  // TODO step 2
//  var tree = new Scout.Desktop.Tree(scout, $parent, widget.pages);
//  var bench = new Scout.Desktop.Bench(scout, $parent);

  // show node
//  var nodes = scout.syncAjax('drilldown', widget.start);
//  tree.addNodes(nodes);
};

