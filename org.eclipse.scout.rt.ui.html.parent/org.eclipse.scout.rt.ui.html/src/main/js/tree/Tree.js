// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.Tree = function() {
  scout.Tree.parent.call(this);
  this.selectedNodeIds = [];
  this.nodes = [];
  this._nodeMap = {};
  this._breadcrumb = false;
  this._treeItemPaddingLeft = 30;
  this._treeItemPaddingLevel = 20;
};
scout.inherits(scout.Tree, scout.ModelAdapter);

scout.Tree.prototype.init = function(model, session) {
  scout.Tree.parent.prototype.init.call(this, model, session);
  this._visitNodes(this.nodes, this._initTreeNode.bind(this));
};

scout.Tree.prototype._initTreeNode = function(parentNode, node) {
  this._nodeMap[node.id] = node;
  if (parentNode) {
    node.parentNode = parentNode;
  }
  if (node.childNodes === undefined) {
    node.childNodes = [];
  }
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

scout.Tree.prototype._render = function($parent) {
  this.$parent = $parent;
  this.$container = $parent.appendDiv('tree');
  this._$viewport = scout.scrollbars.install(this.$container);
  this._addNodes(this.nodes);
  if (this.selectedNodeIds.length > 0) {
    this._renderSelection();
  }
};

scout.Tree.prototype.setBreadcrumb = function(bread) {
  if (bread) {
    this._breadcrumb = true;

    var $selected = this._findSelectedNodes();

    if ($selected.length > 0) {
      var nodeId = $selected.attr('id'),
        expanded = $selected.hasClass('expanded'),
        node = this._nodeMap[nodeId];

      if (!expanded) {
        this.session.send('nodeAction', this.id, {
          'nodeId': nodeId
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
  this._$viewport.find('[data-level="0"]').each(function() {
    var $node = $(this);
    that.setNodeExpanded($node.data('node'), $node , false);
  });

  //Collapse all expanded child nodes (only model)
  this._visitNodes(this.nodes, function(parentNode, node) {
    this.setNodeExpanded(node, null, false);
  }.bind(this));
};

scout.Tree.prototype.setNodeExpanded = function(node, $node, expanded) {
  if (node.expanded !== expanded) {
    node.expanded = expanded;

    this.session.send('nodeExpanded', this.id, {
      'nodeId': node.id,
      'expanded': expanded
    });
  }

  //Only render if $node is given to make it possible to expand/collapse currently invisible nodes (used by collapseAll).
  if ($node && $node.length > 0) {
    this._renderExpansion(node, $node, expanded);
  }
};

scout.Tree.prototype._renderExpansion = function(node, $node, expanded) {
  var $wrapper, level;
  if (expanded === $node.hasClass('expanded')) {
    return;
  }

  // Only expand / collapse if there are child nodes
  if (node.childNodes.length === 0) {
    return true;
  }

  level = $node.attr('data-level');

  if (expanded) {
    this._addNodes(node.childNodes, $node);
    this._updateItemPath();

    if (this._breadcrumb) {
      $node.addClass('expanded');
      return;
    }

    // animated opening
    if (!$node.hasClass('leaf') && !$node.hasClass('expanded')) { // can expand
      var $newNodes = $node.nextUntil(
        function() {
          return $(this).attr('data-level') <= level;
        }
      );
      if ($newNodes.length) {
        $wrapper = $newNodes.wrapAll('<div class="animationWrapper">').parent();
        var h = $newNodes.height() * $newNodes.length;
        var removeContainer = function() {
          $(this).replaceWith($(this).contents());
        };

        $wrapper.css('height', 0).
          animateAVCSD('height', h, removeContainer, scout.scrollbars.update.bind(this, this._$viewport), 200);
      }
    }
    $node.addClass('expanded');
  } else {
    $node.removeClass('expanded');

    // animated closing
    $wrapper = $node.nextUntil(function() {
      return $(this).attr('data-level') <= level;
    }).wrapAll('<div class="animationWrapper">)').parent();

    $wrapper.animateAVCSD('height', 0, $.removeThis, scout.scrollbars.update.bind(this, this._$viewport), 200);
  }
};

scout.Tree.prototype.clearSelection = function() {
  this.setNodesSelected([], []);
};

scout.Tree.prototype.setNodesSelected = function(nodes, $nodes) {
  var nodeIds = new Array(nodes.length), i;
  for (i=0; i < nodes.length; i++) {
    nodeIds[i] = nodes[i].id;
  }

  if (!scout.arrays.equalsIgnoreOrder(nodeIds, this.selectedNodeIds)) {
    this.selectedNodeIds = nodeIds;

    this.session.send('nodesSelected', this.id, {
      'nodeIds': nodeIds
    });
  }

  this._renderSelection($nodes);
};

/**
 *
 * @param $nodes if undefined the nodes will be resolved using this.selectedNodeIds
 */
scout.Tree.prototype._renderSelection = function($nodes) {
  var i, node, $node;

  //If $nodes are given collect the nodes based on this.selectedNodeIds
  if (!$nodes) {
    $nodes = [];
    for (i=0; i < this.selectedNodeIds.length; i++) {
      node = this._nodeMap[this.selectedNodeIds[i]];
      $node = this._findNodeById(node.id);

      //If $node is currently not displayed (due to a collapsed parent node), expand the parents
      if ($node.length === 0) {
        this._expandAllParentNodes(node);
        $node = this._findNodeById(node.id);
        if ($node.length === 0) {
          throw new Error('Still no node found. node=' + node);
        }
      }

      $nodes.push($node);
    }
  }

  this._$viewport.children().select(false);

  // render selection
  for (i=0; i < $nodes.length; i++) {
    $node = $nodes [i];
    $node.select(true);

    // in case of breadcrumb, expand
    if (this._breadcrumb) {
      this.setNodeExpanded($nodes[i].data('node'), $nodes[i], true);
    }
  }

  this._updateItemPath();
};

scout.Tree.prototype._computeTreeItemPaddingLeft = function(level, selected) {
  return level * this._treeItemPaddingLevel + this._treeItemPaddingLeft;
};

scout.Tree.prototype._expandAllParentNodes = function(node) {
  var parentNodes = [], i, $parentNode, currNode = node;

  currNode = node;
  while (currNode.parentNode) {
    parentNodes.push(currNode.parentNode);
    currNode = currNode.parentNode;
  }

  for (i = parentNodes.length - 1; i >= 0; i--) {
    if (!parentNodes[i].expanded) {
      $parentNode = this._findNodeById(parentNodes[i].id);
      if ($parentNode.length === 0) {
        throw new Error('Illegal state, $parentNode should be displayed. Rendered: ' + this.rendered + ', parentNode: ' + parentNodes[i]);
      }
      this.setNodeExpanded(parentNodes[i], $parentNode, true);
    }
  }
};

scout.Tree.prototype._onNodesInserted = function(nodes, parentNodeId) {
  var parentNode, $parentNode;

  if (parentNodeId >= 0) {
    parentNode = this._nodeMap[parentNodeId];
    if (!parentNode) {
      throw new Error('Parent node could not be found. Id: ' + parentNodeId);
    }
  }
  this._visitNodes(nodes, this._initTreeNode.bind(this), parentNode);

  //update parent with new child nodes
  if (parentNode) {
    scout.arrays.pushAll(parentNode.childNodes, nodes);

    if (this.rendered) {
      $parentNode = this._findNodeById(parentNode.id);
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

scout.Tree.prototype._onNodesDeleted = function(nodeIds, parentNodeId) {
  var updateNodeMap, parentNode, i, nodeId, node, deletedNodes = [];

  //update model and nodemap
  updateNodeMap = function(parentNode, node) {
    delete this._nodeMap[node.id];
  }.bind(this);

  if (parentNodeId >= 0) {
    parentNode = this._nodeMap[parentNodeId];
    if (!parentNode) {
      throw new Error('Parent node could not be found. Id: ' + parentNodeId);
    }
  }

  for (i = 0; i < nodeIds.length; i++) {
    nodeId = nodeIds[i];
    node = this._nodeMap[nodeId];
    if (parentNode) {
      if (node.parentNode !== parentNode) {
        throw new Error('Unexpected parent. Node.parent: ' + node.parentNode + ', parentNode: ' + parentNode);
      }
      scout.arrays.remove(parentNode.childNodes, node);
    } else {
      scout.arrays.remove(this.nodes, node);
    }
    delete this._nodeMap[nodeId];
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
    delete this._nodeMap[node.id];
  }.bind(this);

  if (parentNodeId >= 0) {
    parentNode = this._nodeMap[parentNodeId];
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

scout.Tree.prototype._onNodesSelected =  function(nodeIds) {
  this.selectedNodeIds = nodeIds;

  if (this.rendered) {
    this._renderSelection();
  }
};

scout.Tree.prototype._onNodeExpanded = function(nodeId, expanded) {
  var node = this._nodeMap[nodeId];
  node.expanded = expanded;

  if (this.rendered) {
    var $node = this._findNodeById(node.id);
    this._renderExpansion(node, $node, expanded);
  }
};

scout.Tree.prototype._onNodeChanged = function(nodeId, cell) {
  var node = this._nodeMap[nodeId];
  node.text = cell.text;

  if (this.rendered) {
    var $node = this._findNodeById(node.id);
    $node.html(node.text);
    //text() removes complete content -> add tree item control again
    this._renderTreeItemControl($node);
  }
};

/**
 *
 * @param $parentNode optional. If not provided, parentNodeId will be used to find $parentNode.
 */
scout.Tree.prototype._removeNodes = function(nodes, parentNodeId, $parentNode) {
  var i, $node, node, childNodes;
  if (nodes.length === 0) {
    return;
  }

  //Find parentNode to increase search performance and to reset expansion state
  if (!$parentNode && parentNodeId >= 0) {
    $parentNode = this._findNodeById(parentNodeId);
  }

  for (i = 0; i < nodes.length; i++) {
    node = nodes[i];
    $node = this._findNodeById(node.id, $parentNode);

    if (node.childNodes.length > 0) {
      this._removeNodes(node.childNodes, node.id, $node);
    }

    $node.remove();
  }


  //If every child node was deleted mark node as collapsed (independent of the model state)
  //--> makes it consistent with addNodes and expand (expansion is not allowed if there are no child nodes)
  if ($parentNode) {
    childNodes = $parentNode.data('node').childNodes;
    if (!childNodes || childNodes.length === 0) {
      $parentNode.removeClass('expanded');
    }
  }

  scout.scrollbars.update(this._$viewport);
};

scout.Tree.prototype._addNodes = function(nodes, $parent) {
  var node, state, level, $node, $predecessor;

  if (!nodes || nodes.length === 0) {
    return;
  }

  $predecessor = $parent;
  for (var i = 0; i < nodes.length; i++) {
    node = nodes[i];
    state = '';
    if (node.expanded && node.childNodes.length > 0) {
      state = 'expanded ';
    }
    if (node.leaf) {
      state += 'leaf ';
    }
    level = $parent ? $parent.data('level') + 1 : 0;

    $node = $.makeDiv('tree-item ' + state, node.text, node.id)
      .on('click', '', this._onNodeClick.bind(this))
      .on('dblclick', '', this._onNodeDoubleClick.bind(this))
      .data('node', node)
      .attr('data-level', level)
      .css('padding-left', this._computeTreeItemPaddingLeft(level));

    this._renderTreeItemControl($node);

    // append first node and successors
    if ($predecessor) {
      $node.insertAfter($predecessor);
    } else {
      $node.appendTo(this._$viewport);
    }

    // if model demands children, create them
    if (node.expanded && node.childNodes.length > 0) {
      $predecessor = this._addNodes(node.childNodes, $node);
    } else {
      $predecessor = $node;
    }
  }

  scout.scrollbars.update(this._$viewport);

  //return the last created node
  return $node;
};

scout.Tree.prototype._renderTreeItemControl = function($node) {
  $node.prependDiv('tree-item-control')
    .on('click', '', this._onNodeControlClick.bind(this))
    .on('dblclick', '', this._onNodeControlClick.bind(this)); //_onNodeControlClick immediately returns with false to prevent bubbling
};

scout.Tree.prototype._onNodeClick = function(event) {
  if (event.originalEvent.detail > 1) {
    //don't execute on double click events
    return;
  }

  var $node = $(event.currentTarget),
    nodeId = $node.attr('id'),
    node = this._nodeMap[nodeId];

  this.session.send('nodeClicked', this.id, {
    'nodeId': nodeId
  });

  this.setNodesSelected([node], [$node]);
};

scout.Tree.prototype._onNodeDoubleClick = function(event) {
  var $node = $(event.currentTarget),
    nodeId = $node.attr('id'),
    expanded = !$node.hasClass('expanded'),
    node = this._nodeMap[nodeId];

  if (this._breadcrumb) {
    return;
  }

  this.session.send('nodeAction', this.id, {
    'nodeId': nodeId
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

  //TODO cru/cgu: talk about click on not seleced nodes
  this.setNodesSelected([node], [$node]);
  this.setNodeExpanded(node, $node, expanded);

  // prevent immediately reopening
  return false;
};

scout.Tree.prototype._updateItemPath = function() {
  var $selected = this._findSelectedNodes(),
    $allNodes = this._$viewport.children(),
    level = parseFloat($selected.attr('data-level'));

  // first remove and select selected
  $allNodes.removeClass('parent children group');

  // if no selection: mark all top elements as children
  if ($selected.length === 0) {
    $allNodes.addClass('children');
    return;
  }

  // find direct children
  var $start = $selected.next();
  while ($start.length > 0) {
    var l = parseFloat($start.attr('data-level'));
    if (l === level + 1) {
      $start.addClass('children');
    } else if (l === level) {
      break;
    }
    $start = $start.next();
  }

  // find parents
  var $ultimate;
  if ($selected.parent().hasClass('animationWrapper')) {
    //If node expansion animation is in progress, the nodes are wrapped by a div
    $selected = $selected.parent();
  }
  $start = $selected.prev();
  while ($start.length > 0) {
    var k = parseFloat($start.attr('data-level'));
    if (k < level) {
      $start.addClass('parent');
      $ultimate = $start;
      level = k;
    }
    if ($start.parent().hasClass('animationWrapper')) {
      $start = $start.parent();
    }
    $start = $start.prev();
  }

  // find group with same ultimate parent
  $ultimate = $ultimate || $selected;
  $start = $ultimate;
  while ($start.length > 0) {
    $start.addClass('group');
    $start = $start.next();
    if ($start.hasClass('animationWrapper')) {
      $start = $start.children().first();
    }

    var m = parseFloat($start.attr('data-level'));
    if (m === 0 && $start[0] !== $ultimate[0]) {
      break;
    }
  }
};

/**
 * @param $parent if specified only the elements after parent are considered (faster lookup)
 */
scout.Tree.prototype._findNodeById = function(nodeId, $parent) {
  if ($parent) {
    return $parent.next('#' + nodeId);
  } else {
    return this._$viewport.find('#' + nodeId);
  }
};

scout.Tree.prototype._findSelectedNodes = function() {
  return this._$viewport.find('.selected');
};

scout.Tree.prototype.getSelectedModelNodes = function() {
  var nodes = [];
  for (var i=0;i<this.selectedNodeIds.length;i++) {
    nodes.push(this._nodeMap[this.selectedNodeIds[i]]);
  }
  return nodes;
};

scout.Tree.prototype.onModelAction = function(event) {
  if (event.type === 'nodesInserted') {
    this._onNodesInserted(event.nodes, event.commonParentNodeId);
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
  } else {
    $.log.warn('Model event not handled. Widget: Tree. Event: ' + event.type + '.');
  }
};

scout.Tree.prototype._renderMenus = function() {
  // NOP
};

scout.Tree.prototype._renderTitle = function() {
  // NOP
};
