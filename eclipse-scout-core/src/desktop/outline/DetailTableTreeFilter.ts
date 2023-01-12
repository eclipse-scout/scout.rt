/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
