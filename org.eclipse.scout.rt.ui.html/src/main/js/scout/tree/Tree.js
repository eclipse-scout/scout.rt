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
  this._addAdapterProperties(['menus', 'keyStrokes']);
  this._additionalContainerClasses = ''; // may be used by subclasses to set additional CSS classes
  this._treeItemPaddingLeft = 23;
  this._treeItemCheckBoxPaddingLeft = 29;
  this._treeItemPaddingLevel = 15;
  this.staticMenus = [];
  this.menus = [];
  this.menuBar;
};
scout.inherits(scout.Tree, scout.ModelAdapter);

scout.Tree.prototype.init = function(model, session) {
  scout.Tree.parent.prototype.init.call(this, model, session);
  this._visitNodes(this.nodes, this._initTreeNode.bind(this));
  var menuSorter = new scout.MenuItemsOrder(this.session, this.objectType);
  this.menuBar = new scout.MenuBar(this.session, menuSorter);
  this.menuBar.bottom();
  this.addChild(this.menuBar);
  this.keyStrokeAdapter = this._createKeyStrokeAdapter();
};

scout.Tree.prototype._syncMenus = function(menus) {
  var i;
  for (i = 0; i < this.menus.length; i++) {
    this.keyStrokeAdapter.unregisterKeyStroke(this.menus[i]);
  }
  this.menus = menus;
  for (i = 0; i < this.menus.length; i++) {
    if (this.menus[i].enabled) {
      this.keyStrokeAdapter.registerKeyStroke(this.menus[i]);
    }
  }
};

