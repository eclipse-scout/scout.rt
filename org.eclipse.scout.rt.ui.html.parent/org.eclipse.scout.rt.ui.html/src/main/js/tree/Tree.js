// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.Tree = function() {
  scout.Tree.parent.call(this);
  this.$data;
  this.selectedNodeIds = [];
  this.nodes = []; // top-level nodes
  this.nodesMap = {}; // all nodes by id
  this._breadcrumb = false;
  this.events = new scout.EventSupport();
  this._addAdapterProperties('menus');

  this._treeItemPaddingLeft = 20;
  this._treeItemCheckBoxPaddingLeft = 20;
  this._treeItemPaddingLevel = 15;

  this.menuBar;
  this.staticMenus = [];
};
scout.inherits(scout.Tree, scout.ModelAdapter);

scout.Tree.prototype.init = function(model, session) {
  scout.Tree.parent.prototype.init.call(this, model, session);
  this._visitNodes(this.nodes, this._initTreeNode.bind(this));
};

scout.Tree.prototype._initTreeNode = function(parentNode, node) {
  this.nodesMap[node.id] = node;
  if (parentNode) {
    node.parentNode = parentNode;
  }
  scout.defaultValues.applyTo(node, 'TreeNode');
  if (node.childNodes === undefined) {
    node.childNodes = [];
  }
  this._updateMarkChildrenChecked(node, true, node.checked);
};

scout.Tree.prototype._visitNodes = function(nodes, func, parentNode) {
  var i, node;
  if (!nodes) {
    return;
  }

  for (i = 0; i < nodes.length; i++) {
    node = nodes[i];
    func(parentNode, node);
    if (node.childNodes.length > 0) {
      this._visitNodes(node.childNodes, func, node);
    }
  }
};

scout.Tree.prototype._updateMarkChildrenChecked = function(node, init, checked, checkChildrenChecked) {
  if (!this.checkable) {
    return;
  }

  if (checkChildrenChecked) {
    var childrenFound = false;
    for (var j = 0; j < node.childNodes.length > 0; j++) {
      var childNode = node.childNodes[j];
      if (childNode.checked || childNode.childrenChecked) {
        node.childrenChecked = true;
        checked = true;
        childrenFound = true;
        if (this.rendered && node.$node) {
          node.$node.find('label').addClass('childrenChecked');
        }
        break;
      }
    }
    if (!childrenFound) {
      node.childrenChecked = false;
      if (this.rendered && node.$node) {
        node.$node.find('label').removeClass('childrenChecked');
      }
    }
  }

  if (!node.parentNode || node.parentNode.checked) {
    return;
  }

  var stateChanged = false;
  if (!checked && !init) {
    //node was unchecked check siblings
    var hasCheckedSiblings = false;
    for (var i = 0; i < node.parentNode.childNodes.length > 0; i++) {
      var siblingNode = node.parentNode.childNodes[i];
      if (siblingNode.checked || siblingNode.childrenChecked) {
        hasCheckedSiblings = true;
        break;
      }
    }
    if (hasCheckedSiblings !== node.parentNode.childrenChecked) {
      //parentNode.checked should be false
      node.parentNode.childrenChecked = hasCheckedSiblings;
      stateChanged = true;
    }
  }
  if ((checked && !node.parentNode.childrenChecked)) {
    node.parentNode.childrenChecked = true;
    stateChanged = true;
  }
  if (stateChanged) {
    this._updateMarkChildrenChecked(node.parentNode, init, checked);
    if (this.rendered && node.parentNode.$node) {
      if (checked) {
        node.parentNode.$node.find('label').addClass('childrenChecked');
      } else {
        node.parentNode.$node.find('label').removeClass('childrenChecked');
      }
    }
  }

};

scout.Tree.prototype._render = function($parent) {
  this.$parent = $parent;
  this.$container = $parent.appendDiv('tree');

  var layout = new scout.TreeLayout(this);
  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(layout);
  this.htmlComp.pixelBasedSizing = false;

  this.$data = this.$container.appendDiv('tree-data');

  scout.scrollbars.install(this.$data);
  this.session.detachHelper.pushScrollable(this.$data);
  this.menuBar = new scout.MenuBar(this.$container, 'top', scout.TreeMenuItemsOrder.order);
  this._addNodes(this.nodes);
  if (this.selectedNodeIds.length > 0) {
    this._renderSelection();
  }
};

