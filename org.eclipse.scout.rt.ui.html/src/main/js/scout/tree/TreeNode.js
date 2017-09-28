/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
/**
 * @class
 */
scout.TreeNode = function(tree) {
  this.$node; // TODO [7.0] awe, cgu: properties without assignment do not exist after ctor call, use better initial values everywhere?
  this.$text = null;
  this.attached = false;
  this.checked = false;
  this.childNodes = [];
  this.childrenLoaded = false;
  this.enabled = true;
  this.expanded = false;
  this.expandedLazy = false;
  this.filterAccepted = true;
  this.filterDirty;
  this.id;
  this.initialized;
  this.lazyExpandingEnabled = false;
  this.leaf = false;
  this.level = 0;
  this.parentNode;
  this.destroyed = false;
  this.rendered = false;
  this.text;

  /**
   * This internal variable stores the promise which is used when a loadChildren() operation is in progress.
   */
  this._loadChildrenPromise = false;
};

scout.TreeNode.prototype.init = function(model) {
  this._init(model);
  scout.texts.resolveTextProperty(this, 'text', this.parent.session);
};

scout.TreeNode.prototype.destroy = function() {
  if (this.destroyed) {
    // Already destroyed, do nothing
    return;
  }
  this._destroy();
  this.destroyed = true;
};

/**
 * Override this method to do something when TreeNode gets destroyed. The default impl. does nothing.
 */
scout.TreeNode.prototype._destroy = function() {
  // NOP
};

scout.TreeNode.prototype.getTree = function() {
  return this.parent;
};

scout.TreeNode.prototype._init = function(model) {
  scout.assertParameter('parent', model.parent, scout.Tree);
  this.session = model.session || model.parent.session;

  $.extend(this, model);
  scout.defaultValues.applyTo(this);

  // make sure all child nodes are TreeNodes too
  if (this.hasChildNodes()) {
    this.getTree()._ensureTreeNodes(this.childNodes);
  }
};

scout.TreeNode.prototype.hasChildNodes = function() {
  return this.childNodes.length > 0;
};

scout.TreeNode.prototype.reset = function() {
  if (this.$node) {
    this.$node.remove();
    delete this.$node;
  }
  this.rendered = false;
  this.attached = false;
};

/**
 * Check if node is in hierarchy of a parent. is used on removal from flat list.
 */
scout.TreeNode.prototype.isChildOf = function(parentNode) {
  if (parentNode === this.parentNode) {
    return true;
  } else if (!this.parentNode) {
    return false;
  }
  return this.parentNode.isChildOf(parentNode);
};

scout.TreeNode.prototype.isFilterAccepted = function(forceFilter) {
  if (this.filterDirty || forceFilter) {
    this.getTree()._applyFiltersForNode(this);
  }
  return this.filterAccepted;
};

/**
 * This method loads the child nodes of this node and returns a jQuery.Deferred to register callbacks
 * when loading is done or has failed. This method should only be called when childrenLoaded is false.
 *
 * @return {$.Deferred} or null when TreeNode cannot load children (which is the case for all
 *     TreeNodes in the remote case). The default impl. return null.
 */
scout.TreeNode.prototype.loadChildren = function() {
  return $.resolvedDeferred();
};

/**
 * This method calls loadChildren() but does nothing when children are already loaded or when loadChildren()
 * is already in progress.
 * @returns {Promise}
 */
scout.TreeNode.prototype.ensureLoadChildren = function() {
  // when children are already loaded we return an already resolved promise so the caller can continue immediately
  if (this.childrenLoaded) {
    return $.resolvedPromise();
  }
  // when load children is already in progress, we return the same promise
  if (this._loadChildrenPromise) {
    return this._loadChildrenPromise;
  }
  var deferred = this.loadChildren();
  var promise = deferred.promise();
  if (deferred.state() === 'resolved') { // TODO [7.0] awe: better solution as this deferred mess -> create own deferred here?
    this._loadChildrenPromise = null;
    return promise;
  }

  this._loadChildrenPromise = promise;
  promise.done(this._onLoadChildrenDone.bind(this));
  return promise; // we must always return a promise, never null - otherwise caller would throw an error
};

scout.TreeNode.prototype._onLoadChildrenDone = function() {
  this._loadChildrenPromise = null;
};

scout.TreeNode.prototype.setText = function(text) {
  this.text = text;
};

/**
 * This functions renders sets the $node and $text properties.
 *
 * @param {jQuery} $parent the tree DOM
 * @param {number} paddingLeft calculated by tree
 */
