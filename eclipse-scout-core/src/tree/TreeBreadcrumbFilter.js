/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
export default class TreeBreadcrumbFilter {

  constructor(tree) {
    this.tree = tree;
  }

  accept(node) {
    if (this.tree.selectedNodes.length === 0) {
      return node.parentNode === undefined;
    }
    return this.tree.isNodeInBreadcrumbVisible(node);
  }
}
