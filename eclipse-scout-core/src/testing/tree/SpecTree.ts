/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {DoubleClickSupport, Range, Tree, TreeNode} from '../../index';

export class SpecTree extends Tree {
  declare _doubleClickSupport: DoubleClickSupport & { _lastTimestamp: number };

  override _calculateCurrentViewRange(): Range {
    return super._calculateCurrentViewRange();
  }

  override _expandAllParentNodes(node: TreeNode) {
    super._expandAllParentNodes(node);
  }

  override _renderViewRangeForNode(node: TreeNode) {
    super._renderViewRangeForNode(node);
  }

  override _onScroll(event?: JQuery.ScrollEvent) {
    super._onScroll(event);
  }
}
