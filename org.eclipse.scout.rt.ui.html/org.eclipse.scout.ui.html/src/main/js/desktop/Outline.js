Scout.Outline = function (scout, tree, jsonOutline) {
  scout.widgetMap[jsonOutline.id] = this;

  this.onModelAction = onModelAction;
  this.onModelPropertyChange = onModelPropertyChange;
  this.jsonOutline = jsonOutline;

  function onModelPropertyChange(event) {
  }

  function onModelAction(event) {
    if (event.type_ == 'nodesInserted') {
      tree.addNodes(event.nodes, event.commonParentNodeId);
    }
    else if (event.type_ == 'nodesSelected') {
      tree.setNodeSelectedById(event.nodeIds[0]);
    }
    else if (event.type_ == 'nodeExpanded') {
      tree.setNodeExpandedById(event.nodeId, event.expanded);
    }
  }
};
