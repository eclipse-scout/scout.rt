/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.Tree = function() {
  scout.Tree.parent.call(this);
  this.$data;
  this.nodes = []; // top-level nodes
  this.visibleNodesFlat = [];
  this.visibleNodesMap = {};
  this.nodesMap = {}; // all nodes by id
  this._addAdapterProperties(['menus', 'keyStrokes']);
  this._additionalContainerClasses = ''; // may be used by subclasses to set additional CSS classes
  this._treeItemPaddingLeft = 23;
  this._treeItemCheckBoxPaddingLeft = 29;
  this._treeItemPaddingLevel = 15;
  this.menus = [];
  this.menuBar;
  this.checkedNodes = [];
  this._filters = [];
  this._doubleClickSupport = new scout.DoubleClickSupport();
  this._$animationWrapper; // used by _renderExpansion()
  this._$expandAnimationWrappers = [];
  this._filterMenusHandler = this._filterMenus.bind(this);
  //contains all parents of a selected node, the selected node and the first level children
  this._inSelectionPathList = {};
  this.groupedNodes = {};

  this.viewRangeRendered = new scout.Range(0, 0);
  this.viewRangeSize = 20;

  this.startAnimationFunc = function() {
    this.runningAnimations++;
  }.bind(this);
  this.runningAnimations = 0;
  this.runningAnimationsFinishFunc = function() {
    this.runningAnimations--;
    if (this.runningAnimations <= 0) {
      this.runningAnimations = 0;
      this._renderViewportBlocked = false;
      this.invalidateLayoutTree();
    }
  }.bind(this);

  this.nodeHeight = 0;
  this.nodeWidth = 0;
};
scout.inherits(scout.Tree, scout.ModelAdapter);

scout.Tree.DisplayStyle = {
  DEFAULT: 'default',
  BREADCRUMB: 'breadcrumb'
};

scout.Tree.prototype._init = function(model) {
  scout.Tree.parent.prototype._init.call(this, model);
  this.addFilter(new scout.LazyNodeFilter(this), true);
  this.breadcrumbFilter = new scout.TreeBreadcrumbFilter(this);
  if (this.displayStyle === scout.Tree.DisplayStyle.BREADCRUMB) {
    this.addFilter(this.breadcrumbFilter, true);
  }
  this.initialTraversing = true;
  this._visitNodes(this.nodes, this._initTreeNode.bind(this));
  this.initialTraversing = false;
  this.selectedNodes = this._nodesByIds(this.selectedNodes);
  this.menuBar = scout.create('MenuBar', {
    parent: this,
    menuOrder: new scout.MenuItemsOrder(this.session, 'Tree'),
    menuFilter: this._filterMenusHandler
  });
  this.menuBar.bottom();
  this._updateItemPath(true);
  this._syncDisplayStyle(this.displayStyle);
  this._syncKeyStrokes(this.keyStrokes);
  this._syncMenus(this.menus);
};

/**
 * @override ModelAdapter.js
 */
scout.Tree.prototype._initKeyStrokeContext = function(keyStrokeContext) {
  scout.Tree.parent.prototype._initKeyStrokeContext.call(this, keyStrokeContext);

  this._initTreeKeyStrokeContext(keyStrokeContext);
};

scout.Tree.prototype._initTreeKeyStrokeContext = function(keyStrokeContext) {
  var modifierBitMask = scout.keyStrokeModifier.NONE;

  keyStrokeContext.registerKeyStroke([
      new scout.TreeSpaceKeyStroke(this),
      new scout.TreeNavigationUpKeyStroke(this, modifierBitMask),
      new scout.TreeNavigationDownKeyStroke(this, modifierBitMask),
      new scout.TreeCollapseAllKeyStroke(this, modifierBitMask),
      new scout.TreeCollapseOrDrillUpKeyStroke(this, modifierBitMask),
      new scout.TreeNavigationEndKeyStroke(this, modifierBitMask),
      new scout.TreeExpandOrDrillDownKeyStroke(this, modifierBitMask)
    ]
    .concat(this.menus));

  // Prevent default action and do not propagate ↓ or ↑ keys if ctrl- or alt-modifier is not pressed.
  // Otherwise, an '↑-event' on the first node, or an '↓-event' on the last row will bubble up (because not consumed by tree navigation keystrokes) and cause a superior tree to move its selection;
  // Use case: - outline tree with a detail form that contains a tree;
  //           - preventDefault because of smartfield, so that the cursor is not moved on first or last row;
  keyStrokeContext.registerStopPropagationInterceptor(function(event) {
    if (!event.ctrlKey && !event.altKey && scout.isOneOf(event.which, scout.keys.UP, scout.keys.DOWN)) {
      event.stopPropagation();
      event.preventDefault();
    }
  });
};

scout.Tree.prototype._syncMenus = function(menus, oldMenus) {
  this.updateKeyStrokes(menus, oldMenus);
  this.menus = menus;
  this._updateMenuBar();
};

scout.Tree.prototype._updateMenuBar = function() {
  var menuItems = this._filterMenus(this.menus, scout.MenuDestinations.MENU_BAR, false, true);
  this.menuBar.setMenuItems(menuItems);
};

scout.Tree.prototype._syncKeyStrokes = function(keyStrokes, oldKeyStrokes) {
  this.updateKeyStrokes(keyStrokes, oldKeyStrokes);
  this.keyStrokes = keyStrokes;
};

scout.Tree.prototype._syncDisplayStyle = function(newValue) {
  this.setDisplayStyle(newValue, false);
};

scout.Tree.prototype._resetTreeNode = function(node, parentNode) {
  node.rendered = false;
  node.attached = false;
  delete node.$node;
};

scout.Tree.prototype._isSelectedNode = function(node) {
  if (this.initialTraversing) {
    return this.selectedNodes.indexOf(node.id) > -1;
  } else {
    return this.selectedNodes.indexOf(node) > -1;
  }
};

scout.Tree.prototype._initTreeNode = function(node, parentNode) {
  this.nodesMap[node.id] = node;
  if (parentNode) {
    node.parentNode = parentNode;
    node.level = node.parentNode.level + 1;
  } else {
    node.level = 0;
  }
  node.rendered = false;
  node.attached = false;
  //if this node is selected all parent nodes has to be added to selectionPath
  if (this._isSelectedNode(node) && node.parentNode && !this.visibleNodesMap[node.parentNode.id]) {
    var p = node;
    while (p) {
      this._inSelectionPathList[p.id] = true;
      p.filterDirty = true;

      if (p !== node) {
        //ensure node is expanded
        node.expanded = true;
        //if parent was filtered before, try refilter after adding to selection path.
        if (p.level === 0) {
          this._applyFiltersForNode(p);

          //add visible nodes to visible nodes array when they are initialized
          this._addToVisibleFlatList(p, false);

          //process children
          this._addChildsToFlatList(p, this.visibleNodesFlat.length - 1, false, null, true);
        }
      }
      p = p.parentNode;
    }
  } else if (node.parentNode && this._isSelectedNode(node.parentNode)) {
    this._inSelectionPathList[node.id] = true;
  }
  //create function to check if node is in hierarchy of a parent. is used on removal from flat list.
  node.isChildOf = function(parentNode) {
    if (parentNode === this.parentNode) {
      return true;
    } else if (!this.parentNode) {
      return false;
    }
    return this.parentNode.isChildOf(parentNode);
  };
  if (node.checked) {
    this.checkedNodes.push(node);
  }
  scout.defaultValues.applyTo(node, 'TreeNode');
  if (node.childNodes === undefined) {
    node.childNodes = [];
  }
  var that = this;

  this._initTreeNodeInternal(node, parentNode);

  node.isFilterAccepted = function(forceFilter) {
    if (this.filterDirty || forceFilter) {
      that._applyFiltersForNode(this);
    }
    return this.filterAccepted;
  };
  this._applyFiltersForNode(node);

  //add visible nodes to visible nodes array when they are initialized
  this._addToVisibleFlatList(node, false);

  this._updateMarkChildrenChecked(node, true, node.checked);

  node.initialized = true;
};

scout.Tree.prototype._initTreeNodeInternal = function(node, parentNode) {
  //override this if you want a custom node init before filtering.
};

scout.Tree.prototype.destroy = function() {
  scout.Tree.parent.prototype.destroy.call(this);
  this._visitNodes(this.nodes, this._destroyTreeNode.bind(this));
};

scout.Tree.prototype._destroyTreeNode = function(node, parentNode) {
  delete this.nodesMap[node.id];
  scout.arrays.remove(this.selectedNodes, node); // ensure deleted node is not in selection list anymore (in case the model does not update the selection)
  this._removeFromFlatList(node, false); //ensure node is not longer in visible nodes list.

  if (this._onNodeDeleted) { // Necessary for subclasses
    this._onNodeDeleted(node);
  }
};

/**
 * if func returns true the children of the visited node are not visited.
 */
scout.Tree.prototype._visitNodes = function(nodes, func, parentNode) {
  var i, node;
  if (!nodes) {
    return;
  }

  for (i = 0; i < nodes.length; i++) {
    node = nodes[i];
    var doNotProcessChilds = func(node, parentNode);
    if (!doNotProcessChilds && node.childNodes.length > 0) {
      this._visitNodes(node.childNodes, func, node);
    }
  }
};

scout.Tree.prototype._render = function($parent) {
  this.isRendering = true;
  this.$container = $parent.appendDiv('tree');
  if (this._additionalContainerClasses) {
    this.$container.addClass(this._additionalContainerClasses);
  }

  var layout = new scout.TreeLayout(this);
  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(layout);
  this.htmlComp.pixelBasedSizing = false;

  this.$data = this.$container.appendDiv('tree-data')
    .on('contextmenu', this._onContextMenu.bind(this))
    .on('mousedown', '.tree-node', this._onNodeMouseDown.bind(this))
    .on('mouseup', '.tree-node', this._onNodeMouseUp.bind(this))
    .on('dblclick', '.tree-node', this._onNodeDoubleClick.bind(this))
    .on('mousedown', '.tree-node-control', this._onNodeControlMouseDown.bind(this))
    .on('mouseup', '.tree-node-control', this._onNodeControlMouseUp.bind(this))
    .on('dblclick', '.tree-node-control', this._onNodeControlDoubleClick.bind(this))
    .on('scroll', this._onDataScroll.bind(this));

  scout.scrollbars.install(this.$data, {
    parent: this,
    axis: 'y'
      //TODO nbu axis: 'both'
  });
  this._installNodeTooltipSupport();
  this.menuBar.render(this.$container);
  this._updateNodeDimensions();
  this._renderViewport();
  this.invalidateLayoutTree();
  this.isRendering = false;
};

