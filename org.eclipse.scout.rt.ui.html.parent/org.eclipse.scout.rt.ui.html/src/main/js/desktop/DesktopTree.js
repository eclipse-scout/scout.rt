// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.DesktopTree = function() {
  scout.DesktopTree.parent.call(this);
  this._selectedNodes = [];
  this._desktopTable;
};
scout.inherits(scout.DesktopTree, scout.ModelAdapter);

scout.DesktopTree.prototype.init = function(model, session) {
  scout.DesktopTree.parent.prototype.init.call(this, model, session);

  session.getOrCreateModelAdapters(model.menus, this);
};

scout.DesktopTree.prototype._render = function($parent) {
  this.$parent = $parent;
  this.$container = $parent.appendDiv(undefined, 'tree');
  this._$desktopTreeScroll = this.$container.appendDiv('DesktopTreeScroll');
  this.scrollbar = new scout.Scrollbar(this._$desktopTreeScroll, 'y');
  this._addNodes(this.model.nodes);

  // home node and menu section for bread crumb
  this._$desktopTreeScroll.prependDiv('', 'tree-home', '')
    .attr('data-level', -1)
    .on('click', '', onHomeClick);
  this._$treeMenu = this.$container.appendDiv('', 'tree-menu', '');

  var that = this;

  function onHomeClick(event) {
    $(this).selectOne();
    that._$treeMenu.empty();
    that._updateBreadCrumb();
    that.scrollbar.initThumb();
  }
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

    this._showOrHideMenus(this._findNodeById(selectedNode.id));
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

        //TODO cgu: performance issue:
        var table = new scout.DesktopTable();
        table.init(node, this.session);
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
    e.preventDefault();

    //TODO cgu: geht nicht beim ertsen klick?
    $(this).click();
    that._onNodeMenuClicked(e, $('.tree-item-menu', this));
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
  this._updateBreadCrumb();
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
  var $treeMenuContainer = $('.tree-menu-container',  this._$desktopTreeScroll);

  if ($treeMenuContainer.length) {
    removeMenu();
    return;
  }

  var $children = this._$treeMenu.children();
  if ($children.length) {
    $treeMenuContainer =  this._$desktopTreeScroll.appendDiv('', 'tree-menu-container')
      .css('right', 20)
      .css('top', $clicked.offset().top - this._$desktopTreeScroll.offset().top + 30);

    $clicked.parent().addClass('menu-open');

    $children.clone(true).appendTo($treeMenuContainer);

    // animated opening
    $treeMenuContainer.css('height', 0).heightToContent(150);


    // every user action will close menu; menu is removed in 'click' event, see onMenuItemClicked()
    var closingEvents = 'mousedown.treeMenu keydown.treeMenu mousewheel.treeMenu';
    $(document).one(closingEvents, removeMenu);
    $treeMenuContainer.one(closingEvents, $.suppressEvent);
  }

  var that = this;

  function removeMenu() {
    var $treeMenuContainer = $('.tree-menu-container',  this._$desktopTreeScroll);

    if (!$treeMenuContainer.length) {
      return;
    }

    // Animate
    var h = $treeMenuContainer.outerHeight();
    $treeMenuContainer.animateAVCSD('height', 0,
      function() {
        $(this).remove();
        $clicked.parent().removeClass('menu-open');
      }, null, 150);

    // Remove all cleanup handlers
    $(document).off('.treeMenu');
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
    this._$treeMenu.heightToContent(150);
    //this._$desktopTreeScroll.css('height', 'calc(100% - ' + h + ')');
  } else if (!show && this.$parent.hasClass('bread-crumb')) {
    this.$parent.removeClass('bread-crumb');
    this._$treeMenu.animateAVCSD('height', 0, null, null, 150);
  }

  this.scrollbar.initThumb();

};

scout.DesktopTree.prototype._findNodeById = function(nodeId) {
  return this._$desktopTreeScroll.find('#' + nodeId);
};

scout.DesktopTree.prototype._findSelectedNodes = function() {
  return this._$desktopTreeScroll.find('.selected');
};

