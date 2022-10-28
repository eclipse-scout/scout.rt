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
import {DoubleClickSupport, Range, Tree, TreeNode} from '../../index';

export default class SpecTree extends Tree {
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