scout.Tree.prototype._onDataScroll = function() {
  var scrollTop = this.$data[0].scrollTop;
  if (this.scrollTop === scrollTop) {
    return;
  }
  this._renderViewport();
  this.scrollTop = scrollTop;
};

scout.Table.prototype.setScrollTop = function(scrollTop) {
  scout.scrollbars.scrollTop(this.$data, scrollTop);
  this.scrollTop = scrollTop;

  // call _renderViewport to make sure nodes are rendered immediately. The browser fires the scroll event handled by onDataScroll delayed
  this._renderViewport();
};

scout.Tree.prototype._renderViewport = function() {
  if (this.runningAnimations > 0 || this._renderViewportBlocked) {
    //animation pending do not render view port because finishing should rerenderViewport
    return;
  }
  var viewRange = this._calculateCurrentViewRange();
  this._renderViewRange(viewRange);
};

scout.Tree.prototype._calculateCurrentViewRange = function() {
  var node,
    scrollTop = this.$data[0].scrollTop,
    maxScrollTop = this.$data[0].scrollHeight - this.$data[0].clientHeight;

  if (maxScrollTop === 0 && this.visibleNodesFlat.length > 0) {
    // no scrollbars visible
    node = this.visibleNodesFlat[0];
  } else {
    node = this._nodeAtScrollTop(scrollTop);
  }

  return this._calculateViewRangeForNode(node);
};

scout.Tree.prototype._rerenderViewport = function() {
  if (this._renderViewportBlocked) {
    return;
  }
  this._removeRenderedNodes();
  this._renderFiller();
  this._renderViewport();
};

scout.Tree.prototype._removeRenderedNodes = function() {
  var $nodes = this.$data.find('.tree-node');
  $nodes.each(function(i, elem) {
    var $node = $(elem),
      node = $node.data('node');
    if ($node.hasClass('hiding')) {
      // Do not remove nodes which are removed using an animation
      return;
    }
    this._removeNode(node);
  }.bind(this));
  this.viewRangeRendered = new scout.Range(0, 0);
};

scout.Tree.prototype._renderViewRangeForNode = function(node) {
  var viewRange = this._calculateViewRangeForNode(node);
  this._renderViewRange(viewRange);
};

scout.Tree.prototype._renderNodesInRange = function(range) {
  var prepend = false;

  var nodes = this.visibleNodesFlat;
  if (nodes.length === 0) {
    return;
  }

  var maxRange = new scout.Range(0, nodes.length);
  range = maxRange.intersect(range);
  if (!range.intersect(this.viewRangeRendered).equals(new scout.Range(0, 0))) {
    throw new Error('New range must not intersect with existing.');
  }
  if (range.to <= this.viewRangeRendered.from) {
    prepend = true;
  }
  var newRange = this.viewRangeRendered.union(range);
  if (newRange.length === 2) {
    throw new Error('Can only prepend or append rows to the existing range. Existing: ' + this.viewRangeRendered + '. New: ' + newRange);
  }
  this.viewRangeRendered = newRange[0];

  var numNodesRendered = this.ensureRangeVisible(range);

  if ($.log.isTraceEnabled()) {
    $.log.trace(numNodesRendered + ' new nodes rendered from ' + range);
  }
};

scout.Tree.prototype.ensureRangeVisible = function(range) {
  var nodes = this.visibleNodesFlat,
    numNodesRendered = 0;
  for (var r = range.from; r < range.to; r++) {
    var node = nodes[r];
    if (!node.attached) {
      this._insertNodeInDOM(node);
      numNodesRendered++;
    }
  }
  return numNodesRendered;
};

scout.Tree.prototype._renderFiller = function() {
  if (!this.$fillBefore) {
    this.$fillBefore = this.$data.prependDiv('tree-data-fill');
  }

  var fillBeforeDimensions = this._calculateFillerDimension(new scout.Range(0, this.viewRangeRendered.from));
  this.$fillBefore.cssHeight(fillBeforeDimensions.height);
  this.$fillBefore.cssWidth(fillBeforeDimensions.width);
  $.log.trace('FillBefore height: ' + fillBeforeDimensions.height);

  if (!this.$fillAfter) {
    this.$fillAfter = this.$data.appendDiv('tree-data-fill');
  }

  var fillAfterDimensions = {
    height: 0,
    width: 0
  };
  if (this.viewRangeRendered.to !== 0) {
    fillAfterDimensions = this._calculateFillerDimension(new scout.Range(this.viewRangeRendered.to, this.visibleNodesFlat.length));
  }
  this.$fillAfter.cssHeight(fillAfterDimensions.height);
  this.$fillAfter.cssWidth(fillAfterDimensions.width);
  $.log.trace('FillAfter height: ' + fillAfterDimensions.heigth);
};

scout.Tree.prototype._calculateFillerDimension = function(range) {
  var dimension = {
    height: 0,
    width: 0
  };
  for (var i = range.from; i < range.to; i++) {
    var node = this.visibleNodesFlat[i];
    dimension.height += this._heightForNode(node);
    dimension.width = Math.max(dimension.width, this._widthForNode(node));
  }
  return dimension;
};

scout.Tree.prototype._removeNodesInRange = function(range) {
  var fromNode, toNode, node, i,
    numNodesRemoved = 0,
    nodes = this.visibleNodesFlat;

  var maxRange = new scout.Range(0, nodes.length);
  range = maxRange.intersect(range);
  fromNode = nodes[range.from];
  toNode = nodes[range.to];

  var newRange = this.viewRangeRendered.subtract(range);
  if (newRange.length === 2) {
    throw new Error('Can only remove nodes at the beginning or end of the existing range. ' + this.viewRangeRendered + '. New: ' + newRange);
  }
  this.viewRangeRendered = newRange[0];

  for (i = range.from; i < range.to; i++) {
    node = nodes[i];
    this._removeNode(node);
    numNodesRemoved++;
  }

  if ($.log.isTraceEnabled()) {
    $.log.trace(numNodesRemoved + ' nodes removed from ' + range + '.');
  }
};

/**
 * Just removes the node, does NOT adjust this.viewRangeRendered
 */
scout.Tree.prototype._removeNode = function(node) {
  var $node = node.$node;
  if (!$node) {
    return;
  }
  if ($node.hasClass('hiding')) {
    // Do not remove nodes which are removed using an animation
    return;
  }
  //only remove node
  $node.detach();
  node.attached = false;
};

/**
 * Renders the rows visible in the viewport and removes the other rows
 */
scout.Tree.prototype._renderViewRange = function(viewRange) {
  if (viewRange.from === this.viewRangeRendered.from && viewRange.to === this.viewRangeRendered.to && !this.viewRangeDirty) {
    // Range already rendered -> do nothing
    return;
  }
  var scrollTop = this.$data[0].scrollTop;
  if (!this.viewRangeDirty) {
    var rangesToRender = viewRange.subtract(this.viewRangeRendered);
    var rangesToRemove = this.viewRangeRendered.subtract(viewRange);
    rangesToRemove.forEach(function(range) {
      this._removeNodesInRange(range);
    }.bind(this));
    rangesToRender.forEach(function(range) {
      this._renderNodesInRange(range);
    }.bind(this));
  } else {
    //expansion changed
    this.viewRangeRendered = viewRange;
    this.ensureRangeVisible(viewRange);
  }

  // check if at least last and first row in range got correctly rendered
  if (this.viewRangeRendered.size() > 0) {
    var nodes = this.visibleNodesFlat;
    var firstNode = nodes[this.viewRangeRendered.from];
    var lastNode = nodes[this.viewRangeRendered.to - 1];
    if (this.viewRangeDirty) {
      //cleanup nodes before range and after
      var $nodesBeforFirstNode = firstNode.$node.prevAll('.tree-node');
      var $nodesAfterLastNode = lastNode.$node.nextAll('.tree-node');
      this._cleanupNodes($nodesBeforFirstNode);
      this._cleanupNodes($nodesAfterLastNode);
    }
    if (!firstNode.attached || !lastNode.attached) {
      throw new Error('Nodes not rendered as expected. ' + this.viewRangeRendered + '. First: ' + firstNode.$node + '. Last: ' + lastNode.$node);
    }
  }

  this._renderFiller();
  this._renderSelection();
  this.$data[0].scrollTop = scrollTop;
  this.viewRangeDirty = false;
};

scout.Tree.prototype._cleanupNodes = function($nodes) {
  for (var i = 0; i < $nodes.length; i++) {
    this._removeNode($nodes.eq(i).data('node'));
  }
};

/**
 * Returns the index of the node which is at position scrollTop.
 */
scout.Tree.prototype._nodeAtScrollTop = function(scrollTop) {
  var height = 0,
    nodeTop;
  this.visibleNodesFlat.some(function(node, i) {
    height += this._heightForNode(node);
    if (scrollTop < height) {
      nodeTop = node;
      return true;
    }
  }.bind(this));
  var visibleNodesLength = this.visibleNodesFlat.length;
  if (!nodeTop && visibleNodesLength > 0) {
    nodeTop = this.visibleNodesFlat[visibleNodesLength - 1];
  }
  return nodeTop;
};

scout.Tree.prototype._heightForNode = function(node) {
  var height = 0;
  if (node.height) {
    height = node.height;
  } else {
    height = this.nodeHeight;
  }
  return height;
};

scout.Tree.prototype._widthForNode = function(node) {
  var width = 0;
  if (node.width) {
    width = node.width;
  } else {
    width = this.nodeWidth;
  }
  return width;
};

/**
 * Returns a range of size this.viewRangeSize. Start of range is rowIndex - viewRangeSize / 4.
 * -> 1/4 of the nodes are before the viewport 2/4 in the viewport 1/4 after the viewport,
 * assuming viewRangeSize is 2*number of possible nodes in the viewport (see calculateViewRangeSize).
 */
scout.Tree.prototype._calculateViewRangeForNode = function(node) {
  var viewRange = new scout.Range(),
    quarterRange = Math.floor(this.viewRangeSize / 4),
    diff;

  var nodeIndex = this.visibleNodesFlat.indexOf(node);
  if (!node || nodeIndex === -1) {
    return viewRange;
  }

  viewRange.from = Math.max(nodeIndex - quarterRange, 0);
  viewRange.to = Math.min(viewRange.from + this.viewRangeSize, this.visibleNodesFlat.length);

  // Try to use the whole viewRangeSize (extend from if necessary)
  diff = this.viewRangeSize - viewRange.size();
  if (diff > 0) {
    viewRange.from = Math.max(viewRange.to - this.viewRangeSize, 0);
  }
  return viewRange;
};

