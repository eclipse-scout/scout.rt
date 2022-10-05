/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays, ContextMenuPopup, defaultValues, Device, DoubleClickSupport, dragAndDrop, FilterSupport, graphics, HtmlComponent, KeyStrokeContext, keyStrokeModifier, LazyNodeFilter, MenuBar, MenuDestinations, MenuItemsOrder, menus as menus_1, objects, Range, scout, scrollbars, tooltips, TreeBreadcrumbFilter, TreeCollapseAllKeyStroke, TreeCollapseOrDrillUpKeyStroke, TreeExpandOrDrillDownKeyStroke, TreeLayout, TreeNavigationDownKeyStroke, TreeNavigationEndKeyStroke, TreeNavigationUpKeyStroke, TreeNode, TreeSpaceKeyStroke, Widget} from '../index';
import $ from 'jquery';

/**
 * @class
 * @constructor
 */
export default class Tree extends Widget {
  constructor() {
    super();

    this.toggleBreadcrumbStyleEnabled = false;
    this.breadcrumbTogglingThreshold = null;
    this.autoCheckChildren = false;
    this.checkable = false;
    this.checkableStyle = Tree.CheckableStyle.CHECKBOX_TREE_NODE;
    this.displayStyle = Tree.DisplayStyle.DEFAULT;
    this.dropType = 0;
    this.dropMaximumSize = dragAndDrop.DEFAULT_DROP_MAXIMUM_SIZE;
    this.lazyExpandingEnabled = true;
    this.menus = [];
    this.contextMenu = null;
    this.menuBar = null;
    this.keyStrokes = [];
    this.multiCheck = true;
    this.nodes = []; // top-level nodes
    this.nodesMap = {}; // all nodes by id
    this.nodePaddingLevelCheckable = 23; /* padding for one tree-level if the tree is checkable */
    this.nodePaddingLevelNotCheckable = 18; /* padding for one tree-level if the tree is not checkable. this includes outline trees! */
    this.nodePaddingLevelDiffParentHasIcon = null; /* is read from CSS */
    this.nodePaddingLeft = null; /* is read from CSS */
    this.nodeCheckBoxPaddingLeft = 29;
    this.nodeControlPaddingLeft = null; /* is read from CSS */
    this.nodePaddingLevel = this.nodePaddingLevelNotCheckable;
    this.scrollToSelection = false;
    this.scrollTop = 0;
    this.scrollTopHistory = []; // Only necessary for breadcrumb mode
    this.selectedNodes = [];
    this.prevSelectedNode = null; // The previously selected node, relevant for breadcrumb in compact mode

    this.filters = [];
    this.textFilterEnabled = true;
    this.filterSupport = this._createFilterSupport();
    this.filteredElementsDirty = false;
    this.filterAnimated = true;

    // performance optimization: E.g. rather than iterating over the whole tree when unchecking all nodes,
    // we explicitly keep track of nodes to uncheck (useful e.g. for single-check mode in very large trees).
    this.checkedNodes = [];

    this.groupedNodes = {};
    this.visibleNodesFlat = [];
    this.visibleNodesMap = {};
    this._addWidgetProperties(['menus', 'keyStrokes']);
    this._additionalContainerClasses = ''; // may be used by subclasses to set additional CSS classes
    this._doubleClickSupport = new DoubleClickSupport();
    this._$animationWrapper = null; // used by _renderExpansion()
    this._$expandAnimationWrappers = [];
    this._filterMenusHandler = this._filterMenus.bind(this);
    this._popupOpenHandler = this._onDesktopPopupOpen.bind(this);

    // contains all parents of a selected node, the selected node and the first level children
    this._inSelectionPathList = {};
    this._changeNodeTaskScheduled = false;
    this.viewRangeRendered = new Range(0, 0);
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
    this.maxNodeWidth = 0;
    this.nodeWidthDirty = false;
    this.$data = null;
    this._scrolldirections = 'both';
    this.requestFocusOnNodeControlMouseDown = true;
    this._$mouseDownNode = null;
  }

  static DisplayStyle = {
    DEFAULT: 'default',
    BREADCRUMB: 'breadcrumb'
  };

  static CheckableStyle = {
    /**
     * Node check is only possible by checking the checkbox.
     */
    CHECKBOX: 'checkbox',
    /**
     * Node check is possible by clicking anywhere on the node.
     */
    CHECKBOX_TREE_NODE: 'checkbox_tree_node'
  };

  /**
   * Used to calculate the view range size. See {@link calculateViewRangeSize}.
   */
  static VIEW_RANGE_DIVISOR = 4;

  _init(model) {
    super._init(model);
    this.setFilters(this.filters, false);
    this.addFilter(new LazyNodeFilter(this), false);
    this.breadcrumbFilter = new TreeBreadcrumbFilter(this);
    if (this.displayStyle === Tree.DisplayStyle.BREADCRUMB) {
      this.addFilter(this.breadcrumbFilter, false);
    }
    this.initialTraversing = true;
    this._setCheckable(this.checkable);
    this._ensureTreeNodes(this.nodes);
    this._initNodes(this.nodes);
    this.initialTraversing = false;
    this.menuBar = scout.create('MenuBar', {
      parent: this,
      position: MenuBar.Position.BOTTOM,
      menuOrder: new MenuItemsOrder(this.session, 'Tree'),
      menuFilter: this._filterMenusHandler,
      cssClass: 'bounded'
    });
    this._updateItemPath(true);
    this._setDisplayStyle(this.displayStyle);
    this._setKeyStrokes(this.keyStrokes);
    this._setMenus(this.menus);
  }

  /**
   * Initialize nodes, applies filters and updates flat list
   */
  _initNodes(nodes, parentNode) {
    if (!nodes) {
      nodes = this.nodes;
    }
    Tree.visitNodes(this._initTreeNode.bind(this), nodes, parentNode);
    if (typeof this.selectedNodes[0] === 'string') {
      this.selectedNodes = this._nodesByIds(this.selectedNodes);
    }
    this._updateSelectionPath();
    nodes.forEach(node => this.applyFiltersForNode(node));
    Tree.visitNodes((node, parentNode) => this._addToVisibleFlatList(node, false), nodes, parentNode);
  }

  /**
   * Iterates through the given array and converts node-models to instances of TreeNode (or a subclass).
   * If the array element is already a TreeNode the function leaves the element untouched. This function also
   * ensures that the attribute childNodeIndex is set. By default we use the order of the nodes array as index
   * but only if childNodeIndex is undefined.
   *
   * @param nodes Array of node-models (plain object) or nodes (instance of TreeNode)
   */
  _ensureTreeNodes(nodes) {
    let i, node;
    for (i = 0; i < nodes.length; i++) {
      node = nodes[i];
      node.childNodeIndex = scout.nvl(node.childNodeIndex, i);
      if (node instanceof TreeNode) {
        continue;
      }
      nodes[i] = this._createTreeNode(node);
    }
  }

  _createTreeNode(nodeModel) {
    nodeModel = scout.nvl(nodeModel, {});
    nodeModel.parent = this;
    return scout.create('TreeNode', nodeModel);
  }

  /**
   * @override
   */
  _createKeyStrokeContext() {
    return new KeyStrokeContext();
  }

  /**
   * @override
   */
  _initKeyStrokeContext() {
    super._initKeyStrokeContext();

    this._initTreeKeyStrokeContext();
  }

  _initTreeKeyStrokeContext() {
    let modifierBitMask = keyStrokeModifier.NONE;

    this.keyStrokeContext.registerKeyStroke([
      new TreeSpaceKeyStroke(this),
      new TreeNavigationUpKeyStroke(this, modifierBitMask),
      new TreeNavigationDownKeyStroke(this, modifierBitMask),
      new TreeCollapseAllKeyStroke(this, modifierBitMask),
      new TreeCollapseOrDrillUpKeyStroke(this, modifierBitMask),
      new TreeNavigationEndKeyStroke(this, modifierBitMask),
      new TreeExpandOrDrillDownKeyStroke(this, modifierBitMask)
    ]);
  }

  _setMenus(argMenus) {
    this.updateKeyStrokes(argMenus, this.menus);
    this._setProperty('menus', argMenus);
    this._updateMenuBar();
  }

  _updateMenuBar() {
    let menuItems = this._filterMenus(this.menus, MenuDestinations.MENU_BAR, false, true);
    this.menuBar.setMenuItems(menuItems);
    let contextMenuItems = this._filterMenus(this.menus, MenuDestinations.CONTEXT_MENU, true);
    if (this.contextMenu) {
      this.contextMenu.updateMenuItems(contextMenuItems);
    }
  }

  _setKeyStrokes(keyStrokes) {
    this.updateKeyStrokes(keyStrokes, this.keyStrokes);
    this._setProperty('keyStrokes', keyStrokes);
  }

  _resetTreeNode(node, parentNode) {
    node.reset();
  }

  isSelectedNode(node) {
    return this.selectedNodes.indexOf(node) > -1;
  }

  _updateSelectionPath() {
    let selectedNode = this.selectedNodes[0];
    if (!selectedNode) {
      return;
    }
    this._inSelectionPathList[selectedNode.id] = true;

    selectedNode.childNodes.forEach(child => {
      this._inSelectionPathList[child.id] = true;
    });

    let parentNode = selectedNode.parentNode;
    while (parentNode) {
      this._inSelectionPathList[parentNode.id] = true;
      parentNode = parentNode.parentNode;
    }
  }

  _initTreeNode(node, parentNode) {
    this.nodesMap[node.id] = node;
    if (parentNode) {
      node.parentNode = parentNode;
      node.level = node.parentNode.level + 1;
    }
    if (node.checked) {
      this.checkedNodes.push(node);
    }
    this._initTreeNodeInternal(node, parentNode);
    this._updateMarkChildrenChecked(node, true, node.checked);
    node.initialized = true;
  }

  _applyNodeDefaultValues(node) {
    defaultValues.applyTo(node, 'TreeNode');
  }

  /**
   * Override this function if you want a custom node init before filtering.
   * The default impl. applies default values to the given node.
   */
  _initTreeNodeInternal(node, parentNode) {
    this._applyNodeDefaultValues(node);
  }

  _destroy() {
    super._destroy();
    this.visitNodes(this._destroyTreeNode.bind(this));
    this.nodes = []; // finally, clear array with root tree-nodes
  }

  _destroyTreeNode(node) {
    delete this.nodesMap[node.id];
    this._removeFromFlatList(node, false); // ensure node is not longer in visible nodes list.
    node.destroy();

    // noinspection JSUnresolvedVariable
    if (this._onNodeDeleted) { // Necessary for subclasses
      this._onNodeDeleted(node);
    }
  }

  /**
   * pre-order (top-down) traversal of the tree-nodes of this tree.<br>
   * if func returns true the children of the visited node are not visited.
   */
  visitNodes(func, parentNode) {
    return Tree.visitNodes(func, this.nodes, parentNode);
  }

  _render() {
    this.$container = this.$parent.appendDiv('tree');
    if (this._additionalContainerClasses) {
      this.$container.addClass(this._additionalContainerClasses);
    }
    let layout = new TreeLayout(this);
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(layout);
    this._renderData();
    this.menuBar.render();
    this.session.desktop.on('popupOpen', this._popupOpenHandler);
    this._renderCheckableStyle();
  }

  _renderData() {
    this.$data = this.$container.appendDiv('tree-data')
      .on('contextmenu', this._onContextMenu.bind(this))
      .on('mousedown', '.tree-node', this._onNodeMouseDown.bind(this))
      .on('mouseup', '.tree-node', this._onNodeMouseUp.bind(this))
      .on('dblclick', '.tree-node', this._onNodeDoubleClick.bind(this))
      .on('mousedown', '.tree-node-control', this._onNodeControlMouseDown.bind(this))
      .on('mouseup', '.tree-node-control', this._onNodeControlMouseUp.bind(this))
      .on('dblclick', '.tree-node-control', this._onNodeControlDoubleClick.bind(this));
    HtmlComponent.install(this.$data, this.session);

    if (this.isHorizontalScrollingEnabled()) {
      this.$data.toggleClass('scrollable-tree', true);
    }

    this._installScrollbars({
      axis: this._scrolldirections
    });
    this._installNodeTooltipSupport();
    this._updateNodeDimensions();
    // render display style before viewport (not in renderProperties) to have a correct style from the beginning
    this._renderDisplayStyle();
    this._renderViewport();
  }

  _renderProperties() {
    super._renderProperties();
    this._renderTextFilterEnabled();
  }

  _postRender() {
    super._postRender();
    this._renderSelection();
  }

