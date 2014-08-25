// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.Tree = function() {
  scout.Tree.parent.call(this);
  this.selectedNodeIds = [];
  this.nodes = [];
  this._nodeMap = {};
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
};

scout.Tree.prototype._visitNodes = function(nodes, func, parentNode) {
  var i, node;
  if (!nodes) {
    return;
  }

  for (i = 0; i < nodes.length; i++) {
    node = nodes[i];
    if (node.childNodes && node.childNodes.length > 0) {
      this._visitNodes(node.childNodes, func, node);
    }
    func(parentNode, node);
  }
};

scout.Tree.prototype._render = function($parent) {
  this.$parent = $parent;
  this.$container = $parent.appendDIV('tree');
  this._$treeScroll = this.$container.appendDIV('scrollable-y');
  this.scrollbar = new scout.Scrollbar(this._$treeScroll, 'y');
  this._addNodes(this.nodes);

  if (this.selectedNodeIds.length > 0) {
    this._renderSelection();
  }
};

scout.Tree.prototype.setNodeExpanded = function(node, $node, expanded) {
  if (node.expanded !== expanded) {
    node.expanded = expanded;

    this.session.send('nodeExpanded', this.id, {
      'nodeId': node.id,
      'expanded': expanded
    });
  }

  this._renderNodeExpanded(node, $node, expanded);
};

scout.Tree.prototype._renderNodeExpanded = function(node, $node, expanded) {
  var $wrapper;
  var that = this;

  //check for expanding to prevent adding the same nodes twice on fast multiple clicks
  if (expanded === $node.hasClass('expanded') ||  $node.data('expanding')) {
    return;
  }

  //Only expand / collapse if there are child nodes
  if (!node.childNodes || node.childNodes.length === 0) {
    return true;
  }

  var bread = this.$parent.hasClass('bread-crumb'),
    level = $node.attr('data-level'),
    $control,
    rotateControl = function(now, fx) {
      $control.css('transform', 'rotate(' + now + 'deg)');
    };

  if (expanded) {
    this._addNodes(node.childNodes, $node);
    this._updateItemPath();
    this.scrollbar.initThumb();

    if (bread) {
      $node.addClass('expanded');
      return;
    }

    // open node
    if ($node.hasClass('can-expand') && !$node.hasClass('expanded')) {
      var $newNodes = $node.nextUntil(
        function() {
          return $(this).attr('data-level') <= level;
        }
      );
      if ($newNodes.length) {
        // animated opening ;)
        $wrapper = $newNodes.wrapAll('<div class="animationWrapper">').parent();
        var h = $newNodes.height() * $newNodes.length;
        var removeContainer = function() {
          $(this).replaceWith($(this).contents());
          that.scrollbar.initThumb();
        };

        $wrapper.css('height', 0)
          .animateAVCSD('height', h, removeContainer, this.scrollbar.initThumb.bind(this.scrollbar), 200);

        // animated control, at the end: parent is expanded
        $node.data('expanding', true);
        $control = $node.children('.tree-item-control');

        var addExpanded = function() {
          $node.addClass('expanded');
          $node.removeData('expanding');
        };

        $control.css('borderSpacing', 0)
          .animateAVCSD('borderSpacing', 90, addExpanded, rotateControl, 200);
      }
    }
  } else {
    $node.removeClass('expanded');

    // animated closing ;)
    $wrapper = $node.nextUntil(function() {
      return $(this).attr('data-level') <= level;
    }).wrapAll('<div class="animationWrapper">)').parent();

    $wrapper.animateAVCSD('height', 0, $.removeThis, this.scrollbar.initThumb.bind(this.scrollbar), 200);

    // animated control
    $control = $node.children('.tree-item-control');

    $control.css('borderSpacing', 90)
      .animateAVCSD('borderSpacing', 0, null, rotateControl, 200);
  }
};

scout.Tree.prototype.setNodesSelected = function(nodes, $nodes) {
  var nodeIds = new Array(nodes.length), i;
  for (i=0; i < nodes.length; i++) {
    nodeIds[i] = nodes[i];
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
  var i, parentNode, $parentNode, node, $node;

  this._$treeScroll.children().select(false);

  //If $nodes are given, just render the selection
  if ($nodes) {
    for (i=0; i < $nodes.length; i++) {
      $nodes[i].select(true);
    }
  } else { //Otherwise render the selection based on the this.selectedNodeIds
    for (i=0; i < this.selectedNodeIds.length; i++) {
      node = this._nodeMap[this.selectedNodeIds[i]];
      $node = this._findNodeById(node.id);

      //If $node is currently not displayed (due to a collapsed parent node), expand the parents
      if ($node.length === 0) {
        this._expandAllParentNodes(node);
        $node = this._findNodeById(node.id);
        if ($node.length === 0) {
          throw 'Stil no node found. ' + node;
        }
      }

      $node.select(true);
    }
  }

  this._updateItemPath();
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
        throw "Illegal state, $parentNode should be displayed. Rendered: " + this.rendered + ", parentNode: " + parentNodes[i];
      }
      this.setNodeExpanded(parentNodes[i], $parentNode, true);
    }
  }
};

