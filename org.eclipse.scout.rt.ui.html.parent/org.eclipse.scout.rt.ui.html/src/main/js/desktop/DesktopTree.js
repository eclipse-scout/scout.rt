// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.DesktopTree = function(model, session) {
  scout.DesktopTree.parent.call(this, model, session);

  this._selectedNodes = [];
  this._desktopTable;
};
scout.inherits(scout.DesktopTree, scout.ModelAdapter);

scout.DesktopTree.EVENT_SELECTION_MENUS_CHANGED = 'selectionMenusChanged';

scout.DesktopTree.prototype._render = function($parent) {
  this.$container = $parent.appendDiv(undefined, 'tree');
  this._$desktopTreeScroll = this.$container.appendDiv('DesktopTreeScroll');
  this.scrollbar = new scout.Scrollbar(this._$desktopTreeScroll, 'y');
  this._addNodes(this.model.nodes);
};

/**
 * @override
 */
scout.DesktopTree.prototype.detach = function() {
  scout.DesktopTree.parent.prototype.detach.call(this);
  if (this._desktopTable) {
    this._desktopTable.detach();
  }
};

/**
 * @override
 */
scout.DesktopTree.prototype.attach = function($parent) {
  scout.DesktopTree.parent.prototype.attach.call(this, $parent);
  if (this._desktopTable) {
    this._desktopTable.attach($('#DesktopBench'));
  }
};

scout.DesktopTree.prototype.attachModel = function() {
  var selectedNode;

  if (this._selectedNodes.length > 0) {
    selectedNode = this._selectedNodes[0];
    this.setNodeSelectedById(selectedNode.id);

    if (selectedNode.type === 'table' && !this._desktopTable) {
      this._desktopTable = this.session.widgetMap[selectedNode.id];
      this._desktopTable.attach($('#DesktopBench'));
    }
  }
};

scout.DesktopTree.prototype.setNodeExpandedById = function(nodeId, expanded) {
  var $node = this._findNodeById(nodeId);
  this._setNodeExpanded($node, expanded);
};

