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
scout.TreeNode = function() {
  this.$node; // FIXME [awe] 6.1 discuss with CGU... properties without assignment do not exist after ctor call
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
  this.level;
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
 * @return jQuery.Deferred or null when TreeNode cannot load children (which is the case for all
 *     TreeNodes in the remote case). The default impl. return null.
 */
scout.TreeNode.prototype.loadChildren = function() {
  return $.resolvedDeferred();
};

/**
 * This method calls loadChildren() but does nothing when children are already loaded or when loadChildren()
 * is already in progress.
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
  if (deferred.state() === 'resolved') { // FIXME [awe] 6.1 - better solution as this deferred mess -> create own deferred here?
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