  _remove() {
    this.session.desktop.off('popupOpen', this._popupOpenHandler);
    this.filterSupport.remove();

    // stop all animations
    if (this._$animationWrapper) {
      this._$animationWrapper.stop(false, true);
    }
    // Detach nodes from jQuery objects (because those will be removed)
    this.visitNodes(this._resetTreeNode.bind(this));

    dragAndDrop.uninstallDragAndDropHandler(this);
    this._uninstallNodeTooltipSupport();
    this.$fillBefore = null;
    this.$fillAfter = null;
    this.$data = null;
    // reset rendered view range because no range is rendered
    this.viewRangeRendered = new Range(0, 0);
    super._remove();
  }

  isHorizontalScrollingEnabled() {
    return this._scrolldirections === 'both' || this._scrolldirections === 'x';
  }

  isTreeNodeCheckEnabled() {
    return this.checkableStyle === Tree.CheckableStyle.CHECKBOX_TREE_NODE;
  }

  /**
   * @override
   */
  _onScroll() {
    let scrollToSelectionBackup = this.scrollToSelection;
    this.scrollToSelection = false;
    let scrollTop = this.$data[0].scrollTop;
    let scrollLeft = this.$data[0].scrollLeft;
    if (this.scrollTop !== scrollTop && this.rendered) {
      this._renderViewport();
    }
    this.scrollTop = scrollTop;
    this.scrollLeft = scrollLeft;
    this.scrollToSelection = scrollToSelectionBackup;
  }

  /**
   * @override
   */
  setScrollTop(scrollTop) {
    this.setProperty('scrollTop', scrollTop);
    // call _renderViewport to make sure nodes are rendered immediately. The browser fires the scroll event handled by onDataScroll delayed
    if (this.rendered) {
      this._renderViewport();
      // Render scroll top again to make sure the data really is at the expected position
      // This seems only to be necessary for Chrome and the tree, it seems to work for IE and table.
      // It is not optimal, because actually it should be possible to modify the $data[0].scrollTop without using this function
      // Some debugging showed that after reducing the height of the afterFiller in _renderFiller the scrollTop will be wrong.
      // Updating the scrollTop in renderFiller or other view range relevant function is bad because it corrupts smooth scrolling (see also commit c14ce92e0a7bff568d4f2d715e3061a782e728c2)
      this._renderScrollTop();
    }
  }

  /**
   * @override
   */
  _renderScrollTop() {
    if (this.rendering) {
      // Not necessary to do it while rendering since it will be done by the layout
      return;
    }
    scrollbars.scrollTop(this.$data, this.scrollTop);
  }

  /**
   * @override
   */
  get$Scrollable() {
    return this.$data;
  }

  _renderViewport() {
    if (this.runningAnimations > 0 || this._renderViewportBlocked) {
      // animation pending do not render view port because finishing should rerenderViewport
      return;
    }
    if (!this.$container.isEveryParentVisible()) {
      // If the tree is invisible, the width and height of the nodes cannot be determined
      // In that case, the tree won't be layouted either -> as soon as it will be layouted, renderViewport will be called again
      return;
    }
    let viewRange = this._calculateCurrentViewRange();
    this._renderViewRange(viewRange);
  }

  _calculateCurrentViewRange() {
    let node,
      scrollTop = this.$data[0].scrollTop,
      maxScrollTop = this.$data[0].scrollHeight - this.$data[0].clientHeight;

    if (maxScrollTop === 0 && this.visibleNodesFlat.length > 0) {
      // no scrollbars visible
      node = this.visibleNodesFlat[0];
    } else {
      node = this._nodeAtScrollTop(scrollTop);
    }

    return this._calculateViewRangeForNode(node);
  }

  _rerenderViewport() {
    if (this._renderViewportBlocked) {
      return;
    }
    this._removeRenderedNodes();
    this._renderFiller();
    this._updateDomNodeWidth();
    this._renderViewport();
  }

  _removeRenderedNodes() {
    let $nodes = this.$data.find('.tree-node');
    $nodes.each((i, elem) => {
      let $node = $(elem),
        node = $node.data('node');
      if ($node.hasClass('hiding')) {
        // Do not remove nodes which are removed using an animation
        return;
      }
      this._removeNode(node);
    });
    this.viewRangeRendered = new Range(0, 0);
  }

  _renderViewRangeForNode(node) {
    let viewRange = this._calculateViewRangeForNode(node);
    this._renderViewRange(viewRange);
  }

  _renderNodesInRange(range) {
    let prepend = false;

    let nodes = this.visibleNodesFlat;
    if (nodes.length === 0) {
      return;
    }

    let maxRange = new Range(0, nodes.length);
    range = maxRange.intersect(range);
    if (this.viewRangeRendered.size() > 0 && !range.intersect(this.viewRangeRendered).equals(new Range(0, 0))) {
      throw new Error('New range must not intersect with existing.');
    }
    if (range.to <= this.viewRangeRendered.from) {
      prepend = true;
    }
    let newRange = this.viewRangeRendered.union(range);
    if (newRange.length === 2) {
      throw new Error('Can only prepend or append rows to the existing range. Existing: ' + this.viewRangeRendered + '. New: ' + newRange);
    }
    this.viewRangeRendered = newRange[0];

    let numNodesRendered = this.ensureRangeVisible(range);

    $.log.isTraceEnabled() && $.log.trace(numNodesRendered + ' new nodes rendered from ' + range);
  }

  ensureRangeVisible(range) {
    let nodes = this.visibleNodesFlat;
    let nodesToInsert = [];
    for (let r = range.from; r < range.to; r++) {
      let node = nodes[r];
      if (!node.attached) {
        nodesToInsert.push(node);
      }
    }
    this._insertNodesInDOM(nodesToInsert);
    return nodesToInsert.length;
  }

  _renderFiller() {
    if (!this.$fillBefore) {
      this.$fillBefore = this.$data.prependDiv('tree-data-fill');
    }

    let fillBeforeDimensions = this._calculateFillerDimension(new Range(0, this.viewRangeRendered.from));
    this.$fillBefore.cssHeight(fillBeforeDimensions.height);
    if (this.isHorizontalScrollingEnabled()) {
      this.$fillBefore.cssWidth(fillBeforeDimensions.width);
      this.maxNodeWidth = Math.max(fillBeforeDimensions.width, this.maxNodeWidth);
    }
    $.log.isTraceEnabled() && $.log.trace('FillBefore height: ' + fillBeforeDimensions.height);

    if (!this.$fillAfter) {
      this.$fillAfter = this.$data.appendDiv('tree-data-fill');
    }

    let fillAfterDimensions = {
      height: 0,
      width: 0
    };
    fillAfterDimensions = this._calculateFillerDimension(new Range(this.viewRangeRendered.to, this.visibleNodesFlat.length));
    this.$fillAfter.cssHeight(fillAfterDimensions.height);
    if (this.isHorizontalScrollingEnabled()) {
      this.$fillAfter.cssWidth(fillAfterDimensions.width);
      this.maxNodeWidth = Math.max(fillAfterDimensions.width, this.maxNodeWidth);
    }
    $.log.isTraceEnabled() && $.log.trace('FillAfter height: ' + fillAfterDimensions.height);
  }

  _calculateFillerDimension(range) {
    let dataWidth = 0;
    if (this.rendered) {
      // the outer-width is only correct if this tree is already rendered. otherwise wrong values are returned.
      dataWidth = this.$data.width();
    }
    let dimension = {
      height: 0,
      width: Math.max(dataWidth, this.maxNodeWidth)
    };
    for (let i = range.from; i < range.to; i++) {
      let node = this.visibleNodesFlat[i];
      dimension.height += this._heightForNode(node);
      dimension.width = Math.max(dimension.width, this._widthForNode(node));
    }
    return dimension;
  }

  _removeNodesInRange(range) {
    let fromNode, toNode, node, i,
      numNodesRemoved = 0,
      nodes = this.visibleNodesFlat;

    let maxRange = new Range(0, nodes.length);
    range = maxRange.intersect(range);
    fromNode = nodes[range.from];
    toNode = nodes[range.to];

    let newRange = this.viewRangeRendered.subtract(range);
    if (newRange.length === 2) {
      throw new Error('Can only remove nodes at the beginning or end of the existing range. ' + this.viewRangeRendered + '. New: ' + newRange);
    }
    this.viewRangeRendered = newRange[0];

    for (i = range.from; i < range.to; i++) {
      node = nodes[i];
      this._removeNode(node);
      numNodesRemoved++;
    }

    $.log.isTraceEnabled() && $.log.trace(numNodesRemoved + ' nodes removed from ' + range + '.');
  }

  /**
   * Just removes the node, does NOT adjust this.viewRangeRendered
   */
  _removeNode(node) {
    let $node = node.$node;
    if (!$node) {
      return;
    }
    if ($node.hasClass('hiding')) {
      // Do not remove nodes which are removed using an animation
      return;
    }
    // only remove node
    $node.detach();
    node.attached = false;
  }

  /**
   * Renders the rows visible in the viewport and removes the other rows
   */
  _renderViewRange(viewRange) {
    if (viewRange.from === this.viewRangeRendered.from && viewRange.to === this.viewRangeRendered.to && !this.viewRangeDirty) {
      // When node with has changed (because of changes in layout) we must at least
      // update the internal node width even though the view-range has not changed.
      if (this.nodeWidthDirty) {
        this._renderFiller();
        this._updateDomNodeWidth();
      }

      // Range already rendered -> do nothing
      return;
    }
    if (!this.viewRangeDirty) {
      let rangesToRender = viewRange.subtract(this.viewRangeRendered);
      let rangesToRemove = this.viewRangeRendered.subtract(viewRange);
      let maxRange = new Range(0, this.visibleNodesFlat.length);

      rangesToRemove.forEach(range => {
        this._removeNodesInRange(range);
        if (maxRange.to < range.to) {
          this.viewRangeRendered = viewRange;
        }
      });
      rangesToRender.forEach(range => {
        this._renderNodesInRange(range);
      });
    } else {
      // expansion changed
      this.viewRangeRendered = viewRange;
      this.ensureRangeVisible(viewRange);
    }

    // check if at least last and first row in range got correctly rendered
    if (this.viewRangeRendered.size() > 0) {
      let nodes = this.visibleNodesFlat;
      let firstNode = nodes[this.viewRangeRendered.from];
      let lastNode = nodes[this.viewRangeRendered.to - 1];
      if (this.viewRangeDirty) {
        // cleanup nodes before range and after
        let $nodesBeforeFirstNode = firstNode.$node.prevAll('.tree-node');
        let $nodesAfterLastNode = lastNode.$node.nextAll('.tree-node');
        this._cleanupNodes($nodesBeforeFirstNode);
        this._cleanupNodes($nodesAfterLastNode);
      }
      if (!firstNode.attached || !lastNode.attached) {
        throw new Error('Nodes not rendered as expected. ' + this.viewRangeRendered +
          '. First: ' + graphics.debugOutput(firstNode.$node) +
          '. Last: ' + graphics.debugOutput(lastNode.$node) +
          '. Length: visibleNodesFlat=' + this.visibleNodesFlat.length + ' nodes=' + this.nodes.length + ' nodesMap=' + Object.keys(this.nodesMap).length);
      }
    }

    this._postRenderViewRange();
    this.viewRangeDirty = false;
  }

  _postRenderViewRange() {
    this._renderFiller();
    this._updateDomNodeWidth();
    this._renderSelection();
  }

  _visibleNodesInViewRange() {
    return this.visibleNodesFlat.slice(this.viewRangeRendered.from, this.viewRangeRendered.to);
  }

  _updateDomNodeWidth() {
    if (!this.isHorizontalScrollingEnabled()) {
      return;
    }
    if (!this.rendered || !this.nodeWidthDirty) {
      return;
    }
    let nodes = this._visibleNodesInViewRange();
    let maxNodeWidth = this.maxNodeWidth;
    // find max-width
    maxNodeWidth = nodes.reduce((aggr, node) => {
      return Math.max(node.width, aggr);
    }, scout.nvl(maxNodeWidth, 0));
    // set max width on all nodes
    nodes.forEach(node => {
      node.$node.cssWidth(maxNodeWidth);
    });
    this.nodeWidthDirty = false;
  }

  _cleanupNodes($nodes) {
    for (let i = 0; i < $nodes.length; i++) {
      this._removeNode($nodes.eq(i).data('node'));
    }
  }

  /**
   * Returns the index of the node which is at position scrollTop.
   */
  _nodeAtScrollTop(scrollTop) {
    let height = 0,
      nodeTop;
    this.visibleNodesFlat.some((node, i) => {
      height += this._heightForNode(node);
      if (scrollTop < height) {
        nodeTop = node;
        return true;
      }
      return false;
    });
    let visibleNodesLength = this.visibleNodesFlat.length;
    if (!nodeTop && visibleNodesLength > 0) {
      nodeTop = this.visibleNodesFlat[visibleNodesLength - 1];
    }
    return nodeTop;
  }

  _heightForNode(node) {
    let height = 0;
    if (node.height) {
      height = node.height;
    } else {
      height = this.nodeHeight;
    }
    return height;
  }