scout.Tree.prototype._initTreeNode = function(node, parentNode) {
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

scout.Tree.prototype.destroy = function() {
  scout.Tree.parent.prototype.destroy.call(this);
  this._visitNodes(this.nodes, this._destroyTreeNode.bind(this));
};

scout.Tree.prototype._destroyTreeNode = function(node, parentNode) {
  delete this.nodesMap[node.id];
  scout.arrays.remove(this.selectedNodeIds, node.id); // ensure deleted node is not in selection list anymore (in case the model does not update the selection)

  if (this._onNodeDeleted) { // Necessary for subclasses
    this._onNodeDeleted(node);
  }
};

scout.Tree.prototype._createKeyStrokeAdapter = function() {
  return new scout.TreeKeyStrokeAdapter(this);
};

scout.Tree.prototype._visitNodes = function(nodes, func, parentNode) {
  var i, node;
  if (!nodes) {
    return;
  }

  for (i = 0; i < nodes.length; i++) {
    node = nodes[i];
    func(node, parentNode);
    if (node.childNodes.length > 0) {
      this._visitNodes(node.childNodes, func, node);
    }
  }
};

scout.Tree.prototype._render = function($parent) {
  this.$container = $parent.appendDiv('tree');
  if (this._additionalContainerClasses) {
    this.$container.addClass(this._additionalContainerClasses);
  }

  var layout = new scout.TreeLayout(this);
  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(layout);
  this.htmlComp.pixelBasedSizing = false;

  this.$data = this.$container.appendDiv('tree-data')
    .on('click', '.tree-node', this._onNodeClick.bind(this))
    .on('dblclick', '.tree-node', this._onNodeDoubleClick.bind(this))
    .on('click', '.tree-node-control', this._onNodeControlClick.bind(this))
    .on('dblclick', '.tree-node-control', this._onNodeControlClick.bind(this)); //_onNodeControlClick immediately returns with false to prevent bubbling

  scout.scrollbars.install(this.$data);
  this.session.detachHelper.pushScrollable(this.$data);
  this.menuBar.render(this.$container);
  this._addNodes(this.nodes);
  if (this.selectedNodeIds.length > 0) {
    this._renderSelection();
  }
  this._updateItemPath();
};

scout.Tree.prototype._remove = function() {
  this.session.detachHelper.removeScrollable(this.$data);
  scout.Tree.parent.prototype._remove.call(this);
};

scout.Tree.prototype._renderProperties = function() {
  scout.Tree.parent.prototype._renderProperties.call(this);
  this._renderEnabled();
  this._renderMenus();
};

scout.Tree.prototype._renderMenus = function() {
  var menuItems = this._filterMenus(['Tree.EmptySpace', 'Tree.SingleSelection', 'Tree.MultiSelection']);
  menuItems = this.staticMenus.concat(menuItems);
  this.menuBar.updateItems(menuItems);
};

scout.Tree.prototype._filterMenus = function(allowedTypes) {
  allowedTypes = allowedTypes || [];
  if (allowedTypes.indexOf('Tree.SingleSelection') > -1 && this.selectedNodeIds.length !== 1) {
    scout.arrays.remove(allowedTypes, 'Tree.SingleSelection');
  }
  if (allowedTypes.indexOf('Tree.MultiSelection') > -1 && this.selectedNodeIds.length <= 1) {
    scout.arrays.remove(allowedTypes, 'Tree.MultiSelection');
  }
  return scout.menus.filter(this.menus, allowedTypes);
};

scout.Tree.prototype._renderEnabled = function() {
  var enabled = this.enabled;
  this.$data.setEnabled(enabled);
  this.$container.setTabbable(enabled);

  if (this.rendered) {
    // Enable/disable all checkboxes
    this.$nodes().each(function() {
      var $node = $(this),
        node = $node.data('node');
      $node.find('input').setEnabled(enabled && node.enabled);
    });
  }
};

scout.Tree.prototype._renderTitle = function() {
  // NOP
};

scout.Tree.prototype._renderAutoCheckChildren = function() {
  // NOP
};

scout.Tree.prototype.onResize = function() {
  this.updateScrollbar();
};

scout.Tree.prototype.updateScrollbar = function() {
  scout.scrollbars.update(this.$data);
  //FIXME do not call invalidateTree here, replace updateScrollbar with invalidateTree instead, see Table.js
  this.invalidateTree();
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

scout.Tree.prototype.setBreadcrumbEnabled = function(enabled) {
  if (this._breadcrumb !== enabled) {
    // update scrollbar if mode has changed (from tree to bc or vice versa)
    this.updateScrollbar();
  }
  this._breadcrumb = enabled;

  if (!enabled) {
    return;
  }

  if (this.selectedNodes.length > 0) {
    var nodeId = this.selectedNodes[0].id,
      expanded = this.selectedNodes[0].expanded,
      node = this.nodesMap[nodeId];

    if (!expanded) {
      this.session.send(this.id, 'nodeAction', {
        nodeId: nodeId
      });
      this.setNodeExpanded(node, true);
    }
  }
};

scout.Tree.prototype.collapseAll = function() {
  var that = this;

  // Collapse root nodes
  this.$data.find('[data-level="0"]').each(function() {
    var $node = $(this);
    that.setNodeExpanded($node.data('node'), false);
  });

  // Collapse all expanded child nodes (only model)
  this._visitNodes(this.nodes, function(node) {
    this.setNodeExpanded(node, false);
  }.bind(this));
};

scout.Tree.prototype.setNodeExpanded = function(node, expanded) {
  if (node.expanded !== expanded) {
    node.expanded = expanded;

    this.session.send(this.id, 'nodeExpanded', {
      nodeId: node.id,
      expanded: expanded
    });
  }

  if (this.rendered) {
    this._renderExpansion(node);
  }
};

scout.Tree.prototype._renderExpansion = function(node) {
  var $wrapper,
    $node = node.$node,
    expanded = node.expanded;

  // Only render if node is rendered to make it possible to expand/collapse currently invisible nodes (used by collapseAll).
  if (!$node || $node.length === 0) {
    return;
  }

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
        var h = $wrapper.outerHeight(); // FIXME CRU Why was this done like this? $newNodes.outerHeight() * $newNodes.length;
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
    delete node.$showAllNode;

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

scout.Tree.prototype.scrollTo = function(node) {
  scout.scrollbars.scrollTo(this.$data, node.$node);
};

scout.Tree.prototype.revealSelection = function() {
  if (this.selectedNodeIds.length > 0) {
    this.scrollTo(this.nodesMap[this.selectedNodeIds[0]]);
  }
};

scout.Tree.prototype.clearSelection = function() {
  this.setNodesSelected([]);
};

scout.Tree.prototype.setNodesSelected = function(nodes) {
  nodes = scout.arrays.ensure(nodes);
  var nodeIds = scout.arrays.init(nodes.length);
  for (var i = 0; i < nodes.length; i++) {
    nodeIds[i] = nodes[i].id;
  }
  this._updateSelectedNodeIds(nodeIds, true);
};

scout.Tree.prototype._updateSelectedNodeIds = function(selectedNodeIds, notifyServer) {
  if (!scout.arrays.equalsIgnoreOrder(selectedNodeIds, this.selectedNodeIds)) {
    if (this.rendered) {
      this._removeSelection();
    }

    this.selectedNodeIds = selectedNodeIds;

    if (scout.helpers.nvl(notifyServer, true)) {
      this.session.send(this.id, 'nodesSelected', {
        nodeIds: selectedNodeIds
      });
    }

    // FIXME BSH Keystroke | "scroll into view"
    if (this.rendered) {
      this._renderSelection();
      this._renderMenus();
    }
    this._triggerNodesSelected(selectedNodeIds);
  }
};

scout.Tree.prototype._triggerNodesSelected = function(nodeIds) {
  this.events.trigger('nodesSelected', {
    nodeIds: nodeIds
  });
};

scout.Tree.prototype._renderSelection = function() {
  var i, node, $node,
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

  // render selection
  for (i = 0; i < $nodes.length; i++) {
    $node = $nodes[i];
    $node.select(true);
    // If node was previously hidden, show it!
    $node.removeClass('hidden');

    // in case of breadcrumb, expand
    if (this._breadcrumb) {
      this.setNodeExpanded($nodes[i].data('node'), true);
    }
  }

  this._updateItemPath();
  if (this.scrollToSelection) {
    // Execute delayed because tree may be not layouted yet
    setTimeout(this.revealSelection.bind(this));
  }
};

scout.Tree.prototype._removeSelection = function() {
  for (var i = 0; i < this.selectedNodeIds.length; i++) {
    var node = this.nodesMap[this.selectedNodeIds[i]];
    var $node = node.$node;
    if ($node) { // TODO BSH Check if $node can be undefined
      $node.select(false);
    }
  }
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
      this.setNodeExpanded(parentNodes[i], true);
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

  // Update parent with new child nodes
  if (parentNode) {
    if (parentNode.childNodes && parentNode.childNodes.length>0) {
      nodes.forEach(function(entry) {
        scout.arrays.insert(parentNode.childNodes, entry, entry.childNodeIndex?entry.childNodeIndex:0);
      }.bind(this));
    } else {
      scout.arrays.pushAll(parentNode.childNodes, nodes);
    }

    if (this.rendered && parentNode.$node) {
      $parentNode = parentNode.$node;
      if (parentNode.expanded) {
        // If parent is already expanded just add the nodes at the end.
        // Otherwise render the expansion
        if ($parentNode.hasClass('expanded')) {
          this._addNodes(nodes, $parentNode);
        } else {
          this._renderExpansion(parentNode);
        }
      }
    }
  } else {
    if (this.nodes && this.nodes.length > 0) {
      nodes.forEach(function(entry) {
        scout.arrays.insert(this.nodes, entry, entry.childNodeIndex?entry.childNodeIndex:0);
      }.bind(this));
    } else {
      scout.arrays.pushAll(this.nodes, nodes);
    }

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
    if (oldNode.enabled !== updatedNode.enabled) {
      oldNode.enabled = updatedNode.enabled;
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
  var parentNode, i, nodeId, node, deletedNodes = [];

  if (parentNodeId >= 0) {
    parentNode = this.nodesMap[parentNodeId];
    if (!parentNode) {
      throw new Error('Parent node could not be found. Id: ' + parentNodeId);
    }
  }

  for (i = 0; i < nodeIds.length; i++) {
    nodeId = nodeIds[i];
    node = this.nodesMap[nodeId];
    if (parentNode) {
      if (node.parentNode !== parentNode) {
        throw new Error('Unexpected parent. Node.parent: ' + node.parentNode + ', parentNode: ' + parentNode);
      }
      scout.arrays.remove(parentNode.childNodes, node);
    } else {
      scout.arrays.remove(this.nodes, node);
    }
    this._destroyTreeNode(node, node.parentNode);
    deletedNodes.push(node);
    this._updateMarkChildrenChecked(node, false, false);

    //remove children from node map
    this._visitNodes(node.childNodes, this._destroyTreeNode.bind(this));
  }

  //remove node from html document
  if (this.rendered) {
    this._removeNodes(deletedNodes, parentNodeId);
  }
};

scout.Tree.prototype._onAllNodesDeleted = function(parentNodeId) {
  var parentNode, nodes;

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
  this._visitNodes(nodes, updateNodeMap.bind(this));

  // remove node from html document
  if (this.rendered) {
    this._removeNodes(nodes, parentNodeId);
  }

  // --- Helper functions ---

  // Update model and nodemap
  function updateNodeMap(node, parentNode) {
    this._destroyTreeNode(node, parentNode);
    this._updateMarkChildrenChecked(node, false, false);
  }
};

scout.Tree.prototype._onNodesSelected = function(nodeIds) {
  this._updateSelectedNodeIds(nodeIds, false);
};

scout.Tree.prototype._onNodeExpanded = function(nodeId, expanded, recursive) {
  var node = this.nodesMap[nodeId];
  expandNodeInternal.call(this, node);
  if (recursive) {
    this._visitNodes(node.childNodes, function(childNode) {
      expandNodeInternal.call(this, childNode);
    }.bind(this));
  }

  // --- Helper functions ---

  function expandNodeInternal(node) {
    node.expanded = expanded;
    if (this.rendered) {
      this._renderExpansion(node);
    }
  }
};

scout.Tree.prototype._onNodeChanged = function(nodeId, cell) {
  var node = this.nodesMap[nodeId];

  scout.defaultValues.applyTo(cell, 'TreeNode');
  node.text = cell.text;
  node.cssClass = cell.cssClass;
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
      this._renderNodeChecked(this.nodesMap[nodes[i].id]);
    }
  }
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
 * @param parentNodeId optional. If provided, this node's state will be updated (e.g. it will be collapsed)
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
    if (node.$showAllNode) {
      node.$showAllNode.remove();
      delete node.$showAllNode;
    }
  }

  //If every child node was deleted mark node as collapsed (independent of the model state)
  //--> makes it consistent with addNodes and expand (expansion is not allowed if there are no child nodes)
  var parentNode;
  if (!$parentNode && parentNodeId >= 0) {
    parentNode = this.nodesMap[parentNodeId];
    $parentNode = (parentNode ? parentNode.$node : undefined);
  }
  if ($parentNode) {
    if (!parentNode) {
      parentNode = $parentNode.data('node');
    }
    var childNodesOfParent = parentNode.childNodes;
    if (!childNodesOfParent || childNodesOfParent.length === 0) {
      $parentNode.removeClass('expanded');
      if (parentNode.$showAllNode) {
        parentNode.$showAllNode.remove();
        delete parentNode.$showAllNode;
      }
    } else {
      if (parentNode.$showAllNode) {
        this._decorateShowAllNode(parentNode.$showAllNode, parentNode);
      }
    }
  }

  this.updateScrollbar();
};

scout.Tree.prototype._addNodes = function(nodes, $parent) {
  if (!nodes || nodes.length === 0) {
    return;
  }

  var $predecessor = $parent;
  var parentNode = ($parent ? $parent.data('node') : undefined);
  var hasHiddenNodes = false;

  for (var i = 0; i < nodes.length; i++) {
    var node = nodes[i];

    var $node = this._$buildNode(node, $parent);

    // If node wants to be lazy added to the tree, hide the DOM element, except the
    // node is expanded, in which case we never hide it (this provides a cheap
    // way to retain most of the state when the page is reloaded).
    if (parentNode && node.lazyAddToTree && !node.expanded) {
      $node.addClass('hidden');
      hasHiddenNodes = true;
    }
    // append first node and successors
    if ($predecessor) {
      if (parentNode && parentNode.childNodes.indexOf(node) > 0) {
        $predecessor = parentNode.childNodes[parentNode.childNodes.indexOf(node) - 1].$node;
      }
      $node.insertAfter($predecessor);
    } else {
      //insert on top level
      if (this.nodes && this.nodes.indexOf(node)>0) {
        $predecessor = this.nodes[this.nodes.indexOf(node) - 1].$node;
        $node.insertAfter($predecessor);
      }
      else{
        $node.prependTo(this.$data);
      }
    }

    // if model demands children, create them
    if (node.expanded && node.childNodes.length > 0) {
      $predecessor = this._addNodes(node.childNodes, $node);
    } else {
      $predecessor = $node;
    }
  }

  // Update dummy "show all" node
  if (!hasHiddenNodes) {
    // Delete existing $showAllNode
    if (parentNode && parentNode.$showAllNode) {
      parentNode.$showAllNode.remove();
      delete parentNode.$showAllNode;
    }
  } else {
    // If parent is expanded and has not already a $showAllNode, create one
    if (parentNode && parentNode.expanded && !parentNode.$showAllNode) {
      var $showAllNode = this._$buildShowAllNode(parentNode);
      $showAllNode.insertAfter($predecessor);
      $predecessor = $showAllNode;
    } else {
      // Node already exists, just update the text (node count might have changed)
      this._decorateShowAllNode(parentNode.$showAllNode, parentNode);
    }
  }

  this.updateScrollbar();

  //return the last created node
  return $predecessor;
};

scout.Tree.prototype._$buildNode = function(node, $parent) {
  var level = $parent ? parseFloat($parent.attr('data-level')) + 1 : 0;

  var $node = $.makeDiv('tree-node')
    .data('node', node)
    .attr('data-nodeid', node.id)
    .attr('data-level', level)
    .css('padding-left', this._computeTreeItemPaddingLeft(level));
  node.$node = $node;

  this._decorateNode(node);
  this._renderTreeItemControl($node);

  if (this.checkable) {
    this._renderTreeItemCheckbox(node);
  }

  return $node;
};

scout.Tree.prototype._$buildShowAllNode = function(parentNode) {
  var $parent = parentNode.$node;
  var currentLevel = $parent ? parseFloat($parent.attr('data-level')) + 1 : 0;

  var $node = $.makeDiv('tree-node show-all')
    .attr('data-level', currentLevel)
    .css('padding-left', this._computeTreeItemPaddingLeft(currentLevel));

  this._decorateShowAllNode($node, parentNode);
  // TreeItemControl
  var $control = $node.prependDiv('tree-node-control');

  // Link to parent node
  $node.data('parentNode', parentNode);
  parentNode.$showAllNode = $node;

  return $node;
};

scout.Tree.prototype._decorateNode = function(node) {
  var formerClasses,
    $node = node.$node;
  if (!$node) {
    // This node is not yet rendered, nothing to do
    return;
  }

  formerClasses = 'tree-node';
  if ($node.isSelected()) {
    formerClasses += ' selected';
  }
  $node.removeClass();
  $node.addClass(formerClasses);
  $node.addClass(node.cssClass);
  $node.toggleClass('leaf', !! node.leaf);
  $node.toggleClass('expanded', ( !! node.expanded && node.childNodes.length > 0));
  $node.setEnabled( !! node.enabled);

  // Replace only the "text part" of the node, leave control and checkbox untouched
  var preservedChildren = $node.children('.tree-node-control,.tree-node-checkbox').detach();
  $node.empty()
    .append(preservedChildren)
    .append(node.text);

  scout.helpers.legacyCellStyle(node, $node);

  if (scout.strings.hasText(node.tooltipText)) {
    $node.attr('title', node.tooltipText);
  }

  // TODO BSH More attributes...
  // iconId
  // tooltipText
};

scout.Tree.prototype._decorateShowAllNode = function($showAllNode, parentNode) {
  $showAllNode
    .text(this.session.text('ShowAllNodes', parentNode.childNodes.length));
};

scout.Tree.prototype._renderNodeChecked = function(node) {
  if (!node.$node) {
    // if node is not rendered, do nothing
    return;
  }
  var $checkbox = node.$node
    .children('.tree-node-checkbox')
    .children('input');
  $checkbox.prop('checked', node.checked);
};

scout.Tree.prototype.checkNode = function(node, checked, suppressSend) {
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
  updatedNodes = updatedNodes.concat(this.checkChildren(node, checked));
  if (!suppressSend) {
    this.sendNodesChecked(updatedNodes);
  }
  if (this.rendered) {
    this._renderNodeChecked(node);
  }
  return updatedNodes;
};

scout.Tree.prototype.checkChildren = function(node, checked) {
  var updatedNodes = [];
  if (this.autoCheckChildren && node) {
    for (var i = 0; i < node.childNodes.length; i++) {
      updatedNodes = updatedNodes.concat(this.checkNode(node.childNodes[i], checked, true));
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
  var $control = $node.prependDiv('tree-node-control');
  if (this.checkable) {
    $control.addClass('checkable');
  }
};

scout.Tree.prototype._renderTreeItemCheckbox = function(node) {
  var $node = node.$node,
    $controlItem = $node.prependDiv('tree-node-checkbox');
  var $checkbox = $('<input>')
    .attr('tabindex', '-1')
    .attr('type', 'checkbox')
    .prop('checked', node.checked)
    .appendTo($controlItem);
  var $label = $('<label>')
    .appendTo($controlItem);

  $checkbox.setEnabled(this.enabled && node.enabled);

  if (node.childrenChecked) {
    $label.addClass('childrenChecked');
  } else {
    $label.removeClass('childrenChecked');
  }
};

scout.Tree.prototype._onNodeClick = function(event) {
  if (event.originalEvent.detail > 1) {
    //don't execute on double click events
    return;
  }

  var $node = $(event.currentTarget);

  // Handle "show all" dummy node
  if ($node.hasClass('show-all')) {
    this._showAllNodes($node);
    return false;
  }

  var node = $node.data('node');
  this.setNodesSelected(node);

  if (this.checkable && this._isCheckboxClicked(event)) {
    this.checkNode(node, !node.checked);
  }

  this.session.send(this.id, 'nodeClicked', {
    nodeId: node.id
  });
};

scout.Tree.prototype._isCheckboxClicked = function(event) {
  var $target = $(event.target);
  return $target.is('label') && $target.parent().hasClass('tree-node-checkbox');
};

scout.Tree.prototype._onNodeDoubleClick = function(event) {
  var $node = $(event.currentTarget);

  // Handle "show all" dummy node
  if ($node.hasClass('show-all')) {
    this._showAllNodes($node);
    return false;
  }

  var node = $node.data('node'),
    expanded = !$node.hasClass('expanded');

  if (this._breadcrumb) {
    return;
  }

  this.session.send(this.id, 'nodeAction', {
    nodeId: node.id
  });

  this.setNodeExpanded(node, expanded);
};

scout.Tree.prototype._onNodeControlClick = function(event) {
  if (event.originalEvent.detail > 1) {
    //don't execute on double click events
    return false;
  }

  var $node = $(event.currentTarget).parent();

  // Handle "show all" dummy node
  if ($node.hasClass('show-all')) {
    this._showAllNodes($node);
    return false;
  }

  var node = $node.data('node'),
    expanded = !$node.hasClass('expanded');

  //TODO cru/cgu: talk about click on not selected nodes
  this.setNodesSelected(node);
  this.setNodeExpanded(node, expanded);

  // prevent immediately reopening
  return false;
};

scout.Tree.prototype._showAllNodes = function($showAllNode) {
  var parentNode = $showAllNode.data('parentNode'),
    first = true,
    updateFunc = function() {
      this.updateScrollbar();
      this.revealSelection();
    }.bind(this);

  // Remove "show all" dummy node
  delete parentNode.$showAllNode;
  $showAllNode.remove();

  // Show all nodes for this parent
  for (var i = 0; i < parentNode.childNodes.length; i++) {
    var childNode = parentNode.childNodes[i];

    // skip if already visible
    if (childNode.$node.css('display') !== 'none') {
      continue;
    }

    // only animate small trees
    if (parentNode.childNodes.length < 100) {
      var h = childNode.$node.outerHeight(),
        p = childNode.$node.css('padding-top'),
        func = first ? updateFunc : null;

      // only first animated element should handle scrollbar and visibility of selection
      first = false;

      // make height 0
      childNode.$node
        .outerHeight(0)
        .css('padding-top', '0')
        .css('padding-bottom', '0')
        .removeClass('hidden');

      // animate to original height
      childNode.$node
        .animateAVCSD('padding-top', p, null, null, 200)
        .animateAVCSD('padding-bottom', p, null, null, 200)
        .animateAVCSD('height', h, null, func, 200);

    } else {
      childNode.$node.removeClass('hidden');
    }
  }

  // without animation
  this.updateScrollbar();
  this.revealSelection();
};

scout.Tree.prototype._updateItemPath = function() {
  var $selectedNodes, $node, level;

  // first remove and select selected
  this.$data.find('.tree-node').removeClass('parent children group');

  // if no selection: mark all top elements as children
  if (this.selectedNodeIds.length === 0) {
    this.$data.children().addClass('children');
    return;
  }

  // find direct children
  $selectedNodes = this.$selectedNodes();
  $node = $selectedNodes.next();
  level = parseFloat($selectedNodes.attr('data-level'));
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
  if ($selectedNodes.parent().hasClass('animationWrapper')) {
    //If node expansion animation is in progress, the nodes are wrapped by a div
    $selectedNodes = $selectedNodes.parent();
  }
  $node = $selectedNodes.prev();
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
  $ultimate = $ultimate || $selectedNodes;
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
  return this.$data.find('.tree-node');
};

scout.Tree.prototype.selectedNodes = function() {
  var nodes = [];
  for (var i = 0; i < this.selectedNodeIds.length; i++) {
    nodes.push(this.nodesMap[this.selectedNodeIds[i]]);
  }
  return nodes;
};

scout.Tree.prototype._onRequestFocus = function() {
  this.$container.focus();
};

scout.Tree.prototype._onScrollToSelection = function() {
  this.revealSelection();
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
    this._onNodeExpanded(event.nodeId, event.expanded, event.recursive);
  } else if (event.type === 'nodeChanged') {
    this._onNodeChanged(event.nodeId, event);
  } else if (event.type === 'nodesChecked') {
    this._onNodesChecked(event.nodes);
  } else if (event.type === 'childNodeOrderChanged') {
    this._onChildNodeOrderChanged(event.parentNodeId, event.childNodeIds);
  } else if (event.type === 'requestFocus') {
    this._onRequestFocus();
  } else if (event.type === 'scrollToSelection') {
    this._onScrollToSelection();
  } else {
    $.log.warn('Model event not handled. Widget: Tree. Event: ' + event.type + '.');
  }
};

/* --- STATIC HELPERS ------------------------------------------------------------- */

/**
 * @memberOf scout.Tree
 */
scout.Tree.collectSubtree = function($rootNode, includeRootNodeInResult) {
  if (!$rootNode) {
    return $();
  }
  var rootLevel = parseFloat($rootNode.attr('data-level'));
  // Find first node after the root element that has the same or a lower level
  var $nextNode = $rootNode.next();
  while ($nextNode.length > 0) {
    var level = parseFloat($nextNode.attr('data-level'));
    if (isNaN(level) || level <= rootLevel) {
      break;
    }
    $nextNode = $nextNode.next();
  }

  // The result set consists of all nodes between the root node and the found node
  var $result = $rootNode.nextUntil($nextNode);
  if (includeRootNodeInResult === undefined || includeRootNodeInResult) {
    $result = $result.add($rootNode);
  }
  return $result;
};