/**
 * Calculates the optimal view range size (number of nodes to be rendered).
 * It uses the default node height to estimate how many nodes fit in the view port.
 * The view range size is this value * 2.
 */
scout.Tree.prototype.calculateViewRangeSize = function() {
  // Make sure row height is up to date (row height may be different after zooming)
  this._updateNodeDimensions();

  if (this.nodeHeight === 0) {
    throw new Error('Cannot calculate view range with nodeHeight = 0');
  }
  return Math.ceil(this.$data.outerHeight() / this.nodeHeight) * 2;
};

scout.Tree.prototype.setViewRangeSize = function(viewRangeSize) {
  if (this.viewRangeSize === viewRangeSize) {
    return;
  }
  this.viewRangeSize = viewRangeSize;
  if (this.rendered) {
    this._renderViewport();
  }
};

scout.Tree.prototype._updateNodeDimensions = function() {
  var $emptyNode = this.$data.appendDiv('tree-node');
  $emptyNode.html('&nbsp;');
  this.nodeHeight = $emptyNode.outerHeight(true);
  this.nodeWidth = $emptyNode.outerWidth(true);
  $emptyNode.remove();
};

scout.Tree.prototype._postRender = function() {
  this._renderSelection();
};

scout.Tree.prototype._remove = function() {
  // Detach nodes from jQuery objects (because those will be removed)
  this._visitNodes(this.nodes, this._resetTreeNode.bind(this));

  scout.scrollbars.uninstall(this.$data, this.session);
  this._uninstallNodeTooltipSupport();
  this.$fillBefore = null;
  this.$fillAfter = null;
  //reset rendered view range because now range is rendered
  this.viewRangeRendered = new scout.Range(0, 0);
  scout.Tree.parent.prototype._remove.call(this);
};

scout.Tree.prototype._renderProperties = function() {
  scout.Tree.parent.prototype._renderProperties.call(this);
  this._renderEnabled();
  this._renderMenus();
  this._renderDisplayStyle();
  this._renderDropType();
};

/**
 * @param parentNode optional. If provided, this node's state will be updated (e.g. it will be collapsed)
 */
scout.Tree.prototype._removeNodes = function(nodes, parentNode) {
  if (nodes.length === 0) {
    return;
  }

  nodes.forEach(function(node) {
    this._removeFromFlatList(node, true);
    if (node.childNodes.length > 0) {
      this._removeNodes(node.childNodes, node);
    }
    if (node.$node) {
      node.$node.remove();
      node.rendered = false;
      node.attached = false;
      delete node.$node;
    }
  }, this);

  //If every child node was deleted mark node as collapsed (independent of the model state)
  //--> makes it consistent with addNodes and expand (expansion is not allowed if there are no child nodes)
  var $parentNode = (parentNode ? parentNode.$node : undefined);
  if ($parentNode) {
    var childNodesOfParent = parentNode.childNodes;
    if (!childNodesOfParent || childNodesOfParent.length === 0) {
      $parentNode.removeClass('expanded');
      $parentNode.removeClass('lazy');
    }
  }

  this.invalidateLayoutTree();
};

scout.Tree.prototype._$buildNode = function(node) {
  var $node = this.$container.makeDiv('tree-node')
    .data('node', node)
    .attr('data-nodeid', node.id)
    .attr('data-level', node.level)
    .css('padding-left', this._computeTreeItemPaddingLeft(node.level));
  node.$node = $node;
  $node.appendSpan('text');

  this._renderTreeItemControl($node);

  if (this.checkable) {
    this._renderTreeItemCheckbox(node);
  }

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
  if ($node.hasClass('ancestor-of-selected')) {
    formerClasses += ' ancestor-of-selected';
  }
  if ($node.hasClass('parent-of-selected')) {
    formerClasses += ' parent-of-selected';
  }
  $node.removeClass();
  $node.addClass(formerClasses);
  $node.addClass(node.cssClass);
  $node.toggleClass('leaf', !!node.leaf);
  $node.toggleClass('expanded', (!!node.expanded && node.childNodes.length > 0));
  $node.toggleClass('lazy', $node.hasClass('expanded') && node.expandedLazy);
  $node.toggleClass('group', !!this.groupedNodes[node.id]);
  $node.setEnabled(!!node.enabled);
  $node.children('.tree-node-checkbox')
    .children('.check-box')
    .toggleClass('disabled', !(this.enabled && node.enabled));

  if (!node.parentNode && this.selectedNodes.length === 0) {
    // Root nodes have class child-of-selected if no node is selected
    $node.addClass('child-of-selected');
  } else if (node.parentNode && this.selectedNodes.indexOf(node.parentNode) > -1) {
    $node.addClass('child-of-selected');
  }

  this._renderNodeText(node);

  scout.styles.legacyStyle(node, $node);

  // TODO [5.2] bsh: More attributes...
  // iconId

  // If parent node is marked as 'lazy', check if any visible child nodes remain.
  if (node.parentNode && node.parentNode.expandedLazy) {
    var hasVisibleNodes = node.parentNode.childNodes.some(function(childNode) {
      if (this.visibleNodesMap[childNode.id]) {
        return true;
      }
    }.bind(this));
    if (!hasVisibleNodes && node.parentNode.$node) {
      // Remove 'lazy' from parent
      node.parentNode.$node.removeClass('lazy');
    }
  }
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
  var $checkboxDiv = $controlItem
    .appendDiv('check-box')
    .toggleClass('checked', node.checked)
    .toggleClass('disabled', !(this.enabled && node.enabled));

  if (node.childrenChecked) {
    $checkboxDiv.toggleClass('children-checked', true);
  } else {
    $checkboxDiv.toggleClass('children-checked', false);
  }
};

scout.Tree.prototype._renderNodeText = function(node) {
  var $node = node.$node,
    $text = $node.children('.text');
  if (node.htmlEnabled) {
    $text.html(node.text);
  } else {
    $text.textOrNbsp(node.text);
  }
};

scout.Tree.prototype._renderNodeChecked = function(node) {
  if (!node.$node) {
    // if node is not rendered, do nothing
    return;
  }

  node.$node
    .children('.tree-node-checkbox')
    .children('.check-box')
    .toggleClass('checked', node.checked);
};

scout.Tree.prototype._renderMenus = function() {
  // NOP
};

scout.Tree.prototype._removeMenus = function() {
  // menubar takes care about removal
};

scout.Tree.prototype._filterMenus = function(menus, destination, onlyVisible, enableDisableKeyStroke) {
  return scout.menus.filterAccordingToSelection('Tree', this.selectedNodes.length, menus, destination, onlyVisible, enableDisableKeyStroke);
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

      $node.children('.tree-node-checkbox')
        .children('.check-box')
        .toggleClass('disabled', !(enabled && node.enabled));
    });
  }
};

scout.Tree.prototype._renderTitle = function() {
  // NOP
};

scout.Tree.prototype._renderAutoCheckChildren = function() {
  // NOP
};

scout.Tree.prototype._renderCheckable = function() {
  // Define helper functions
  var isNodeRendered = function(node) {
    return !!node.$node;
  };
  var updateCheckableStateRec = function(node) {
    var $node = node.$node;
    var $control = $node.children('.tree-node-control');
    var $checkbox = $node.children('.tree-node-checkbox');

    if (this.checkable) {
      $control.addClass('checkable');
      if ($checkbox.length === 0) {
        this._renderTreeItemCheckbox(node);
      }
    } else {
      $control.removeClass('checkable');
      $checkbox.remove();
    }

    $node.css('padding-left', this._computeTreeItemPaddingLeft(parseFloat($node.attr('data-level'))));

    // Recursion
    if (node.childNodes) {
      node.childNodes.filter(isNodeRendered).forEach(updateCheckableStateRec);
    }
  }.bind(this);

  // Start recursion
  this.nodes.filter(isNodeRendered).forEach(updateCheckableStateRec);
};

scout.Tree.prototype._renderMultiCheck = function() {
  // NOP
};

scout.Tree.prototype._renderDisplayStyle = function() {
  this.$container.toggleClass('breadcrumb', this.isBreadcrumbStyleActive());

  // update scrollbar if mode has changed (from tree to bc or vice versa)
  this.invalidateLayoutTree();
};

scout.Tree.prototype._renderExpansion = function(node, options) {
  var opts = {
    expandLazyChanged: false,
    expansionChanged: false
  };
  $.extend(opts, options);

  var $node = node.$node,
    expanded = node.expanded;

  // Only render if node is rendered to make it possible to expand/collapse currently hidden nodes (used by collapseAll).
  if (!$node || $node.length === 0) {
    return;
  }

  // Only expand / collapse if there are child nodes
  if (node.childNodes.length === 0) {
    return true;
  }

  $node.toggleClass('lazy', expanded && node.expandedLazy);
  if (!opts.expansionChanged && !opts.expandLazyChanged) {
    // Expansion state has not changed -> return
    return;
  }

  if (expanded) {
    $node.addClass('expanded');
  } else {
    $node.removeClass('expanded');
  }
};

scout.Tree.prototype._renderSelection = function() {
  // Add children class to root nodes if no nodes are selected
  if (this.selectedNodes.length === 0) {
    this.nodes.forEach(function(childNode) {
      if (childNode.rendered) {
        childNode.$node.addClass('child-of-selected');
      }
    }, this);
  }

  this.selectedNodes.forEach(function(node) {
    var $node = node.$node;

    if (this.visibleNodesMap[node.id]) {
      $node = node.$node;
      if (!node.rendered) {
        return;
      }
    } else {
      return;
    }

    $node.select(true);

    // Mark all ancestor nodes, especially necessary for bread crumb mode
    var parentNode = node.parentNode;
    if (parentNode && parentNode.rendered) {
      parentNode.$node.addClass('parent-of-selected');
    }
    while (parentNode) {
      if (parentNode.rendered) {
        parentNode.$node.addClass('ancestor-of-selected');
      }
      parentNode = parentNode.parentNode;
    }

    // Mark all child nodes
    if (node.expanded) {
      node.childNodes.forEach(function(childNode) {
        if (childNode.rendered) {
          childNode.$node.addClass('child-of-selected');
        }
      }, this);
    }
  }, this);

  if (this.scrollToSelection) {
    // Execute delayed because tree may be not layouted yet
    setTimeout(this.revealSelection.bind(this));
  }
  this._redecorateViewRange();
};

