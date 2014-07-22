// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.DesktopTree = function() {
  scout.DesktopTree.parent.call(this);
  this._selectedNodes = [];
  this._detailTable;
  this._detailForm;
  this._addAdapterProperties('menus');
  this.nodes = [];
  this._nodeMap = {};
};
scout.inherits(scout.DesktopTree, scout.ModelAdapter);

scout.DesktopTree.prototype.init = function(model, session) {
  scout.DesktopTree.parent.prototype.init.call(this, model, session);

  var initNodeMap = function(parentNode, node) {
    this._nodeMap[node.id] = node;

    if (parentNode) {
      node.parentNode = parentNode;
    }

    //init this._selectedNodes
    if (this.selectedNodeIds && this.selectedNodeIds.indexOf(node.id) > -1) {
      if (this._selectedNodes.indexOf(node) <= -1) {
        this._selectedNodes.push(node);
      }
    }

  }.bind(this);

  this._visitNodes(this.nodes, initNodeMap);
};

scout.DesktopTree.prototype._visitNodes = function(nodes, func, parentNode) {
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

scout.DesktopTree.prototype._render = function($parent) {
  this.$parent = $parent;
  this.$container = $parent.appendDiv(undefined, 'tree');
  this._$desktopTreeScroll = this.$container.appendDiv('DesktopTreeScroll');
  this.scrollbar = new scout.Scrollbar(this._$desktopTreeScroll, 'y');
  this._addNodes(this.nodes);

  var selectedNode;
  if (this._selectedNodes.length > 0) {
    selectedNode = this._selectedNodes[0];
    this.setNodeSelectedById(selectedNode.id);

    this._renderMenus(this._findNodeById(selectedNode.id));
  }

  // home node for bread crumb
  this._$desktopTreeScroll.prependDiv('', 'tree-home', '')
    .attr('data-level', -1)
    .on('click', '', onHomeClick);

  var that = this;

  function onHomeClick(event) {
    $(this).selectOne();
    that._renderMenus();
    that._updateBreadCrumb();
    that.scrollbar.initThumb();
  }
};

scout.DesktopTree.prototype.setNodeDetailFormChanged = function(nodeId, detailForm) {
  var node = this._nodeMap[nodeId];
  node.detailForm = this.session.getOrCreateModelAdapter(detailForm, this);

  if (this._selectedNodes.indexOf(node) >= 0) {
    this.showNodeDetailForm(node);
  }
};

scout.DesktopTree.prototype.showNodeDetailForm = function(node) {
  //unlink detail form if it was closed
  if (node.detailForm && node.detailForm.destroyed) {
    node.detailForm = null;
  }

  if (this._detailForm && this._detailForm !== node.detailForm) {
    this.session.desktop.removeForm(this._detailForm);
    this._detailForm = null;
  }

  if (node.detailForm) {
    this._detailForm = node.detailForm;
    if (!this._detailForm.rendered) {
      this.session.desktop.addForm(this._detailForm);
    }
  }
};

scout.DesktopTree.prototype.showNodeDetailTable = function(node) {
  var detailTable = node.table;

  if (this._detailTable && this._detailTable !== detailTable) {
    this._detailTable.remove();
    this._detailTable.desktopMenuContributor = false;
    this._detailTable = null;
  }

  if (detailTable) {
    this._detailTable = detailTable;
    this._detailTable.desktopMenuContributor = true;
    if (!this._detailTable.rendered) {
      this._detailTable.render($('#DesktopBench'));
    }
  }
};

scout.DesktopTree.prototype.setNodeExpandedById = function(nodeId, expanded) {
  var node = this._nodeMap[nodeId];
  var $node = this._findNodeById(nodeId);
  this._setNodeExpanded(node, $node, expanded);
};

scout.DesktopTree.prototype._setNodeExpanded = function(node, $node, expanded) {
  node.expanded = expanded;

  if (!this.rendered) {
    return;
  }
  if ($node.length === 0) {
    throw '$node must be set.';
  }
  if (!$node.hasClass('can-expand') || $node.data('expanding') || expanded == $node.hasClass('expanded')) {
    return true;
  }

  if (!this.session.processingEvents) {
    this.session.send('nodeExpanded', this.id, {
      'nodeId': node.id,
      'expanded': expanded
    });
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
    this._updateBreadCrumb();

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
        $newNodes.wrapAll('<div id="TreeItemAnimate"></div>)');
        var that = this;
        var h = $newNodes.height() * $newNodes.length,
          removeContainer = function() {
            $(this).replaceWith($(this).contents());
            that.scrollbar.initThumb();
          };

        $('#TreeItemAnimate').css('height', 0)
          .animateAVCSD('height', h, removeContainer, this.scrollbar.initThumb.bind(this.scrollbar), 200);

        // animated control, at the end: parent is expanded
        $node.data('expanding', true); //save expanding state to prevent adding the same nodes twice
        $control = $node.children('.tree-item-control');

        var addExpanded = function() {
          $node.addClass('expanded');
          $node.removeData('expanding');
        };

        $control.css('borderSpacing', 0)
          .animateAVCSD('borderSpacing', 135, addExpanded, rotateControl, 200);
      }
    }
  } else {
    $node.removeClass('expanded');

    // animated closing ;)
    $node.nextUntil(function() {
      return $(this).attr('data-level') <= level;
    })
      .wrapAll('<div id="TreeItemAnimate"></div>)');
    $('#TreeItemAnimate').animateAVCSD('height', 0, $.removeThis, this.scrollbar.initThumb.bind(this.scrollbar), 200);

    // animated control
    $control = $node.children('.tree-item-control');

    $control.css('borderSpacing', 135)
      .animateAVCSD('borderSpacing', 0, null, rotateControl, 200);
  }
};