scout.Tree.prototype._remove = function() {
  this.session.detachHelper.removeScrollable(this.$data);
  scout.Tree.parent.prototype._remove.call(this);
};

scout.Tree.prototype.onResize = function() {
  this.updateScrollbar();
};

scout.Tree.prototype.updateScrollbar = function() {
  scout.scrollbars.update(this.$data);
};

scout.Tree.prototype.setBreadcrumb = function(bread) {
  if (bread) {
    this._breadcrumb = true;

    var $selected = this.$selectedNodes();

    if ($selected.length > 0) {
      var nodeId = $selected.attr('id'),
        expanded = $selected.hasClass('expanded'),
        node = this.nodesMap[nodeId];

      if (!expanded) {
        this.session.send(this.id, 'nodeAction', {
          nodeId: nodeId
        });
        this.setNodeExpanded(node, $selected, true);
      }
    }
  } else {
    this._breadcrumb = false;
  }
};

scout.Tree.prototype.collapseAll = function() {
  var that = this;

  //Collapse root nodes
  this.$data.find('[data-level="0"]').each(function() {
    var $node = $(this);
    that.setNodeExpanded($node.data('node'), $node, false);
  });

  //Collapse all expanded child nodes (only model)
  this._visitNodes(this.nodes, function(parentNode, node) {
    this.setNodeExpanded(node, null, false);
  }.bind(this));
};

scout.Tree.prototype.setNodeExpanded = function(node, $node, expanded) {
  if (node.expanded !== expanded) {
    node.expanded = expanded;

    this.session.send(this.id, 'nodeExpanded', {
      nodeId: node.id,
      expanded: expanded
    });
  }

  //Only render if $node is given to make it possible to expand/collapse currently invisible nodes (used by collapseAll).
  if ($node && $node.length > 0) {
    this._renderExpansion(node, $node, expanded);
  }
};

scout.Tree.prototype._renderExpansion = function(node, $node, expanded) {
  var $wrapper;

  if (expanded === $node.hasClass('expanded')) {
    return;
  }

  // Only expand / collapse if there are child nodes
  if (node.childNodes.length === 0) {
    return true;
  }

  if (expanded) {
    this._addNodes(node.childNodes, $node);
    this._updateItemPath();

    if (this._breadcrumb) {
      $node.addClass('expanded');
      return;
    }

    // animated opening
    if (!$node.hasClass('leaf') && !$node.hasClass('expanded')) { // can expand
      var $newNodes = scout.Tree.collectSubtree($node, false);
      if ($newNodes.length) {
        $wrapper = $newNodes.wrapAll('<div class="animationWrapper">').parent();
        var h = $newNodes.height() * $newNodes.length;
        var removeContainer = function() {
          $(this).replaceWith($(this).contents());
        };

        $wrapper.css('height', 0)
          .animateAVCSD('height', h, removeContainer, this.updateScrollbar.bind(this), 200);
      }
    }
    $node.addClass('expanded');
  } else {
    $node.removeClass('expanded');

    // animated closing
    $wrapper = scout.Tree.collectSubtree($node, false).each(function() {
      // unlink '$nodes' from 'nodes' before deleting them
      var node = $(this).data('node');
      if (node) { // FIXME BSH Tree | This if should not be necessary! 'node' should not be undefined, but is sometimes... Check why!
        delete node.$node;
      }
    }).wrapAll('<div class="animationWrapper">)').parent();

    $wrapper.animateAVCSD('height', 0, $.removeThis, this.updateScrollbar.bind(this), 200);
  }
};

scout.Tree.prototype.clearSelection = function() {
  this.setNodesSelected([], []);
};