  _widthForNode(node) {
    let width = 0;
    if (node.width) {
      width = node.width;
    } else {
      width = this.nodeWidth;
    }
    return width;
  }

  /**
   * Returns a range of size this.viewRangeSize. Start of range is nodeIndex - viewRangeSize / 4.
   * -> 1/4 of the nodes are before the viewport 2/4 in the viewport 1/4 after the viewport,
   * assuming viewRangeSize is 2*number of possible nodes in the viewport (see calculateViewRangeSize).
   */
  _calculateViewRangeForNode(node) {
    let viewRange = new Range(),
      quarterRange = Math.floor(this.viewRangeSize / Tree.VIEW_RANGE_DIVISOR),
      diff;

    let nodeIndex = this.visibleNodesFlat.indexOf(node);
    viewRange.from = Math.max(nodeIndex - quarterRange, 0);
    viewRange.to = Math.min(viewRange.from + this.viewRangeSize, this.visibleNodesFlat.length);
    if (!node || nodeIndex === -1) {
      return viewRange;
    }

    // Try to use the whole viewRangeSize (extend from if necessary)
    diff = this.viewRangeSize - viewRange.size();
    if (diff > 0) {
      viewRange.from = Math.max(viewRange.to - this.viewRangeSize, 0);
    }
    return viewRange;
  }

  /**
   * Calculates the optimal view range size (number of nodes to be rendered).
   * It uses the default node height to estimate how many nodes fit in the view port.
   * The view range size is this value * 2.
   * <p>
   * Note: the value calculated by this function is important for calculating the
   * 'insertBatch'. When the value becomes smaller than 4 ({@link Tree.VIEW_RANGE_DIVISOR}) this
   * will cause errors on inserting nodes at the right position. See #262890.
   */
  calculateViewRangeSize() {
    // Make sure row height is up to date (row height may be different after zooming)
    this._updateNodeDimensions();
    if (this.nodeHeight === 0) {
      throw new Error('Cannot calculate view range with nodeHeight = 0');
    }
    let viewRangeMultiplier = Tree.VIEW_RANGE_DIVISOR / 2; // See  _calculateViewRangeForNode
    let viewRange = Math.ceil(this.$data.outerHeight() / this.nodeHeight) * viewRangeMultiplier;
    return Math.max(Tree.VIEW_RANGE_DIVISOR, viewRange);
  }

  setViewRangeSize(viewRangeSize) {
    if (this.viewRangeSize === viewRangeSize) {
      return;
    }
    this._setProperty('viewRangeSize', viewRangeSize);
    if (this.rendered) {
      this._renderViewport();
    }
  }

  _updateNodeDimensions() {
    let emptyNode = this._createTreeNode();
    let $node = this._renderNode(emptyNode).appendTo(this.$data);
    this.nodeHeight = $node.outerHeight(true);
    if (this.isHorizontalScrollingEnabled()) {
      let oldNodeWidth = this.nodeWidth;
      this.nodeWidth = $node.outerWidth(true);
      if (oldNodeWidth !== this.nodeWidth) {
        this.viewRangeDirty = true;
      }
    }
    emptyNode.reset();
  }

  /**
   * Updates the node heights for every visible node and clears the height of the others
   */
  updateNodeHeights() {
    this.visibleNodesFlat.forEach(node => {
      if (!node.attached) {
        node.height = null;
      } else {
        node.height = node.$node.outerHeight(true);
      }
    });
  }

  removeAllNodes() {
    this._removeNodes(this.nodes);
  }

  /**
   * @param parentNode
   *          Optional. If provided, this node's state will be updated (e.g. it will be collapsed
   *          if it does no longer have child nodes). Can also be an array, in which case all of
   *          those nodes are updated.
   */
  _removeNodes(nodes, parentNode) {
    if (nodes.length === 0) {
      return;
    }

    nodes.forEach(function(node) {
      this._removeFromFlatList(node, true);
      if (node.childNodes.length > 0) {
        this._removeNodes(node.childNodes, node);
      }
      if (node.$node) {
        if (this._$animationWrapper && this._$animationWrapper.find(node.$node).length > 0) {
          this._$animationWrapper.stop(false, true);
        }
        node.reset();
      }
    }, this);

    // If every child node was deleted mark node as collapsed (independent of the model state)
    // --> makes it consistent with addNodes and expand (expansion is not allowed if there are no child nodes)
    arrays.ensure(parentNode).forEach(p => {
      if (p && p.$node && p.childNodes.length === 0) {
        p.$node.removeClass('expanded lazy');
      }
    });
    if (this.rendered) {
      this.viewRangeDirty = true;
      this.invalidateLayoutTree();
    }
  }

  _renderNode(node) {
    let paddingLeft = this._computeNodePaddingLeft(node);
    node.render(this.$container, paddingLeft);
    return node.$node;
  }

  _removeMenus() {
    // menubar takes care about removal
  }

  _filterMenus(argMenus, destination, onlyVisible, enableDisableKeyStroke) {
    return menus_1.filterAccordingToSelection('Tree', this.selectedNodes.length, argMenus, destination, onlyVisible, enableDisableKeyStroke);
  }

  /**
   * @override Widget.js
   */
  _renderEnabled() {
    super._renderEnabled();

    this._installOrUninstallDragAndDropHandler();
    let enabled = this.enabledComputed;
    this.$data.setEnabled(enabled);
    this.$container.setTabbableOrFocusable(enabled);
  }

  /**
   * @override Widget.js
   */
  _renderDisabledStyle() {
    super._renderDisabledStyle();
    this._renderDisabledStyleInternal(this.$data);
  }

  setCheckable(checkable) {
    this.setProperty('checkable', checkable);
  }

  _setCheckable(checkable) {
    this._setProperty('checkable', checkable);
    this._updateNodePaddingLevel();
  }

  _updateNodePaddingLevel() {
    if (this.isBreadcrumbStyleActive()) {
      this.nodePaddingLevel = 0;
    } else if (this.checkable) {
      this.nodePaddingLevel = this.nodePaddingLevelCheckable;
    } else {
      this.nodePaddingLevel = this.nodePaddingLevelNotCheckable;
    }
  }

  setCheckableStyle(checkableStyle) {
    this.setProperty('checkableStyle', checkableStyle);
  }

  _renderCheckable() {
    // Define helper functions
    let isNodeRendered = node => Boolean(node.$node);
    let updateCheckableStateRec = node => {
      let $node = node.$node;
      let $control = $node.children('.tree-node-control');
      let $checkbox = $node.children('.tree-node-checkbox');

      node._updateControl($control);
      if (this.checkable) {
        if ($checkbox.length === 0) {
          node._renderCheckbox();
        }
      } else {
        $checkbox.remove();
      }

      $node.cssPaddingLeft(this._computeNodePaddingLeft(node));

      // Recursion
      if (node.childNodes) {
        node.childNodes.filter(isNodeRendered).forEach(updateCheckableStateRec);
      }
    };

    // Start recursion
    this.nodes.filter(isNodeRendered).forEach(updateCheckableStateRec);
  }

  _renderDisplayStyle() {
    this.$container.toggleClass('breadcrumb', this.isBreadcrumbStyleActive());
    this.nodePaddingLeft = null;
    this.nodeControlPaddingLeft = null;
    this._updateNodePaddingsLeft();
    // update scrollbar if mode has changed (from tree to bc or vice versa)
    this.invalidateLayoutTree();
  }

  _renderExpansion(node, options) {
    let opts = {
      expandLazyChanged: false,
      expansionChanged: false
    };
    $.extend(opts, options);

    let $node = node.$node,
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
  }

  _renderSelection() {
    // Add children class to root nodes if no nodes are selected
    if (this.selectedNodes.length === 0) {
      this.nodes.forEach(childNode => {
        if (childNode.rendered) {
          childNode.$node.addClass('child-of-selected');
        }
      }, this);
    }

    this.$container.toggleClass('no-nodes-selected', this.selectedNodes.length === 0);

    this.selectedNodes.forEach(function(node) {
      if (!this.visibleNodesMap[node.id]) {
        return;
      }

      // Mark all ancestor nodes, especially necessary for bread crumb mode
      let parentNode = node.parentNode;
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
        node.childNodes.forEach(childNode => {
          if (childNode.rendered) {
            childNode.$node.addClass('child-of-selected');
          }
        }, this);
      }

      if (node.rendered) {
        node.$node.select(true);
      }
    }, this);

    // Update 'group' markers for all rendered nodes
    for (let i = this.viewRangeRendered.from; i < this.viewRangeRendered.to; i++) {
      if (i >= this.visibleNodesFlat.length) {
        break;
      }
      let node = this.visibleNodesFlat[i];
      if (node && node.rendered) {
        node.$node.toggleClass('group', Boolean(this.groupedNodes[node.id]));
      }
    }

    this._updateNodePaddingsLeft();
    this._highlightPrevSelectedNode();