scout.DesktopTree.prototype.setNodeSelectedById = function(nodeId) {
  var $node, node;
  if (nodeId) {
    $node = this._findNodeById(nodeId);
    node = this._nodeMap[nodeId];
    if (node === undefined) {
      throw 'No node found for id ' + nodeId;
    }
  }

  this._setNodeSelected(node, $node);
};

scout.DesktopTree.prototype._setNodeSelected = function(node, $node) {
  if (!node) {
    this._selectedNodes = [];
    this._$desktopTreeScroll.children().select(false);
    return;
  }
  if ($node.length === 0) {
    throw '$node must be set.';
  }
  this._selectedNodes = [node];
  if ($node.isSelected()) {
    return;
  }

  $node.selectOne();

  this.showNodeDetailTable(node);
  this.showNodeDetailForm(node);

  if (!this.session.processingEvents) {
    this.session.send('nodesSelected', this.id, {
      'nodeIds': [node.id]
    });
  }
};

scout.DesktopTree.prototype._onNodesInserted = function(nodes, parentNodeId) {
  var updateNodeMap, parentNode, $parentNode;

  updateNodeMap = function(parentNode, node) {
    if (parentNode) {
      node.parentNode = parentNode;
    }
    this._nodeMap[node.id] = node;
  }.bind(this);

  parentNode = this._nodeMap[parentNodeId];
  this._visitNodes(nodes, updateNodeMap, parentNode);

  //update parent with new child nodes
  parentNode.childNodes.push.apply(parentNode.childNodes, nodes);

  $parentNode = this._findNodeById(parentNode.id);
  if (parentNode.expanded) {
    this._setNodeExpanded(parentNode, $parentNode, true);
  }
};

