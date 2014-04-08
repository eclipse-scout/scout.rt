// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

Scout.DesktopTree = function(scout, $parent, model) {
  this.model = model;
  this.scout = scout;
  this._desktopTable;

  this.scout.widgetMap[model.id] = this;
  this._$desktopTreeScroll = $parent.appendDiv('DesktopTreeScroll');
  this.scrollbar = new Scout.Scrollbar(this._$desktopTreeScroll, 'y');
  this._addNodes(this.model.nodes);
};

Scout.DesktopTree.EVENT_SELECTION_MENUS_CHANGED = 'selectionMenusChanged';

Scout.DesktopTree.prototype.detach = function() {
  this._$desktopTreeScroll.detach();
  if (this._desktopTable) {
    this._desktopTable.detach();
  }
};

Scout.DesktopTree.prototype.attach = function($container) {
  this._$desktopTreeScroll.appendTo($container);
  if (this._desktopTable) {
    this._desktopTable.attach($('#DesktopBench'));
  }
};

Scout.DesktopTree.prototype.attachModel = function() {
  if (this.model.selectedNodeIds) {
    var nodeId = this.model.selectedNodeIds[0];
    this.setNodeSelectedById(nodeId);
  }

  if (this.model.detailTableId && !this._desktopTable) {
    this._desktopTable = this.scout.widgetMap[this.model.detailTableId];
    this._desktopTable.attach($('#DesktopBench'));
  }
};

Scout.DesktopTree.prototype.setNodeExpandedById = function(nodeId, expanded) {
  var $node = this._findNodeById(nodeId);
  this._setNodeExpanded($node, expanded);
};

Scout.DesktopTree.prototype._setNodeExpanded = function($node, expanded) {
  if (!$node.hasClass('can-expand') || $node.data('expanding') || expanded == $node.hasClass('expanded')) {
    return true;
  }

  var node = $node.data('node');
  if (node.expanded != expanded) {
    if (!this.updateFromModelInProgress) {
      this.scout.send('nodeExpanded', this.model.id, {
        "nodeId": node.id,
        "expanded": expanded
      });
    }
  }
  node.expanded = expanded;

  //Only expand / collapse if there are child nodes
  if (!node.childNodes || node.childNodes.length === 0) {
    return true;
  }
  var level = $node.attr('data-level'),
    $control,
    rotateControl;

  if (expanded) {
    this._addNodes(node.childNodes, $node);

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
          .animateAVCSD('height', h, removeContainer, this.scrollbar.initThumb.bind(this.scrollbar));

        // animated control, at the end: parent is expanded
        $node.data('expanding', true); //save expanding state to prevent adding the same nodes twice
        $control = $node.children('.tree-item-control');

        rotateControl = function(now /*, fx*/ ) {
          $control.css('transform', 'rotate(' + now + 'deg)');
        };

        var addExpanded = function() {
          $node.addClass('expanded');
          $node.removeData('expanding');
        };

        $control.css('borderSpacing', 0)
          .animateAVCSD('borderSpacing', 90, addExpanded, rotateControl);
      }
    }
  } else {
    $node.removeClass('expanded');

    // animated closing ;)
    $node.nextUntil(function() {
      return $(this).attr("data-level") <= level;
    })
      .wrapAll('<div id="TreeItemAnimate"></div>)');
    $('#TreeItemAnimate').animateAVCSD('height', 0, $.removeThis, this.scrollbar.initThumb.bind(this.scrollbar));

    // animated control
    $control = $node.children('.tree-item-control');
    rotateControl = function(now /*, fx*/ ) {
      $control.css('transform', 'rotate(' + now + 'deg)');
    };
    $control.css('borderSpacing', 90)
      .animateAVCSD('borderSpacing', 0, null, rotateControl);
  }
};

Scout.DesktopTree.prototype.setNodeSelectedById = function(nodeId) {
  var $node;
  if (nodeId) {
    $node = this._findNodeById(nodeId);
    if ($node.data('node') === undefined) {
      //FIXME happens if tree with a selected node gets collapsed selected node should be showed again after outline switch.
      throw "No node found for id " + nodeId;
    }
  }

  this._setNodeSelected($node);
};