scout.Tree.prototype.setNodesSelected = function(nodes, $nodes) {
  var i, nodeIds = scout.arrays.init(nodes.length);

  nodes = scout.arrays.ensure(nodes);
  $nodes = scout.arrays.ensure($nodes);
  for (i = 0; i < nodes.length; i++) {
    nodeIds[i] = nodes[i].id;
  }
  if (!scout.arrays.equalsIgnoreOrder(nodeIds, this.selectedNodeIds)) {
    this.selectedNodeIds = nodeIds;
    this.session.send(this.id, 'nodesSelected', {
      nodeIds: nodeIds
    });
    // FIXME BSH Keystroke | "scroll into view"
    this._renderSelection($nodes);
    this._triggerNodesSelected(nodeIds);
    this._renderNodeMenus(this.selectedNodeIds);
  }
};

scout.Tree.prototype._triggerNodesSelected = function(nodeIds) {
  this.events.trigger('nodesSelected', {
    nodeIds: nodeIds
  });
};

/**
 * @param $nodes if undefined the nodes will be resolved using this.selectedNodeIds
 */
scout.Tree.prototype._renderSelection = function($nodes) {
  var i, node, $node;

  // If $nodes are not given collect the nodes based on this.selectedNodeIds
  if (!$nodes || $nodes.length === 0) {
    $nodes = [];
    for (i = 0; i < this.selectedNodeIds.length; i++) {
      node = this.nodesMap[this.selectedNodeIds[i]];
      $node = node.$node;

      // If $node is currently not displayed (due to a collapsed parent node), expand the parents
      if (!$node) {
        this._expandAllParentNodes(node);
        $node = node.$node;
        if (!$node || $node.length === 0) {
          throw new Error('Still no node found. node=' + node);
        }
      }

      $nodes.push($node);
    }
  }

  this.$data.children().select(false);

  // render selection
  for (i = 0; i < $nodes.length; i++) {
    $node = $nodes[i];
    $node.select(true);

    // in case of breadcrumb, expand
    if (this._breadcrumb) {
      this.setNodeExpanded($nodes[i].data('node'), $nodes[i], true);
    }
  }

  this._updateItemPath();
};

scout.Tree.prototype._computeTreeItemPaddingLeft = function(level, selected) {
  if (this.checkable) {
    return level * this._treeItemPaddingLevel + this._treeItemPaddingLeft + this._treeItemCheckBoxPaddingLeft;
  }
  return level * this._treeItemPaddingLevel + this._treeItemPaddingLeft;
};

scout.Tree.prototype._expandAllParentNodes = function(node) {
  var i, $parentNode, currNode = node,
    parentNodes = [];

  currNode = node;
  while (currNode.parentNode) {
    parentNodes.push(currNode.parentNode);
    currNode = currNode.parentNode;
  }

  for (i = parentNodes.length - 1; i >= 0; i--) {
    if (!parentNodes[i].expanded) {
      $parentNode = parentNodes[i].$node;
      if (!$parentNode) {
        throw new Error('Illegal state, $parentNode should be displayed. Rendered: ' + this.rendered + ', parentNode: ' + parentNodes[i]);
      }
      this.setNodeExpanded(parentNodes[i], $parentNode, true);
    }
  }
};

scout.Tree.prototype._onNodesInserted = function(nodes, parentNodeId) {
  var parentNode, $parentNode;

  if (parentNodeId >= 0) {
    parentNode = this.nodesMap[parentNodeId];
    if (!parentNode) {
      throw new Error('Parent node could not be found. Id: ' + parentNodeId);
    }
  }
  this._visitNodes(nodes, this._initTreeNode.bind(this), parentNode);

  //update parent with new child nodes
  if (parentNode) {
    scout.arrays.pushAll(parentNode.childNodes, nodes);

    if (this.rendered && parentNode.$node) {
      $parentNode = parentNode.$node;
      if (parentNode.expanded) {
        //If parent is already expanded just add the nodes at the end.
        //Otherwise render the expansion
        if ($parentNode.hasClass('expanded')) {
          this._addNodes(nodes, $parentNode);
        } else {
          this._renderExpansion(parentNode, $parentNode, true);
        }
      }
    }
  } else {
    scout.arrays.pushAll(this.nodes, nodes);

    if (this.rendered) {
      this._addNodes(nodes);
    }
  }
};

