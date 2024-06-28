/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.desktop.hybrid;

import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.tree.JsonTree;

public class TreeNodeContextElementResolver implements IHybridActionContextElementResolver {

  @Override
  public Object resolveElement(IJsonAdapter<?> adapter, Object element) {
    if (adapter instanceof JsonTree && element instanceof String) {
      return ((JsonTree) adapter).getTreeNodeForNodeId((String) element);
    }
    return null;
  }

  @Override
  public Object dissolveElement(IJsonAdapter<?> adapter, Object element) {
    if (adapter instanceof JsonTree && element instanceof ITreeNode) {
      JsonTree treeAdapter = (JsonTree) adapter;
      treeAdapter.processBufferedEvents();
      return treeAdapter.getNodeId((ITreeNode) element);
    }
    return null;
  }
}
