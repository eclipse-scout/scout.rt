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
import org.eclipse.scout.rt.ui.html.json.tree.JsonTree;

public class TreeNodeContextElementConverter extends AbstractHybridActionContextElementConverter<JsonTree<?>, String, ITreeNode> {

  @Override
  public ITreeNode jsonToElement(JsonTree<?> adapter, String jsonElement) {
    return adapter.getTreeNodeForNodeId(jsonElement);
  }

  @Override
  public String elementToJson(JsonTree<?> adapter, ITreeNode element) {
    adapter.processBufferedEvents();
    return adapter.getNodeId(element);
  }
}