scout.Tree.prototype._onNodesUpdated = function(nodes, parentNodeId) {
  // Update model
  var propertiesChanged = false;
  for (var i = 0; i < nodes.length; i++) {
    var updatedNode = nodes[i];
    var oldNode = this.nodesMap[updatedNode.id];

    // Only update _some_ of the properties. Everything else will be handled with separate events.
    // --> See also: JsonTree.java/handleModelNodesUpdated()
    scout.defaultValues.applyTo(updatedNode, 'TreeNode');
    if (oldNode.leaf !== updatedNode.leaf) {
      oldNode.leaf = updatedNode.leaf;
      propertiesChanged = true;
    }

    if (this.rendered && propertiesChanged) {
      this._decorateNode(oldNode);
    }
  }

  if (this.rendered && propertiesChanged) {
    this._updateItemPath();
  }
};

scout.Tree.prototype._onNodesDeleted = function(nodeIds, parentNodeId) {
  var updateNodeMap, parentNode, i, nodeId, node, deletedNodes = [];

  //update model and nodemap
  updateNodeMap = function(parentNode, node) {
    delete this.nodesMap[node.id];
    if (this._onNodeDeleted) { // Necessary for subclasses
      this._onNodeDeleted(node);
    }
  }.bind(this);

  if (parentNodeId >= 0) {
    parentNode = this.nodesMap[parentNodeId];
    if (!parentNode) {
      throw new Error('Parent node could not be found. Id: ' + parentNodeId);
    }
  }

  for (i = 0; i < nodeIds.length; i++) {
    nodeId = nodeIds[i];
    node = this.nodesMap[nodeId];
    this._updateMarkChildrenChecked(node, false, false);
    if (parentNode) {
      if (node.parentNode !== parentNode) {
        throw new Error('Unexpected parent. Node.parent: ' + node.parentNode + ', parentNode: ' + parentNode);
      }
      scout.arrays.remove(parentNode.childNodes, node);
    } else {
      scout.arrays.remove(this.nodes, node);
    }
    delete this.nodesMap[nodeId];
    if (this._onNodeDeleted) { // Necessary for subclasses
      this._onNodeDeleted(node);
    }
    deletedNodes.push(node);

    //remove children from node map
    this._visitNodes(node.childNodes, updateNodeMap);
  }

  //remove node from html document
  if (this.rendered) {
    this._removeNodes(deletedNodes, parentNodeId);
  }
};

scout.Tree.prototype._onAllNodesDeleted = function(parentNodeId) {
  var updateNodeMap, parentNode, nodes;

  //Update model and nodemap
  updateNodeMap = function(parentNode, node) {
    this._updateMarkChildrenChecked(node, false, false);
    delete this.nodesMap[node.id];
  }.bind(this);

  if (parentNodeId >= 0) {
    parentNode = this.nodesMap[parentNodeId];
    if (!parentNode) {
      throw new Error('Parent node could not be found. Id: ' + parentNodeId);
    }
  }
  if (parentNode) {
    nodes = parentNode.childNodes;
    parentNode.childNodes = [];
  } else {
    nodes = this.nodes;
    this.nodes = [];
  }
  this._visitNodes(nodes, updateNodeMap);

  //remove node from html document
  if (this.rendered) {
    this._removeNodes(nodes, parentNodeId);
  }
};

scout.Tree.prototype._onNodesSelected = function(nodeIds) {
  this.selectedNodeIds = nodeIds;
  this._renderNodeMenus(this.selectedNodeIds);
  if (this.rendered) {
    this._renderSelection();
  }
  this._triggerNodesSelected(nodeIds);
};

scout.Tree.prototype._onNodeExpanded = function(nodeId, expanded) {
  var node = this.nodesMap[nodeId];
  node.expanded = expanded;

  if (this.rendered && node.$node) {
    this._renderExpansion(node, node.$node, expanded);
  }
};

scout.Tree.prototype._onNodeChanged = function(nodeId, cell) {
  var node = this.nodesMap[nodeId];

  scout.defaultValues.applyTo(cell, 'TreeNode');
  node.text = cell.text;
  node.iconId = cell.iconId;
  node.tooltipText = cell.tooltipText;
  node.foregroundColor = cell.foregroundColor;
  node.backgroundColor = cell.backgroundColor;
  node.font = cell.font;

  if (this.rendered) {
    this._decorateNode(node);
  }
};

