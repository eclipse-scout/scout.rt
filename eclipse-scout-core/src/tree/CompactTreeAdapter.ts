/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {App, CompactTree, objects, TreeAdapter} from '../index';

export class CompactTreeAdapter extends TreeAdapter {
  declare widget: CompactTree;

  protected override _getDefaultNodeObjectType(): string {
    return 'CompactTreeNode';
  }

  /**
   * Static method to modify the prototype of CompactTree.
   */
  static modifyCompactTreePrototype() {
    if (!App.get().remote) {
      return;
    }

    objects.replacePrototypeFunction(CompactTree, '_createTreeNode', TreeAdapter._createTreeNodeRemote, true);
  }
}

App.addListener('bootstrap', CompactTreeAdapter.modifyCompactTreePrototype);
