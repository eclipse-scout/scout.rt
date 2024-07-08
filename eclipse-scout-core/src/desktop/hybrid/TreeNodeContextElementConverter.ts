/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {HybridActionContextElementConverter, ModelAdapter, TreeAdapter, TreeNode} from '../../index';

export class TreeNodeContextElementConverter extends HybridActionContextElementConverter {

  override jsonToElement(adapter: ModelAdapter, jsonElement: any): any {
    if (adapter instanceof TreeAdapter && typeof jsonElement === 'string') {
      let tree = adapter.widget;
      return tree.nodesMap[jsonElement];
    }
    return null;
  }

  override elementToJson(adapter: ModelAdapter, element: any): any {
    if (adapter instanceof TreeAdapter && element instanceof TreeNode) {
      return element.id;
    }
  }
}

HybridActionContextElementConverter.register(TreeNodeContextElementConverter);
