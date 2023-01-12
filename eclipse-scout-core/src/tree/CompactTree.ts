/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  arrays, CompactTreeDownKeyStroke, CompactTreeLeftKeyStroke, CompactTreeNode, CompactTreeRightKeyStroke, CompactTreeUpKeyStroke, HtmlComponent, InitModelOf, MenuBar, MenuItemsOrder, Range, scout, Tree, TreeLayout, TreeNode, TreeNodeModel,
  TreeRenderExpansionOptions
} from '../index';

export class CompactTree extends Tree {

  $nodesContainer: JQuery;

  constructor() {
    super();
    this._scrollDirections = 'y';
  }

  protected override _initTreeKeyStrokeContext() {
    this.keyStrokeContext.registerKeyStrokes([
      new CompactTreeUpKeyStroke(this),
      new CompactTreeDownKeyStroke(this),
      new CompactTreeLeftKeyStroke(this),
      new CompactTreeRightKeyStroke(this)
    ]);
  }

  protected override _createTreeNode(nodeModel?: TreeNodeModel): CompactTreeNode {
    nodeModel = scout.nvl(nodeModel, {});
    nodeModel.parent = this;
    return scout.create(CompactTreeNode, nodeModel as InitModelOf<CompactTreeNode>);
  }

  protected override _render() {
    this.$container = this.$parent.appendDiv('compact-tree');

    let layout = new TreeLayout(this);
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(layout);

    this.$data = this.$container.appendDiv('tree-data');
    this._installScrollbars({
      borderless: true
    });
    this.menuBar = scout.create(MenuBar, {
      parent: this,
      menuOrder: new MenuItemsOrder(this.session, 'Tree')
    });
    this.menuBar.render();

    this.$nodesContainer = this.$data.appendDiv('nodes');
    this._updateNodeDimensions();
    this._renderViewport();
    this.invalidateLayoutTree();
  }

  protected override _calculateCurrentViewRange(): Range {
    this.viewRangeSize = this.visibleNodesFlat.length;
    return new Range(0, Math.max(this.visibleNodesFlat.length, 0));
  }

  override calculateViewRangeSize(): number {
    return this.visibleNodesFlat.length;
  }

  protected override _insertNodeInDOMAtPlace(node: CompactTreeNode, index: number) {
    let visibleNodeBefore = this.visibleNodesFlat[index - 1];
    let n;
    if (!visibleNodeBefore) {
      node.$node.prependTo(this.$nodesContainer);
    } else if (visibleNodeBefore.level < node.level) {
      // insert after first child node (title from the level above)
      node.$node.insertAfter(visibleNodeBefore.$node.children()[0]);
    } else {
      n = visibleNodeBefore.$node;
      for (let i = 0; i < visibleNodeBefore.level - node.level; i++) {
        n = n.parent();
      }
      node.$node.insertAfter(n);
    }
  }

  override selectNodes(nodes: CompactTreeNode | CompactTreeNode[], debounceSend?: boolean) {
    let selectedSectionNodes: CompactTreeNode[] = [];
    nodes = arrays.ensure(nodes);
    nodes.forEach(node => {
      // If a section is selected, automatically change selection to first section-node
      if (node.isSection()) {
        if (node.childNodes.length > 0) {
          selectedSectionNodes.push(node.childNodes[0]);
        }
      } else {
        selectedSectionNodes.push(node);
      }
    });

    super.selectNodes(selectedSectionNodes, debounceSend);
  }

  protected override _renderExpansion(node: TreeNode, options?: TreeRenderExpansionOptions) {
    // nop (not supported by CompactTree)
  }

  protected override _updateItemPath(selectionChanged: boolean, ultimate?: TreeNode) {
    // nop (not supported by CompactTree)
  }
}
