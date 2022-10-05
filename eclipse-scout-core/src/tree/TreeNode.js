/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {defaultValues, icons, objects, scout, styles, texts, Tree} from '../index';
import $ from 'jquery';

/**
 * @class
 */
export default class TreeNode {

  constructor() {
    this.$node = null;
    this.$text = null;
    this.attached = false;
    this.checked = false;
    this.childNodes = [];
    this.childrenLoaded = false;
    this.childrenChecked = false;
    this.cssClass = null;
    this.destroyed = false;
    this.enabled = true;
    this.expanded = false;
    this.expandedLazy = false;
    this.filterAccepted = true;
    this.filterDirty = false;
    this.htmlEnabled = false;
    this.iconId = null;
    this.id = null;
    this.initialized = false;
    this.initialExpanded = false;
    this.lazyExpandingEnabled = false;
    this.leaf = false;
    this.level = 0;
    this.parent = null;
    this.parentNode = undefined;
    this.prevSelectionAnimationDone = false;
    this.rendered = false;
    this.session = null;
    this.text = null;

    /**
     * This internal variable stores the promise which is used when a loadChildren() operation is in progress.
     */
    this._loadChildrenPromise = false;
  }

  init(model) {
    let staticModel = this._jsonModel();
    if (staticModel) {
      model = $.extend({}, staticModel, model);
    }
    this._init(model);
    if (model.initialExpanded === undefined) {
      this.initialExpanded = this.expanded;
    }
  }

  destroy() {
    if (this.destroyed) {
      // Already destroyed, do nothing
      return;
    }
    this._destroy();
    this.destroyed = true;
  }

  /**
   * Override this method to do something when TreeNode gets destroyed. The default impl. does nothing.
   */
  _destroy() {
    // NOP
  }

  getTree() {
    return this.parent;
  }

  _init(model) {
    scout.assertParameter('parent', model.parent, Tree);
    this.session = model.session || model.parent.session;

    $.extend(this, model);
    defaultValues.applyTo(this);

    texts.resolveTextProperty(this, 'text');
    icons.resolveIconProperty(this, 'iconId');

    // make sure all child nodes are TreeNodes too
    if (this.hasChildNodes()) {
      this.getTree()._ensureTreeNodes(this.childNodes);
    }
  }

  _jsonModel() {
  }

  reset() {
    if (this.$node) {
      this.$node.remove();
      this.$node = null;
    }
    this.rendered = false;
    this.attached = false;
  }

  hasChildNodes() {
    return this.childNodes.length > 0;
  }

  /**
   * @returns {boolean} true, if the node is an ancestor of the given node
   */
  isAncestorOf(node) {
    while (node) {
      if (node.parentNode === this) {
        return true;
      }
      node = node.parentNode;
    }
    return false;
  }

  /**
   * @returns {boolean} true, if the node is a descendant of the given node
   */
  isDescendantOf(node) {
    if (node === this.parentNode) {
      return true;
    }
    if (!this.parentNode) {
      return false;
    }
    return this.parentNode.isDescendantOf(node);
  }

  setFilterAccepted(filterAccepted) {
    this.filterAccepted = filterAccepted;
  }

  isFilterAccepted(forceFilter) {
    if (this.filterDirty || forceFilter) {
      this.getTree().applyFiltersForNode(this);
    }
    return this.filterAccepted;
  }

  /**
   * This method loads the child nodes of this node and returns a jQuery.Deferred to register callbacks
   * when loading is done or has failed. This method should only be called when childrenLoaded is false.
   *
   * @return {$.Deferred} or null when TreeNode cannot load children (which is the case for all
   *     TreeNodes in the remote case). The default impl. return null.
   */
  loadChildren() {
    return $.resolvedDeferred();
  }