scout.Tree.prototype._onNodesChecked = function(nodes) {
  for (var i = 0; i < nodes.length; i++) {
    this.nodesMap[nodes[i].id].checked = nodes[i].checked;
    this._updateMarkChildrenChecked(this.nodesMap[nodes[i].id], false, nodes[i].checked, true);
    if (this.rendered) {
      this._renderNodeChecked(nodes[i]);
    }
  }
};

scout.Tree.prototype._onNodeFilterChanged = function() {
  // TODO BSH Tree | Replace nodes, restore selection and expansion
};

scout.Tree.prototype._onChildNodeOrderChanged = function(parentNodeId, childNodeIds) {
  var i,
    parentNode = this.nodesMap[parentNodeId],
    $lastChildNode = parentNode.childNodes[parentNode.childNodes.length - 1].$node;

  // Sort model nodes
  var newPositionsMap = {};
  for (i = 0; i < childNodeIds.length; i++) {
    newPositionsMap[childNodeIds[i]] = i;
  }
  parentNode.childNodes.sort(compare.bind(this));

  // Render sorted nodes
  if (this.rendered && $lastChildNode) {
    // Find the last affected node DIV
    $lastChildNode = scout.Tree.collectSubtree($lastChildNode).last();

    // Insert a marker DIV
    var $marker = $lastChildNode.afterDiv();
    for (i = 0; i < parentNode.childNodes.length; i++) {
      var node = parentNode.childNodes[i];
      var $node = node.$node;
      if ($node) {
        // Move the element in DOM tree. Note: Inserting the element at the new position is sufficient
        // in jQuery. There is no need to remove() it at the old position. Also, removing would break
        // the application, because remove() detaches all listeners!
        scout.Tree.collectSubtree($node).insertBefore($marker);
      }
    }
    $marker.remove();
  }

  function compare(node1, node2) {
    var pos1 = newPositionsMap[node1.id];
    var pos2 = newPositionsMap[node2.id];
    if (pos1 < pos2) {
      return -1;
    }
    if (pos1 > pos2) {
      return 1;
    }
    return 0;
  }
};

/**
 *
 * @param $parentNode optional. If not provided, parentNodeId will be used to find $parentNode.
 */
scout.Tree.prototype._removeNodes = function(nodes, parentNodeId, $parentNode) {
  if (nodes.length === 0) {
    return;
  }

  for (var i = 0; i < nodes.length; i++) {
    var node = nodes[i];
    if (node.childNodes.length > 0) {
      this._removeNodes(node.childNodes, node.id, node.$node);
    }
    if (node.$node) {
      node.$node.remove();
      delete node.$node;
    }
  }

  //If every child node was deleted mark node as collapsed (independent of the model state)
  //--> makes it consistent with addNodes and expand (expansion is not allowed if there are no child nodes)
  if (!$parentNode && parentNodeId >= 0) {
    var parentNode = this.nodesMap[parentNodeId];
    $parentNode = (parentNode ? parentNode.$node : undefined);
  }
  if ($parentNode) {
    var childNodes = $parentNode.data('node').childNodes;
    if (!childNodes || childNodes.length === 0) {
      $parentNode.removeClass('expanded');
    }
  }

  this.updateScrollbar();
};

scout.Tree.prototype._addNodes = function(nodes, $parent) {
  if (!nodes || nodes.length === 0) {
    this._renderNodeMenus(this.selectedNodeIds);
    return;
  }

  var $predecessor = $parent;
  for (var i = 0; i < nodes.length; i++) {
    var node = nodes[i];
    var $node = this._$buildNode(node, $parent);

    // append first node and successors
    if ($predecessor) {
      $node.insertAfter($predecessor);
    } else {
      $node.appendTo(this.$data);
    }

    // if model demands children, create them
    if (node.expanded && node.childNodes.length > 0) {
      $predecessor = this._addNodes(node.childNodes, $node);
    } else {
      $predecessor = $node;
    }
  }
  this._renderNodeMenus(this.selectedNodeIds);
  this.updateScrollbar();

  //return the last created node
  return $predecessor;
};

