/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, CalendarsPanelLookupCall, CalendarsPanelTreeNode, HtmlComponent, InitModelOf, LookupRow, ObjectOrModel, scout, SingleLayout, Tree, TreeBox, TreeBoxTreeNode, TreeNodesCheckedEvent, Widget} from '../index';

export class CalendarsPanel extends Widget {
  treeBox: CalendarsPanelTreeBox;

  protected override _init(model: InitModelOf<this>) {
    super._init(model);

    this.treeBox = scout.create(CalendarsPanelTreeBox, {
      parent: this,
      lookupCall: CalendarsPanelLookupCall,
      labelVisible: false,
      statusVisible: false,
      tree: {
        objectType: CalendarsPanelTree,
        checkable: true,
        textFilterEnabled: false,
        _scrollDirections: 'y',
        autoCheckChildren: true
      }
    });
  }

  protected override _render() {
    this.$container = this.$parent.appendDiv('calendars-panel-container');
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(new SingleLayout());
    this.treeBox.render();
  }
}

class CalendarsPanelTreeBox extends TreeBox<string> {
  declare tree: CalendarsPanelTree;
  declare lookupCall: CalendarsPanelLookupCall;

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
    }
  }
}

class CalendarsPanelTree extends Tree {
  declare nodes: CalendarsPanelTreeNode[];

  override insertNode(node: ObjectOrModel<CalendarsPanelTreeNode>, parentNode?: CalendarsPanelTreeNode, index?: number) {
    super.insertNode(node, parentNode, index);
  }
}
