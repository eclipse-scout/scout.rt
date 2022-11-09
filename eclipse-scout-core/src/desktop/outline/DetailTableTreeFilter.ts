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
import {Filter, Page} from '../../index';

export class DetailTableTreeFilter implements Filter<Page> {

  /**
   * Must correspond with logic in P_TableFilterBasedTreeNodeFilter
   */
  accept(node: Page): boolean {
    if (!node.parentNode) {
      // top level nodes may not be filtered
      return true;
    }
    if (!node.parentNode.filterAccepted) {
      // hide node if parent node is hidden
      return false;
    }
    if (!node.parentNode.detailTable) {
      // if parent has no detail table, node.row won't be set
      // detailTable may be undefined if node.detailTableVisible is false
      return true;
    }
    if (!node.row) {
      // link not yet established, as soon as row gets inserted and filtered, a refilter will be triggered on the tree
      return true;
    }
    return node.row.filterAccepted;
  }
}