scout.Tree.prototype._redecorateViewRange = function() {
  if (this.rendered) {
    for (var i = this.viewRangeRendered.from; i < this.viewRangeRendered.to; i++) {
      if (i >= this.visibleNodesFlat.length) {
        break;
      }
      this._decorateNode(this.visibleNodesFlat[i]);
    }
  }
};

scout.Tree.prototype._removeSelection = function() {
  // Remove children class on root nodes if no nodes were selected
  if (this.selectedNodes.length === 0) {
    this.nodes.forEach(function(childNode) {
      if (childNode.rendered) {
        childNode.$node.removeClass('child-of-selected');
      }
    }, this);
  }

  this.selectedNodes.forEach(function(node) {
    var $node = node.$node;
    if ($node) { // TODO [5.2] bsh: Check if $node can be undefined
      $node.select(false);

      // remove ancestor and child classes
      var parentNode = node.parentNode;
      if (parentNode) {
        parentNode.$node.removeClass('parent-of-selected');
      }
      while (parentNode) {
        parentNode.$node.removeClass('ancestor-of-selected');
        parentNode = parentNode.parentNode;
      }
      if (node.expanded) {
        node.childNodes.forEach(function(childNode) {
          if (childNode.rendered) {
            childNode.$node.removeClass('child-of-selected');
          }
        }, this);
      }
    }
  }, this);
};

scout.Tree.prototype._renderDropType = function() {
  if (this.dropType) {
    this._installDragAndDropHandler();
  } else {
    this._uninstallDragAndDropHandler();
  }
};

scout.Tree.prototype._installDragAndDropHandler = function(event) {
  if (this.dragAndDropHandler) {
    return;
  }
  this.dragAndDropHandler = scout.dragAndDrop.handler(this, {
    supportedScoutTypes: scout.dragAndDrop.SCOUT_TYPES.FILE_TRANSFER,
    dropType: function() {
      return this.dropType;
    }.bind(this),
    dropMaximumSize: function() {
      return this.dropMaximumSize;
    }.bind(this),
    additionalDropProperties: function(event) {
      var $target = $(event.currentTarget);
      var properties = {
        nodeId: ''
      };
      if ($target.hasClass('tree-node')) {
        var node = $target.data('node');
        properties.nodeId = node.id;
      }
      return properties;
    }.bind(this)
  });
  this.dragAndDropHandler.install(this.$container, '.tree-data,.tree-node');
};

scout.Tree.prototype._uninstallDragAndDropHandler = function(event) {
  if (!this.dragAndDropHandler) {
    return;
  }
  this.dragAndDropHandler.uninstall();
  this.dragAndDropHandler = null;
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
          node.$node
            .children('.tree-node-checkbox')
            .children('.check-box')
            .toggleClass('children-checked', true);
        }
        break;
      }
    }
    if (!childrenFound) {
      node.childrenChecked = false;
      if (this.rendered && node.$node) {
        node.$node.children('.tree-node-checkbox')
          .children('.check-box')
          .toggleClass('children-checked', false);
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
        node.parentNode.$node.children('.tree-node-checkbox')
          .children('.check-box')
          .toggleClass('children-checked', true);
      } else {
        node.parentNode.$node.children('.tree-node-checkbox')
          .children('.check-box')
          .toggleClass('children-checked', false);
      }
    }
  }
};

scout.Tree.prototype._installNodeTooltipSupport = function() {
  scout.tooltips.install(this.$data, {
    parent: this,
    selector: '.tree-node',
    text: this._nodeTooltipText.bind(this),
    arrowPosition: 50,
    arrowPositionUnit: '%',
    nativeTooltip: !scout.device.isCustomEllipsisTooltipPossible()
  });
};

scout.Tree.prototype._uninstallNodeTooltipSupport = function() {
  scout.tooltips.uninstall(this.$data);
};

scout.Tree.prototype._nodeTooltipText = function($node) {
  var node = $node.data('node');

  if (node.tooltipText) {
    return node.tooltipText;
  } else if (this._isTruncatedNodeTooltipEnabled() && $node.isContentTruncated()) {
    return $node.children('.text').text();
  }
};

scout.Tree.prototype._isTruncatedNodeTooltipEnabled = function() {
  return true;
};

scout.Tree.prototype.setDisplayStyle = function(displayStyle, notifyServer) {
  if (this.displayStyle === displayStyle) {
    return;
  }
  this._renderViewportBlocked = true;
  this._setProperty('displayStyle', displayStyle);
  notifyServer = scout.nvl(notifyServer, true);
  if (notifyServer) {
    this._sendProperty('displayStyle');
  }

  if (displayStyle && this.selectedNodes.length > 0) {
    var selectedNode = this.selectedNodes[0];
    if (!selectedNode.expanded) {
      this.expandNode(selectedNode);
    }
  }

  if (this.displayStyle === scout.Tree.DisplayStyle.BREADCRUMB) {
    this.addFilter(this.breadcrumbFilter, true);
    this.filterVisibleNodes();
  } else if (this.displayStyle !== scout.Tree.DisplayStyle.BREADCRUMB) {
    this.removeFilter(this.breadcrumbFilter);
    this.filter();
  }

  if (this.rendered) {
    this._renderDisplayStyle();
  }
  this._renderViewportBlocked = false;
};

scout.Tree.prototype.setBreadcrumbStyleActive = function(active, notifyServer) {
  if (active) {
    this.setDisplayStyle(scout.Tree.DisplayStyle.BREADCRUMB, notifyServer);
  } else if (!active) {
    this.setDisplayStyle(scout.Tree.DisplayStyle.DEFAULT, notifyServer);
  }
};

scout.Tree.prototype.isNodeInBreadcrumbVisible = function(node) {
  return this._inSelectionPathList[node.id] === undefined ? false : this._inSelectionPathList[node.id];
};

scout.Tree.prototype.isBreadcrumbStyleActive = function() {
  return this.displayStyle === scout.Tree.DisplayStyle.BREADCRUMB;
};

scout.Tree.prototype.setBreadcrumbTogglingThreshold = function(width) {
  this.breadcrumbTogglingThreshold = width;
};

scout.Tree.prototype.expandNode = function(node, opts) {
  this.setNodeExpanded(node, true, opts);
};

scout.Tree.prototype.collapseNode = function(node, opts) {
  this.setNodeExpanded(node, false, opts);
};

scout.Tree.prototype.collapseAll = function() {
  this.rebuildSuppressed = true;
  // Collapse all expanded child nodes (only model)
  this._visitNodes(this.nodes, function(node) {
    this.collapseNode(node);
  }.bind(this));
  //ensure correct rendering
  this._rerenderViewport();
  this.rebuildSuppressed = false;
};

scout.Tree.prototype.setNodeExpanded = function(node, expanded, opts) {
  opts = opts || {};
  var lazy = scout.nvl(opts.lazy, node.lazyExpandingEnabled);
  var notifyServer = scout.nvl(opts.notifyServer, true);

  // Never do lazy expansion if it is disabled on the tree
  if (!this.lazyExpandingEnabled) {
    lazy = false;
  }

  if (this.isBreadcrumbStyleActive()) {
    // Do not allow to collapse a selected node
    if (!expanded && this.selectedNodes.indexOf(node) > -1) {
      this.setNodeExpanded(node, true, opts);
      return;
    }
  }

  // Optionally collapse all children (recursively)
  if (opts.collapseChildNodes) {
    // Suppress render expansion
    var childOpts = scout.objects.valueCopy(opts);
    childOpts.renderExpansion = false;

    node.childNodes.forEach(function(childNode) {
      if (childNode.expanded) {
        this.collapseNode(childNode, childOpts);
      }
    }.bind(this));
  }
  var renderExpansionOpts = {
    expansionChanged: false,
    expandLazyChanged: false
  };

  // Set expansion state
  if (node.expanded !== expanded || node.expandedLazy !== lazy) {
    renderExpansionOpts.expansionChanged = node.expanded !== expanded;
    renderExpansionOpts.expandLazyChanged = node.expandedLazy !== lazy;
    node.expanded = expanded;
    node.expandedLazy = lazy;
    if (this.groupedNodes[node.id]) {
      this._updateItemPath(false, node);
    }
    var filterStateChanged = this._applyFiltersForNode(node);
    if (filterStateChanged && renderExpansionOpts.expansionChanged) {
      this._rebuildParent(node.parentNode, opts);
    } else if (renderExpansionOpts.expandLazyChanged) {
      node.childNodes.forEach(function(child) {
        this._applyFiltersForNode(child);
      }.bind(this));
    }

    if (node.expanded) {
      this._addChildsToFlatList(node, null, true, null, true);
    } else {
      this._removeChildsFromFlatList(node, true);
    }
    if (notifyServer) {
      this._send('nodeExpanded', {
        nodeId: node.id,
        expanded: expanded,
        expandedLazy: lazy
      });
    }
    this.viewRangeDirty = true;
  }

  // Render expansion
  if (this.rendered && scout.nvl(opts.renderExpansion, true)) {
    this._renderExpansion(node, renderExpansionOpts);
  }
};

scout.Tree.prototype._rebuildParent = function(node, opts) {
  if (this.rebuildSuppressed) {
    return;
  }
  if (node.expanded || node.expandedLazy) {
    this._addChildsToFlatList(node, null, false, null, true);
  } else {
    this._removeChildsFromFlatList(node, false);
  }
  //Render expansion
  if (this.rendered && scout.nvl(opts.renderExpansion, true)) {
    var renderExpansionOpts = {
      expansionChanged: true
    };
    this._renderExpansion(node, renderExpansionOpts);
  }
};

