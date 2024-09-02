/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, CalendarResourceLookupCall, HtmlComponent, InitModelOf, LookupRow, ObjectOrModel, ResourcePanelTreeNode, scout, SingleLayout, Tree, TreeBox, TreeBoxTreeNode, TreeNode, TreeNodesCheckedEvent, Widget} from '../index';

export class ResourcePanel extends Widget {
  treeBox: ResourcePanelTreeBox;

  protected override _init(model: InitModelOf<this>) {
    super._init(model);

    this.treeBox = scout.create(ResourcePanelTreeBox, {
      parent: this,
      lookupCall: CalendarResourceLookupCall,
      labelVisible: false,
      statusVisible: false,
      tree: {
        objectType: ResourcePanelTree,
        cssClass: 'resource-panel-tree',
        checkable: true,
        textFilterEnabled: false,
        _scrollDirections: 'y',
        autoCheckChildren: true
      }
    });
  }

  protected override _render() {
    this.$container = this.$parent.appendDiv('resource-panel-container');
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(new SingleLayout());
    this.treeBox.render();
  }
}

class ResourcePanelTreeBox extends TreeBox<string> {
  declare tree: ResourcePanelTree;
  declare lookupCall: CalendarResourceLookupCall;

  protected override _render() {
    super._render();
    this.removeMandatoryIndicator();
  }

  protected override _renderFocused() {
    // NOP
  }

  protected override _createNode(lookupRow: LookupRow<string>): TreeBoxTreeNode<string> {
    let node = super._createNode(lookupRow);
    node.expanded = true;
    return node;
  }

  protected override _onTreeNodesChecked(event: TreeNodesCheckedEvent) {
    // Make impossible to uncheck all nodes
    if (arrays.hasElements(this.tree.checkedNodes)) {
      super._onTreeNodesChecked(event);
    } else if (!this._populating) {
      // Reapply the value to the tree
      this._syncValueToTree(this.value);
      this._triggerShakeAnimation(event.nodes[0]);
    }
  }

  protected _triggerShakeAnimation(node: TreeNode) {
    node.$node.children('.tree-node-checkbox').addClassForAnimation('animate-unable-uncheck');
  }
}

class ResourcePanelTree extends Tree {
  declare nodes: ResourcePanelTreeNode[];

  override insertNode(node: ObjectOrModel<ResourcePanelTreeNode>, parentNode?: ResourcePanelTreeNode, index?: number) {
    super.insertNode(node, parentNode, index);
  }
}