scout.DesktopTree.prototype.filterSingleSelectionNodeMenus = function(menus) {
  return scout.menus.filter(menus, ['SingleSelection']);
};

scout.DesktopTree.prototype.filterMultiSelectionNodeMenus = function(menus) {
  return scout.menus.filter(menus, ['MultiSelection']);
};

scout.DesktopTree.prototype._setMenus = function(menus) {
  this.model.menus = menus;

  //Register new menus
  //FIXME CGU refactor to getOrCreate
  if (menus) {
    for (var i = 0; i < this.model.menus.length; i++) {
      if (!this.session.widgetMap[menus[i]]) {
        var menu = this.session.objectFactory.create(menus[i]);
        menu.owner = this;
      }
    }
  }

  if (this.model.selectedNodeIds && this.model.selectedNodeIds.length > 0) {
    var $node = this._findNodeById(this.model.selectedNodeIds[0]);
    this._showOrHideMenus($node);
  }
};

scout.DesktopTree.prototype._showOrHideMenus = function($node) {
  var menus = this.model.menus;
  if (menus) {
    menus = this.filterSingleSelectionNodeMenus(this.model.menus);
  }
  if (menus && menus.length > 0) {
    this._addNodeMenu($node, menus);
  } else {
    this._removeNodeMenu($node);
  }

};

scout.DesktopTree.prototype._addNodeMenu = function($node, menus) {
  var $menu = $node.appendDiv('', 'tree-item-menu')
    .data('menus', menus)
    .on('click', '', onNodeMenuClicked);

  // delete old menu menu
  this._$treeMenu.empty();

  // create menu-item and menu-button
  if (menus && menus.length > 0) {
    for (var i = 0; i < menus.length; i++) {
      if (menus[i].separator) {
        continue;
      }
      if (menus[i].iconId) {
        this._$treeMenu.appendDiv('', 'menu-button')
          .attr('id', menus[i].id)
          .attr('data-icon', menus[i].iconId)
          .attr('data-label', menus[i].text)
          .on('click', '', onMenuItemClicked)
          .mouseenter(onHoverIn);
      } else {
        this._$treeMenu.appendDiv('', 'menu-item', menus[i].text)
          .attr('id', menus[i].id)
          .on('click', '', onMenuItemClicked);
      }
    }

    // wrap menu-buttons and add div for label
    $('.menu-button',  this._$treeMenu).wrapAll('<div class="menu-buttons"></div>');
    var $menuButton = $('.menu-buttons',  this._$treeMenu);
    $menuButton.mouseleave(onHoverOut);
    $menuButton.prependDiv('', 'menu-buttons-label');
    this._$treeMenu.append($menuButton);

    // size menu
    this._$treeMenu.heightToContent(150);
  }

  var that = this;

  function onNodeMenuClicked(event) {
    return that._onNodeMenuClicked(event, $(this));
  }

  function onHoverIn() {
    var $container = $(this).parent().parent();
    $container.css('height', 'auto');
    $('.menu-buttons-label', $container)
      .text($(this).data('label'))
      .heightToContent(150);
  }

  function onHoverOut() {
    var $container = $(this).parent().parent();

    $('.menu-buttons-label', $container)
      .stop()
      .animateAVCSD('height', 0, null, function() { $(this).text(''); }, 150);
  }

  function onMenuItemClicked() {
    that.session.send('menuAction', $(this).attr('id'));
  }
};

scout.DesktopTree.prototype._removeNodeMenu = function($node) {
  $node.find('.tree-item-menu').remove();
  this._$treeMenu.empty();
};

scout.DesktopTree.prototype.onModelPropertyChange = function(event) {
  if (event.hasOwnProperty('menus')) {
    this._setMenus(event.menus);
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
  } else {
    $.log("Model event not handled. Widget: DesktopTree. Event: " + event.type_ + ".");
  }
};

scout.DesktopTree.prototype.onMenuPropertyChange = function(event) {
  //FIXME CGU implement
};