scout.Tree.prototype._removeChildsFromFlatList = function(parentNode, animatedRemove) {
  //Only if a parent is available the childs are available.
  if (this.visibleNodesMap[parentNode.id]) {
    var parentIndex = this.visibleNodesFlat.indexOf(parentNode);
    var elementsToDelete = 0;
    var parentLevel = parentNode.level;
    var removedNodes = [];
    animatedRemove = animatedRemove && this.rendered;
    if (this._$animationWrapper) {
      // Note: Do _not_ use finish() here! Although documentation states that it is "similar" to stop(true, true),
      // this does not seem to be the case. Implementations differ slightly in details. The effect is, that when
      // calling stop() the animation stops and the 'complete' callback is executed immediately. However, when calling
      // finish(), the callback is _not_ executed! (This may or may not be a bug in jQuery, I cannot tell...)
      this._$animationWrapper.stop(false, true);
    }
    this._$expandAnimationWrappers.forEach(function($wrapper) {
      $wrapper.stop(false, true);
    });
    for (var i = parentIndex + 1; i < this.visibleNodesFlat.length; i++) {
      if (this.visibleNodesFlat[i].level > parentLevel) {
        var node = this.visibleNodesFlat[i];
        delete this.visibleNodesMap[this.visibleNodesFlat[i].id];
        if (node.attached && animatedRemove) {
          if (!this._$animationWrapper) {
            this._$animationWrapper = $('<div class="animation-wrapper">').insertBefore(node.$node);
            this._$animationWrapper.data('parentNode', parentNode);
          }
          if (node.isChildOf(this._$animationWrapper.data('parentNode'))) {
            this._$animationWrapper.append(node.$node);
          }
          node.attached = false;
          node.displayBackup = node.$node.css('display');
          removedNodes.push(node);
        } else if (node.attached && !animatedRemove) {
          this.hideNode(node, false, false);
        }
        elementsToDelete++;
      } else {
        break;
      }
    }

    this.visibleNodesFlat.splice(parentIndex + 1, elementsToDelete);
    // animate closing
    if (animatedRemove) { // don't animate while rendering (not necessary, or may even lead to timing issues)
      this._renderViewportBlocked = true;
      if (removedNodes.length > 0) {
        this._$animationWrapper
          .animateAVSCSD('height', 0, this.startAnimationFunc, onAnimationComplete.bind(this, removedNodes), function() {}, 200);
      } else if (this._$animationWrapper) {
        this._$animationWrapper.remove();
        this._$animationWrapper = null;
        onAnimationComplete.call(this, removedNodes);
      } else {
        this._renderViewportBlocked = false;
      }
    }
    return removedNodes;
  }

  //----- Helper functions -----
  function onAnimationComplete(affectedNodes) {
    affectedNodes.forEach(function(node) {
      node.$node.detach();
      node.$node.css('display', node.displayBackup);
      node.displayBackup = null;
    });
    if (this._$animationWrapper) {
      this._$animationWrapper.remove();
      this._$animationWrapper = null;
    }
    this.runningAnimationsFinishFunc();
  }

};

scout.Tree.prototype._removeFromFlatList = function(node, animatedRemove) {
  var removedNodes = [];
  if (this.visibleNodesMap[node.id]) {
    var index = this.visibleNodesFlat.indexOf(node);
    this._removeChildsFromFlatList(node, false);
    removedNodes = scout.arrays.ensure(this.visibleNodesFlat.splice(index, 1));
    delete this.visibleNodesMap[node.id];
    this.hideNode(node, animatedRemove);
  }
  removedNodes.push(node);
  return removedNodes;
};

scout.Tree.prototype._addToVisibleFlatList = function(node, renderingAnimated) {
  //if node already in visible list don't do anything. if no parentNode is available this node is on toplevel, if a parent is available
  // it has to be in visible list and also be expanded
  if (!this.visibleNodesMap[node.id] && node.isFilterAccepted() && (!node.parentNode ||
      (node.parentNode.expanded && this.visibleNodesMap[node.parentNode.id]))) {
    if (this.initialTraversing) {
      //for faster index calculation
      this._addToVisibleFlatListNoCheck(node, this.visibleNodesFlat.length, renderingAnimated);
    } else {
      var insertIndex = this._findIndexToInsertNode(node);
      this._addToVisibleFlatListNoCheck(node, insertIndex, renderingAnimated);
    }
  }
};

scout.Tree.prototype._findIndexToInsertNode = function(node) {
  var findValidSiblingBefore = function(childNodeIndex, siblings) {
    for (var i = childNodeIndex - 1; i >= 0; i--) {
      if (siblings[i].isFilterAccepted()) {
        return siblings[i];
      }
    }
    //no sibling before
    return null;
  }.bind(this);
  //function to traverse last child nodes to first child nodes of a parent.
  var findLastVisibleNodeInParent = function(parent) {
    if (parent.expanded) {
      for (var i = parent.childNodes.length - 1; i >= 0; i--) {
        if (parent.childNodes[i].isFilterAccepted()) {
          if (parent.childNodes[i].expanded) {
            return findLastVisibleNodeInParent(parent.childNodes[i]);
          } else if (this.visibleNodesMap[parent.childNodes[i].id]) {
            return parent.childNodes[i];
          }
        }
      }
    }
    return parent;
  }.bind(this);

  var parentNode = node.parentNode,
    siblingBefore, nodeBefore;
  if (!parentNode) {
    //use toplevel to find index
    siblingBefore = findValidSiblingBefore(node.childNodeIndex, this.nodes);
    if (!siblingBefore) {
      return 0;
    }
    nodeBefore = findLastVisibleNodeInParent(siblingBefore);
    return this.visibleNodesFlat.indexOf(nodeBefore) + 1;
  } else {
    siblingBefore = findValidSiblingBefore(node.childNodeIndex, node.parentNode.childNodes);
    if (!siblingBefore) {
      return this.visibleNodesFlat.indexOf(parentNode) + 1;
    }
    nodeBefore = findLastVisibleNodeInParent(siblingBefore);
    return this.visibleNodesFlat.indexOf(nodeBefore) + 1;
  }
};

scout.Tree.prototype._addChildsToFlatList = function(parentNode, parentIndex, animatedRendering, insertBatch, forceFilter) {
  //add nodes recursively
  if (!this.visibleNodesMap[parentNode.id]) {
    return 0;
  }
  var isSubAdding = !!parentIndex;
  parentIndex = parentIndex ? parentIndex : this.visibleNodesFlat.indexOf(parentNode);
  animatedRendering = animatedRendering && this.rendered; // don't animate while rendering (not necessary, or may even lead to timing issues)
  if (this._$animationWrapper && !isSubAdding) {
    // Note: Do _not_ use finish() here! Although documentation states that it is "similar" to stop(true, true),
    // this does not seem to be the case. Implementations differ slightly in details. The effect is, that when
    // calling stop() the animation stops and the 'complete' callback is executed immediately. However, when calling
    // finish(), the callback is _not_ executed! (This may or may not be a bug in jQuery, I cannot tell...)
    this._$animationWrapper.stop(false, true);
  }
  insertBatch = insertBatch ? insertBatch : this.setUpInsertBatch(parentIndex + 1);
  parentNode.childNodes.forEach(function(node, index) {
    var isAlreadyAdded = this.visibleNodesMap[node.id];
    if (node.initialized && node.isFilterAccepted(forceFilter) && !isAlreadyAdded) {
      insertBatch.insertNodes.push(node);
      this.visibleNodesMap[node.id] = true;
      insertBatch = this.checkAndHandleBatch(insertBatch, parentNode, animatedRendering);
      if (node.expanded) {
        insertBatch = this._addChildsToFlatList(node, insertBatch.lastBatchInsertIndex(), animatedRendering, insertBatch, forceFilter);
      }
    } else if (node.initialized && node.isFilterAccepted(forceFilter) && isAlreadyAdded) {
      this.insertBatchInVisibleNodes(insertBatch, this.viewRangeRendered.from + this.viewRangeSize >= insertBatch.lastBatchInsertIndex() && this.viewRangeRendered.from <= insertBatch.lastBatchInsertIndex(), animatedRendering);
      this.checkAndHandleBatchAnimationWrapper(parentNode, animatedRendering, insertBatch);
      insertBatch = this.setUpInsertBatch(insertBatch.lastBatchInsertIndex()+1);
      if (node.expanded) {
        insertBatch = this._addChildsToFlatList(node, insertBatch.lastBatchInsertIndex(), animatedRendering, insertBatch, forceFilter);
      }
      //do not animate following
      animatedRendering = false;
    }
  }.bind(this));

  if (!isSubAdding) {
    // animation is not done yet and all added nodes are in visible range
    this.insertBatchInVisibleNodes(insertBatch, this.viewRangeRendered.from + this.viewRangeSize >= insertBatch.lastBatchInsertIndex() && this.viewRangeRendered.from <= insertBatch.lastBatchInsertIndex(), animatedRendering);
    this.invalidateLayoutTree();
  }

  return insertBatch;

};

scout.Tree.prototype.setUpInsertBatch = function(insertIndex) {
  return {
    insertNodes: [insertIndex, 0],
    $animationWrapper: null,
    lastBatchInsertIndex: function() {
      if(this.insertNodes.length === 2){
        return this.insertNodes[0];
      }
      return this.insertNodes[0] + this.insertNodes.length - 3;
    }
  };
};

scout.Tree.prototype.checkAndHandleBatchAnimationWrapper = function(parentNode, animatedRendering, insertBatch) {
  if (animatedRendering && this.viewRangeRendered.from <= insertBatch.lastBatchInsertIndex() && this.viewRangeRendered.to >= insertBatch.lastBatchInsertIndex() && !insertBatch.$animationWrapper) {
    //we are in visible area so we need a animation wrapper
    //if parent is in visible area insert after parent else insert before first node.
    var nodeBefore = this.viewRangeRendered.from === insertBatch.lastBatchInsertIndex() ? null : this.visibleNodesFlat[insertBatch.lastBatchInsertIndex() - 1];
    if (nodeBefore && nodeBefore.attached) {
      insertBatch.$animationWrapper = $('<div class="animation-wrapper">').insertAfter(nodeBefore.$node);
    } else if (parentNode.attached) {
      insertBatch.$animationWrapper = $('<div class="animation-wrapper">').insertAfter(parentNode.$node);
    } else if (this.$fillBefore) {
      insertBatch.$animationWrapper = $('<div class="animation-wrapper">').insertAfter(this.$fillBefore);
    } else {
      var nodeAfter = this.visibleNodesFlat[insertBatch.lastBatchInsertIndex()];
      insertBatch.$animationWrapper = $('<div class="animation-wrapper">').insertBefore(nodeAfter.$node);
    }
    insertBatch.animationCompleteFunc = onAnimationComplete;
    this._$expandAnimationWrappers.push(insertBatch.$animationWrapper);
  }
  //----- Helper functions ----- //

  function onAnimationComplete() {
    insertBatch.$animationWrapper.replaceWith(insertBatch.$animationWrapper.contents());
    scout.arrays.remove(this._$expandAnimationWrappers, insertBatch.$animationWrapper);
    insertBatch.$animationWrapper = null;
    this.runningAnimationsFinishFunc();
  }
};

scout.Tree.prototype.checkAndHandleBatch = function(insertBatch, parentNode, animatedRendering) {
  if (this.viewRangeRendered.from - 1 === insertBatch.lastBatchInsertIndex()) {
    //do immediate rendering because list could be longer
    this.insertBatchInVisibleNodes(insertBatch, false, false);
    insertBatch = this.setUpInsertBatch(insertBatch.lastBatchInsertIndex() + 1);
  }
  this.checkAndHandleBatchAnimationWrapper(parentNode, animatedRendering, insertBatch);

  if (this.viewRangeRendered.from + this.viewRangeSize - 1 === insertBatch.lastBatchInsertIndex()) {
    //do immediate rendering because list could be longer
    this.insertBatchInVisibleNodes(insertBatch, true, animatedRendering);
    insertBatch = this.setUpInsertBatch(insertBatch.lastBatchInsertIndex() + 1);
  }
  return insertBatch;
};