Scout.DesktopTree.prototype._setNodeSelected = function($node) {
  if (!$node) {
    this._$desktopTreeScroll.children().select(false);
    return;
  }
  if ($node.isSelected()) {
    return;
  }
  var node = $node.data('node');
  this.model.selectedNodeIds = [node.id];

  $node.selectOne();

  //FIXME create superclass to handle update generally? or set flag on session and ignore EVERY event? probably not
  if (!this.updateFromModelInProgress) {
    this.scout.send('nodesSelected', this.model.id, {
      "nodeIds": [node.id]
    });
  }

  //Menu visibility depend on selectionMenusChanged event which is triggered by selection -> await possible event
  //event is NOT fired if the selectionMenus haven't changed
  var that = this;
  if (this.scout.areRequestsPending() || this.scout.areEventsQueued()) {
    this.scout.listen().done(onEventsProcessed);
  } else {
    this._showSelectionMenuAndHideOthers();
  }

  function onEventsProcessed(eventTypes) {
    //Only process if not already processed by _onSelectionMenuChanged
    if(eventTypes.indexOf(Scout.DesktopTree.EVENT_SELECTION_MENUS_CHANGED) < 0) {
      that._showSelectionMenuAndHideOthers($node);
    }
  }

};

Scout.DesktopTree.prototype._showSelectionMenuAndHideOthers = function($node) {
  var hasSelectionMenus = this.model.selectionMenus && this.model.selectionMenus.length > 0;
  if (hasSelectionMenus && $node.find('.tree-item-menu').length > 0) {
    //Already there
    return;
  }

  if (hasSelectionMenus) {
    this._addNodeMenu($node);
  }
  else {
    //Don't touch other nodes -> cache 'has menu' state to make it more responsive. Alternative would be to always remove every node menu before adding a new one
    this._removeNodeMenu($node);
  }
};

Scout.DesktopTree.prototype._onNodesInserted = function(nodes, parentNodeId) {
  var $parent = this._findNodeById(parentNodeId);
  var parentNode = $parent.data('node');
  if (parentNode === undefined) {
    throw "No parentNode found for id " + parentNodeId;
  }

  //update parent with new child nodes
  parentNode.childNodes.push.apply(parentNode.childNodes, nodes);

  if (parentNode.expanded) {
    this._setNodeExpanded($parent, true);
  }
};

Scout.DesktopTree.prototype._addNodes = function(nodes, $parent) {
  var $allNodes = $('');

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
      .on('click', '', onNodeClicked)
      .data('node', node)
      .attr('data-level', level)
      .css('margin-left', level * 20)
      .css('width', 'calc(100% - ' + (level * 20 + 20) + 'px)');

    // decorate with (close) control
    var $control = $node.appendDiv('', 'tree-item-control')
      .on('click', '', onNodeControlClicked);

    // rotate control if expanded
    if ($node.hasClass('expanded')) {
      $control.css('transform', 'rotate(90deg)');
    }

    // append first node and successors
    if ($parent) {
      $node.insertAfter($parent);
    } else {
      $node.prependTo(this._$desktopTreeScroll);
    }

    // Create tables for table pages
    //FIXME really always create table (no gui is created)
    if (node.table) {
      var desktopTable = this.scout.widgetMap[node.table.id];
      if (!desktopTable) {
        node.outlineId = this.model.id;
        new Scout.DesktopTable(this.scout, node, this.model.id);
      }
    }

    // collect all nodes for later return
    $allNodes = $allNodes.add($node);

    // if model demands children, create them
    if (node.expanded && node.childNodes) {
      var $n = this._addNodes(node.childNodes, $node);
      $allNodes = $allNodes.add($n);
    }
  }

  var that = this;

  function onNodeClicked(event) {
    return that._onNodeClicked(event, $(this));
  }

  function onNodeControlClicked(event) {
    return that._onNodeControlClicked(event, $(this));
  }

  // return all created nodes
  return $allNodes;
};

Scout.DesktopTree.prototype._onNodeClicked = function(event, $clicked) {
  var nodeId = $clicked.attr('id');
  this.scout.send('nodeClicked', this.model.id, {
    "nodeId": nodeId
  });

  this._setNodeSelected($clicked);
  this._setNodeExpanded($clicked, true);
};