scout.Tree.prototype._$buildNode = function(node, $parent) {
  var level = $parent ? parseFloat($parent.attr('data-level')) + 1 : 0;

  var $node = $.makeDiv('tree-item', undefined, node.id)
    .on('click', '', this._onNodeClick.bind(this))
    .on('dblclick', '', this._onNodeDoubleClick.bind(this))
    .data('node', node)
    .attr('data-level', level)
    .css('padding-left', this._computeTreeItemPaddingLeft(level));
  node.$node = $node;

  this._decorateNode(node);
  this._renderTreeItemControl($node);

  if (this.checkable) {
    this._renderTreeItemCheckbox($node, node);
  }

  return $node;
};

scout.Tree.prototype._decorateNode = function(node) {
  var $node = node.$node;
  if (!$node) {
    // This node is not yet rendered, nothing to do
    return;
  }

  $node.toggleClass('leaf', !! node.leaf);
  $node.toggleClass('expanded', ( !! node.expanded && node.childNodes.length > 0));

  // Replace only the text node in the DOM, but leave inner DIVs untouched (e.g. tree item control)
  var textDomNodes = $node.contents().filter(function() {
    return (this.nodeType == 3 && this.textContent.trim() !== '');
  });
  if (textDomNodes.length > 0) {
    textDomNodes[0].textContent = node.text;
  } else {
    $node.append(node.text);
  }

  scout.helpers.legacyCellStyle(node, $node);

  if (scout.strings.hasText(node.tooltipText)) {
    $node.attr('title', node.tooltipText);
  }

  // TODO BSH More attributes...
  // iconId
  // tooltipText
};

scout.Tree.prototype._renderNodeChecked = function(node) {
  var $checkbox = $('#' + node.id + '-tree-checkable', this.$data);
  $checkbox.prop('checked', node.checked);
};

scout.Tree.prototype.checkNodeAndRender = function(node, checked) {
  this.checkNode(node, checked, true);
};

scout.Tree.prototype.checkNode = function(node, checked, render, suppressSend) {
  var updatedNodes = [];
  if (!this.enabled || !this.checkable || !node.enabled || node.checked === checked) {
    return updatedNodes;
  }
  if (!this.multiCheck && checked) {
    for (var i = 0; i < this.nodes.length; i++) {
      if (this.nodes[i].checked) {
        this.nodes[i].checked = false;
        this._updateMarkChildrenChecked(this.nodes[i], false, false, true);
        updatedNodes.push(this.nodes[i]);
        if (this.rendered) {
          this._renderNodeChecked(this.nodes[i]);
        }
      }
    }
  }
  node.checked = checked;
  updatedNodes.push(node);
  this._updateMarkChildrenChecked(node, false, checked, true);
  updatedNodes = updatedNodes.concat(this.checkChildren(node));
  if (!suppressSend) {
    this.sendNodesChecked(updatedNodes);
  }
  if (this.rendered && render) {
    this._renderNodeChecked(node);
  }
  return updatedNodes;
};

scout.Tree.prototype.checkChildren = function(node) {
  var updatedNodes = [];
  if (this.autoCheckChildren && node && node.checked) {
    for (var i = 0; i < node.childNodes.length; i++) {
      updatedNodes = updatedNodes.concat(this.checkNode(node.childNodes[i], true, node.checked, true));
    }
  }
  return updatedNodes;
};

scout.Tree.prototype.sendNodesChecked = function(nodes) {
  var data = {
    nodes: []
  };

  for (var i = 0; i < nodes.length; i++) {
    data.nodes.push({
      nodeId: nodes[i].id,
      checked: nodes[i].checked
    });
  }

  this.session.send(this.id, 'nodesChecked', data);
};

scout.Tree.prototype._renderTreeItemControl = function($node) {
  var $control = $node.prependDiv('tree-item-control')
    .on('click', '', this._onNodeControlClick.bind(this))
    .on('dblclick', '', this._onNodeControlClick.bind(this)); //_onNodeControlClick immediately returns with false to prevent bubbling

  if (this.checkable) {
    $control.addClass('checkable');
  }
};

