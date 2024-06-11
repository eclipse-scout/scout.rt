/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {aria, FilterElement, icons, InitModelOf, objects, ObjectWithType, scout, Session, SomeRequired, styles, texts, Tree, TreeNodeModel} from '../index';
import $ from 'jquery';

export class TreeNode implements TreeNodeModel, ObjectWithType, FilterElement {
  declare model: TreeNodeModel;
  declare initModel: SomeRequired<this['model'], 'parent'>;

  objectType: string;
  checked: boolean;
  childNodes: TreeNode[];
  cssClass: string;
  enabled: boolean;
  expanded: boolean;
  expandedLazy: boolean;
  htmlEnabled: boolean;
  iconId: string;
  id: string;
  initialExpanded: boolean;
  lazyExpandingEnabled: boolean;
  leaf: boolean;
  level: number;
  parent: Tree;
  parentNode: TreeNode;
  session: Session;
  text: string;
  tooltipText: string;
  foregroundColor: string;
  backgroundColor: string;
  font: string;

  initialized: boolean;
  rendered: boolean;
  attached: boolean;
  destroyed: boolean;
  filterAccepted: boolean;
  filterDirty: boolean;
  childrenLoaded: boolean;
  childrenChecked: boolean;
  height: number;
  width: number;
  displayBackup: string;
  prevSelectionAnimationDone: boolean;
  $node: JQuery;
  $text: JQuery<HTMLSpanElement>;
  childNodeIndex: number;

  /**
   * This internal variable stores the promise which is used when a loadChildren() operation is in progress.
   */
  protected _loadChildrenPromise: JQuery.Promise<any>;

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