Scout.DesktopTree.prototype._onNodeControlClicked = function(event, $clicked) {
  var $node = $clicked.parent(),
    expanded = !$node.hasClass('expanded');

  //TODO cru/cgu: talk about click on not seleced nodes
  this._onNodeClicked(event, $node);

  if ($node.hasClass('can-expand')) {
    this._setNodeExpanded($node, expanded);
  }

  // prevent immediately reopening
  return false;
};

Scout.DesktopTree.prototype._onNodeMenuClicked = function(event, $clicked) {
  var that = this;

  //This switch is necessary because we keep 'has menu'-state, see also _showSelectionMenuAndHideOthers
  if (!$clicked.parent().isSelected()) {
    //make sure node is selected when activating the menu, otherwise the wrong menus are returned
    this.scout.listen().done(openNodeMenu);
    this._setNodeSelected($clicked.parent(), true);
  } else if (this.scout.areRequestsPending() || this.scout.areEventsQueued()) {
    //Await pending requests before opening the menu, to make sure the selectionMenus haven't changed in the meantime
    this.scout.listen().done(openNodeMenu);
  } else {
    openNodeMenu();
  }

  function openNodeMenu() {
    var nodeId = $clicked.parent().attr('id'),
      x = $clicked.offset().left,
      y = $clicked.offset().top,
      emptySpace = !nodeId;

    var menus = that.model.selectionMenus;
    if (emptySpace) {
      menus = that.model.emptySpaceMenus;
    }

    if (menus && menus.length > 0) {
      new Scout.Menu(that.scout, menus, x, y);
    }

    return false;
  }
};

Scout.DesktopTree.prototype._onSelectionMenusChanged = function(selectedNodeIds, menus) {
  this.model.selectionMenus = menus;
  var $node = this._findNodeById(this.model.selectedNodeIds[0]);

  //Add menu, but only if the selection on the gui hasn't changed in the meantime
  if (arrays.equalsIgnoreOrder(selectedNodeIds, this.model.selectedNodeIds)) {
    var $selectedNode = this._findSelectedNodes();

    this._showSelectionMenuAndHideOthers($selectedNode);
  }
};

Scout.DesktopTree.prototype._findNodeById = function(nodeId) {
  return this._$desktopTreeScroll.find('#' + nodeId);
};

Scout.DesktopTree.prototype._findSelectedNodes = function() {
  return this._$desktopTreeScroll.find('.selected');
};

Scout.DesktopTree.prototype._addNodeMenu = function($node) {
  if ($node.find('.tree-item-menu').length > 0) {
    return;
  }

  $node.appendDiv('', 'tree-item-menu')
    .on('click', '', onNodeMenuClicked);

  var that = this;

  function onNodeMenuClicked(event) {
    return that._onNodeMenuClicked(event, $(this));
  }
};

Scout.DesktopTree.prototype._removeNodeMenu = function($node) {
  $node.find('.tree-item-menu').remove();
};

Scout.DesktopTree.prototype.onModelPropertyChange = function(event) {
  if (this._desktopTable) {
    this._desktopTable.detach();
    this.model.detailTable = null;
  }

  if (event.detailTableId) {
    this._desktopTable = this.scout.widgetMap[event.detailTableId];
    this._desktopTable.attach($('#DesktopBench'));
    this.model.detailTable = this._desktopTable.model;
  }
};

Scout.DesktopTree.prototype.onModelCreate = function() {};

Scout.DesktopTree.prototype.onModelAction = function(event) {
  if (event.type_ == 'nodesInserted') {
    this._onNodesInserted(event.nodes, event.commonParentNodeId);
  } else if (event.type_ == 'nodesDeleted') {
    //FIXME implement
    //    this.removeNodes(event.nodeIds);
  } else if (event.type_ == 'nodesSelected') {
    this.setNodeSelectedById(event.nodeIds[0]);
  } else if (event.type_ == 'nodeExpanded') {
    this.setNodeExpandedById(event.nodeId, event.expanded);
  } else if (event.type_ == Scout.DesktopTree.EVENT_SELECTION_MENUS_CHANGED) {
    this._onSelectionMenusChanged(event.nodeIds, event.menus);
  } else {
    log("Model event not handled. Widget: DesktopTree. Event: " + event.type_ + ".");
  }
};
