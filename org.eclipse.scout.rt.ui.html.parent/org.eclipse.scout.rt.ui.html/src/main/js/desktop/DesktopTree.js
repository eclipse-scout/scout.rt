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

  var initNodeMap = function(node) {
    this._nodeMap[node.id] = node;

    //init this._selectedNodes
    if (this.selectedNodeIds && this.selectedNodeIds.indexOf(node.id) > -1) {
      if (this._selectedNodes.indexOf(node) <= -1) {
        this._selectedNodes.push(node);
      }
    }

  }.bind(this);

  this._visitNodes(this.nodes, initNodeMap);
};

scout.DesktopTree.prototype._visitNodes = function(nodes, func) {
  var i, node;

  for (i = 0; i < nodes.length; i++) {
    node = nodes[i];
    if (node.childNodes && node.childNodes.length > 0) {
      this._visitNodes(node.childNodes, func);
    }
    func(node);
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

    this._showOrHideMenus(this._findNodeById(selectedNode.id));
  }

  // home node for bread crumb
  this._$desktopTreeScroll.prependDiv('', 'tree-home', '')
    .attr('data-level', -1)
    .on('click', '', onHomeClick);

  var that = this;

  function onHomeClick(event) {
    $(this).selectOne();
    that._showOrHideMenus();
    that._updateBreadCrumb();
    that.scrollbar.initThumb();
  }
};

scout.DesktopTree.prototype.setNodeDetailFormChanged = function(nodeId, detailForm) {
  var node = this._nodeMap[nodeId];
  node.detailForm = this.session.getOrCreateModelAdapter(detailForm, this);

  if (this._selectedNodes.indexOf(node) >= 0) {
    this.changeDetailForm(node.detailForm);
  }
};

scout.DesktopTree.prototype.changeDetailForm = function(detailForm) {
  if (this._detailForm && this._detailForm !== detailForm) {
    this.session.desktop.removeForm(this._detailForm);
    this._detailForm = null;
  }

  if (detailForm) {
    this._detailForm = detailForm;
    if (!this._detailForm.isRendered()) {
      this.session.desktop.addForm(this._detailForm);
    }
  }
};

scout.DesktopTree.prototype.changeDetailTable = function(detailTable) {
  if (this._detailTable && this._detailTable !== detailTable) {
    this._detailTable.remove();
    this._detailTable = null;
  }

  if (detailTable) {
    this._detailTable = detailTable;
    if (!this._detailTable.isRendered()) {
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

  if (!this.isRendered()) {
    return;
  }
  if ($node.length === 0) {
    throw "$node must be set.";
  }
  if (!$node.hasClass('can-expand') || $node.data('expanding') || expanded == $node.hasClass('expanded')) {
    return true;
  }

  if (!this.session.processingEvents) {
    this.session.send('nodeExpanded', this.id, {
      "nodeId": node.id,
      "expanded": expanded
    });
  }

  //Only expand / collapse if there are child nodes
  if (!node.childNodes || node.childNodes.length === 0) {
    return true;
  }
  var bread = this.$parent.hasClass('bread-crumb'),
    level = $node.attr('data-level'),
    $control,
    rotateControl = function (now /*, fx*/ ) { $control.css('transform', 'rotate(' + now + 'deg)'); };

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
          return $(this).attr("data-level") <= level;
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
      return $(this).attr("data-level") <= level;
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
      throw "No node found for id " + nodeId;
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
    throw "$node must be set.";
  }
  this._selectedNodes = [node];
  if ($node.isSelected()) {
    return;
  }

  $node.selectOne();

  this.changeDetailTable(node.table);
  this.changeDetailForm(node.detailForm);

  if (!this.session.processingEvents) {
    this.session.send('nodesSelected', this.id, {
      "nodeIds": [node.id]
    });
  }
};

scout.DesktopTree.prototype._onNodesInserted = function(nodes, parentNodeId) {
  var updateNodeMap, parentNode, $parentNode;

  updateNodeMap = function(node) {
    this._nodeMap[node.id] = node;
  }.bind(this);

  this._visitNodes(nodes, updateNodeMap);

  parentNode = this._nodeMap[parentNodeId];
  if (parentNode === undefined) {
    throw "No parentNode found for id " + parentNodeId;
  }

  //update parent with new child nodes
  parentNode.childNodes.push.apply(parentNode.childNodes, nodes);

  $parentNode = this._findNodeById(parentNode.id);
  if (parentNode.expanded) {
    this._setNodeExpanded(parentNode, $parentNode, true);
  }
};

scout.DesktopTree.prototype._addNodes = function(nodes, $parent) {
  for (var i = nodes.length - 1; i >= 0; i--) {
    // create node
    var node = nodes[i];
    var state = '';
    if (node.expanded && node.childNodes.length > 0) {
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
    "nodeId": nodeId
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
    var l = parseFloat($start.attr("data-level"));
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
    var k = $start.attr("data-level");
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

scout.DesktopTree.prototype._findNodeById = function(nodeId) {
  return this._$desktopTreeScroll.find('#' + nodeId);
};

scout.DesktopTree.prototype._findSelectedNodes = function() {
  return this._$desktopTreeScroll.find('.selected');
};

scout.DesktopTree.prototype._setMenus = function(menus) {
  if (this._selectedNodes.length > 0) {
    var $node = this._findNodeById(this._selectedNodes[0].id);
    this._showOrHideMenus($node);
  }
};

scout.DesktopTree.prototype._showOrHideMenus = function($node) {
  var desktopMenu = $('.desktop-menu').data('this'); //FIXME CGU should be done in desktop menu with a listener
  if (desktopMenu) {
    desktopMenu.addItems(scout.menus.filter(this.menus), true);
  }
};

scout.DesktopTree.prototype.onModelAction = function(event) {
  if (event.type_ == 'nodesInserted') {
    this._onNodesInserted(event.nodes, event.commonParentNodeId);
  } else if (event.type_ == 'nodesDeleted') {
    //FIXME implement
    //    this.removeNodes(event.nodeIds);
  } else if (event.type_ == 'nodesSelected') {
    this.setNodeSelectedById(event.nodeIds[0]);
  } else if (event.type_ == 'nodeExpanded') {
    this.setNodeExpandedById(event.nodeId, event.expanded);
  } else if (event.type_ == 'detailFormChanged') {
    this.setNodeDetailFormChanged(event.nodeId, event.detailForm);
  }
  else {
    $.log("Model event not handled. Widget: DesktopTree. Event: " + event.type_ + ".");
  }
};

scout.DesktopTree.prototype.onMenuPropertyChange = function(event) {
  //FIXME CGU implement
};