scout.Tree.prototype._renderTreeItemCheckbox = function($node, node) {
  var that = this,
    $controlItem = $node.prependDiv('tree-item-checkbox'),
    forRefId = node.id + '-tree-checkable';
  var $checkbox = $('<input>')
    .attr('id', forRefId)
    .attr('type', 'checkbox')
    .appendTo($controlItem)
    .prop('checked', node.checked);
  var $label = $('<label>')
    .attr('for', forRefId)
    .appendTo($controlItem).
  on('mouseup', node, onNodeChecked);

  if (!this.enabled || !node.enabled) {
    $checkbox.prop('disabled', 'disabled');
  }

  if (node.childrenChecked) {
    $label.addClass('childrenChecked');
  } else {
    $label.removeClass('childrenChecked');
  }

  function onNodeChecked(event) {
    var node = event.data;
    that.checkNode(node, !node.checked);
  }
};

scout.Tree.prototype._onNodeClick = function(event) {
  if (event.originalEvent.detail > 1) {
    //don't execute on double click events
    return;
  }

  var $node = $(event.currentTarget),
    nodeId = $node.attr('id'),
    node = this.nodesMap[nodeId];

  this.session.send(this.id, 'nodeClicked', {
    nodeId: nodeId
  });

  this.setNodesSelected([node], [$node]);
};

scout.Tree.prototype._onNodeDoubleClick = function(event) {
  var $node = $(event.currentTarget),
    nodeId = $node.attr('id'),
    expanded = !$node.hasClass('expanded'),
    node = this.nodesMap[nodeId];

  if (this._breadcrumb) {
    return;
  }

  this.session.send(this.id, 'nodeAction', {
    nodeId: nodeId
  });

  this.setNodeExpanded(node, $node, expanded);
};

scout.Tree.prototype._onNodeControlClick = function(event) {
  if (event.originalEvent.detail > 1) {
    //don't execute on double click events
    return false;
  }

  var $clicked = $(event.currentTarget),
    $node = $clicked.parent(),
    expanded = !$node.hasClass('expanded'),
    node = $node.data('node');

  //TODO cru/cgu: talk about click on not selected nodes
  this.setNodesSelected([node], [$node]);
  this.setNodeExpanded(node, $node, expanded);

  // prevent immediately reopening
  return false;
};

scout.Tree.prototype._updateItemPath = function() {
  var $selected = this.$selectedNodes(),
    level = parseFloat($selected.attr('data-level'));

  // first remove and select selected
  this.$data.find('.tree-item').removeClass('parent children group');

  // if no selection: mark all top elements as children
  if ($selected.length === 0) {
    this.$data.children().addClass('children');
    return;
  }

  // find direct children
  var $node = $selected.next();
  while ($node.length > 0) {
    if ($node.hasClass('animationWrapper')) {
      $node = $node.children().first();
    }
    var l = parseFloat($node.attr('data-level'));
    if (l === level + 1) {
      $node.addClass('children');
    } else if (l === level) {
      break;
    }
    if ($node.next().length === 0 && $node.parent().hasClass('animationWrapper')) {
      // If there is no next node but we are inside an animationWrapper, step out the wrapper
      $node = $node.parent();
    }
    $node = $node.next();
  }

  // find parents
  var $ultimate;
  if ($selected.parent().hasClass('animationWrapper')) {
    //If node expansion animation is in progress, the nodes are wrapped by a div
    $selected = $selected.parent();
  }
  $node = $selected.prev();
  while ($node.length > 0) {
    var k = parseFloat($node.attr('data-level'));
    if (k < level) {
      $node.addClass('parent');
      $ultimate = $node;
      level = k;
    }
    if ($node.parent().hasClass('animationWrapper')) {
      $node = $node.parent();
    }
    $node = $node.prev();
  }

  // find group with same ultimate parent
  $ultimate = $ultimate || $selected;
  $node = $ultimate;
  while ($node.length > 0) {
    $node.addClass('group');
    if ($node.next().length === 0 && $node.parent().hasClass('animationWrapper')) {
      // If there is no next node but we are inside an animationWrapper, step out the wrapper
      $node = $node.parent();
    }
    $node = $node.next();
    if ($node.hasClass('animationWrapper')) {
      $node = $node.children().first();
    }

    var m = parseFloat($node.attr('data-level'));
    if (m === 0 && $node[0] !== $ultimate[0]) {
      break;
    }
  }
};