scout.DesktopTree.prototype._setNodeExpanded = function($node, expanded) {
  if (!$node.hasClass('can-expand') || $node.data('expanding') || expanded == $node.hasClass('expanded')) {
    return true;
  }

  var node = $node.data('node');
  if (node.expanded != expanded) {
    if (!this.updateFromModelInProgress) {
      this.session.send('nodeExpanded', this.model.id, {
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
          .animateAVCSD('borderSpacing', 135, addExpanded, rotateControl);
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
    $control.css('borderSpacing', 135)
      .animateAVCSD('borderSpacing', 0, null, rotateControl);
  }
};

scout.DesktopTree.prototype.setNodeSelectedById = function(nodeId) {
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

scout.DesktopTree.prototype._setNodeSelected = function($node) {
  if (!$node) {
    this._$desktopTreeScroll.children().select(false);
    return;
  }
  if ($node.isSelected()) {
    return;
  }
  var node = $node.data('node');
  this.model.selectedNodeIds = [node.id];
  this._selectedNodes = [node];

  $node.selectOne();

  if (this._desktopTable) {
    this._desktopTable.detach();
  }

  if (node.type === 'table') {
    this._desktopTable = this.session.widgetMap[node.id];
    this._desktopTable.attach($('#DesktopBench'));
  }


  //FIXME create superclass to handle update generally? or set flag on session and ignore EVERY event? probably not
  if (!this.updateFromModelInProgress) {
    this.session.send('nodesSelected', this.model.id, {
      "nodeIds": [node.id]
    });
  }

  //Menu visibility depend on selectionMenusChanged event which is triggered by selection -> await possible event
  //event is NOT fired if the selectionMenus haven't changed
  var that = this;
  if (this.session.areRequestsPending() || this.session.areEventsQueued()) {
    this.session.listen().done(onEventsProcessed);
  } else {
    this._showSelectionMenuAndHideOthers($node);
  }

  function onEventsProcessed(eventTypes) {
    //Only process if not already processed by _onSelectionMenuChanged
    if (eventTypes.indexOf(scout.DesktopTree.EVENT_SELECTION_MENUS_CHANGED) < 0) {
      that._showSelectionMenuAndHideOthers($node);
    }
  }

};

scout.DesktopTree.prototype._showSelectionMenuAndHideOthers = function($node) {
  var hasSelectionMenus = this.model.selectionMenus && this.model.selectionMenus.length > 0;
  if (hasSelectionMenus && $node.find('.tree-item-menu').length > 0) {
    //Already there
    return;
  }

  if (hasSelectionMenus) {
    this._addNodeMenu($node);
  } else {
    //Don't touch other nodes -> cache 'has menu' state to make it more responsive. Alternative would be to always remove every node menu before adding a new one
    this._removeNodeMenu($node);
  }
};

scout.DesktopTree.prototype._onNodesInserted = function(nodes, parentNodeId) {
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

scout.DesktopTree.prototype._addNodes = function(nodes, $parent) {
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
      .addClass('bread-children')
      .on('click', '', onNodeClicked)
      .data('node', node)
      .attr('data-level', level)
      .css('margin-left', level * 20)
      .css('width', 'calc(100% - ' + (level * 20 + 20) + 'px)')
      .on('contextmenu', onNodeContextClick);

    // decorate with (close) control
    var $control = $node.appendDiv('', 'tree-item-control')
      .on('click', '', onNodeControlClicked);

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

    // Create tables for table pages
    //FIXME really always create table (no gui is created)
    if (node.table) {
      var desktopTable = this.session.widgetMap[node.id];
      if (!desktopTable) {
        node.outlineId = this.model.id;
        new scout.DesktopTable(node, this.session);
      }
    }

    if (this.model.selectedNodeIds && this.model.selectedNodeIds.indexOf(node.id) > -1) {
      if (this._selectedNodes.indexOf(node) <= -1) {
        this._selectedNodes.push(node);
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

  function onNodeContextClick(e) {
    $(this).click();
    //TODO cgu: geht nicht beim ertsen klick?
    $('.tree-item-menu', $(this)).click();
    e.preventDefault();
  }

  // return all created nodes
  return $allNodes;
};

scout.DesktopTree.prototype._onNodeClicked = function(event, $clicked) {
  var nodeId = $clicked.attr('id');
  this.session.send('nodeClicked', this.model.id, {
    "nodeId": nodeId
  });

  this._setNodeSelected($clicked);
  this._setNodeExpanded($clicked, true);

  //TODO cru: update after loading all nodes
  this._updateBreadCrum();
};

scout.DesktopTree.prototype._onNodeControlClicked = function(event, $clicked) {
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

scout.DesktopTree.prototype._onNodeMenuClicked = function(event, $clicked) {
  var that = this;

  //This switch is necessary because we keep 'has menu'-state, see also _showSelectionMenuAndHideOthers
  if (!$clicked.parent().isSelected()) {
    //make sure node is selected when activating the menu, otherwise the wrong menus are returned
    this.session.listen().done(openNodeMenu);
    this._setNodeSelected($clicked.parent(), true);
  } else if (this.session.areRequestsPending() || this.session.areEventsQueued()) {
    //Await pending requests before opening the menu, to make sure the selectionMenus haven't changed in the meantime
    this.session.listen().done(openNodeMenu);
  } else {
    openNodeMenu();
  }

  function openNodeMenu() {
    var $node = $clicked.parent(),
      nodeId = $node.attr('id'),
      y = $clicked.offset().top - that._$desktopTreeScroll.offset().top + 30,
      menus = that.model.selectionMenus;

    if ($('#TreeMenuContainer').length) {
      removeMenu();
      return;
    }

    if (menus && menus.length > 0) {
      var $TreeMenuContainer = $node.parent().appendDiv('TreeMenuContainer')
        .css('right', 24).css('top', y);

      $node.addClass('menu-open');

      // create menu-item and menu-button
      for (var i = 0; i < menus.length; i++) {
        if (menus[i].iconId) {
          $TreeMenuContainer.appendDiv('', 'menu-button')
            .attr('id', menus[i].id)
            .attr('data-icon', menus[i].iconId)
            .attr('data-label', menus[i].text)
            .on('click', '', onMenuItemClicked)
            .hover(onHoverIn, onHoverOut);
        } else {
          $TreeMenuContainer.appendDiv('', 'menu-item', menus[i].text)
            .attr('id', menus[i].id)
            .on('click', '', onMenuItemClicked);
        }
      }

      // wrap menu-buttons and add one div for label
      $('.menu-button', $TreeMenuContainer).wrapAll('<div id="MenuButtons"></div>');
      $('#MenuButtons', $TreeMenuContainer).appendDiv('MenuButtonsLabel');
      $TreeMenuContainer.append($('#MenuButtons', $TreeMenuContainer));

      // animated opening
      var h = $TreeMenuContainer.outerHeight();
      $TreeMenuContainer.css('height', 0).animateAVCSD('height', h);

      // every user action will close menu
      $('*').one('mousedown.treeMenu keydown.treeMenu mousewheel.treeMenu', removeMenu);
    }

    function onHoverIn() {
      $('#MenuButtonsLabel').text($(this).data('label'));
    }

    function onHoverOut() {
      $('#MenuButtonsLabel').text('');
    }

    function onMenuItemClicked() {
      that.session.send('menuAction', $(this).attr('id'));
    }

    function removeMenu() {
      var $TreeMenuContainer = $('#TreeMenuContainer'),
        h = $TreeMenuContainer.outerHeight();

      $TreeMenuContainer.animateAVCSD('height', 0,
        function() {
          $(this).remove();
          $node.removeClass('menu-open');
        });

      $('*').off('.treeMenu');
    }
  }
};

scout.DesktopTree.prototype._onSelectionMenusChanged = function(selectedNodeIds, menus) {
  this.model.selectionMenus = menus;
  var $node = this._findNodeById(this.model.selectedNodeIds[0]);

  //Add menu, but only if the selection on the gui hasn't changed in the meantime
  if (scout.arrays.equalsIgnoreOrder(selectedNodeIds, this.model.selectedNodeIds)) {
    var $selectedNode = this._findSelectedNodes();

    this._showSelectionMenuAndHideOthers($selectedNode);
  }
};

scout.DesktopTree.prototype._updateBreadCrum = function() {
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
    $.log($start.text(), level, l);
    if (l === level + 1) {
      $start.addClass('bread-children');
    } else  if (l === level) {
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

scout.DesktopTree.prototype._findNodeById = function(nodeId) {
  return this._$desktopTreeScroll.find('#' + nodeId);
};

scout.DesktopTree.prototype._findSelectedNodes = function() {
  return this._$desktopTreeScroll.find('.selected');
};

scout.DesktopTree.prototype._addNodeMenu = function($node) {
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

scout.DesktopTree.prototype._removeNodeMenu = function($node) {
  $node.find('.tree-item-menu').remove();
};

scout.DesktopTree.prototype.onModelPropertyChange = function(event) {

};

scout.DesktopTree.prototype.onModelCreate = function() {};

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
  } else if (event.type_ == scout.DesktopTree.EVENT_SELECTION_MENUS_CHANGED) {
    this._onSelectionMenusChanged(event.nodeIds, event.menus);
  } else {
    $.log("Model event not handled. Widget: DesktopTree. Event: " + event.type_ + ".");
  }
};
