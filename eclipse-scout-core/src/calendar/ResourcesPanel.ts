/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {HtmlComponent, InitModelOf, ObjectOrModel, ResourcesPanelTreeNode, scout, Tree, Widget} from '../index';

export class ResourcesPanel extends Widget {
  tree: ResourcesPanelTree;

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this.tree = scout.create(ResourcesPanelTree, {
      parent: this,
      checkable: true,
      textFilterEnabled: false
    });
  }

  protected override _render() {
    this.$container = this.$parent.appendDiv('resources-panel-container');
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.tree.render();
  }
}

class ResourcesPanelTree extends Tree {
  declare nodes: ResourcesPanelTreeNode[];


  constructor() {
    super();

    this._scrollDirections = 'y';
  }

  override insertNode(node: ObjectOrModel<ResourcesPanelTreeNode>, parentNode?: ResourcesPanelTreeNode, index?: number) {
    super.insertNode(node, parentNode, index);
  }
}