scout.Tree.prototype._onNodesInserted = function(nodes, parentNodeId) {
  var parentNode, $parentNode;

  parentNode = this._nodeMap[parentNodeId];
  this._visitNodes(nodes, this._initTreeNode.bind(this), parentNode);

  //update parent with new child nodes
  parentNode.childNodes.push.apply(parentNode.childNodes, nodes);

  $parentNode = this._findNodeById(parentNode.id);
  if (parentNode.expanded) {
    this._renderNodeExpanded(parentNode, $parentNode, true);
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

    //FIXME CGU handle expansion
    //    if (parentNode) {
    //      var $parentNode = this._findNodeById(parentNode.id);
    //      this.setNodeExpanded(parentNode, $parentNode, false);
    //    }
  }
};

scout.Tree.prototype._onAllNodesDeleted = function(parentNodeId) {
  var updateNodeMap, parentNode, i, node, nodes;

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
    this._renderNodeExpanded(node, $node, expanded);
  }
};

/**
 *
 * @param $parentNode optional. If not provided, parentNodeId will be used to find $parentNode.
 */
scout.Tree.prototype._removeNodes = function(nodes, parentNodeId, $parentNode) {
  var i, $node, node;

  //Find parentNode to increase search performance. If there is only one child there is no benefit by searching its parent first.
  if (!$parentNode && parentNodeId >= 0 && nodes.length > 1) {
    $parentNode = this._findNodeById(parentNodeId);
  }

  for (i = 0; i < nodes.length; i++) {
    node = nodes[i];
    $node = this._findNodeById(node.id, $parentNode);

    if (node.childNodes && node.childNodes.length > 0) {
      this._removeNodes(node.childNodes, node.id, $node);
    }

    $node.remove();
  }
};

scout.Tree.prototype._addNodes = function(nodes, $parent) {
  if (!nodes) {
    return;
  }

  for (var i = nodes.length - 1; i >= 0; i--) {
    // create node
    var node = nodes[i];
    var state = '';
    if (node.expanded && node.childNodes && node.childNodes.length > 0) {
      state = 'expanded ';
    }
    if (!node.leaf) {
      state += 'can-expand '; //TODO rename to leaf
    }
    var level = $parent ? $parent.data('level') + 1 : 0;

    var $node = $.makeDiv(node.id, 'tree-item ' + state, node.text)
      .on('click', '', this._onNodeClicked.bind(this))
      .data('node', node)
      .attr('data-level', level)
      .css('padding-left', level * 20 + 30);

    // decorate with (close) control
    var $control = $node.prependDiv('', 'tree-item-control')
      .on('click', '', this._onNodeControlClicked.bind(this));

    // rotate control if expanded
    if ($node.hasClass('expanded')) {
      $control.css('transform', 'rotate(90deg)');
    }

    // append first node and successors
    if ($parent) {
      $node.insertAfter($parent);
    } else {
      $node.prependTo(this._$treeScroll);
    }

    // if model demands children, create them
    if (node.expanded && node.childNodes) {
      this._addNodes(node.childNodes, $node);
    }
  }
};

scout.Tree.prototype._onNodeClicked = function(event) {
  var $clicked = $(event.currentTarget),
    nodeId = $clicked.attr('id'),
    node = this._nodeMap[nodeId];

  this.session.send('nodeClicked', this.id, {
    'nodeId': nodeId
  });

  this.setNodesSelected([node.id], [$clicked]);
};

scout.Tree.prototype._onNodeControlClicked = function(event) {
  var $clicked = $(event.currentTarget),
    $node = $clicked.parent(),
    expanded = !$node.hasClass('expanded'),
    node = $node.data('node');

  //TODO cru/cgu: talk about click on not seleced nodes
  this.setNodesSelected([node.id], [$node]);
  this.setNodeExpanded(node, $node, expanded);

  // prevent immediately reopening
  return false;
};

scout.Tree.prototype._updateItemPath = function() {
  var $selected = this._findSelectedNodes(),
    $allNodes = this._$treeScroll.children(),
    level = parseFloat($selected.attr('data-level'));

  // first remove and select selected
  $allNodes.removeClass('parent children group');

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
    return this._$treeScroll.find('#' + nodeId);
  }
};

scout.Tree.prototype._findSelectedNodes = function() {
  return this._$treeScroll.find('.selected');
};

scout.Tree.prototype.onModelAction = function(event) {
  if (event.type == 'nodesInserted') {
    this._onNodesInserted(event.nodes, event.commonParentNodeId);
  } else if (event.type == 'nodesDeleted') {
    this._onNodesDeleted(event.nodeIds, event.commonParentNodeId);
  } else if (event.type == 'allNodesDeleted') {
    this._onAllNodesDeleted(event.commonParentNodeId);
  } else if (event.type == 'nodesSelected') {
    this._onNodesSelected(event.nodeIds);
  } else if (event.type == 'nodeExpanded') {
    this._onNodeExpanded(event.nodeId, event.expanded);
  } else {
    $.log('Model event not handled. Widget: Tree. Event: ' + event.type + '.');
  }
};

scout.Tree.prototype._setMenus = function() {
  // TODO cru: braucht es doch nicht mehr!
};