scout.Tree.prototype.insertBatchInVisibleNodes = function(insertBatch, showNodes, animate) {
  if (insertBatch.insertNodes < 3) {
    //nothing to add
    return;
  }
  this.visibleNodesFlat.splice.apply(this.visibleNodesFlat, insertBatch.insertNodes);
  if (showNodes) {
    var indexHint = insertBatch.insertNodes[0];
    for (var i = 2; i < insertBatch.insertNodes.length; i++) {
      var node = insertBatch.insertNodes[i];
      this.showNode(node, false, indexHint);
      if (insertBatch.$animationWrapper) {
        insertBatch.$animationWrapper.append(node.$node);
      }
      indexHint++;
    }
    if (insertBatch.$animationWrapper) {
      var h = insertBatch.$animationWrapper.outerHeight();
      insertBatch.$animationWrapper
        .css('height', 0)
        .animateAVSCSD('height', h, this.startAnimationFunc, insertBatch.animationCompleteFunc.bind(this), function() {}, 200);
    }
  }
};

scout.Tree.prototype._addToVisibleFlatListNoCheck = function(node, insertIndex, animatedRendering) {
  scout.arrays.insert(this.visibleNodesFlat, node, insertIndex);
  this.visibleNodesMap[node.id] = true;
  if (this.rendered) {
    this.showNode(node, animatedRendering);
  }
};

scout.Tree.prototype.scrollTo = function(node) {
  if (this.viewRangeRendered.size() === 0) {
    // Cannot scroll to a node if no node is rendered
    return;
  }
  if (!node.attached) {
    this._renderViewRangeForNode(node);
  }
  scout.scrollbars.scrollTo(this.$data, node.$node);
};

scout.Tree.prototype.revealSelection = function() {
  if (this.selectedNodes.length > 0) {
    this.scrollTo(this.selectedNodes[0]);
  }
};

scout.Tree.prototype.deselectAll = function() {
  this.selectNodes([]);
};

scout.Tree.prototype.selectNode = function(node, notifyServer, debounceSend) {
  this.selectNodes(node);
};

scout.Tree.prototype.selectNodes = function(nodes, notifyServer, debounceSend) {
  nodes = scout.arrays.ensure(nodes);
  notifyServer = scout.nvl(notifyServer, true);

  if (scout.arrays.equalsIgnoreOrder(nodes, this.selectedNodes)) {
    return;
  }

  if (this.rendered) {
    this._removeSelection();
  }

  // Make a copy so that original array stays untouched
  this.selectedNodes = nodes.slice();
  if (notifyServer) {
    var eventData = {
      nodeIds: this._nodesToIds(nodes)
    };

    // send delayed to avoid a lot of requests while selecting
    // coalesce: only send the latest selection changed event for a field
    this._send('nodesSelected', eventData, debounceSend ? 250 : 0, function(previous) {
      return this.id === previous.id && this.type === previous.type;
    });
  }

  this._triggerNodesSelected(debounceSend);
  if (this.selectedNodes.length > 0 && !this.visibleNodesMap[this.selectedNodes[0].id]) {
    this._expandAllParentNodes(this.selectedNodes[0]);
  }

  this._updateItemPath(true);
  if (this.isBreadcrumbStyleActive()) {
    // In breadcrumb mode selected node has to expanded
    if (this.selectedNodes.length > 0 && !this.selectedNodes[0].expanded) {
      this.expandNode(this.selectedNodes[0]);
      this.selectedNodes[0].filterDirty = true;
    }
    this.filter(true);
  }
  this._updateMenuBar();
  if (this.rendered) {
    this._renderSelection();
  }
};

scout.Tree.prototype.deselectNode = function(node) {
  this.deselectNodes(node);
};

scout.Tree.prototype.deselectNodes = function(nodes) {
  nodes = scout.arrays.ensure(nodes);
  var selectedNodes = this.selectedNodes.slice(); // copy
  if (scout.arrays.removeAll(selectedNodes, nodes)) {
    this.selectNodes(selectedNodes);
  }
};

scout.Tree.prototype.isNodeSelected = function(node) {
  return this.selectedNodes.indexOf(node) > -1;
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
  var nodesToInsert = [];
  while (currNode.parentNode) {
    parentNodes.push(currNode.parentNode);
    if (!this.visibleNodesMap[currNode.id] && !currNode.isFilterAccepted() && this._applyFiltersForNode(currNode)) {
      nodesToInsert.push(currNode);
    }
    currNode = currNode.parentNode;
  }

  for (i = parentNodes.length - 1; i >= 0; i--) {
    if (nodesToInsert.indexOf(parentNodes[i]) !== -1) {
      this._addToVisibleFlatList(parentNodes[i], false);
    }
    if (!parentNodes[i].expanded) {
      $parentNode = parentNodes[i].$node;
      if (!$parentNode) {
        throw new Error('Illegal state, $parentNode should be displayed. Rendered: ' + this.rendered + ', parentNode: ' + parentNodes[i]);
      }
      this.expandNode(parentNodes[i], {
        renderExpansion: false
      });
    }
  }
  if (nodesToInsert.length > 0) {
    this._rerenderViewport();
    this.invalidateLayoutTree();
  }
};

scout.Tree.prototype._updateChildNodeIndex = function(nodes, startIndex) {
  for (var i = scout.nvl(startIndex, 0); i < nodes.length; i++) {
    nodes[i].childNodeIndex = i;
  }
};

scout.Tree.prototype.insertNodes = function(nodes, parentNode) {
  // Append continuous node blocks
  nodes.sort(function(a, b) {
    return a.childNodeIndex - b.childNodeIndex;
  });

  // Update parent with new child nodes
  if (parentNode) {
    if (parentNode.childNodes && parentNode.childNodes.length > 0) {
      nodes.forEach(function(entry) {
        scout.arrays.insert(parentNode.childNodes, entry, entry.childNodeIndex);
      }.bind(this));
      this._updateChildNodeIndex(parentNode.childNodes, nodes[0].childNodeIndex);
    } else {
      nodes.forEach(function(entry) {
        parentNode.childNodes.push(entry);
      }.bind(this));
    }
    //initialize node and add to visible list if node is visible
    this._visitNodes(nodes, this._initTreeNode.bind(this), parentNode);
    if (this.groupedNodes[parentNode.id]) {
      this._updateItemPath(false, parentNode);
    }
    if (this.rendered) {
      var opts = {
        expansionChanged: true
      };
      this._renderExpansion(parentNode, opts);
    }
  } else {
    if (this.nodes && this.nodes.length > 0) {
      nodes.forEach(function(entry) {
        scout.arrays.insert(this.nodes, entry, entry.childNodeIndex);
      }.bind(this));
      this._updateChildNodeIndex(this.nodes, nodes[0].childNodeIndex);
    } else {
      scout.arrays.pushAll(this.nodes, nodes);
    }
    //initialize node and add to visible list if node is visible
    this._visitNodes(nodes, this._initTreeNode.bind(this), parentNode);
  }
  if (this.rendered) {
    this._renderViewport();
    this.invalidateLayoutTree();
  }
  this.trigger('nodesInserted', {
    nodes: nodes,
    parentNode: parentNode
  });
};

scout.Tree.prototype.updateNodes = function(nodes) {
  // Update model
  var anyPropertiesChanged = false;
  for (var i = 0; i < nodes.length; i++) {
    var updatedNode = nodes[i];
    var oldNode = this.nodesMap[updatedNode.id];

    scout.defaultValues.applyTo(updatedNode, 'TreeNode');
    var propertiesChanged = this._applyUpdatedNodeProperties(oldNode, updatedNode);
    anyPropertiesChanged = anyPropertiesChanged || propertiesChanged;
    if (propertiesChanged) {
      if (this._applyFiltersForNode(oldNode)) {
        if (!oldNode.isFilterAccepted()) {
          this._nodesFiltered([oldNode]);
          this._removeFromFlatList(oldNode, false);
        } else {
          this._addToVisibleFlatList(oldNode, false);
        }
      }
      this._updateItemPath(false, oldNode.parentNode);
      if (this.rendered) {
        this._decorateNode(oldNode);
      }
    }
  }

  this.trigger('nodesUpdated', {
    nodes: nodes
  });
};

/**
 * Called by _onNodesUpdated for every updated node. The function is expected to apply
 * all updated properties from the updatedNode to the oldNode. May be overridden by
 * subclasses so update their specific node properties.
 *
 * @param oldNode
 *          The target node to be updated
 * @param updatedNode
 *          The new node with potentially updated properties. Default values are already applied!
 * @returns
 *          true if at least one property has changed, false otherwise. This value is used to
 *          determine if the node has to be rendered again.
 */
scout.Tree.prototype._applyUpdatedNodeProperties = function(oldNode, updatedNode) {
  // Note: We only update _some_ of the properties, because everything else will be handled
  // with separate events. --> See also: JsonTree.java/handleModelNodesUpdated()
  var propertiesChanged = false;
  if (oldNode.leaf !== updatedNode.leaf) {
    oldNode.leaf = updatedNode.leaf;
    propertiesChanged = true;
  }
  if (oldNode.enabled !== updatedNode.enabled) {
    oldNode.enabled = updatedNode.enabled;
    propertiesChanged = true;
  }
  if (oldNode.lazyExpandingEnabled !== updatedNode.lazyExpandingEnabled) {
    oldNode.lazyExpandingEnabled = updatedNode.lazyExpandingEnabled;
    // Also make sure expandedLazy is resetted (same code as in AbstractTreeNode.setLazyExpandingEnabled)
    oldNode.expandedLazy = updatedNode.lazyExpandingEnabled && this.lazyExpandingEnabled;
    propertiesChanged = true;
  }
  return propertiesChanged;
};

scout.Tree.prototype.deleteNodes = function(nodes, parentNode) {
  var deletedNodes = [];

  nodes.forEach(function(node) {
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

    // remove children from node map
    this._visitNodes(node.childNodes, this._destroyTreeNode.bind(this));
  }, this);

  // remove node from html document
  if (this.rendered) {
    this._removeNodes(deletedNodes, parentNode);
  }

  this.trigger('nodesDeleted', {
    nodes: nodes,
    parentNode: parentNode
  });
};

