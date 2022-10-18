/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {FormField, Tree, TreeFieldEventMap, TreeFieldModel, Widget} from '../../../index';

export default class TreeField extends FormField implements TreeFieldModel {
  declare model: TreeFieldModel;
  declare eventMap: TreeFieldEventMap;

  tree: Tree;

  constructor() {
    super();

    this.gridDataHints.weightY = 1.0;
    this.gridDataHints.h = 3;
    this._addWidgetProperties(['tree']);
  }

  protected _render() {
    this.addContainer(this.$parent, 'tree-field');
    this.addLabel();
    this.addMandatoryIndicator();
    this.addStatus();
    if (this.tree) {
      this._renderTree();
    }
  }

  setTree(tree: Tree) {
    this.setProperty('tree', tree);
  }

  protected _setTree(tree: Tree) {
    this._setProperty('tree', tree);
    if (tree) {
      tree.setScrollTop(this.scrollTop);
    }
  }

  protected _renderTree() {
    if (!this.tree) {
      return;
    }
    this.tree.render();
    this.addField(this.tree.$container);
    this.$field.addDeviceClass();
    this.invalidateLayoutTree();
  }

  protected _removeTree() {
    if (!this.tree) {
      return;
    }
    this.tree.remove();
    this._removeField();
    this.invalidateLayoutTree();
  }

  override getDelegateScrollable(): Widget {
    return this.tree;
  }
}
