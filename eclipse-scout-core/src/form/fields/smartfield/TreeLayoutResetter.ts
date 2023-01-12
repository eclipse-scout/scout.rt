/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {ProposalChooserLayoutResetter, Tree} from '../../../index';

/**
 * This class is used to reset and restore styles in the DOM, so we can measure the preferred size of the tree.
 */
export class TreeLayoutResetter implements ProposalChooserLayoutResetter {
  cssSelector: string;
  protected _tree: Tree;

  constructor(tree: Tree) {
    this._tree = tree;
    this.cssSelector = '.tree';
  }

  modifyDom() {
    this._ensureFirstLast();
    this._tree.$container
      .css('display', 'inline-block')
      .css('width', 'auto')
      .css('height', 'auto');
    this._tree.$data
      .css('display', 'inline-block');
  }

  restoreDom() {
    this._tree.$container
      .css('display', 'block')
      .css('width', '100%')
      .css('height', '100%');
    this._tree.$data
      .css('display', 'block');
  }

  protected _ensureFirstLast() {
    let $nodes = this._tree.$data
      .children('.tree-node')
      .removeClass('first last');
    $nodes.first()
      .addClass('first');
    $nodes.last()
      .addClass('last');
  }
}