scout.Tree.prototype.deleteAllChildNodes = function(parentNode) {
  var nodes;
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
    this._removeNodes(nodes, parentNode);
  }

  this.trigger('allChildNodesDeleted', {
    parentNode: parentNode
  });

  // --- Helper functions ---

  // Update model and nodemap
  function updateNodeMap(node, parentNode) {
    this._destroyTreeNode(node, parentNode);
    this._updateMarkChildrenChecked(node, false, false);
  }
};

scout.Tree.prototype.checkNode = function(node, checked, notifyServer) {
  this.checkNodes([node], {
    checked: checked,
    notifyServer: notifyServer
  });
};

scout.Tree.prototype.checkNodes = function(nodes, options) {
  var opts = {
    checked: true,
    notifyServer: true,
    checkOnlyEnabled: true,
    isCheckChildren: false
  };
  $.extend(opts, options);
  var updatedNodes = [];
  if (!this.checkable || (!this.enabled && opts.checkOnlyEnabled)) {
    return updatedNodes;
  }
  nodes = scout.arrays.ensure(nodes);
  nodes.forEach(function(node) {
    if ((!node.enabled && opts.checkOnlyEnabled) || node.checked === opts.checked) {
      if (opts.isCheckChildren) {
        updatedNodes = updatedNodes.concat(this.checkChildren(node, opts.checked));
      }
      return;
    }
    if (!this.multiCheck && opts.checked) {
      for (var i = 0; i < this.checkedNodes.length; i++) {
        this.checkedNodes[i].checked = false;
        this._updateMarkChildrenChecked(this.checkedNodes[i], false, false, true);
        updatedNodes.push(this.checkedNodes[i]);
      }
      this.checkedNodes = [];
    }
    node.checked = opts.checked;
    if (node.checked) {
      this.checkedNodes.push(node);
    }
    updatedNodes.push(node);
    this._updateMarkChildrenChecked(node, false, opts.checked, true);
    if (opts.notifyServer) {
      updatedNodes = updatedNodes.concat(this.checkChildren(node, opts.checked));
    }
  }, this);

  if (opts.notifyServer) {
    this._sendNodesChecked(updatedNodes);
  }
  if (this.rendered) {
    updatedNodes.forEach(function(node) {
      this._renderNodeChecked(node);
    }, this);
  }
  return updatedNodes;
};

scout.Tree.prototype.uncheckNode = function(node, notifyServer) {
  this.uncheckNodes([node], {
    notifyServer: notifyServer,
    checkOnlyEnabled: true
  });
};

scout.Tree.prototype.uncheckNodes = function(nodes, options) {
  options.checked = false;
  this.checkNodes(nodes, options);
};

scout.Tree.prototype.checkChildren = function(node, checked) {
  var updatedNodes = [];
  if (this.autoCheckChildren && node) {
    updatedNodes = this.checkNodes(node.childNodes, {
      checked: checked,
      notifyServer: false,
      isCheckChildren: true
    });
  }
  return updatedNodes;
};

scout.Tree.prototype._sendNodesChecked = function(nodes) {
  var data = {
    nodes: []
  };

  for (var i = 0; i < nodes.length; i++) {
    data.nodes.push({
      nodeId: nodes[i].id,
      checked: nodes[i].checked
    });
  }

  this._send('nodesChecked', data);
};

scout.Tree.prototype._triggerNodesSelected = function(debounce) {
  this.trigger('nodesSelected', {
    debounce: debounce
  });
};

scout.Tree.prototype._showContextMenu = function(event) {
  var func = function(event) {
    event.preventDefault();

    var filteredMenus = this._filterMenus(this.menus, scout.MenuDestinations.CONTEXT_MENU, true),
      $part = $(event.currentTarget);
    if (filteredMenus.length === 0) {
      return; // at least one menu item must be visible
    }
    var popup = scout.create('ContextMenuPopup', {
      parent: this,
      menuItems: filteredMenus,
      location: {
        x: event.pageX,
        y: event.pageY
      },
      $anchor: $part,
      menuFilter: this._filterMenusHandler
    });
    popup.open();

    // Set table style to focused, so that it looks as it still has the focus.
    // Must be called after open(), because opening the popup might cause another
    // popup to close first (which will remove the 'focused' class).
    if (this.enabled) {
      this.$container.addClass('focused');
      popup.on('close', function(event) {
        this.$container.removeClass('focused');
      }.bind(this));
    }
  };

  scout.menus.showContextMenuWithWait(this.session, func.bind(this), event);
};

scout.Tree.prototype._onNodeMouseDown = function(event) {
  this._doubleClickSupport.mousedown(event);
  if (this._doubleClickSupport.doubleClicked()) {
    //don't execute on double click events
    return false;
  }

  var $node = $(event.currentTarget);
  var node = $node.data('node');
  this._$mouseDownNode = $node;
  $node.window().one('mouseup', function() {
    this._$mouseDownNode = null;
  }.bind(this));

  this.selectNodes(node);

  if (this.checkable && this._isCheckboxClicked(event)) {
    if (scout.device.supportsFocusEmptyBeforeDiv) {
      this.session.focusManager.requestFocus(this.$container);
      event.preventDefault();
    }
    this.checkNode(node, !node.checked);
  }
  return true;
};

scout.Tree.prototype._onNodeMouseUp = function(event) {
  if (this._doubleClickSupport.doubleClicked()) {
    //don't execute on double click events
    return false;
  }

  var $node = $(event.currentTarget);
  var node = $node.data('node');
  if (!this._$mouseDownNode || this._$mouseDownNode[0] !== $node[0]) {
    // Don't accept if mouse up happens on another node than mouse down, or mousedown didn't happen on a node at all
    return;
  }

  this._send('nodeClicked', {
    nodeId: node.id
  });
  return true;
};

scout.Tree.prototype._isCheckboxClicked = function(event) {
  return $(event.target).is('.check-box');
};

scout.Tree.prototype._updateItemPath = function(selectionChanged, ultimate) {
  var selectedNodes, node, level;
  if (selectionChanged) {
    // first remove and select selected
    this.groupedNodes = {};

    this._inSelectionPathList = {};
  }

  if (!ultimate) {
    // find direct children
    selectedNodes = this.selectedNodes;
    if (selectedNodes.length === 0) {
      return;
    }
    node = selectedNodes[0];

    if (selectionChanged) {
      this._inSelectionPathList[node.id] = true;
      if (node.childNodes) {
        node.childNodes.forEach(function(child) {
          this._inSelectionPathList[child.id] = true;
        }.bind(this));
      }
    }
    level = node.level;

    // find grouping end (ultimate parent)
    while (node.parentNode) {
      var parent = node.parentNode;
      if (this._isGroupingEnd(parent) && !ultimate) {
        ultimate = node;
        if (!selectionChanged) {
          break;
        }
      }
      if (selectionChanged) {
        this._inSelectionPathList[parent.id] = true;
      }
      node = parent;
    }
    // find group with same ultimate parent
    ultimate = ultimate || selectedNodes[0];
    this.groupedNodes[ultimate.id] = true;
  }
  node = ultimate;
  if (node && node.expanded && this.groupedNodes[node.id]) {
    addToGroup.call(this, node.childNodes);
  }
  //------ helper function ------//

  function addToGroup(nodes) {
    nodes.forEach(function(node) {
      this.groupedNodes[node.id] = true;
      this._decorateNode(node);
      if (node.expanded && node.isFilterAccepted()) {
        addToGroup.call(this, node.childNodes);
      }
    }.bind(this));
  }
};

scout.Tree.prototype._isGroupingEnd = function(node) {
  // May be implemented by subclasses, default tree has no grouping parent
  return false;
};

scout.Tree.prototype.$selectedNodes = function() {
  return this.$data.find('.selected');
};

scout.Tree.prototype.$nodes = function() {
  return this.$data.find('.tree-node');
};

/**
 * @param filter object with createKey() and accept()
 */
scout.Tree.prototype.addFilter = function(filter, doNotFilter) {
  if (this._filters.indexOf(filter) < 0) {
    this._filters.push(filter);
    if (!doNotFilter) {
      this.filter();
    }
    return true;
  }
  return false;
};

scout.Tree.prototype.removeFilter = function(filter) {
  scout.arrays.remove(this._filters, filter);
  this.filter();
};

scout.Tree.prototype.filter = function(notAnimated) {
  var useAnimation = !!!notAnimated,
    changedNodes = [],
    newHiddenNodes = [];
  // Filter nodes
  this._visitNodes(this.nodes, function(node) {
    var changed = this._applyFiltersForNode(node);
    if (changed) {
      changedNodes.push(node);
      if (!node.isFilterAccepted()) {
        scout.arrays.pushAll(newHiddenNodes, this._removeFromFlatList(node, useAnimation));
      } else {
        this._addToVisibleFlatList(node, useAnimation);
      }
      this.viewRangeDirty = true;
    }
    if ((node.expanded || node.expandedLazy) && node.isFilterAccepted()) {
      return false;
    }
    //don't process children->optimize performance
    return true;
  }.bind(this));

  this._nodesFiltered(newHiddenNodes);
};

/**
 * use filtered nodes are removed from visible nodes
 */
scout.Tree.prototype.filterVisibleNodes = function(animated) {
  // Filter nodes
  var newHiddenNodes = [];
  for (var i = 0; i < this.visibleNodesFlat.length; i++) {
    var node = this.visibleNodesFlat[i];
    var changed = this._applyFiltersForNode(node);
    if (changed) {
      if (!node.isFilterAccepted()) {
        i--;
        scout.arrays.pushAll(newHiddenNodes, this._removeFromFlatList(node, animated));
      }
      this.viewRangeDirty = true;
    }
  }

  this._nodesFiltered(newHiddenNodes);
};

scout.Tree.prototype._nodesFiltered = function(hiddenNodes) {
  // non visible nodes must be deselected
  this.deselectNodes(hiddenNodes);
};

scout.Tree.prototype._nodeAcceptedByFilters = function(node) {
  for (var i = 0; i < this._filters.length; i++) {
    var filter = this._filters[i];
    if (!filter.accept(node)) {
      return false;
    }
  }
  return true;
};

/**
 * @returns {Boolean} true if node state has changed, false if not
 */
scout.Tree.prototype._applyFiltersForNode = function(node) {
  var changed = node.filterDirty;
  if (this._nodeAcceptedByFilters(node)) {
    if (!node.filterAccepted) {
      node.filterAccepted = true;
      changed = true;
    }
  } else {
    if (node.filterAccepted) {
      node.filterAccepted = false;
      changed = true;
    }
  }
  if (changed) {
    node.filterDirty = false;
    node.childNodes.forEach(function(childNode) {
      childNode.filterDirty = true;
    });
    return true;
  }
  return false;
};