scout.TreeNode.prototype.render = function($parent, paddingLeft) {
  this.$node = $parent.makeDiv('tree-node')
    .data('node', this)
    .attr('data-nodeid', this.id)
    .attr('data-level', this.level)
    .css('padding-left', paddingLeft);
  this.$text = this.$node.appendSpan('text');

  this._renderControl();
  if (this.getTree().checkable) {
    this._renderCheckbox();
  }
  this._renderText();
  this._renderIcon();
};

scout.TreeNode.prototype._renderText = function() {
  if (this.htmlEnabled) {
    this.$text.html(this.text);
  } else {
    this.$text.textOrNbsp(this.text);
  }
};

scout.TreeNode.prototype._renderChecked = function() {
  // if node is not rendered, do nothing
  if (!this.rendered) {
    return;
  }

  this.$node
    .children('.tree-node-checkbox')
    .children('.check-box')
    .toggleClass('checked', this.checked);
};

scout.TreeNode.prototype._renderIcon = function() {
  if (this.getTree().session.showTreeIcons) {
    this.$node.icon(this.iconId, function($icon) {
      $icon.insertBefore(this.$text);
    }.bind(this));
  }
};

scout.TreeNode.prototype.$icon = function() {
  return this.$node.children('.icon');
};

scout.TreeNode.prototype._renderControl = function() {
  var $control = this.$node.prependDiv('tree-node-control');
  if (this.getTree().checkable) {
    $control.addClass('checkable');
  }
  $control.setVisible(!this.leaf);
};

scout.TreeNode.prototype._renderCheckbox = function() {
  var $checkboxContainer = this.$node.prependDiv('tree-node-checkbox');
  var $checkbox = $checkboxContainer
    .appendDiv('check-box')
    .toggleClass('checked', this.checked)
    .toggleClass('disabled', !(this.getTree().enabled && this.enabled));
  $checkbox.toggleClass('children-checked', !!this.childrenChecked);
};

scout.TreeNode.prototype._decorate = function() {
  // This node is not yet rendered, nothing to do
  if (!this.$node) {
    return;
  }

  var $node = this.$node,
    tree = this.getTree();

  $node.attr('class', this._preserveCssClasses($node));
  $node.addClass(this.cssClass);
  $node.toggleClass('leaf', !!this.leaf);
  $node.toggleClass('expanded', (!!this.expanded && this.childNodes.length > 0));
  $node.toggleClass('lazy', $node.hasClass('expanded') && this.expandedLazy);
  $node.toggleClass('group', !!tree.groupedNodes[this.id]);
  $node.setEnabled(!!this.enabled);
  $node.children('.tree-node-control').setVisible(!this.leaf);
  $node
    .children('.tree-node-checkbox')
    .children('.check-box')
    .toggleClass('disabled', !(tree.enabled && this.enabled));

  if (!this.parentNode && tree.selectedNodes.length === 0 || // root nodes have class child-of-selected if no node is selected
    tree._isChildOfSelectedNodes(this)) {
    $node.addClass('child-of-selected');
  }

  this._renderText();
  this._renderIcon();
  scout.styles.legacyStyle(this, $node);

  // If parent node is marked as 'lazy', check if any visible child nodes remain.
  if (this.parentNode && this.parentNode.expandedLazy) {
    var hasVisibleNodes = this.parentNode.childNodes.some(function(childNode) {
      if (tree.visibleNodesMap[childNode.id]) {
        return true;
      }
    }.bind(this));
    if (!hasVisibleNodes && this.parentNode.$node) {
      // Remove 'lazy' from parent
      this.parentNode.$node.removeClass('lazy');
    }
  }
};

/**
 * This function extracts all CSS classes that are set externally by the tree.
 * The classes depend on the tree hierarchy or the selection and thus cannot determined
 * by the node itself.
 */
scout.TreeNode.prototype._preserveCssClasses = function($node) {
  var cssClass = 'tree-node';
  if ($node.isSelected()) {
    cssClass += ' selected';
  }
  if ($node.hasClass('ancestor-of-selected')) {
    cssClass += ' ancestor-of-selected';
  }
  if ($node.hasClass('parent-of-selected')) {
    cssClass += ' parent-of-selected';
  }
  return cssClass;
};

scout.TreeNode.prototype._updateIconWidth = function() {
  var cssWidth = '';
  if (this.iconId) {
    // always add 1 pixel to the result of outer-width to prevent rendering errors in IE, where
    // the complete text is replaced by an ellipsis, when the .text element is a bit too large
    cssWidth = 'calc(100% - '+ (this.$icon().outerWidth() + 1) + 'px)';
  }
  this.$text.css('max-width', cssWidth);
};
