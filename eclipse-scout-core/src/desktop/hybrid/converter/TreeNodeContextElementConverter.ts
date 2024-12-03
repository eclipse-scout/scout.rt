/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {HybridActionContextElementConverter, HybridActionContextElementConverters, ModelAdapter, objects, scout, TreeAdapter, TreeNode} from '../../../index';

export class TreeNodeContextElementConverter extends HybridActionContextElementConverter<TreeAdapter, string, TreeNode> {

  override _acceptAdapter(adapter: ModelAdapter): adapter is TreeAdapter {
    return adapter instanceof TreeAdapter;
  }

  override _acceptJsonElement(jsonElement: any): jsonElement is string {
    return objects.isString(jsonElement);
  }

  override _acceptModelElement(element: any): element is TreeNode {
    return element instanceof TreeNode;
  }

  override _jsonToElement(adapter: TreeAdapter, jsonElement: string): TreeNode {
    let tree = adapter.widget;
    let treeNode = tree.nodesMap[jsonElement];
    return scout.assertValue(treeNode, `Unknown node with id "${jsonElement}" in tree ${adapter.id}`);
  }

  override _elementToJson(adapter: TreeAdapter, element: TreeNode): string {
    return element.id;
  }
}

HybridActionContextElementConverters.get().register(TreeNodeContextElementConverter);
