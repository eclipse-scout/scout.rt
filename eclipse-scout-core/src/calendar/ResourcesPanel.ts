/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {HtmlComponent, InitModelOf, scout, Tree, Widget} from '../index';

export class ResourcesPanel extends Widget {
  tree: Tree;

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this.tree = scout.create(Tree, {
      parent: this,
      checkable: true,
      textFilterEnabled: false
    });

    this.tree.insertNode({
      text: 'test',
      childNodes: [
        {
          text: 'child test',
          leaf: true
        }
      ]
    });
  }

  protected override _render() {
    this.$container = this.$parent.appendDiv('resources-panel-container');
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.tree.render();
  }
}