    if (this.scrollToSelection) {
      this.revealSelection();
    }
  }

  _renderCheckableStyle() {
    this.$data.toggleClass('checkable', this.isTreeNodeCheckEnabled());
  }

  _highlightPrevSelectedNode() {
    if (!this.isBreadcrumbStyleActive()) {
      return;
    }
    if (!this.prevSelectedNode || !this.prevSelectedNode.rendered || this.prevSelectedNode.prevSelectionAnimationDone) {
      return;
    }
    // Highlight previously selected node, but do it only once
    if (this.prevSelectedNode.$node.hasClass('animate-prev-selected')) {
      return this;
    }
    this.prevSelectedNode.$node.addClassForAnimation('animate-prev-selected').oneAnimationEnd(() => {
      this.prevSelectedNode.prevSelectionAnimationDone = true;
    });
  }

  _removeSelection() {
    // Remove children class on root nodes if no nodes were selected
    if (this.selectedNodes.length === 0) {
      this.nodes.forEach(childNode => {
        if (childNode.rendered) {
          childNode.$node.removeClass('child-of-selected');
        }
      }, this);
    }

    // Ensure animate-prev-selected class is removed (in case animation did not start)
    if (this.prevSelectedNode && this.prevSelectedNode.rendered) {
      this.prevSelectedNode.$node.removeClass('animate-prev-selected');
    }

    this.selectedNodes.forEach(this._removeNodeSelection, this);
  }

  _removeNodeSelection(node) {
    if (node.rendered) {
      node.$node.select(false);
    }

    // remove ancestor and child classes
    let parentNode = node.parentNode;
    if (parentNode && parentNode.rendered) {
      parentNode.$node.removeClass('parent-of-selected');
    }
    while (parentNode && parentNode.rendered) {
      parentNode.$node.removeClass('ancestor-of-selected');
      parentNode = parentNode.parentNode;
    }
    if (node.expanded) {
      node.childNodes.forEach(childNode => {
        if (childNode.rendered) {
          childNode.$node.removeClass('child-of-selected');
        }
      }, this);
    }
  }

  setDropType(dropType) {
    this.setProperty('dropType', dropType);
  }

  _renderDropType() {
    this._installOrUninstallDragAndDropHandler();
  }

  setDropMaximumSize(dropMaximumSize) {
    this.setProperty('dropMaximumSize', dropMaximumSize);
  }

  _installOrUninstallDragAndDropHandler() {
    dragAndDrop.installOrUninstallDragAndDropHandler(
      {
        target: this,
        doInstall: () => this.dropType && this.enabledComputed,
        selector: '.tree-data,.tree-node',
        onDrop: event => this.trigger('drop', event),
        dropType: () => this.dropType,
        additionalDropProperties: event => {
          let $target = $(event.currentTarget);
          let properties = {
            nodeId: ''
          };
          if ($target.hasClass('tree-node')) {
            let node = $target.data('node');
            properties.nodeId = node.id;
          }
          return properties;
        }
      });
  }

  _updateMarkChildrenChecked(node, init, checked, checkChildrenChecked) {
    if (!this.checkable) {
      return;
    }

    if (checkChildrenChecked) {
      let childrenFound = false;
      for (let j = 0; j < node.childNodes.length > 0; j++) {
        let childNode = node.childNodes[j];
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

    let stateChanged = false;
    if (!checked && !init) {
      // node was unchecked check siblings
      let hasCheckedSiblings = false;
      for (let i = 0; i < node.parentNode.childNodes.length > 0; i++) {
        let siblingNode = node.parentNode.childNodes[i];
        if (siblingNode.checked || siblingNode.childrenChecked) {
          hasCheckedSiblings = true;
          break;
        }
      }
      if (hasCheckedSiblings !== node.parentNode.childrenChecked) {
        // parentNode.checked should be false
        node.parentNode.childrenChecked = hasCheckedSiblings;
        stateChanged = true;
      }
    }
    if (checked && !node.parentNode.childrenChecked) {
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
  }

  _installNodeTooltipSupport() {
    tooltips.install(this.$data, {
      parent: this,
      selector: '.tree-node',
      text: this._nodeTooltipText.bind(this),
      arrowPosition: 50,
      arrowPositionUnit: '%',
      nativeTooltip: !Device.get().isCustomEllipsisTooltipPossible()
    });
  }

  _uninstallNodeTooltipSupport() {
    tooltips.uninstall(this.$data);
  }

  _nodeTooltipText($node) {
    let node = $node.data('node');
    if (node.tooltipText) {
      return node.tooltipText;
    } else if (this._isTruncatedNodeTooltipEnabled() && $node.isContentTruncated()) {
      return node.$text.text();
    }
  }

  _isTruncatedNodeTooltipEnabled() {
    return true;
  }

  setDisplayStyle(displayStyle) {
    if (this.displayStyle === displayStyle) {
      return;
    }
    this._renderViewportBlocked = true;
    this._setDisplayStyle(displayStyle);
    if (this.rendered) {
      this._renderDisplayStyle();
    }
    this._renderViewportBlocked = false;
  }

  _setDisplayStyle(displayStyle) {
    this._setProperty('displayStyle', displayStyle);

    if (this.displayStyle === Tree.DisplayStyle.BREADCRUMB) {
      if (this.selectedNodes.length > 0) {
        let selectedNode = this.selectedNodes[0];
        if (!selectedNode.expanded) {
          this.expandNode(selectedNode);
        }
      }
      this.filterAnimated = false;
      this.addFilter(this.breadcrumbFilter, false);
      this.filterVisibleNodes();
    } else {
      this.removeFilter(this.breadcrumbFilter);
      this.filterAnimated = true;
    }
    this._updateNodePaddingLevel();
  }

  _updateNodePaddingsLeft() {
    this.$nodes().each((index, element) => {
      let $node = $(element);
      let node = $node.data('node');
      let paddingLeft = this._computeNodePaddingLeft(node);
      $node.cssPaddingLeft(objects.isNullOrUndefined(paddingLeft) ? '' : paddingLeft);
      node._updateControl($node.children('.tree-node-control'));
    });
  }

  setBreadcrumbStyleActive(active) {
    if (active) {
      this.setDisplayStyle(Tree.DisplayStyle.BREADCRUMB);
    } else {
      this.setDisplayStyle(Tree.DisplayStyle.DEFAULT);
    }
  }

  isNodeInBreadcrumbVisible(node) {
    return this._inSelectionPathList[node.id] === undefined ? false : this._inSelectionPathList[node.id];
  }

  isBreadcrumbStyleActive() {
    return this.displayStyle === Tree.DisplayStyle.BREADCRUMB;
  }

  setToggleBreadcrumbStyleEnabled(enabled) {
    this.setProperty('toggleBreadcrumbStyleEnabled', enabled);
  }

  setBreadcrumbTogglingThreshold(width) {
    this.setProperty('breadcrumbTogglingThreshold', width);
  }

  expandNode(node, opts) {
    this.setNodeExpanded(node, true, opts);
  }

  collapseNode(node, opts) {
    this.setNodeExpanded(node, false, opts);
  }

  collapseAll() {
    this.rebuildSuppressed = true;
    // Collapse all expanded child nodes (only model)
    this.visitNodes(node => {
      this.collapseNode(node);
    });

    if (this.rendered) {
      // ensure correct rendering
      this._rerenderViewport();
    }

    this.rebuildSuppressed = false;
  }

  setNodeExpanded(node, expanded, opts) {
    opts = opts || {};
    let lazy = opts.lazy;
    if (objects.isNullOrUndefined(lazy)) {
      if (node.expanded === expanded) {
        // no state change: Keep the current "expandedLazy" state
        lazy = node.expandedLazy;
      } else if (expanded) {
        // collapsed -> expanded: Set the "expandedLazy" state to the node's "lazyExpandingEnabled" flag
        lazy = node.lazyExpandingEnabled;
      } else {
        // expanded -> collapsed: Set the "expandedLazy" state to false
        lazy = false;
      }
    }
    let renderAnimated = scout.nvl(opts.renderAnimated, true);

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
      let childOpts = objects.valueCopy(opts);
      childOpts.renderExpansion = false;

      node.childNodes.forEach(childNode => {
        if (childNode.expanded) {
          this.collapseNode(childNode, childOpts);
        }
      });
    }
    let renderExpansionOpts = {
      expansionChanged: false,
      expandLazyChanged: false
    };

    // Set expansion state
    if (node.expanded !== expanded || node.expandedLazy !== lazy) {
      renderExpansionOpts.expansionChanged = node.expanded !== expanded;
      renderExpansionOpts.expandLazyChanged = node.expandedLazy !== lazy;
      node.expanded = expanded;
      node.expandedLazy = lazy;

      if (renderExpansionOpts.expansionChanged) {
        if (node.parentNode) {
          // ensure node is visible under the parent node if there is a parent.
          this._rebuildParent(node.parentNode, opts);
        } else if (node.filterAccepted) {
          this._addToVisibleFlatList(node, false);
        } else {
          this._removeFromFlatList(node, false);
        }
      } else if (renderExpansionOpts.expandLazyChanged) {
        this.applyFiltersForNode(node, false, renderAnimated);
      }

      if (this.groupedNodes[node.id]) {
        this._updateItemPath(false, node);
      }

      if (node.expanded) {
        node.ensureLoadChildren().done(this._addChildrenToFlatList.bind(this, node, null, renderAnimated, null, true /* required that ctrl+shift+add expands all rows of a table-page */));
      } else {
        this._removeChildrenFromFlatList(node, renderAnimated);
      }
      this.trigger('nodeExpanded', {
        node: node,
        expanded: expanded,
        expandedLazy: lazy
      });
      this.viewRangeDirty = true;
    }

    // Render expansion
    if (this.rendered && scout.nvl(opts.renderExpansion, true)) {
      this._renderExpansion(node, renderExpansionOpts);
    }

    if (this.rendered) {
      this.ensureExpansionVisible(node);
    }
  }

  setNodeExpandedRecursive(nodes, expanded, opts) {
    Tree.visitNodes(childNode => {
      this.setNodeExpanded(childNode, expanded, opts);
    }, nodes);
  }

  _rebuildParent(node, opts) {
    if (this.rebuildSuppressed) {
      return;
    }
    if (node.expanded || node.expandedLazy) {
      this._addChildrenToFlatList(node, null, true, null, true /* required so that double clicking a table-page-row expands the clicked child row */);
    } else {
      this._removeChildrenFromFlatList(node, false);
    }
    // Render expansion
    if (this.rendered && scout.nvl(opts.renderExpansion, true)) {
      let renderExpansionOpts = {
        expansionChanged: true
      };
      this._renderExpansion(node, renderExpansionOpts);
    }
  }

  _removeChildrenFromFlatList(parentNode, animatedRemove) {
    // Only if a parent is available the children are available.
    if (this.visibleNodesMap[parentNode.id]) {
      let parentIndex = this.visibleNodesFlat.indexOf(parentNode);
      let elementsToDelete = 0;
      let parentLevel = parentNode.level;
      let removedNodes = [];
      animatedRemove = animatedRemove && this.rendered;
      if (this._$animationWrapper) {
        // Note: Do _not_ use finish() here! Although documentation states that it is "similar" to stop(true, true),
        // this does not seem to be the case. Implementations differ slightly in details. The effect is, that when
        // calling stop() the animation stops and the 'complete' callback is executed immediately. However, when calling
        // finish(), the callback is _not_ executed! (This may or may not be a bug in jQuery, I cannot tell...)
        this._$animationWrapper.stop(false, true);
      }
      this._$expandAnimationWrappers.forEach($wrapper => {
        $wrapper.stop(false, true);
      });
      for (let i = parentIndex + 1; i < this.visibleNodesFlat.length; i++) {
        if (this.visibleNodesFlat[i].level > parentLevel) {
          let node = this.visibleNodesFlat[i];
          if (this.isHorizontalScrollingEnabled()) {
            // if node is the node which defines the widest width then recalculate width for render
            if (node.width === this.maxNodeWidth) {
              this.maxNodeWidth = 0;
              this.nodeWidthDirty = true;
            }
          }
          delete this.visibleNodesMap[this.visibleNodesFlat[i].id];
          if (node.attached && animatedRemove) {
            if (!this._$animationWrapper) {
              this._$animationWrapper = $('<div class="animation-wrapper">').insertBefore(node.$node);
              this._$animationWrapper.data('parentNode', parentNode);
            }
            if (node.isDescendantOf(this._$animationWrapper.data('parentNode'))) {
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
          this._$animationWrapper.animate({
            height: 0
          }, {
            start: this.startAnimationFunc,
            complete: onAnimationComplete.bind(this, removedNodes),
            step: this.revalidateLayoutTree.bind(this),
            duration: 200,
            queue: false
          });
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

    // ----- Helper functions -----
    function onAnimationComplete(affectedNodes) {
      affectedNodes.forEach(node => {
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
  }

  _removeFromFlatList(node, animatedRemove) {
    if (this.visibleNodesMap[node.id]) {
      let index = this.visibleNodesFlat.indexOf(node);
      this._removeChildrenFromFlatList(node, false);
      if (this.isHorizontalScrollingEnabled()) {
        // if node is the node which defines the widest width then recalculate width for render
        if (node.width === this.maxNodeWidth) {
          this.maxNodeWidth = 0;
          this.nodeWidthDirty = true;
        }
      }
      this.visibleNodesFlat.splice(index, 1);
      delete this.visibleNodesMap[node.id];
      this.hideNode(node, animatedRemove);
    }
  }

  /**
   * @returns {boolean} whether or not the function added a node to the flat list
   */
  _addToVisibleFlatList(node, renderingAnimated) {
    // if node already is in visible list don't do anything. If no parentNode is available this node is on toplevel, if a parent is available
    // it has to be in visible list and also be expanded
    if (!this.visibleNodesMap[node.id] && node.filterAccepted
      && (!node.parentNode || node.parentNode.expanded && this.visibleNodesMap[node.parentNode.id])) {
      if (this.initialTraversing) {
        // for faster index calculation
        this._addToVisibleFlatListNoCheck(node, this.visibleNodesFlat.length, renderingAnimated);
      } else {
        let insertIndex = this._findInsertPositionInFlatList(node);
        this._addToVisibleFlatListNoCheck(node, insertIndex, renderingAnimated);
      }
    }
  }

  // TODO [7.0] CGU applies to all the add/remove to/from flat list methods:
  // Is it really necessary to update dom on every operation? why not just update the list and renderViewport at the end?
  // The update of the flat list is currently implemented quite complicated -> it should be simplified.
  // And: because add to flat list renders all the children the rendered node count is greater than the viewRangeSize until
  // the layout renders the viewport again -> this must not happen (can be seen when a node gets expanded)
  _addChildrenToFlatList(parentNode, parentIndex, animatedRendering, insertBatch, forceFilter) {
    // add nodes recursively
    if (!this.visibleNodesMap[parentNode.id]) {
      return 0;
    }

    let isSubAdding = Boolean(insertBatch);
    parentIndex = parentIndex ? parentIndex : this.visibleNodesFlat.indexOf(parentNode);
    animatedRendering = animatedRendering && this.rendered; // don't animate while rendering (not necessary, or may even lead to timing issues)
    if (this._$animationWrapper && !isSubAdding) {
      // Note: Do _not_ use finish() here! Although documentation states that it is "similar" to stop(true, true),
      // this does not seem to be the case. Implementations differ slightly in details. The effect is, that when
      // calling stop() the animation stops and the 'complete' callback is executed immediately. However, when calling
      // finish(), the callback is _not_ executed! (This may or may not be a bug in jQuery, I cannot tell...)
      this._$animationWrapper.stop(false, true);
    }

    if (insertBatch) {
      insertBatch.setInsertAt(parentIndex);
    } else {
      insertBatch = this.newInsertBatch(parentIndex + 1);
    }

    parentNode.childNodes.forEach((node, index) => {
      if (!node.initialized || !node.isFilterAccepted(forceFilter)) {
        return;
      }

      let insertIndex, isAlreadyAdded = this.visibleNodesMap[node.id];
      if (isAlreadyAdded) {
        this.insertBatchInVisibleNodes(insertBatch, this._showNodes(insertBatch), animatedRendering);
        // Animate rendering is always false because it would generate a bunch of animation wrappers which stay forever without really starting an animation...
        this.checkAndHandleBatchAnimationWrapper(parentNode, false, insertBatch);
        insertBatch = this.newInsertBatch(insertBatch.nextBatchInsertIndex());
        insertBatch = this._addChildrenToFlatListIfExpanded(1, node, insertIndex, animatedRendering, insertBatch, forceFilter);
      } else {
        insertBatch.insertNodes.push(node);
        this.visibleNodesMap[node.id] = true;
        insertBatch = this.checkAndHandleBatch(insertBatch, parentNode, animatedRendering);
        insertBatch = this._addChildrenToFlatListIfExpanded(0, node, insertIndex, animatedRendering, insertBatch, forceFilter);
      }
    });

    if (!isSubAdding) {
      // animation is not done yet and all added nodes are in visible range
      this.insertBatchInVisibleNodes(insertBatch, this._showNodes(insertBatch), animatedRendering);
      this.invalidateLayoutTree();
    }

    return insertBatch;
  }

  /**
   * Checks if the given node is expanded, and if that's the case determine the insert index of the node and add its children to the flat list.
   *
   * @param {number} indexOffset either 0 or 1, offset is added to the insert index
   */
  _addChildrenToFlatListIfExpanded(indexOffset, node, insertIndex, animatedRendering, insertBatch, forceFilter) {
    if (node.expanded && node.childNodes.length) {
      if (insertBatch.containsNode(node.parentNode) || insertBatch.length() > 1) {
        // if parent node is already in the batch, do not change the insertIndex,
        // only append child nodes below that parent node
        // Also, if the batch is not empty (i.e. contains more nodes than the current node),
        // the insert index was already calculated previously and must not be changed.
        insertIndex = insertBatch.insertAt();
      } else {
        insertIndex = this._findInsertPositionInFlatList(node);
      }
      insertIndex += indexOffset;
      insertBatch = this._addChildrenToFlatList(node, insertIndex, animatedRendering, insertBatch, forceFilter);
    }

    return insertBatch;
  }

  _showNodes(insertBatch) {
    return this.viewRangeRendered.from + this.viewRangeSize >= insertBatch.lastBatchInsertIndex() &&
      this.viewRangeRendered.from <= insertBatch.lastBatchInsertIndex();
  }

  /**
   * This function tries to find the correct insert position within the flat list for the given node.
   * The function must consider the order of child nodes in the original tree structure and then check
   * where in the flat list this position is.
   */
  _findInsertPositionInFlatList(node) {
    let childNodes,
      parentNode = node.parentNode;

    // use root nodes as nodes when no other parent node is available (root case)
    if (parentNode) {
      childNodes = parentNode.childNodes;
    } else {
      childNodes = this.nodes;
    }

    // find all visible siblings for our node (incl. our own node, which is probably not yet
    // in the visible nodes map)
    let thatNode = node;
    let siblings = childNodes.filter(node => {
      return Boolean(this.visibleNodesMap[node.id]) || node === thatNode;
    });

    // when there are no visible siblings, insert below the parent node
    if (siblings.length === 0) {
      return this._findPositionInFlatList(parentNode) + 1;
    }

    let nodePos = siblings.indexOf(node);

    // when there are no prev. siblings in the flat list, insert below the parent node
    if (nodePos === 0) {
      return this._findPositionInFlatList(parentNode) + 1;
    }

    let prevSiblingNode = siblings[nodePos - 1];
    let prevSiblingPos = this._findPositionInFlatList(prevSiblingNode);

    // when the prev. sibling is not in the flat list, insert below the parent node
    if (prevSiblingPos === -1) {
      return this._findPositionInFlatList(parentNode) + 1;
    }

    // find the index of the last child element of our prev. sibling node
    // that's where we want to insert the new node. We go down the flat list
    // starting from the prev. sibling node, until we hit a node that does not
    // belong to the sub tree of the prev. sibling node.
    let i, checkNode;
    for (i = prevSiblingPos; i < this.visibleNodesFlat.length; i++) {
      checkNode = this.visibleNodesFlat[i];
      if (!this._isInSameSubTree(prevSiblingNode, checkNode)) {
        return i;
      }
    }

    // insert at the end of the list
    return this.visibleNodesFlat.length;
  }

  _findPositionInFlatList(node) {
    return this.visibleNodesFlat.indexOf(node);
  }

  /**
   * Checks whether the given checkNode belongs to the same sub tree (or is) the given node.
   * The function goes up all parentNodes of the checkNode.
   *
   * @param {TreeNode} node which is used to for the sub tree comparison
   * @param {TreeNode} checkNode node which is checked against the given node
   * @returns {boolean}
   */
  _isInSameSubTree(node, checkNode) {
    do {
      if (checkNode === node || checkNode.parentNode === node) {
        return true;
      }
      checkNode = checkNode.parentNode;
    } while (checkNode);

    return false;
  }

  /**
   * Returns true if the given node is a child of one of the selected nodes.
   * The functions goes up the parent node hierarchy.
   *
   * @param {TreeNode} node to check
   * @returns {boolean}
   */
  _isChildOfSelectedNodes(node) {
    while (node) {
      if (this.selectedNodes.indexOf(node.parentNode) > -1) {
        return true;
      }
      node = node.parentNode;
    }
    return false;
  }

  /**
   * Info: the object created here is a bit weird: the array 'insertNodes' is used as function arguments to the Array#splice function at some point.
   * The signature of that function is: array.splice(index, deleteCount[, element1[,  element2 [, ...]]])
   * So the first two elements are numbers and all the following elements are TreeNodes or Pages.
   * @returns {*}
   */
  newInsertBatch(insertIndex) {
    return {
      insertNodes: [insertIndex, 0], // second element is always 0 (used as argument for deleteCount in Array#splice)
      $animationWrapper: null,
      lastBatchInsertIndex: function() {
        if (this.isEmpty()) {
          return this.insertAt();
        }
        return this.insertAt() + this.insertNodes.length - 3;
      },
      nextBatchInsertIndex: function() {
        // only NBU knows what this means
        return this.lastBatchInsertIndex() + (this.isEmpty() ? 1 : 2);
      },
      isEmpty: function() {
        return this.insertNodes.length === 2;
      },
      length: function() {
        return this.insertNodes.length - 2;
      },
      insertAt: function() {
        return this.insertNodes[0];
      },
      setInsertAt: function(insertAt) {
        this.insertNodes[0] = insertAt;
      },
      containsNode: function(node) {
        return this.insertNodes.indexOf(node) !== -1;
      }
    };
  }

  checkAndHandleBatchAnimationWrapper(parentNode, animatedRendering, insertBatch) {
    if (animatedRendering && this.viewRangeRendered.from <= insertBatch.lastBatchInsertIndex() && this.viewRangeRendered.to >= insertBatch.lastBatchInsertIndex() && !insertBatch.$animationWrapper) {
      // we are in visible area so we need a animation wrapper
      // if parent is in visible area insert after parent else insert before first node.
      let lastNodeIndex = insertBatch.lastBatchInsertIndex() - 1,
        nodeBefore = this.viewRangeRendered.from === insertBatch.lastBatchInsertIndex() ? null : this.visibleNodesFlat[lastNodeIndex];
      if (nodeBefore && lastNodeIndex >= this.viewRangeRendered.from && lastNodeIndex < this.viewRangeRendered.to && !nodeBefore.attached) {
        // ensure node before is visible
        this.showNode(nodeBefore, false, lastNodeIndex);
      }
      if (nodeBefore && nodeBefore.attached) {
        insertBatch.$animationWrapper = $('<div class="animation-wrapper">').insertAfter(nodeBefore.$node);
      } else if (parentNode.attached) {
        insertBatch.$animationWrapper = $('<div class="animation-wrapper">').insertAfter(parentNode.$node);
      } else if (this.$fillBefore) {
        insertBatch.$animationWrapper = $('<div class="animation-wrapper">').insertAfter(this.$fillBefore);
      } else {
        let nodeAfter = this.visibleNodesFlat[insertBatch.lastBatchInsertIndex()];
        insertBatch.$animationWrapper = $('<div class="animation-wrapper">').insertBefore(nodeAfter.$node);
      }
      insertBatch.animationCompleteFunc = onAnimationComplete;
      this._$expandAnimationWrappers.push(insertBatch.$animationWrapper);
    }

    // ----- Helper functions ----- //

    function onAnimationComplete() {
      insertBatch.$animationWrapper.replaceWith(insertBatch.$animationWrapper.contents());
      arrays.remove(this._$expandAnimationWrappers, insertBatch.$animationWrapper);
      insertBatch.$animationWrapper = null;
      this.runningAnimationsFinishFunc();
    }
  }

  checkAndHandleBatch(insertBatch, parentNode, animatedRendering) {
    if (this.viewRangeRendered.from - 1 === insertBatch.lastBatchInsertIndex()) {
      // do immediate rendering because list could be longer
      this.insertBatchInVisibleNodes(insertBatch, false, false);
      insertBatch = this.newInsertBatch(insertBatch.lastBatchInsertIndex() + 1);
    }
    this.checkAndHandleBatchAnimationWrapper(parentNode, animatedRendering, insertBatch);

    if (this.viewRangeRendered.from + this.viewRangeSize - 1 === insertBatch.lastBatchInsertIndex()) {
      // do immediate rendering because list could be longer
      this.insertBatchInVisibleNodes(insertBatch, true, animatedRendering);
      insertBatch = this.newInsertBatch(insertBatch.lastBatchInsertIndex() + 1);
    }
    return insertBatch;
  }

  insertBatchInVisibleNodes(insertBatch, showNodes, animate) {
    if (insertBatch.isEmpty()) {
      // nothing to add
      return;
    }
    this.visibleNodesFlat.splice(...insertBatch.insertNodes);
    if (showNodes) {
      let indexHint = insertBatch.insertAt();
      for (let i = 2; i < insertBatch.insertNodes.length; i++) {
        let node = insertBatch.insertNodes[i];
        this.showNode(node, false, indexHint);
        if (insertBatch.$animationWrapper) {
          insertBatch.$animationWrapper.append(node.$node);
        }
        indexHint++;
      }
      if (insertBatch.$animationWrapper) {
        let h = insertBatch.$animationWrapper.outerHeight();
        insertBatch.$animationWrapper
          .css('height', 0)
          .animate({
            height: h
          }, {
            start: this.startAnimationFunc,
            complete: insertBatch.animationCompleteFunc.bind(this),
            step: this.revalidateLayoutTree.bind(this),
            duration: 200,
            queue: false
          });
      }
    } else if (insertBatch.$animationWrapper && insertBatch.animationCompleteFunc) {
      insertBatch.animationCompleteFunc.call(this);
    }
  }

  _addToVisibleFlatListNoCheck(node, insertIndex, animatedRendering) {
    arrays.insert(this.visibleNodesFlat, node, insertIndex);
    this.visibleNodesMap[node.id] = true;
    this.showNode(node, animatedRendering, insertIndex);
  }

  scrollTo(node, options) {
    if (this.viewRangeRendered.size() === 0) {
      // Cannot scroll to a node if no node is rendered
      return;
    }
    if (!node.attached) {
      this._renderViewRangeForNode(node);
    }
    if (!node.attached) {
      // Node may not be visible due to the filter -> don't try to scroll because it would fail
      return;
    }
    scrollbars.scrollTo(this.$data, node.$node, options);
  }

  revealSelection() {
    if (!this.rendered) {
      // Execute delayed because tree may be not layouted yet
      this.session.layoutValidator.schedulePostValidateFunction(this.revealSelection.bind(this));
      return;
    }

    if (this.selectedNodes.length > 0) {
      if (!this.visibleNodesMap[this.selectedNodes[0].id]) {
        this._expandAllParentNodes(this.selectedNodes[0]);
      }
      this.scrollTo(this.selectedNodes[0]);
      this.ensureExpansionVisible(this.selectedNodes[0]);
    }
  }

  ensureExpansionVisible(node) {
    // only scroll if treenode is in dom and the current node is selected (user triggered expansion change)
    if (!node || !node.$node || this.selectedNodes[0] !== node) {
      return;
    }
    scrollbars.ensureExpansionVisible({
      element: node,
      $element: node.$node,
      $scrollable: this.get$Scrollable(),
      isExpanded: element => element.expanded,
      getChildren: parent => parent.childNodes,
      nodePaddingLevel: this.nodePaddingLevel,
      defaultChildHeight: this.nodeHeight
    });
  }

  deselectAll() {
    this.selectNodes([]);
  }

  selectNode(node, debounceSend) {
    this.selectNodes(node, debounceSend);
  }

  selectNodes(nodes, debounceSend) {
    nodes = arrays.ensure(nodes);

    // TODO [8.0] CGU Actually, the nodes should be filtered here so that invisible nodes may not be selected
    // But this is currently not possible because the LazyNodeFilter would not accept the nodes
    // We would have to keep track of the clicked nodes and check them in the lazy node filter (e.g. selectedNode.parentNode.lazySelectedChildNodes[selectedNode.id] = selectedNode).
    // But since this requires a change in setNodeExpanded as well we decided to not implement it until the TODO at _addChildrenToFlatList is solved

    if (arrays.equalsIgnoreOrder(nodes, this.selectedNodes)) {
      return;
    }

    if (this.rendered) {
      this._rememberScrollTopBeforeSelection();
      this._removeSelection();
    }
    if (this.prevSelectedNode) {
      this.prevSelectedNode.prevSelectionAnimationDone = false;
    }
    this.prevSelectedNode = this.selectedNodes[0];
    this._setSelectedNodes(nodes, debounceSend);
    if (this.rendered) {
      this._renderSelection();
      this._updateScrollTopAfterSelection();
    }
  }

  _rememberScrollTopBeforeSelection() {
    if (this.isBreadcrumbStyleActive()) {
      // Save the current scrollTop for future up navigation
      if (this.selectedNodes.length > 0) {
        this.scrollTopHistory[this.selectedNodes[0].level] = this.$data[0].scrollTop;
      }
    } else {
      // Clear history if user now works with tree to not get confused when returning to bc mode
      this.scrollTopHistory = [];
    }
  }

  _updateScrollTopAfterSelection() {
    if (!this.isBreadcrumbStyleActive()) {
      return;
    }
    let currentLevel = -1;
    if (this.selectedNodes.length > 0) {
      currentLevel = this.selectedNodes[0].level;
    }
    // Remove positions after the current level (no restore when going down, only when going up)
    this.scrollTopHistory.splice(currentLevel + 1);
    // Read the scroll top for the current level and use that one if it is set
    let scrollTopForLevel = this.scrollTopHistory[currentLevel];
    if (scrollTopForLevel >= 0) {
      this.setScrollTop(scrollTopForLevel);
    }
  }

  _setSelectedNodes(nodes, debounceSend) {
    // Make a copy so that original array stays untouched
    this.selectedNodes = nodes.slice();
    this._nodesSelectedInternal();
    this._triggerNodesSelected(debounceSend);

    if (this.selectedNodes.length > 0 && !this.visibleNodesMap[this.selectedNodes[0].id]) {
      this._expandAllParentNodes(this.selectedNodes[0]);
    }

    this._updateItemPath(true);
    if (this.isBreadcrumbStyleActive()) {
      // In breadcrumb mode selected node has to be expanded
      if (this.selectedNodes.length > 0 && !this.selectedNodes[0].expanded) {
        this.expandNode(this.selectedNodes[0]);
        this.selectedNodes[0].filterDirty = true;
      }
      this.filter();
    }
    this.session.onRequestsDone(this._updateMenuBar.bind(this));
  }

  /**
   * This method is overridden by subclasses of Tree. The default impl. does nothing.
   */
  _nodesSelectedInternal(node) {
    // NOP
  }

  deselectNode(node) {
    this.deselectNodes(node);
  }

  /**
   * @param nodes the nodes to deselect
   * @param options.collectChildren true to add the selected children to the list of nodes to deselect
   */
  deselectNodes(nodes, options) {
    nodes = arrays.ensure(nodes);
    options = options || {};
    if (options.collectChildren) {
      nodes = nodes.concat(this._collectNodesIfDescendants(nodes, this.selectedNodes));
    }
    let selectedNodes = this.selectedNodes.slice(); // copy
    if (arrays.removeAll(selectedNodes, nodes)) {
      this.selectNodes(selectedNodes);
    }
  }

  isNodeSelected(node) {
    return this.selectedNodes.indexOf(node) > -1;
  }

  _computeNodePaddingLeft(node) {
    this._computeNodePaddings();
    if (this.isBreadcrumbStyleActive()) {
      return this.nodePaddingLeft;
    }
    let padding = this.nodePaddingLeft + this._computeNodePaddingLeftForLevel(node);
    if (this.checkable) {
      padding += this.nodeCheckBoxPaddingLeft;
    }
    return padding;
  }

  _computeNodeControlPaddingLeft(node) {
    return this.nodeControlPaddingLeft + this._computeNodePaddingLeftForLevel(node);
  }

  _computeNodePaddingLeftForLevel(node) {
    if (this.checkable || !this.nodePaddingLevelDiffParentHasIcon) {
      return node.level * this.nodePaddingLevel;
    }
    let padding = 0;
    let parentNode = node.parentNode;
    while (parentNode) {
      padding += this.nodePaddingLevel;
      // Increase the padding if the parent node has an icon to make the hierarchy more clear
      // This is not necessary if the child nodes have icons as well, the padding even looks too big, as it is the case for checkable trees.
      // We only check the first child node for an icon because that one has the biggest impact on the hierarchy visualization. It also increases performance a little.
      if (parentNode.iconId && !parentNode.childNodes[0].iconId) {
        padding += this.nodePaddingLevelDiffParentHasIcon;
      }
      parentNode = parentNode.parentNode;
    }
    return padding;
  }

  /**
   * Reads the paddings from CSS and stores them in nodePaddingLeft and nodeControlPaddingLeft
   */
  _computeNodePaddings() {
    if (this.nodePaddingLeft !== null && this.nodeControlPaddingLeft !== null && this.nodePaddingLevelDiffParentHasIcon !== null) {
      return;
    }
    let $dummyNode = this.$data.appendDiv('tree-node');
    let $dummyNodeControl = $dummyNode.appendDiv('tree-node-control');
    if (this.nodePaddingLeft === null) {
      this.nodePaddingLeft = $dummyNode.cssPaddingLeft();
    }
    if (this.nodeControlPaddingLeft === null) {
      this.nodeControlPaddingLeft = $dummyNodeControl.cssPaddingLeft();
    }
    if (this.nodePaddingLevelDiffParentHasIcon === null) {
      this.nodePaddingLevelDiffParentHasIcon = this.$container.cssPxValue('--node-padding-level-diff-parent-has-icon');
    }
    $dummyNode.remove();
  }

  _expandAllParentNodes(node) {
    let i, currNode = node,
      parentNodes = [];

    currNode = node;
    let nodesToInsert = [];
    while (currNode.parentNode) {
      parentNodes.push(currNode.parentNode);
      if (!this.visibleNodesMap[currNode.id]) {
        nodesToInsert.push(currNode);
      }
      currNode = currNode.parentNode;
    }

    for (i = parentNodes.length - 1; i >= 0; i--) {
      if (nodesToInsert.indexOf(parentNodes[i]) !== -1) {
        this._addToVisibleFlatList(parentNodes[i], false);
      }
      if (!parentNodes[i].expanded) {
        this.expandNode(parentNodes[i], {
          renderExpansion: false,
          renderAnimated: false
        });
      }
    }
    if (this.rendered && nodesToInsert.length > 0) {
      this._rerenderViewport();
      this.invalidateLayoutTree();
    }
  }

  _updateChildNodeIndex(nodes, startIndex) {
    if (!nodes || !nodes.length) {
      return;
    }
    for (let i = scout.nvl(startIndex, 0); i < nodes.length; i++) {
      nodes[i].childNodeIndex = i;
    }
  }

  /**
   * @param {TreeNode} [parentNode]
   */
  insertNode(node, parentNode) {
    this.insertNodes([node], parentNode);
  }

  /**
   * @param {TreeNode} [parentNode]
   */
  insertNodes(nodes, parentNode) {
    nodes = arrays.ensure(nodes).slice();
    if (nodes.length === 0) {
      return;
    }
    this._ensureTreeNodes(nodes);
    if (parentNode && !(parentNode instanceof TreeNode)) {
      throw new Error('parent has to be a tree node: ' + parentNode);
    }

    // Append continuous node blocks
    nodes.sort((a, b) => {
      return a.childNodeIndex - b.childNodeIndex;
    });

    // Update parent with new child nodes
    if (parentNode) {
      if (parentNode.childNodes && parentNode.childNodes.length > 0) {
        nodes.forEach(entry => {
          // only insert node if not already existing
          if (parentNode.childNodes.indexOf(entry) < 0) {
            arrays.insert(parentNode.childNodes, entry, entry.childNodeIndex);
          }
        });
        this._updateChildNodeIndex(parentNode.childNodes, nodes[0].childNodeIndex);
      } else {
        nodes.forEach(entry => {
          parentNode.childNodes.push(entry);
        });
      }
      this._initNodes(nodes, parentNode);
      if (this.groupedNodes[parentNode.id]) {
        this._updateItemPath(false, parentNode);
      }
      if (this.rendered) {
        let opts = {
          expansionChanged: true
        };
        this._renderExpansion(parentNode, opts);
        this.ensureExpansionVisible(parentNode);
      }
    } else {
      if (this.nodes && this.nodes.length > 0) {
        nodes.forEach(entry => {
          // only insert node if not already existing
          if (this.nodes.indexOf(entry) < 0) {
            arrays.insert(this.nodes, entry, entry.childNodeIndex);
          }
        });
        this._updateChildNodeIndex(this.nodes, nodes[0].childNodeIndex);
      } else {
        arrays.pushAll(this.nodes, nodes);
      }
      this._initNodes(nodes, parentNode);
    }
    if (this.rendered) {
      this.viewRangeDirty = true;
      this.invalidateLayoutTree();
    }
    this.trigger('nodesInserted', {
      nodes: nodes,
      parentNode: parentNode
    });
  }

  updateNode(node) {
    this.updateNodes([node]);
  }

  updateNodes(nodes) {
    nodes = arrays.ensure(nodes);
    if (nodes.length === 0) {
      return;
    }
    nodes.forEach(function(updatedNode) {
      let propertiesChanged,
        oldNode = this.nodesMap[updatedNode.id];

      // if same instance has been updated we must set the flag always to true
      // because we cannot compare against an "old" node
      if (updatedNode === oldNode) {
        propertiesChanged = true;
      } else {
        this._applyNodeDefaultValues(updatedNode);
        propertiesChanged = this._applyUpdatedNodeProperties(oldNode, updatedNode);
      }

      if (propertiesChanged) {
        this.applyFiltersForNode(oldNode);
        this._updateItemPath(false, oldNode.parentNode);
        if (this.rendered) {
          oldNode._decorate();
        }
      }
    }, this);

    this.trigger('nodesUpdated', {
      nodes: nodes
    });
  }

  /**
   * Called by _onNodesUpdated for every updated node. The function is expected to apply
   * all updated properties from the updatedNode to the oldNode. May be overridden by
   * subclasses so update their specific node properties.
   *
   * @param oldNode
   *          The target node to be updated
   * @param updatedNode
   *          The new node with potentially updated properties. Default values are already applied!
   * @returns {boolean}
   *          true if at least one property has changed, false otherwise. This value is used to
   *          determine if the node has to be rendered again.
   */
  _applyUpdatedNodeProperties(oldNode, updatedNode) {
    // Note: We only update _some_ of the properties, because everything else will be handled
    // with separate events. --> See also: JsonTree.java/handleModelNodesUpdated()
    let propertiesChanged = false;
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
      // Also make sure expandedLazy is reset to false when lazyExpanding is disabled (same code as in AbstractTreeNode.setLazyExpandingEnabled)
      if (!updatedNode.lazyExpandingEnabled || !this.lazyExpandingEnabled) {
        oldNode.expandedLazy = false;
      }
      propertiesChanged = true;
    }
    return propertiesChanged;
  }

  deleteNode(node, parentNode) {
    this.deleteNodes([node], parentNode);
  }

  deleteAllNodes() {
    this.deleteAllChildNodes();
  }

  deleteNodes(nodes, parentNode) {
    let deletedNodes = [];
    let parentNodesToReindex = [];
    let topLevelNodesToReindex = [];
    nodes = arrays.ensure(nodes).slice(); // copy
    if (nodes.length === 0) {
      return;
    }

    nodes.forEach(function(node) {
      let p = parentNode || node.parentNode;
      if (p) {
        if (node.parentNode !== p) {
          throw new Error('Unexpected parent. Node.parent: ' + node.parentNode + ', parentNode: ' + parentNode);
        }
        arrays.remove(p.childNodes, node);
        if (parentNodesToReindex.indexOf(p) === -1) {
          parentNodesToReindex.push(p);
        }
      } else {
        arrays.remove(this.nodes, node);
        topLevelNodesToReindex = this.nodes;
      }
      this._destroyTreeNode(node);
      deletedNodes.push(node);
      this._updateMarkChildrenChecked(node, false, false);

      // remove children from node map
      Tree.visitNodes(this._destroyTreeNode.bind(this), node.childNodes);
    }, this);

    // update child node indices
    parentNodesToReindex.forEach(p => this._updateChildNodeIndex(p.childNodes));
    this._updateChildNodeIndex(topLevelNodesToReindex);

    this.deselectNodes(deletedNodes, {collectChildren: true});
    this.uncheckNodes(deletedNodes, {collectChildren: true});

    // remove node from html document
    if (this.rendered) {
      this._removeNodes(deletedNodes, parentNode || parentNodesToReindex);
    }

    this.trigger('nodesDeleted', {
      nodes: nodes,
      parentNode: parentNode
    });
  }

  _collectNodesIfDescendants(nodes, nodesToCheck) {
    let result = [];
    nodesToCheck.forEach(nodeToCheck => {
      if (nodes.some(node => {
        return node.isAncestorOf(nodeToCheck);
      })) {
        result.push(nodeToCheck);
      }
    });
    return result;
  }

  deleteAllChildNodes(parentNode) {
    let nodes;
    if (parentNode) {
      nodes = parentNode.childNodes;
      parentNode.childNodes = [];
    } else {
      nodes = this.nodes;
      this.nodes = [];
    }
    Tree.visitNodes(updateNodeMap.bind(this), nodes);

    this.deselectNodes(nodes, {collectChildren: true});
    this.uncheckNodes(nodes, {collectChildren: true});

    // remove node from html document
    if (this.rendered) {
      this._removeNodes(nodes, parentNode);
    }

    this.trigger('allChildNodesDeleted', {
      parentNode: parentNode
    });

    // --- Helper functions ---

    // Update model and nodemap
    function updateNodeMap(node) {
      this._destroyTreeNode(node);
      this._updateMarkChildrenChecked(node, false, false);
    }
  }

  updateNodeOrder(childNodes, parentNode) {
    childNodes = arrays.ensure(childNodes);

    this._updateChildNodeIndex(childNodes);
    if (parentNode) {
      if (parentNode.childNodes.length !== childNodes.length) {
        throw new Error('Node order may not be updated because lengths of the arrays differ.');
      }
      // Make a copy so that original array stays untouched
      parentNode.childNodes = childNodes.slice();
      this._removeChildrenFromFlatList(parentNode, false);
      if (parentNode.expanded) {
        this._addChildrenToFlatList(parentNode, null, false);
      }
    } else {
      if (this.nodes.length !== childNodes.length) {
        throw new Error('Node order may not be updated because lengths of the arrays differ.');
      }
      // Make a copy so that original array stays untouched
      this.nodes = childNodes.slice();
      this.nodes.forEach(function(node) {
        this._removeFromFlatList(node, false);
        this._addToVisibleFlatList(node, false);
        if (node.expanded) {
          this._addChildrenToFlatList(node, null, false);
        }
      }, this);
    }

    this.trigger('childNodeOrderChanged', {
      parentNode: parentNode
    });
  }

  checkNode(node, checked, options) {
    let opts = $.extend(options, {
      checked: checked
    });
    this.checkNodes([node], opts);
  }

  checkNodes(nodes, options) {
    let opts = {
      checked: true,
      checkOnlyEnabled: true,
      checkChildren: this.autoCheckChildren,
      triggerNodesChecked: true
    };
    $.extend(opts, options);
    let updatedNodes = [];
    // use enabled computed because when the parent of the table is disabled, it should not be allowed to check rows
    if (!this.checkable || !this.enabledComputed && opts.checkOnlyEnabled) {
      return;
    }
    nodes = arrays.ensure(nodes);
    nodes.forEach(function(node) {
      if (!node.enabled && opts.checkOnlyEnabled || node.checked === opts.checked || !node.filterAccepted) {
        if (opts.checkChildren) {
          this.checkNodes(node.childNodes, opts);
        }
        return;
      }
      if (!this.multiCheck && opts.checked) {
        for (let i = 0; i < this.checkedNodes.length; i++) {
          this.checkedNodes[i].checked = false;
          this._updateMarkChildrenChecked(this.checkedNodes[i], false, false, true);
          updatedNodes.push(this.checkedNodes[i]);
        }
        this.checkedNodes = [];
      }
      node.checked = opts.checked;
      if (node.checked) {
        this.checkedNodes.push(node);
      } else {
        arrays.remove(this.checkedNodes, node);
      }
      updatedNodes.push(node);
      this._updateMarkChildrenChecked(node, false, opts.checked, true);
      if (opts.checkChildren) {
        let childOpts = $.extend({}, opts, {
          triggerNodesChecked: false
        });
        this.checkNodes(node.childNodes, childOpts);
      }
    }, this);

    if (opts.triggerNodesChecked && updatedNodes.length > 0) {
      this.trigger('nodesChecked', {
        nodes: updatedNodes
      });
    }
    if (this.rendered) {
      updatedNodes.forEach(node => {
        node._renderChecked();
      });
    }
  }

  uncheckNode(node, options) {
    let opts = $.extend({
      checkOnlyEnabled: true
    }, options);
    this.uncheckNodes([node], opts);
  }

  /**
   * @param nodes the nodes to uncheck
   * @param options.collectChildren true to add the checked children to the list of nodes to uncheck
   */
  uncheckNodes(nodes, options) {
    let opts = {
      checked: false
    };
    $.extend(opts, options);
    if (opts.collectChildren) {
      nodes = nodes.concat(this._collectNodesIfDescendants(nodes, this.checkedNodes));
    }
    this.checkNodes(nodes, opts);
  }

  _triggerNodesSelected(debounce) {
    this.trigger('nodesSelected', {
      debounce: debounce
    });
  }

  _showContextMenu(event) {
    let func = function(event) {
      if (!this.rendered) { // check needed because function is called asynchronously
        return;
      }
      let filteredMenus = this._filterMenus(this.menus, MenuDestinations.CONTEXT_MENU, true),
        $part = $(event.currentTarget);
      if (filteredMenus.length === 0) {
        return; // at least one menu item must be visible
      }
      // Prevent firing of 'onClose'-handler during contextMenu.open()
      // (Can lead to null-access when adding a new handler to this.contextMenu)
      if (this.contextMenu) {
        this.contextMenu.close();
      }
      this.contextMenu = scout.create('ContextMenuPopup', {
        parent: this,
        menuItems: filteredMenus,
        location: {
          x: event.pageX,
          y: event.pageY
        },
        $anchor: $part,
        menuFilter: this._filterMenusHandler
      });
      this.contextMenu.open();
    };

    this.session.onRequestsDone(func.bind(this), event);
  }

  _onNodeMouseDown(event) {
    this._doubleClickSupport.mousedown(event);
    if (this._doubleClickSupport.doubleClicked()) {
      // don't execute on double click events
      return false;
    }

    let $node = $(event.currentTarget);
    let node = $node.data('node');
    if (!this.hasNode(node)) {
      // if node does not belong to this tree, do nothing (may happen if another tree is embedded inside the node)
      return;
    }
    this._$mouseDownNode = $node;
    $node.window().one('mouseup', () => {
      this._$mouseDownNode = null;
    });

    this.selectNodes(node);

    if (this.checkable && node.enabled && this._isCheckboxClicked(event)) {
      if (Device.get().loosesFocusIfPseudoElementIsRemoved()) {
        this.focusAndPreventDefault(event);
      }
      this.checkNode(node, !node.checked);
    }
    return true;
  }

  _onNodeMouseUp(event) {
    if (this._doubleClickSupport.doubleClicked()) {
      // don't execute on double click events
      return false;
    }

    let $node = $(event.currentTarget);
    let node = $node.data('node');
    if (!this._$mouseDownNode || this._$mouseDownNode[0] !== $node[0]) {
      // Don't accept if mouse up happens on another node than mouse down, or mousedown didn't happen on a node at all
      return;
    }

    this.trigger('nodeClick', {
      node: node
    });
    return true;
  }

  _isCheckboxClicked(event) {
    // with CheckableStyle.CHECKBOX_TREE_NODE a click anywhere on the node should trigger the check
    if (this.isTreeNodeCheckEnabled()) {
      return true;
    }
    return $(event.target).is('.check-box');
  }

  _updateItemPath(selectionChanged, ultimate) {
    let selectedNodes, node, level;
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
          node.childNodes.forEach(child => {
            this._inSelectionPathList[child.id] = true;
          });
        }
      }
      level = node.level;

      // find grouping end (ultimate parent)
      while (node.parentNode) {
        let parent = node.parentNode;
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

    // ------ helper function ------//

    function addToGroup(nodes) {
      nodes.forEach(node => {
        this.groupedNodes[node.id] = true;
        node._decorate();
        if (node.expanded && node.isFilterAccepted()) {
          addToGroup.call(this, node.childNodes);
        }
      });
    }
  }

  _isGroupingEnd(node) {
    // May be implemented by subclasses, default tree has no grouping parent
    return false;
  }

  /**
   * @returns {TreeNode} the first selected node or null when no node is selected.
   */
  selectedNode() {
    if (this.selectedNodes.length === 0) {
      return null;
    }
    return this.selectedNodes[0];
  }

  $selectedNodes() {
    return this.$data.find('.selected');
  }

  $nodes() {
    return this.$data.find('.tree-node');
  }

  /**
   * @param {Filter|function|(Filter|function)[]} filter The filters to add.
   * @param {boolean} applyFilter Whether to apply the filters after modifying the filter list or not. Default is true.
   */
  addFilter(filter, applyFilter = true) {
    this.filterSupport.addFilter(filter, applyFilter);
  }

  /**
   * @param {Filter|function|(Filter|function)[]} filter The filters to remove.
   * @param {boolean} applyFilter Whether to apply the filters after modifying the filter list or not. Default is true.
   */
  removeFilter(filter, applyFilter = true) {
    this.filterSupport.removeFilter(filter, applyFilter);
  }

  /**
   * @param {Filter|function|(Filter|function)[]} filter The new filters.
   * @param {boolean} applyFilter Whether to apply the filters after modifying the filter list or not. Default is true.
   */
  setFilters(filters, applyFilter = true) {
    this.filterSupport.setFilters(filters, applyFilter);
  }

  filter() {
    this.filterSupport.filter();
  }

  _filter() {
    let newlyHidden = [],
      newlyShown = [];
    // Filter nodes
    this.nodes.forEach(node => {
      let result = this.applyFiltersForNode(node, false, this.filterAnimated);
      newlyHidden.push(...result.newlyHidden);
      newlyShown.push(...result.newlyShown);
    });
    return {
      newlyHidden: newlyHidden,
      newlyShown: newlyShown
    };
  }

  updateFilteredElements(result, opts) {
    if (!this.filteredElementsDirty) {
      return;
    }
    if (opts.textFilterText) {
      this._nodesByIds(Object.keys(this.nodesMap))
        .filter(it => it.filterAccepted)
        .forEach(node => this._expandAllParentNodes(node));
    }
    result.newlyShown.forEach(node => this._addToVisibleFlatList(node, this.filterAnimated));
    result.newlyHidden.forEach(node => this._removeFromFlatList(node, this.filterAnimated));
    this.filteredElementsDirty = false;
  }

  filterVisibleNodes(animated) {
    // Filter nodes
    let newlyHidden = [];
    // iterate from end to beginning (child nodes first) so that the state of the children has already been updated
    for (let i = this.visibleNodesFlat.length - 1; i >= 0; i--) {
      let node = this.visibleNodesFlat[i];
      let result = this._applyFiltersForNodeRec(node, true, animated);
      if (result.newlyHidden.length) {
        if (!node.isFilterAccepted()) {
          newlyHidden.push(...result.newlyHidden);
        }
        this.viewRangeDirty = true;
      }
    }
    newlyHidden.forEach(h => this._removeFromFlatList(h, animated));
    this._nodesFiltered(newlyHidden);
  }

  _nodesFiltered(hiddenNodes) {
    // non visible nodes must be deselected
    this.deselectNodes(hiddenNodes);
  }

  applyFiltersForNode(node, applyNewHiddenShownNodes = true, animated = false) {
    let result = this._applyFiltersForNodeRec(node, true, animated);

    // the result so far only includes the node and all its children.
    // always include the parent nodes as well so that the filter has an effect
    let parent = node.parentNode;
    while (parent) {
      let parentResult = this._applyFiltersForNodeRec(parent, false, animated);
      result.newlyHidden.unshift(...parentResult.newlyHidden);
      result.newlyShown.unshift(...parentResult.newlyShown);
      parent = parent.parentNode;
    }
    this._nodesFiltered(result.newlyHidden);

    if (applyNewHiddenShownNodes) {
      result.newlyShown.forEach(node => this._addToVisibleFlatList(node, animated));
      result.newlyHidden.forEach(node => this._removeFromFlatList(node, animated));
    }
    return result;
  }

  _applyFiltersForNodeRec(node, recursive, animated = false) {
    let newlyHidden = [], newlyShown = [];
    animated = animated && this.filterAnimated;

    let changed = this._applyFiltersForNode(node);
    let hasChildrenWithFilterAccepted = false;
    if (node.level < 32 /* see org.eclipse.scout.rt.client.ui.basic.tree.AbstractTree.expandAllRec */) {
      if (recursive) {
        node.childNodes.forEach(childNode => {
          let result = this._applyFiltersForNodeRec(childNode, true, animated);
          newlyHidden.push(...result.newlyHidden);
          newlyShown.push(...result.newlyShown);
          hasChildrenWithFilterAccepted = hasChildrenWithFilterAccepted || childNode.filterAccepted;
        });
      } else if (!node.filterAccepted) {
        // Check children only if filterAccepted is false because only then hasChildrenWithFilterAccepted is used (see below).
        // This has great impact on performance when there are many nodes
        hasChildrenWithFilterAccepted = node.childNodes.some(childNode => childNode.filterAccepted);
      }
    }

    // set filter accepted on this node if it has children with filter accepted (so that the children are visible)
    if (!node.filterAccepted && hasChildrenWithFilterAccepted) {
      node.setFilterAccepted(true);
      changed = !changed;
    }

    // remember changed node
    if (changed) {
      let pushTo = node.filterAccepted ? newlyShown : newlyHidden;
      pushTo.unshift(node);

      if (this.rendered) {
        this.viewRangeDirty = true;
      }
    }

    return {newlyHidden: newlyHidden, newlyShown: newlyShown};
  }

  /**
   * @returns {Boolean} true if node state has changed, false if not
   */
  _applyFiltersForNode(node) {
    let changed = this.filterSupport.applyFiltersForElement(node);
    if (changed || node.filterDirty) {
      node.filterDirty = false;
      node.childNodes.forEach(childNode => {
        childNode.filterDirty = true;
      });
    }
    return changed;
  }

  /**
   * @returns {FilterSupport}
   */
  _createFilterSupport() {
    return new FilterSupport({
      widget: this,
      $container: () => this.$container,
      filterElements: this._filter.bind(this),
      getElementText: node => node.text
    });
  }

  setTextFilterEnabled(textFilterEnabled) {
    this.setProperty('textFilterEnabled', textFilterEnabled);
  }

  isTextFilterFieldVisible() {
    return this.textFilterEnabled;
  }

  _renderTextFilterEnabled() {
    this.filterSupport.renderFilterField();
  }

  /**
   * Just insert node in DOM. NO check if in viewRange
   */
  _insertNodesInDOM(nodes, indexHint) {
    if (!this.rendered && !this.rendering) {
      return;
    }
    nodes = nodes.filter(function(node) {
      let index = indexHint === undefined ? this.visibleNodesFlat.indexOf(node) : indexHint;
      if (index === -1 || !(this.viewRangeRendered.from + this.viewRangeSize >= index && this.viewRangeRendered.from <= index && this.viewRangeSize > 0) || node.attached) {
        // node is not visible
        return false;
      }
      if (!node.rendered) {
        this._renderNode(node);
      }
      node._decorate();
      this._insertNodeInDOMAtPlace(node, index);
      if (this.prevSelectedNode === node) {
        this._highlightPrevSelectedNode();
      }
      node.rendered = true;
      node.attached = true;
      return true;
    }, this);
    this._installNodes(nodes);
  }

  _installNodes(nodes) {
    // The measuring is separated into 3 blocks for performance reasons -> separates reading and setting of styles
    // 1. Prepare style for measuring
    if (this.isHorizontalScrollingEnabled()) {
      nodes.forEach(node => {
        node.$node.css('width', 'auto');
        node.$node.css('display', 'inline-block');
      }, this);
    }

    // 2. Measure
    nodes.forEach(function(node) {
      node.height = node.$node.outerHeight(true);
      if (!this.isHorizontalScrollingEnabled()) {
        return;
      }
      let newWidth = node.$node.outerWidth();
      let oldWidth = node.width ? node.width : 0;
      if (oldWidth === this.maxNodeWidth && newWidth < this.maxNodeWidth) {
        this.maxNodeWidth = 0;
        this.nodeWidthDirty = true;
      } else if (newWidth > this.maxNodeWidth) {
        this.maxNodeWidth = newWidth;
        this.nodeWidthDirty = true;
      } else if (newWidth === oldWidth && newWidth === 0) {
        // newWidth and oldWidth are 0: this might be because the tree is invisible while a node is added:
        // Mark as dirty to update the width later during layouting (when the tree gets visible and the width is available)
        this.nodeWidthDirty = true;
      }
      node.width = newWidth;
    }, this);

    // 3. Reset style
    if (this.isHorizontalScrollingEnabled()) {
      nodes.forEach(function(node) {
        if (!this.nodeWidthDirty) {
          node.$node.css('width', this.maxNodeWidth);
        }
        node.$node.css('display', '');
      }, this);
    }
  }

  /**
   * Attaches node to DOM, if it is visible and in view range
   * */
  _ensureNodeInDOM(node, useAnimation, indexHint) {
    if (node && !node.attached && node === this.visibleNodesFlat[indexHint] && indexHint >= this.viewRangeRendered.from && indexHint < this.viewRangeRendered.to) {
      this.showNode(node, useAnimation, indexHint);
    }
  }

  _insertNodeInDOMAtPlace(node, index) {
    let $node = node.$node;

    if (index === 0) {
      if (this.$fillBefore) {
        $node.insertAfter(this.$fillBefore);
      } else {
        this.$data.prepend($node);
      }
      return;
    }

    // append after index
    let nodeBefore = this.visibleNodesFlat[index - 1];
    this._ensureNodeInDOM(nodeBefore, false, index - 1);
    if (nodeBefore.attached) {
      $node.insertAfter(nodeBefore.$node);
      return;
    }

    if (index + 1 < this.visibleNodesFlat.length) {
      let nodeAfter = this.visibleNodesFlat[index + 1];
      if (nodeAfter.attached) {
        $node.insertBefore(nodeAfter.$node);
        return;
      }
    }

    // used when the tree is scrolled
    if (this.$fillBefore) {
      $node.insertAfter(this.$fillBefore);
    } else {
      this.$data.prepend($node);
    }
  }

  showNode(node, useAnimation, indexHint) {
    if (!this.rendered || (node.attached && !node.$node.hasClass('hiding'))) {
      return;
    }
    if (!node.attached) {
      this._ensureNodeInDOM(node.parentNode, useAnimation, indexHint - 1);
      this._insertNodesInDOM([node], indexHint);
    }
    if (!node.rendered) {
      return;
    }
    let $node = node.$node;
    if ($node.is('.showing')) {
      return;
    }
    $node.removeClass('hiding');
    let that = this;
    if (useAnimation) {
      $node.addClass('showing');
      // hide node first and then make it appear using slideDown (setVisible(false) won't work because it would stay invisible during the animation)
      $node.hide();
      $node.stop(false, true).slideDown({
        duration: 250,
        start: that.startAnimationFunc,
        complete: () => {
          that.runningAnimationsFinishFunc();
          $node.removeClass('showing');
        }
      });
    }
  }

  hideNode(node, useAnimation, suppressDetachHandling) {
    if (!this.rendered || !node.attached) {
      return;
    }
    this.viewRangeDirty = true;
    let that = this,
      $node = node.$node;
    if (!$node) {
      // node is not rendered
      return;
    }

    if ($node.is('.hiding')) {
      return;
    }
    $node.removeClass('showing');
    if (useAnimation) {
      $node.addClass('hiding');
      this._renderViewportBlocked = true;
      $node.stop(false, true).slideUp({
        duration: 250,
        start: that.startAnimationFunc,
        complete: () => {
          that.runningAnimationsFinishFunc();
          $node.removeClass('hiding');
          if (!$node.hasClass('showing')) {
            // JQuery sets display to none which we don't need because node will be detached.
            // If node is added using another method than slideDown (used by show node), it would be invisible.
            // Example: parent is collapsed while nodes are hiding -> remove filter, expand parent -> invisible nodes
            $node.css('display', '');
            $node.detach();
            node.attached = false;
          }
        }
      });
    } else if (!suppressDetachHandling) {
      $node.detach();
      node.attached = false;
      that.invalidateLayoutTree();
    }
  }

  _nodesToIds(nodes) {
    return nodes.map(node => node.id);
  }

  _nodesByIds(ids) {
    return ids.map(id => this.nodesMap[id]);
  }

  _nodeById(id) {
    return this.nodesMap[id];
  }

  /**
   * Checks whether the given node is contained in the tree. Uses the id of the node for the lookup.
   */
  hasNode(node) {
    return Boolean(this._nodeById(node.id));
  }

  _onNodeDoubleClick(event) {
    if (this.isBreadcrumbStyleActive()) {
      return;
    }

    let $node = $(event.currentTarget);
    let node = $node.data('node');
    let expanded = !$node.hasClass('expanded');
    this.doNodeAction(node, expanded);
  }

  doNodeAction(node, expanded) {
    this.trigger('nodeAction', {
      node: node
    });

    // For CheckableStyle.CHECKBOX_TREE_NODE expand on double click is only enabled for disabled nodes. Otherwise it would conflict with the "check on node click" behavior.
    if (!(this.checkable === true && this.isTreeNodeCheckEnabled() && node.enabled)) {
      this.setNodeExpanded(node, expanded, {
        lazy: false // always show all nodes on node double click
      });
    }
  }

  _onNodeControlMouseDown(event) {
    this._doubleClickSupport.mousedown(event);
    if (this._doubleClickSupport.doubleClicked()) {
      // don't execute on double click events
      return false;
    }

    let $node = $(event.currentTarget).parent();
    let node = $node.data('node');
    let expanded = !$node.hasClass('expanded');
    let expansionOpts = {
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
    // Because we suppress handling by browser we have to set focus manually
    if (this.requestFocusOnNodeControlMouseDown) {
      this.focus();
    }
    this.selectNodes(node); // <---- ### 1
    this.setNodeExpanded(node, expanded, expansionOpts); // <---- ### 2
    // prevent bubbling to _onNodeMouseDown()
    $.suppressEvent(event);

    // ...but return true, so Outline.js can override this method and check if selection has been changed or not
    return true;
  }

  _onNodeControlMouseUp(event) {
    // prevent bubbling to _onNodeMouseUp()
    return false;
  }

  _onNodeControlDoubleClick(event) {
    // prevent bubbling to _onNodeDoubleClick()
    return false;
  }

  _onContextMenu(event) {
    event.preventDefault();
    this._showContextMenu(event);
  }

  changeNode(node) {
    this.applyFiltersForNode(node);
    if (this.rendered) {
      node._decorate();
      // The padding size of a node depends on whether the node or the parent node has an icon, see _computeNodePaddingLeftForLevel
      // Unfortunately, we cannot easily detect whether the icon has changed or not.
      // However, the padding calculation only needs to be done if the node that toggles the icon is visible and expanded or has an expanded parent.
      let paddingDirty = !!this.nodePaddingLevelDiffParentHasIcon && !!this.visibleNodesMap[node.id] && (node.expanded || !!node.parentNode);
      if (paddingDirty && !this._changeNodeTaskScheduled) {
        // Because the change node event is not batch capable, performance would slow down if many change node events are processed
        // To mitigate this, the updating is done later
        queueMicrotask(() => {
          this._updateNodePaddingsLeft();
          this._changeNodeTaskScheduled = false;
        });
        this._changeNodeTaskScheduled = true;
      }
    }
    this.trigger('nodeChanged', {
      node: node
    });
  }

  // same as on Table.prototype._onDesktopPopupOpen
  _onDesktopPopupOpen(event) {
    let popup = event.popup;
    if (!this.isFocusable(false)) {
      return;
    }
    // Set tree style to focused if a context menu or a menu bar popup opens, so that it looks as it still has the focus
    if (this.has(popup) && popup instanceof ContextMenuPopup) {
      this.$container.addClass('focused');
      popup.one('destroy', () => {
        if (this.rendered) {
          this.$container.removeClass('focused');
        }
      });
    }
  }

  updateScrollbars() {
    scrollbars.update(this.$data);
  }

  /* --- STATIC HELPERS ------------------------------------------------------------- */

  /**
   * @memberOf Tree
   */
  static collectSubtree($rootNode, includeRootNodeInResult) {
    if (!$rootNode) {
      return $();
    }
    let rootLevel = parseFloat($rootNode.attr('data-level'));
    // Find first node after the root element that has the same or a lower level
    let $nextNode = $rootNode.next();
    while ($nextNode.length > 0) {
      let level = parseFloat($nextNode.attr('data-level'));
      if (isNaN(level) || level <= rootLevel) {
        break;
      }
      $nextNode = $nextNode.next();
    }

    // The result set consists of all nodes between the root node and the found node
    let $result = $rootNode.nextUntil($nextNode);
    if (includeRootNodeInResult === undefined || includeRootNodeInResult) {
      $result = $result.add($rootNode);
    }
    return $result;
  }

  /**
   * pre-order (top-down) traversal of the tree-nodes provided.<br>
   * if func returns true the children of the visited node are not visited.
   */
  static visitNodes(func, nodes, parentNode) {
    let i, node;
    if (!nodes) {
      return;
    }

    for (i = 0; i < nodes.length; i++) {
      node = nodes[i];
      let doNotProcessChildren = func(node, parentNode);
      if (!doNotProcessChildren && node.childNodes.length > 0) {
        Tree.visitNodes(func, node.childNodes, node);
      }
    }
  }
}