/**
 * Just insert node in DOM. NO check if in viewRange
 */
scout.Tree.prototype._insertNodeInDOM = function(node, indexHint) {
  if (!this.rendered && !this.isRendering) {
    return;
  }
  var index = indexHint === undefined ? this.visibleNodesFlat.indexOf(node) : indexHint;
  if (index === -1 || !(this.viewRangeRendered.from + this.viewRangeSize >= index && this.viewRangeRendered.from <= index) || node.attached) {
    //node is not visible
    return;
  }
  if (!node.$node) {
    this._$buildNode(node);
  }
  this._decorateNode(node);

  this._insertNodeInDOMAtPlace(node, index);

  node.height = node.$node.outerHeight();
  node.width = node.$node.outerWidth();
  node.rendered = true;
  node.attached = true;
};

scout.Tree.prototype._insertNodeInDOMAtPlace = function(node, index) {
  var $node = node.$node,
  added = false;
  if (index === 0) {
    if (this.$fillBefore) {
      added = true;
      $node.insertAfter(this.$fillBefore);
    } else {
      added = true;
      this.$data.prepend($node);
    }
  } else {
    //append after index
    var nodeBefore = this.visibleNodesFlat[index - 1];
    if (nodeBefore.attached) {
      $node.insertAfter(nodeBefore.$node);
      added = true;
    } else if (index + 1 < this.visibleNodesFlat.length) {
      var nodeAfter = this.visibleNodesFlat[index + 1];
      if (nodeAfter.attached) {
        added = true;
        $node.insertBefore(nodeAfter.$node);
      }
    }

    if (!added && this.$fillBefore) {
      $node.insertAfter(this.$fillBefore);
    } else if (!added) {
      this.$data.prepend($node);
    }
  }
};

scout.Tree.prototype.showNode = function(node, useAnimation, indexHint) {
  if (node.attached || !this.rendered) {
    return;
  }
  this._insertNodeInDOM(node, indexHint);
  if (!node.rendered) {
    return;
  }
  var $node = node.$node;
  if ($node.is('.showing')) {
    return;
  }
  $node.showFast();
  $node.addClass('showing');
  $node.removeClass('hiding');
  var that = this;
  if (useAnimation) {
    $node.data('oldStyle', $node.attr('style'));
    $node.setVisible(false);
    $node.stop().slideDown({
      duration: 250,
      start: that.startAnimationFunc,
      complete: function() {
        that.runningAnimationsFinishFunc();
        var oldStyle = $node.data('oldStyle');
        if (oldStyle) {
          $node.removeData('oldStyle');
          $node.attrOrRemove('style', oldStyle);
        }
      }
    });
  }

};

scout.Tree.prototype.hideNode = function(node, useAnimation, suppressDetachHandling) {
  if (!node.attached) {
    return;
  }
  var that = this,
    $node = node.$node;
  if (!$node) {
    //node is not rendered
    return;
  }

  if ($node.is('.hiding')) {
    return;
  }

  $node.addClass('hiding');
  $node.removeClass('showing');

  if (useAnimation) {
    this._renderViewportBlocked = true;
    $node.data('oldStyle', $node.attr('style'));
    $node.stop().slideUp({
      duration: 250,
      start: that.startAnimationFunc,
      complete: function() {
        that.runningAnimationsFinishFunc();
        $node.detach();
        node.attached = false;
        var oldStyle = $node.data('oldStyle');
        if (oldStyle) {
          $node.removeData('oldStyle');
          $node.attrOrRemove('style', oldStyle);
        }
      }
    });
  } else if (!suppressDetachHandling) {
    $node.hideFast();
    $node.detach();
    node.attached = false;
    that.invalidateLayoutTree();
  }
};

scout.Tree.prototype._nodesToIds = function(nodes) {
  return nodes.map(function(node) {
    return node.id;
  });
};

scout.Tree.prototype._nodesByIds = function(ids) {
  return ids.map(function(id) {
    return this.nodesMap[id];
  }.bind(this));
};

scout.Tree.prototype._nodeById = function(id) {
  return this.nodesMap[id];
};

scout.Tree.prototype._onNodeDoubleClick = function(event) {
  var $node = $(event.currentTarget);
  var node = $node.data('node');
  var expanded = !$node.hasClass('expanded');

  if (this.isBreadcrumbStyleActive()) {
    return;
  }

  this._send('nodeAction', {
    nodeId: node.id
  });

  this.setNodeExpanded(node, expanded, {
    lazy: false // always show all nodes on node double click
  });
};

scout.Tree.prototype._onNodeControlMouseDown = function(event) {
  this._doubleClickSupport.mousedown(event);
  if (this._doubleClickSupport.doubleClicked()) {
    //don't execute on double click events
    return false;
  }

  var $node = $(event.currentTarget).parent();
  var node = $node.data('node');
  var expanded = !$node.hasClass('expanded');
  var expansionOpts = {
    lazy: false // always show all nodes when the control gets clicked
  };

  // Click on "show all" control shows all nodes
  if ($node.hasClass('lazy')) {
    if (event.ctrlKey || event.shiftKey) {
      // Collapse
      expanded = false;
      expansionOpts.collapseChildNodes = true;
    } else {
      // Show all nodes
      this.expandNode(node, expansionOpts);
      return false;
    }
  }
  //because we suppress handling by browser we have to set focus manually.
  this._onNodeControlMouseDownDoFocus();
  this.selectNodes(node);
  this.setNodeExpanded(node, expanded, expansionOpts);
  // prevent bubbling to _onNodeMouseDown()
  $.suppressEvent(event);

  // ...but return true, so Outline.js can override this method and check if selection has been changed or not
  return true;
};

//some fields doesn't want to set focus on container.
scout.Tree.prototype._onNodeControlMouseDownDoFocus = function() {
  this.session.focusManager.requestFocus(this.$container);
};

scout.Tree.prototype._onNodeControlMouseUp = function(event) {
  // prevent bubbling to _onNodeMouseUp()
  return false;
};

scout.Tree.prototype._onNodeControlDoubleClick = function(event) {
  // prevent bubbling to _onNodeDoubleClick()
  return false;
};

scout.Tree.prototype._onContextMenu = function(event) {
  this._showContextMenu(event);
};

scout.Tree.prototype._onRequestFocus = function() {
  this.session.focusManager.requestFocus(this.$container);
};

scout.Tree.prototype._onScrollToSelection = function() {
  this.revealSelection();
};

scout.Tree.prototype._onNodesInserted = function(nodes, parentNodeId) {
  var parentNode;
  if (parentNodeId !== null && parentNodeId !== undefined) {
    parentNode = this.nodesMap[parentNodeId];
    if (!parentNode) {
      throw new Error('Parent node could not be found. Id: ' + parentNodeId);
    }
  }
  this.insertNodes(nodes, parentNode);
};

scout.Tree.prototype._onNodesUpdated = function(nodes) {
  this.updateNodes(nodes);
};

scout.Tree.prototype._onNodesDeleted = function(nodeIds, parentNodeId) {
  var parentNode;
  if (parentNodeId !== null && parentNodeId !== undefined) {
    parentNode = this.nodesMap[parentNodeId];
    if (!parentNode) {
      throw new Error('Parent node could not be found. Id: ' + parentNodeId);
    }
  }
  var nodes = this._nodesByIds(nodeIds);
  this.deleteNodes(nodes, parentNode);
};

scout.Tree.prototype._onAllChildNodesDeleted = function(parentNodeId) {
  var parentNode;
  if (parentNodeId !== null && parentNodeId !== undefined) {
    parentNode = this.nodesMap[parentNodeId];
    if (!parentNode) {
      throw new Error('Parent node could not be found. Id: ' + parentNodeId);
    }
  }
  this.deleteAllChildNodes(parentNode);
};

scout.Tree.prototype._onNodesSelected = function(nodeIds) {
  var nodes = this._nodesByIds(nodeIds);
  this.selectNodes(nodes, false);
};

scout.Tree.prototype._onNodeExpanded = function(nodeId, event) {
  var node = this.nodesMap[nodeId],
    expanded = event.expanded,
    recursive = event.recursive,
    lazy = event.expandedLazy;

  this.setNodeExpanded(node, expanded, {
    notifyServer: false,
    lazy: lazy
  });
  if (recursive) {
    this._visitNodes(node.childNodes, function(childNode) {
      this.setNodeExpanded(childNode, expanded, {
        notifyServer: false,
        lazy: lazy
      });
    }.bind(this));
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

  if (this._applyFiltersForNode(node)) {
    if (node.isFilterAccepted()) {
      this._addToVisibleFlatList(node, false);
    } else {
      this._removeFromFlatList(node, false);
    }
  }

  if (this.rendered) {
    this._decorateNode(node);
  }

  this.trigger('nodeChanged', {
    node: node
  });
};

scout.Tree.prototype._onNodesChecked = function(nodes) {
  var checkedNodes = [],
    uncheckedNodes = [];

  nodes.forEach(function(nodeData) {
    var node = this._nodeById(nodeData.id);
    if (nodeData.checked) {
      checkedNodes.push(node);
    } else {
      uncheckedNodes.push(node);
    }
  }, this);

  this.checkNodes(checkedNodes, {
    checked: true,
    notifyServer: false,
    checkOnlyEnabled: false
  });
  this.uncheckNodes(uncheckedNodes, {
    notifyServer: false,
    checkOnlyEnabled: false
  });
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
  this._removeChildsFromFlatList(parentNode, false);
  this._addChildsToFlatList(parentNode, null, false);

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
  this.trigger('childNodeOrderChanged', {
    parentNode: parentNode
  });

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

scout.Tree.prototype.onModelAction = function(event) {
  if (event.type === 'nodesInserted') {
    this._onNodesInserted(event.nodes, event.commonParentNodeId);
  } else if (event.type === 'nodesUpdated') {
    this._onNodesUpdated(event.nodes);
  } else if (event.type === 'nodesDeleted') {
    this._onNodesDeleted(event.nodeIds, event.commonParentNodeId);
  } else if (event.type === 'allChildNodesDeleted') {
    this._onAllChildNodesDeleted(event.commonParentNodeId);
  } else if (event.type === 'nodesSelected') {
    this._onNodesSelected(event.nodeIds);
  } else if (event.type === 'nodeExpanded') {
    this._onNodeExpanded(event.nodeId, event);
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
    scout.Tree.parent.prototype.onModelAction.call(this, event);
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
