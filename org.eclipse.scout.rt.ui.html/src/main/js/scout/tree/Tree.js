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
  this._animationNodeLimit = 25;
  this._keyStrokeSupport = new scout.KeyStrokeSupport(this);
  this._doubleClickSupport = new scout.DoubleClickSupport();
  this._$animationWrapper; // used by _renderExpansion()
  this._filterMenusHandler = this._filterMenus.bind(this);
};
scout.inherits(scout.Tree, scout.ModelAdapter);

scout.Tree.DisplayStyle = {
  DEFAULT: 'default',
  BREADCRUMB: 'breadcrumb'
};

scout.Tree.prototype._init = function(model) {
  scout.Tree.parent.prototype._init.call(this, model);
  this._visitNodes(this.nodes, this._initTreeNode.bind(this));
  this.selectedNodes = this._nodesByIds(this.selectedNodes);
  this.menuBar = scout.create('MenuBar', {
    parent: this,
    menuOrder: new scout.MenuItemsOrder(this.session, 'Tree'),
    menuFilter: this._filterMenusHandler
  });
  this.menuBar.bottom();
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

scout.Tree.prototype._syncMenus = function(newMenus, oldMenus) {
  this._keyStrokeSupport.syncMenus(newMenus, oldMenus);
  this._updateMenuBar();
};

scout.Tree.prototype._updateMenuBar = function() {
  var menuItems = this._filterMenus(this.menus, scout.MenuDestinations.MENU_BAR, false, true);
  this.menuBar.setMenuItems(menuItems);
};

scout.Tree.prototype._syncKeyStrokes = function(newKeyStrokes, oldKeyStrokes) {
  this._keyStrokeSupport.syncKeyStrokes(newKeyStrokes, oldKeyStrokes);
};

scout.Tree.prototype._syncDisplayStyle = function(newValue) {
  this.setDisplayStyle(newValue, false);
};

scout.Tree.prototype._initTreeNode = function(node, parentNode) {
  this.nodesMap[node.id] = node;
  if (parentNode) {
    node.parentNode = parentNode;
  }
  if (node.checked) {
    this.checkedNodes.push(node);
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
  scout.arrays.remove(this.selectedNodes, node); // ensure deleted node is not in selection list anymore (in case the model does not update the selection)

  if (this._onNodeDeleted) { // Necessary for subclasses
    this._onNodeDeleted(node);
  }
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
    .on('contextmenu', this._onContextMenu.bind(this))
    .on('mousedown', '.tree-node', this._onNodeMouseDown.bind(this))
    .on('mouseup', '.tree-node', this._onNodeMouseUp.bind(this))
    .on('dblclick', '.tree-node', this._onNodeDoubleClick.bind(this))
    .on('mousedown', '.tree-node-control', this._onNodeControlMouseDown.bind(this))
    .on('mouseup', '.tree-node-control', this._onNodeControlMouseUp.bind(this))
    .on('dblclick', '.tree-node-control', this._onNodeControlDoubleClick.bind(this));

  scout.scrollbars.install(this.$data, {
    parent: this,
    axis: 'y'
  });
  this._installNodeTooltipSupport();
  this.menuBar.render(this.$container);
  this._addNodes(this.nodes);
};

scout.Tree.prototype._postRender = function() {
  this._renderSelection();
};

scout.Tree.prototype._remove = function() {
  // Detach nodes from jQuery objects (because those will be removed)
  this.nodes.forEach(function(node) {
    delete node.$node;
  });
  scout.scrollbars.uninstall(this.$data, this.session);
  this._uninstallNodeTooltipSupport();
  scout.Tree.parent.prototype._remove.call(this);
};

scout.Tree.prototype._renderProperties = function() {
  scout.Tree.parent.prototype._renderProperties.call(this);
  this._renderEnabled();
  this._renderMenus();
  this._renderDisplayStyle();
  this._renderDropType();
};

scout.Tree.prototype._addNodes = function(nodes, $parent, $predecessor) {
  if (!nodes || nodes.length === 0) {
    return;
  }

  $predecessor = $predecessor || $parent;
  var parentNode = ($parent ? $parent.data('node') : null);

  for (var i = 0; i < nodes.length; i++) {
    var node = nodes[i];
    var $node = this._$buildNode(node, $parent);

    // If parent node wants its children to be lazy added to the tree, hide the DOM element, except
    // the node is expanded, in which case we never hide it. (The last check provides a cheap
    // way to retain most of the state when the page is reloaded).
    if (!node.expanded && parentNode && parentNode.expandedLazy) {
      $node.addClass('hidden');
    }
    // append first node and successors
    if ($predecessor) {
      $node.insertAfter($predecessor);
    } else {
      $node.prependTo(this.$data);
    }

    // if model demands children, create them
    if (node.expanded && node.childNodes.length > 0) {
      $predecessor = this._addNodes(node.childNodes, $node);
    } else {
      $predecessor = $node;
    }
  }

  this.invalidateLayoutTree();

  //return the last created node
  return $predecessor;
};

/**
 * @param parentNode optional. If provided, this node's state will be updated (e.g. it will be collapsed)
 */
scout.Tree.prototype._removeNodes = function(nodes, parentNode) {
  if (nodes.length === 0) {
    return;
  }

  nodes.forEach(function(node) {
    if (node.childNodes.length > 0) {
      this._removeNodes(node.childNodes, node);
    }
    if (node.$node) {
      node.$node.remove();
      delete node.$node;
    }
  }, this);

  //If every child node was deleted mark node as collapsed (independent of the model state)
  //--> makes it consistent with addNodes and expand (expansion is not allowed if there are no child nodes)
  var $parentNode = (parentNode ? parentNode.$node : undefined);
  if ($parentNode) {
    if (!parentNode) {
      parentNode = $parentNode.data('node');
    }
    var childNodesOfParent = parentNode.childNodes;
    if (!childNodesOfParent || childNodesOfParent.length === 0) {
      $parentNode.removeClass('expanded');
      $parentNode.removeClass('lazy');
    }
  }

  this.invalidateLayoutTree();
};

scout.Tree.prototype._$buildNode = function(node, $parent) {
  var level = $parent ? parseFloat($parent.attr('data-level')) + 1 : 0;

  var $node = this.$container.makeDiv('tree-node')
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
  if ($node.hasClass('group')) {
    formerClasses += ' group';
  }
  $node.removeClass();
  $node.addClass(formerClasses);
  $node.addClass(node.cssClass);
  $node.toggleClass('leaf', !!node.leaf);
  $node.toggleClass('expanded', (!!node.expanded && node.childNodes.length > 0));
  $node.toggleClass('lazy', $node.hasClass('expanded') && node.expandedLazy);
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

  // Replace only the "text part" of the node, leave control and checkbox untouched
  var preservedChildren = $node.children('.tree-node-control,.tree-node-checkbox').detach();
  $node.empty();
  if (node.htmlEnabled) {
    $node.html(node.text);
  } else {
    $node.textOrNbsp(node.text);
  }
  $node.prepend(preservedChildren);

  scout.styles.legacyStyle(node, $node);

  // apply node filter
  if (this._applyFiltersForNode(node)) {
    var newHiddenNodes = [];
    if (!node.filterAccepted) {
      newHiddenNodes.push(node);
    }
    this._nodesFiltered(newHiddenNodes);
  }
  this._renderNodeFilterAccepted(node);

  // TODO [5.2] bsh: More attributes...
  // iconId

  // If parent node is marked as 'lazy', check if any hidden child nodes remain.
  if (node.parentNode && node.parentNode.$node.hasClass('lazy')) {
    var hasHiddenNodes = node.parentNode.childNodes.some(function(childNode) {
      if (!childNode.$node || childNode.$node.hasClass('hidden')) {
        return true;
      }
      return false;
    });
    if (!hasHiddenNodes) {
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

scout.Tree.prototype._renderNodeFilterAccepted = function(node, animated) {
  if (node.filterAccepted) {
    this.showNode(node.$node, animated);
  } else {
    this.hideNode(node.$node, animated);
  }
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

scout.Tree.prototype._renderExpansion = function(node, $predecessor, animate) {
  animate = scout.nvl(animate, true);

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

  // Render lazy expansion
  if (expanded && $node.hasClass('expanded') && !node.expandedLazy && $node.hasClass('lazy')) {
    // If node is already expanded but only lazy -> expand completely
    this._showAllNodes(node);
  }
  $node.toggleClass('lazy', expanded && node.expandedLazy);

  if (expanded === $node.hasClass('expanded')) {
    // Expansion state has not changed -> return
    return;
  }

  // If there is already an animation is already going on for this node, stop it immediately
  if (this._$animationWrapper) {
    // Note: Do _not_ use finish() here! Although documentation states that it is "similar" to stop(true, true),
    // this does not seem to be the case. Implementations differ slightly in details. The effect is, that when
    // calling stop() the animation stops and the 'complete' callback is executed immediately. However, when calling
    // finish(), the callback is _not_ executed! (This may or may not be a bug in jQuery, I cannot tell...)
    this._$animationWrapper.stop(false, true);
  }

  if (expanded) {
    this._addNodes(node.childNodes, $node, $predecessor);
    this._updateItemPath();

    // animate opening
    animate = animate && this.rendered; // don't animate while rendering (not necessary, or may even lead to timing issues)
    animate = animate && !$node.hasClass('leaf') && !$node.hasClass('expanded') && !this.displayStyle;
    if (animate) {
      var $newNodes = scout.Tree.collectSubtree($node, false);
      if ($newNodes.length) {
        this._$animationWrapper = $newNodes.wrapAll('<div class="animation-wrapper">').parent();
        var h = this._$animationWrapper.outerHeight();
        this._$animationWrapper
          .css('height', 0)
          .animateAVCSD('height', h, onAnimationComplete.bind(this, true), this.revalidateLayoutTree.bind(this), 200);
      }
    }
    $node.addClass('expanded');
  } else {
    $node.removeClass('expanded');

    // animate closing
    if (this.rendered) { // don't animate while rendering (not necessary, or may even lead to timing issues)
      var $existingNodes = scout.Tree.collectSubtree($node, false);
      if ($existingNodes.length) {
        $existingNodes.each(function() {
          // unlink '$nodes' from 'nodes' before deleting them
          var node = $(this).data('node');
          if (node) { // FIXME bsh: Tree | This if should not be necessary! 'node' should not be undefined, but is sometimes... Check why!
            delete node.$node;
          }
        });
        if (animate) {
          this._$animationWrapper = $existingNodes.wrapAll('<div class="animation-wrapper">)').parent();
          this._$animationWrapper
            .animateAVCSD('height', 0, onAnimationComplete.bind(this, false), this.revalidateLayoutTree.bind(this), 200);
        } else {
          $existingNodes.remove();
          this.invalidateLayoutTree();
        }
      }
    }
  }

  // ----- Helper functions -----

  function onAnimationComplete(expanding) {
    if (expanding) {
      this._$animationWrapper.replaceWith(this._$animationWrapper.contents());
    } else {
      this._$animationWrapper.remove();
    }
    this._$animationWrapper = null;
  }
};

scout.Tree.prototype._renderSelection = function() {
  // Add children class to root nodes if no nodes are selected
  if (this.selectedNodes.length === 0) {
    this.nodes.forEach(function(childNode) {
      childNode.$node.addClass('child-of-selected');
    }, this);
  }

  this.selectedNodes.forEach(function(node) {
    var $node = node.$node;

    // If $node is currently not displayed (due to a collapsed parent node), expand the parents
    if (!$node) {
      this._expandAllParentNodes(node);
      $node = node.$node;
      if (!$node || $node.length === 0) {
        throw new Error('Still no node found. node=' + node);
      }
    }

    $node.select(true);

    // If node was previously hidden, show it!
    $node.removeClass('hidden');

    // Mark all ancestor nodes, especially necessary for bread crumb mode
    var parentNode = node.parentNode;
    if (parentNode) {
      parentNode.$node.addClass('parent-of-selected');
    }
    while (parentNode) {
      parentNode.$node.addClass('ancestor-of-selected');
      parentNode = parentNode.parentNode;
    }

    // Mark all child nodes
    if (node.expanded) {
      node.childNodes.forEach(function(childNode) {
        childNode.$node.addClass('child-of-selected');
      }, this);
    }
  }, this);

  this._updateItemPath();
  if (this.scrollToSelection) {
    // Execute delayed because tree may be not layouted yet
    setTimeout(this.revealSelection.bind(this));
  }
};

scout.Tree.prototype._removeSelection = function() {
  // Remove children class on root nodes if no nodes were selected
  if (this.selectedNodes.length === 0) {
    this.nodes.forEach(function(childNode) {
      childNode.$node.removeClass('child-of-selected');
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
          childNode.$node.removeClass('child-of-selected');
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
    return $node.text();
  }
};

scout.Tree.prototype._isTruncatedNodeTooltipEnabled = function() {
  return true;
};

scout.Tree.prototype.setDisplayStyle = function(displayStyle, notifyServer) {
  if (this.displayStyle === displayStyle) {
    return;
  }

  this.displayStyle = displayStyle;
  notifyServer = scout.nvl(notifyServer, true);
  if (notifyServer) {
    this._sendDisplayStyleChange();
  }

  if (displayStyle && this.selectedNodes.length > 0) {
    var selectedNode = this.selectedNodes[0];
    if (!selectedNode.expanded) {
      this.expandNode(selectedNode);
    }
  }

  if (this.rendered) {
    this._renderDisplayStyle();
  }
};

scout.Tree.prototype.setBreadcrumbStyleActive = function(active, notifyServer) {
  if (active) {
    this.setDisplayStyle(scout.Tree.DisplayStyle.BREADCRUMB, notifyServer);
  } else {
    this.setDisplayStyle(scout.Tree.DisplayStyle.DEFAULT, notifyServer);
  }
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
  var that = this;

  // Collapse root nodes
  this.$data.find('[data-level="0"]').each(function() {
    var $node = $(this);
    that.collapseNode($node.data('node'));
  });

  // Collapse all expanded child nodes (only model)
  this._visitNodes(this.nodes, function(node) {
    this.collapseNode(node);
  }.bind(this));
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

  // Set expansion state
  if (node.expanded !== expanded || node.expandedLazy !== lazy) {
    node.expanded = expanded;
    node.expandedLazy = lazy;

    if (notifyServer) {
      this._send('nodeExpanded', {
        nodeId: node.id,
        expanded: expanded,
        expandedLazy: lazy
      });
    }
  }

  // Render expansion
  if (this.rendered && scout.nvl(opts.renderExpansion, true)) {
    this._renderExpansion(node, null, opts.animateExpansion);
  }
};

scout.Tree.prototype.scrollTo = function(node) {
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

  // In breadcrumb mode selected node has to expanded
  if (this.isBreadcrumbStyleActive() && this.selectedNodes.length > 0 && !this.selectedNodes[0].expanded) {
    this.expandNode(this.selectedNodes[0]);
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
      this.expandNode(parentNodes[i], {
        renderExpansion: true
      }); // force render expansion
    }
  }
};

scout.Tree.prototype._updateChildNodeIndex = function(nodes, startIndex) {
  for (var i = scout.nvl(startIndex, 0); i < nodes.length; i++) {
    nodes[i].childNodeIndex = i;
  }
};

scout.Tree.prototype.insertNodes = function(nodes, parentNode) {
  var $parentNode, $predecessor;

  // Append continuous node blocks
  nodes.sort(function(a, b) {
    return a.childNodeIndex - b.childNodeIndex;
  });
  this._visitNodes(nodes, this._initTreeNode.bind(this), parentNode);

  // Update parent with new child nodes
  if (parentNode) {
    if (parentNode.childNodes && parentNode.childNodes.length > 0) {
      nodes.forEach(function(entry) {
        scout.arrays.insert(parentNode.childNodes, entry, entry.childNodeIndex);
      }.bind(this));
      this._updateChildNodeIndex(parentNode.childNodes, nodes[0].childNodeIndex);
    } else {
      scout.arrays.pushAll(parentNode.childNodes, nodes);
    }

    if (this.rendered && parentNode.$node) {
      $parentNode = parentNode.$node;
      if (nodes[0].childNodeIndex === 0) {
        $predecessor = $parentNode;
      } else {
        $predecessor = calcPredecessor(parentNode.childNodes[nodes[0].childNodeIndex - 1]);
      }
      if (parentNode.expanded) {
        // If parent is already expanded just add the nodes at the end.
        // Otherwise render the expansion
        if ($parentNode.hasClass('expanded')) {
          this._addNodes(nodes, $parentNode, $predecessor);
          this._updateItemPath();
        } else {
          this._renderExpansion(parentNode, $predecessor);
        }
      }
    }
  } else {
    if (this.nodes && this.nodes.length > 0) {
      nodes.forEach(function(entry) {
        scout.arrays.insert(this.nodes, entry, entry.childNodeIndex);
      }.bind(this));
      this._updateChildNodeIndex(this.nodes, nodes[0].childNodeIndex);

      if (nodes[0].childNodeIndex !== 0) {
        $predecessor = calcPredecessor(this.nodes[nodes[0].childNodeIndex - 1]);
      }
    } else {
      scout.arrays.pushAll(this.nodes, nodes);
    }

    if (this.rendered) {
      this._addNodes(nodes, undefined, $predecessor);
      this._updateItemPath();
    }
  }

  this.trigger('nodesInserted', {
    nodes: nodes,
    parentNode: parentNode
  });

  // ----- Helper functions -----

  function calcPredecessor(nodeBefore) {
    if (!nodeBefore) {
      return undefined;
    }
    if (nodeBefore && (!nodeBefore.childNodes || nodeBefore.childNodes.length === 0) && nodeBefore.$node) {
      return nodeBefore.$node;
    }
    var $node = calcPredecessor(nodeBefore.childNodes[nodeBefore.childNodes.length - 1]);
    if ($node) {
      return $node;
    } else if (nodeBefore.$node) {
      return nodeBefore.$node;
    }
    return undefined;
  }
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

    if (this.rendered && propertiesChanged) {
      this._decorateNode(oldNode);
    }
  }

  if (this.rendered && anyPropertiesChanged) {
    this._updateItemPath();
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
    oldNode.expandedLazy = updatedNode.lazyExpandingEnabled;
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
  if (!this.enabled || (!this.checkable && opts.checkOnlyEnabled)) {
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

scout.Tree.prototype._sendDisplayStyleChange = function() {
  this._send('displayStyle', {
    displayStyle: this.displayStyle
  });
};

scout.Tree.prototype._triggerNodesSelected = function(debounce) {
  this.trigger('nodesSelected', {debounce: debounce});
};

scout.Tree.prototype._showContextMenu = function(event) {
  var func = function func(event) {
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
    popup.open(null, event);

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

scout.Tree.prototype._showAllNodes = function(parentNode) {
  var updateFunc = function() {
    this.revalidateLayoutTree();
    this.revealSelection();
  }.bind(this);

  var completeFunc = function() {
    // "this" will be bound to the $node that completed the animation
    var oldStyle = this.data('oldStyle');
    if (oldStyle) {
      this.removeData('oldStyle');
      this.attrOrRemove('style', oldStyle);
    }
  };

  // Show all nodes for this parent
  for (var i = 0; i < parentNode.childNodes.length; i++) {
    var childNode = parentNode.childNodes[i];

    // skip if already visible
    if (childNode.$node.css('display') !== 'none') {
      continue;
    }

    childNode.$node.removeClass('hidden');

    // only animate small trees
    if (parentNode.childNodes.length < 100) {
      var h = childNode.$node.outerHeight(),
        p = childNode.$node.css('padding-top');

      // Backup the current value of the 'style' attribute, so we can remove the animated
      // properties again when the animation is complete (otherwise, the values might
      // interfere with CSS definitions, e.g. in breadcrumb mode)
      childNode.$node.data('oldStyle', childNode.$node.attr('style'));

      // make height 0
      childNode.$node
        .outerHeight(0)
        .css('padding-top', '0')
        .css('padding-bottom', '0');

      // animate to original height
      childNode.$node
        .animateAVCSD('padding-top', p, null, null, 200)
        .animateAVCSD('padding-bottom', p, null, null, 200)
        .animateAVCSD('height', h, null, updateFunc, 200)
        .promise().done(completeFunc);
    }

    // only first animated element should handle scrollbar and visibility of selection
    updateFunc = null;
  }

  // without animation
  this.revalidateLayoutTree();
  this.revealSelection();
};

scout.Tree.prototype._updateItemPath = function() {
  var $selectedNodes, $node, level;

  // first remove and select selected
  this.$data.find('.tree-node').removeClass('group');

  // find direct children
  $selectedNodes = this.$selectedNodes();
  $node = $selectedNodes.next();
  level = parseFloat($selectedNodes.attr('data-level'));

  // find grouping end (ultimate parent)
  var $ultimate;
  if ($selectedNodes.parent().hasClass('animation-wrapper')) {
    //If node expansion animation is in progress, the nodes are wrapped by a div
    $node = $selectedNodes.parent().prev();
  } else {
    $node = $selectedNodes.prev();
  }
  while ($node.length > 0) {
    var k = parseFloat($node.attr('data-level'));
    if (k < level) {
      if (this._isGroupingEnd($node.data('node'))) {
        break;
      }

      level = k;
      $ultimate = $node;
    }
    if ($node.parent().hasClass('animation-wrapper')) {
      $node = $node.parent();
    }
    $node = $node.prev();
  }

  // find group with same ultimate parent
  $ultimate = $ultimate || $selectedNodes;
  $node = $ultimate;
  level = $node.attr('data-level');
  while ($node.length > 0) {
    $node.addClass('group');
    if ($node.next().length === 0 && $node.parent().hasClass('animation-wrapper')) {
      // If there is no next node but we are inside an animationWrapper, step out the wrapper
      $node = $node.parent();
    }
    $node = $node.next();
    if ($node.hasClass('animation-wrapper')) {
      $node = $node.children().first();
    }

    var m = parseFloat($node.attr('data-level'));
    if (m <= level) {
      break;
    }
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
scout.Tree.prototype.addFilter = function(filter) {
  if (this._filters.indexOf(filter) < 0) {
    this._filters.push(filter);
  }
};

scout.Tree.prototype.removeFilter = function(filter) {
  scout.arrays.remove(this._filters, filter);
};

scout.Tree.prototype.filter = function() {
  var useAnimation = false,
    changedNodes = [],
    newHiddenNodes = [];

  // Filter nodes
  this._visitNodes(this.nodes, function(node) {
    if (!node.$node) {
      // filter may only be called for rendered nodes because the filter may want to use the actual html content
      // if a node is collapsed, it is not rendered -> filter is called when node gets expanded
      //TODO CGU make filter not depend on $node -> filter must not be called in decorate rows, but in init, insert, update, delete instead (as done in table)
      return;
    }

    var changed = this._applyFiltersForNode(node);
    if (changed) {
      changedNodes.push(node);
      if (!node.filterAccepted) {
        newHiddenNodes.push(node);
      }
    }
  }.bind(this));

  if (changedNodes.length === 0) {
    return;
  }

  // Show / hide rows that changed their state during filtering
  if (this.rendered) {
    useAnimation = changedNodes.length <= this._animationNodeLimit;
    changedNodes.forEach(function(node) {
      this._renderNodeFilterAccepted(node, useAnimation);
    }, this);
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
  if (this._nodeAcceptedByFilters(node)) {
    if (!node.filterAccepted) {
      node.filterAccepted = true;
      return true;
    }
  } else {
    if (node.filterAccepted) {
      node.filterAccepted = false;
      return true;
    }
  }
  return false;
};

scout.Tree.prototype.showNode = function($node, useAnimation) {
  var that = this;
  if ($node.isVisible() && !$node.is(':animated')) {
    return;
  }

  if (useAnimation) {
    $node.removeClass('hidden');
    $node.stop().slideDown({
      duration: 250,
      complete: function() {
        that.invalidateLayoutTree();
      }
    });
  } else {
    $node.showFast();
    $node.removeClass('hidden');
    that.invalidateLayoutTree();
  }
};

scout.Tree.prototype.hideNode = function($node, useAnimation) {
  var that = this;
  if (!$node.isVisible() && !$node.is(':animated')) {
    return;
  }

  if (useAnimation) {
    $node.stop().slideUp({
      duration: 250,
      complete: function() {
        $node.addClass('hidden');
        that.invalidateLayoutTree();
      }
    });
  } else {
    $node.hideFast();
    $node.addClass('hidden');
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

  // Render sorted nodes
  if (this.rendered && $lastChildNode) {
    // Find the last affected node DIV
    $lastChildNode = scout.Tree.collectSubtree($lastChildNode).last();

    if (this._$animationWrapper) {
      // Stop the animation wrapper
      this._$animationWrapper.stop(false, true);
    }

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