scout.Tree.prototype.$selectedNodes = function() {
  return this.$data.find('.selected');
};

scout.Tree.prototype.$nodes = function() {
  return this.$data.find('.tree-item');
};

scout.Tree.prototype.selectedNodes = function() {
  var nodes = [];
  for (var i = 0; i < this.selectedNodeIds.length; i++) {
    nodes.push(this.nodesMap[this.selectedNodeIds[i]]);
  }
  return nodes;
};

scout.Tree.prototype.onModelAction = function(event) {
  if (event.type === 'nodesInserted') {
    this._onNodesInserted(event.nodes, event.commonParentNodeId);
  } else if (event.type === 'nodesUpdated') {
    this._onNodesUpdated(event.nodes, event.commonParentNodeId);
  } else if (event.type === 'nodesDeleted') {
    this._onNodesDeleted(event.nodeIds, event.commonParentNodeId);
  } else if (event.type === 'allNodesDeleted') {
    this._onAllNodesDeleted(event.commonParentNodeId);
  } else if (event.type === 'nodesSelected') {
    this._onNodesSelected(event.nodeIds);
  } else if (event.type === 'nodeExpanded') {
    this._onNodeExpanded(event.nodeId, event.expanded);
  } else if (event.type === 'nodeChanged') {
    this._onNodeChanged(event.nodeId, event);
  } else if (event.type === 'nodesChecked') {
    this._onNodesChecked(event.nodes);
  } else if (event.type === 'nodeFilterChanged') {
    this._onNodeFilterChanged();
  } else if (event.type === 'childNodeOrderChanged') {
    this._onChildNodeOrderChanged(event.parentNodeId, event.childNodeIds);
  } else {
    $.log.warn('Model event not handled. Widget: Tree. Event: ' + event.type + '.');
  }
};

scout.Tree.prototype._renderMenus = function(menus) {
  this._renderNodeMenus(this.selectedNodeIds);
};

scout.Tree.prototype._renderNodeMenus = function(selectedNodeIds) {
  var menuItems = this._filterMenus(selectedNodeIds, ['Tree.EmptySpace', 'Tree.Header']);
  if (menuItems) {
    menuItems = this.staticMenus.concat(menuItems);
  }
  this.menuBar.updateItems(menuItems);
};

scout.Tree.prototype._filterMenus = function(selectedNodeIds, allowedTypes) {
  allowedTypes = allowedTypes || [];
  if (selectedNodeIds && selectedNodeIds.length === 1) {
    allowedTypes.push('Tree.SingleSelection');
  } else if (selectedNodeIds && selectedNodeIds.length > 1) {
    allowedTypes.push('Tree.MultiSelection');
  }
  return scout.menus.filter(this.menus, allowedTypes);
};

scout.Tree.prototype._renderTitle = function() {
  // NOP
};

scout.Tree.prototype._renderAutoCheckChildren = function() {
  // NOP
};

/* --- STATIC HELPERS ------------------------------------------------------------- */

/**
 * @memberOf scout.Tree
 */
scout.Tree.collectSubtree = function($rootNode, includeRootNodeInResult) {
  var $result = $();
  if (!$rootNode) {
    return $result;
  }
  var rootLevel = parseFloat($rootNode.attr('data-level'));
  if (includeRootNodeInResult === undefined || includeRootNodeInResult) {
    $result = $result.add($rootNode);
  }

  var $nextNode = $rootNode.next();
  while ($nextNode) {
    var level = parseFloat($nextNode.attr('data-level'));
    if (level > rootLevel) {
      $result = $result.add($nextNode);
    }
    else {
      break;
    }
    $nextNode = $nextNode.next();
  }
  return $result;
};