    this._loadChildrenPromise = null;
  }

  init(model: InitModelOf<this>) {
    this.loadFromModel(model);
    this._init(model);
    if (model.initialExpanded === undefined) {
      this.initialExpanded = this.expanded;
    }
  }

  loadFromModel(model: InitModelOf<this>) {
    let staticModel = this._jsonModel();
    if (staticModel) {
      model = $.extend({}, staticModel, model);
    }
    $.extend(this, model);
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
  protected _destroy() {
    // NOP
  }

  getTree(): Tree {
    return this.parent;
  }

  protected _init(model: InitModelOf<this>) {
    scout.assertParameter('parent', model.parent, Tree);
    this.session = model.session || model.parent.session;

    texts.resolveTextProperty(this, 'text');
    icons.resolveIconProperty(this, 'iconId');

    // make sure all child nodes are TreeNodes too
    if (this.hasChildNodes()) {
      this.getTree().ensureTreeNodes(this.childNodes, this);
    }
  }

  protected _jsonModel(): Record<string, any> {
    return null;
  }

  reset() {
    if (this.$node) {
      this.$node.remove();
      this.$node = null;
    }
    this.rendered = false;
    this.attached = false;
  }

  hasChildNodes(): boolean {
    return this.childNodes.length > 0;
  }

  /**
   * @returns true, if this node is an ancestor of the given node
   */
  isAncestorOf(node: TreeNode): boolean {
    while (node) {
      if (node.parentNode === this) {
        return true;
      }
      node = node.parentNode;
    }
    return false;
  }

  /**
   * @returns true, if the node is a descendant of the given node
   */
  isDescendantOf(node: TreeNode): boolean {
    if (node === this.parentNode) {
      return true;
    }
    if (!this.parentNode) {
      return false;
    }
    return this.parentNode.isDescendantOf(node);
  }

  setFilterAccepted(filterAccepted: boolean) {
    this.filterAccepted = filterAccepted;
  }

  /**
   * This method loads the child nodes of this node and returns a jQuery.Promise to register callbacks
   * when loading is done or has failed. To skip loading the children when they are already loaded, use
   * {@link #ensureLoadChildren} instead.
   *
   * @returns a Promise or null when TreeNode cannot load children (which is the case for all
   *     TreeNodes in the remote case). The default impl. returns an empty resolved promise.
   */
  loadChildren(): JQuery.Promise<any> {
    return $.resolvedPromise();
  }

  /**
   * This method calls loadChildren() but does nothing when children are already loaded or when loadChildren()
   * is already in progress.
   */
  ensureLoadChildren(): JQuery.Promise<any> {
    // when children are already loaded we return an already resolved promise so the caller can continue immediately
    if (this.childrenLoaded) {
      return $.resolvedPromise();
    }
    // when load children is already in progress, we return the same promise
    if (this._loadChildrenPromise) {
      return this._loadChildrenPromise;
    }
    let promise = this.loadChildren();
    if (promise.state() === 'resolved') {
      this._loadChildrenPromise = null;
      return promise;
    }

    this._loadChildrenPromise = promise;
    promise.then(this._onLoadChildrenDone.bind(this));
    return promise; // we must always return a promise, never null - otherwise caller would throw an error
  }

  protected _onLoadChildrenDone() {
    this._loadChildrenPromise = null;
  }

  setText(text: string) {
    this.text = text;
  }

  /**
   * This functions renders sets the $node and $text properties.
   *
   * @param $parent the tree DOM
   * @param paddingLeft calculated by tree
   */
  render($parent: JQuery, paddingLeft: number) {
    this.$node = $parent.makeDiv('tree-node')
      .data('node', this)
      .attr('data-nodeid', this.id)
      .attr('data-level', this.level);

    aria.role(this.$node, 'treeitem');
    aria.level(this.$node, this.level + 1); // starts counting from 1

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

  protected _renderText() {
    if (this.htmlEnabled) {
      this.$text.html(this.text);
    } else {
      this.$text.textOrNbsp(this.text);
    }
  }

  /** @internal */
  _renderChecked() {
    // if node is not rendered, do nothing
    if (!this.rendered) {
      return;
    }

    this.$node
      .children('.tree-node-checkbox')
      .children('.check-box')
      .toggleClass('checked', this.checked);

    aria.checked(this.$node, this.checked);
  }

  protected _renderIcon() {
    this.$node.toggleClass('has-icon', !!this.iconId);
    this.$node.icon(this.iconId, $icon => $icon.insertBefore(this.$text));
  }

  $icon(): JQuery<HTMLElement> {
    return this.$node.children('.icon');
  }

  protected _renderControl() {
    let $control = this.$node.prependDiv('tree-node-control');
    this._updateControl($control);
  }

  /** @internal */
  _updateControl($control: JQuery) {
    let tree = this.getTree();
    $control.toggleClass('checkable', tree.checkable);
    $control.cssPaddingLeft(tree._computeNodeControlPaddingLeft(this));
    $control.setVisible(!this.leaf);
  }

  /** @internal */
  _renderCheckbox() {
    let $checkboxContainer = this.$node.prependDiv('tree-node-checkbox');
    let $checkbox = $checkboxContainer
      .appendDiv('check-box')
      .toggleClass('checked', this.checked)
      .toggleClass('disabled', !this.enabled);
    aria.role($checkbox, 'checkbox');
    aria.checked($checkbox, this.checked);
    aria.checked(this.$node, this.checked);
    $checkbox.toggleClass('children-checked', !!this.childrenChecked);

    this._renderChildrenChecked();
  }

  /** @internal */
  _renderChildrenChecked() {
    // if node is not rendered, do nothing
    if (!this.$node) {
      return;
    }

    this.$node.children('.tree-node-checkbox')
      .children('.check-box')
      .toggleClass('children-checked', !!this.childrenChecked);
  }

  /** @internal */
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

    aria.disabled($node, $node.hasClass('disabled') || null);
    aria.expanded($node, $node.hasClass('leaf') ? null : $node.hasClass('expanded'));

    if (!this.parentNode && tree.selectedNodes.length === 0 || // root nodes have class child-of-selected if no node is selected
      tree.isChildOfSelectedNodes(this)) {
      $node.addClass('child-of-selected');
    }

    if (this.parentNode) {
      aria.posinset($node, this.childNodeIndex + 1); // starts counting from 1
      aria.setsize($node, this.parentNode.childNodes.length);
    }

    this._renderText();
    this._renderIcon();
    styles.legacyStyle(this._getStyles(), $node);

    // If parent node is marked as 'lazy', check if any visible child nodes remain.
    if (this.parentNode && this.parentNode.expandedLazy) {
      let hasVisibleNodes = this.parentNode.childNodes.some(childNode => !!tree.visibleNodesMap[childNode.id]);
      if (!hasVisibleNodes && this.parentNode.$node) {
        // Remove 'lazy' from parent
        this.parentNode.$node.removeClass('lazy');
      }
    }
  }

  /**
   * @returns The object that has the properties used for styles (colors, fonts, etc.)
   *     The default impl. returns "this". Override this function to return another object.
   */
  protected _getStyles(): object {
    return this;
  }

  /**
   * This function extracts all CSS classes that are set externally by the tree.
   * The classes depend on the tree hierarchy or the selection and thus cannot be determined by the node itself.
   */
  protected _preserveCssClasses($node: JQuery): string {
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