  /**
   * This method calls loadChildren() but does nothing when children are already loaded or when loadChildren()
   * is already in progress.
   * @returns {Promise}
   */
  ensureLoadChildren() {
    // when children are already loaded we return an already resolved promise so the caller can continue immediately
    if (this.childrenLoaded) {
      return $.resolvedPromise();
    }
    // when load children is already in progress, we return the same promise
    if (this._loadChildrenPromise) {
      return this._loadChildrenPromise;
    }
    let deferred = this.loadChildren();
    let promise = deferred.promise();
    // check if we can get rid of this state-check in a future release
    if (deferred.state() === 'resolved') {
      this._loadChildrenPromise = null;
      return promise;
    }

    this._loadChildrenPromise = promise;
    promise.done(this._onLoadChildrenDone.bind(this));
    return promise; // we must always return a promise, never null - otherwise caller would throw an error
  }

  _onLoadChildrenDone() {
    this._loadChildrenPromise = null;
  }

  setText(text) {
    this.text = text;
  }

  /**
   * This functions renders sets the $node and $text properties.
   *
   * @param {jQuery} $parent the tree DOM
   * @param {number} paddingLeft calculated by tree
   */
  render($parent, paddingLeft) {
    this.$node = $parent.makeDiv('tree-node')
      .data('node', this)
      .attr('data-nodeid', this.id)
      .attr('data-level', this.level);
    if (!objects.isNullOrUndefined(paddingLeft)) {
      this.$node.cssPaddingLeft(paddingLeft);
    }
    this.$text = this.$node.appendSpan('text');

    this._renderControl();
    if (this.getTree().checkable) {
      this._renderCheckbox();
    }
    this._renderText();
    this._renderIcon();
  }

  _renderText() {
    if (this.htmlEnabled) {
      this.$text.html(this.text);
    } else {
      this.$text.textOrNbsp(this.text);
    }
  }

  _renderChecked() {
    // if node is not rendered, do nothing
    if (!this.rendered) {
      return;
    }

    this.$node
      .children('.tree-node-checkbox')
      .children('.check-box')
      .toggleClass('checked', this.checked);
  }

  _renderIcon() {
    this.$node.toggleClass('has-icon', !!this.iconId);
    this.$node.icon(this.iconId, $icon => $icon.insertBefore(this.$text));
  }

  $icon() {
    return this.$node.children('.icon');
  }

  _renderControl() {
    let $control = this.$node.prependDiv('tree-node-control');
    this._updateControl($control);
  }

  _updateControl($control) {
    let tree = this.getTree();
    $control.toggleClass('checkable', tree.checkable);
    $control.cssPaddingLeft(tree._computeNodeControlPaddingLeft(this));
    $control.setVisible(!this.leaf);
  }

  _renderCheckbox() {
    let $checkboxContainer = this.$node.prependDiv('tree-node-checkbox');
    let $checkbox = $checkboxContainer
      .appendDiv('check-box')
      .toggleClass('checked', this.checked)
      .toggleClass('disabled', !this.enabled);
    $checkbox.toggleClass('children-checked', !!this.childrenChecked);
  }

  _decorate() {
    // This node is not yet rendered, nothing to do
    if (!this.$node) {
      return;
    }

    let $node = this.$node,
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
      .toggleClass('disabled', !this.enabled);

    if (!this.parentNode && tree.selectedNodes.length === 0 || // root nodes have class child-of-selected if no node is selected
      tree._isChildOfSelectedNodes(this)) {
      $node.addClass('child-of-selected');
    }

    this._renderText();
    this._renderIcon();
    styles.legacyStyle(this._getStyles(), $node);

    // If parent node is marked as 'lazy', check if any visible child nodes remain.
    if (this.parentNode && this.parentNode.expandedLazy) {
      let hasVisibleNodes = this.parentNode.childNodes.some(childNode => {
        return !!tree.visibleNodesMap[childNode.id];
      });
      if (!hasVisibleNodes && this.parentNode.$node) {
        // Remove 'lazy' from parent
        this.parentNode.$node.removeClass('lazy');
      }
    }
  }

  /**
   * @return {object} The object that has the properties used for styles (colors, fonts, etc.)
   *     The default impl. returns "this". Override this function to return another object.
   */
  _getStyles() {
    return this;
  }

  /**
   * This function extracts all CSS classes that are set externally by the tree.
   * The classes depend on the tree hierarchy or the selection and thus cannot determined
   * by the node itself.
   */
  _preserveCssClasses($node) {
    let cssClass = 'tree-node';
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
  }
}