scout.DesktopTree.prototype._onNodesDeleted = function(nodeIds, parentNodeId) {
  var updateNodeMap, parentNode, i, nodeId, node, deletedNodes = [];

  //update model and nodemap
  updateNodeMap = function(parentNode, node) {
    delete this._nodeMap[node.id];
  }.bind(this);

  if (parentNodeId >= 0) {
    parentNode = this._nodeMap[parentNodeId];
    if (!parentNode) {
      throw 'Parent node could not be found. Id: ' + parentNodeId;
    }
  }

  for (i = 0; i < nodeIds.length; i++) {
    nodeId = nodeIds[i];
    node = this._nodeMap[nodeId];
    if (parentNode) {
      if (node.parentNode !== parentNode) {
        throw 'Unexpected parent. Node.parent: ' + node.parentNode + ', parentNode: ' + parentNode;
      }
      scout.arrays.remove(parentNode.childNodes, node);
    }
    else {
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
//      this._setNodeExpanded(parentNode, $parentNode, false);
//    }
  }
};

scout.DesktopTree.prototype._onAllNodesDeleted = function(parentNodeId) {
  var updateNodeMap, parentNode, i, node, nodes;

  //Update model and nodemap
  updateNodeMap = function(parentNode, node) {
    delete this._nodeMap[node.id];
  }.bind(this);

  if (parentNodeId >= 0) {
    parentNode = this._nodeMap[parentNodeId];
    if (!parentNode) {
      throw 'Parent node could not be found. Id: ' + parentNodeId;
    }
  }
  if (parentNode) {
    nodes = parentNode.childNodes;
    parentNode.childNodes = [];
  }
  else {
    nodes = this.nodes;
    this.nodes = [];
  }
  this._visitNodes(nodes, updateNodeMap);

  //remove node from html document
  if (this.rendered) {
    this._removeNodes(nodes, parentNodeId);
  }
};

/**
 *
 * @param $parentNode optional. If not provided, parentNodeId will be used to find $parentNode.
 */
scout.DesktopTree.prototype._removeNodes = function(nodes, parentNodeId, $parentNode) {
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

scout.DesktopTree.prototype._addNodes = function(nodes, $parent) {
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
      .css('margin-left', level * 20)
      .css('width', 'calc(100% - ' + (level * 20 + 20) + 'px)')
      .on('contextmenu', this._onNodeContextClick.bind(this));

    // decorate with (close) control
    var $control = $node.appendDiv('', 'tree-item-control')
      .on('click', '', this._onNodeControlClicked.bind(this));

    // rotate control if expanded
    if ($node.hasClass('expanded')) {
      $control.css('transform', 'rotate(270deg)');
    }

    // append first node and successors
    if ($parent) {
      $node.insertAfter($parent);
    } else {
      $node.prependTo(this._$desktopTreeScroll);
    }

    if (node.table) {
      node.table = this.session.getOrCreateModelAdapter(node.table, this);
    }

    if (node.detailForm) {
      node.detailForm = this.session.getOrCreateModelAdapter(node.detailForm, this);
    }

    // if model demands children, create them
    if (node.expanded && node.childNodes) {
      this._addNodes(node.childNodes, $node);
    }
  }
};

scout.DesktopTree.prototype._onNodeClicked = function(event) {
  var $clicked = $(event.currentTarget),
    nodeId = $clicked.attr('id'),
    node = this._nodeMap[nodeId];

  this.session.send('nodeClicked', this.id, {
    'nodeId': nodeId
  });

  this._setNodeSelected(node, $clicked);
  this._setNodeExpanded(node, $clicked, true);
  this._updateBreadCrumb();
};

scout.DesktopTree.prototype._onNodeControlClicked = function(event) {
  var $clicked = $(event.currentTarget),
    $node = $clicked.parent(),
    expanded = !$node.hasClass('expanded'),
    node = $node.data('node');

  //TODO cru/cgu: talk about click on not seleced nodes
  this._setNodeSelected(node, $node);
  this._setNodeExpanded(node, $node, expanded);

  // prevent immediately reopening
  return false;
};

scout.DesktopTree.prototype._onNodeContextClick = function(event) {
  var $clicked = $(event.currentTarget);

  event.preventDefault();
  $clicked.click();

  var x = 20,
    y = $clicked.offset().top - this._$desktopTreeScroll.offset().top + 32;

  scout.menus.showContextMenuWithWait(this.session, showContextMenu.bind(this));

  function showContextMenu() {
    scout.menus.showContextMenu(scout.menus.filter(this.menus), this._$desktopTreeScroll, $clicked, undefined, x, y);
  }
};

scout.DesktopTree.prototype._updateBreadCrumb = function() {
  var $selected = $('.selected', this._$desktopTreeScroll),
    $allNodes = this._$desktopTreeScroll.children(),
    level = parseFloat($selected.attr('data-level'));

  // first remove and select selected
  $allNodes.removeClass('bread-parent bread-selected bread-children');
  $selected.addClass('bread-selected');

  // find all parents
  var $start = $selected.next();
  while ($start.length > 0) {
    var l = parseFloat($start.attr('data-level'));
    if (l === level + 1) {
      $start.addClass('bread-children');
    } else if (l === level) {
      break;
    }
    $start = $start.next();
  }

  // find direct children
  $start = $selected.prev();
  while ($start.length > 0) {
    var k = $start.attr('data-level');
    if (k < level) {
      $start.addClass('bread-parent');
      level = k;
    }
    $start = $start.prev();
  }
};

scout.DesktopTree.prototype.doBreadCrumb = function(show) {
  if (show && !this.$parent.hasClass('bread-crumb')) {
    this._updateBreadCrumb();
    this.$parent.addClass('bread-crumb');
  } else if (!show && this.$parent.hasClass('bread-crumb')) {
    this.$parent.removeClass('bread-crumb');
  }

  this.scrollbar.initThumb();

};

/**
 * @param $parent if specified only the elements after parent are considered (faster lookup)
 */
scout.DesktopTree.prototype._findNodeById = function(nodeId, $parent) {
  if ($parent) {
    return $parent.next('#' + nodeId);
  } else {
    return this._$desktopTreeScroll.find('#' + nodeId);
  }
};

scout.DesktopTree.prototype._findSelectedNodes = function() {
  return this._$desktopTreeScroll.find('.selected');
};

scout.DesktopTree.prototype._setMenus = function(menus) {
  if (this._selectedNodes.length > 0) {
    var $node = this._findNodeById(this._selectedNodes[0].id);
    this._renderMenus($node);
  }
};

scout.DesktopTree.prototype._renderMenus = function($node) {
  if (this.session.desktop) {
    this.session.desktop.onMenusUpdated('tree', scout.menus.filter(this.menus));
  }
};

scout.DesktopTree.prototype.onModelAction = function(event) {
  if (event.type == 'nodesInserted') {
    this._onNodesInserted(event.nodes, event.commonParentNodeId);
  } else if (event.type == 'nodesDeleted') {
    this._onNodesDeleted(event.nodeIds, event.commonParentNodeId);
  } else if (event.type == 'allNodesDeleted') {
    this._onAllNodesDeleted(event.commonParentNodeId);
  } else if (event.type == 'nodesSelected') {
    this.setNodeSelectedById(event.nodeIds[0]);
  } else if (event.type == 'nodeExpanded') {
    this.setNodeExpandedById(event.nodeId, event.expanded);
  } else if (event.type == 'detailFormChanged') {
    this.setNodeDetailFormChanged(event.nodeId, event.detailForm);
  } else {
    $.log('Model event not handled. Widget: DesktopTree. Event: ' + event.type + '.');
  }
};

scout.DesktopTree.prototype.onMenuPropertyChange = function(event) {
  //FIXME CGU implement
};
