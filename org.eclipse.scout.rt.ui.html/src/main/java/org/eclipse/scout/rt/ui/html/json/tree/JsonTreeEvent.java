/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.tree;

import java.util.Collection;
import java.util.EventObject;

import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;

@SuppressWarnings({"serial", "squid:S2057"})
public class JsonTreeEvent extends EventObject {

  public static final int TYPE_NODES_INSERTED = 100;
  public static final int TYPE_NODES_DELETED = 200;

  private final int m_type;
  private Collection<? extends ITreeNode> m_nodes;

  public JsonTreeEvent(JsonTree<?> source, int type, Collection<? extends ITreeNode> nodes) {
    super(source);
    m_type = type;
    m_nodes = nodes;
  }

  @Override
  public JsonTree<? extends ITree> getSource() {
    return (JsonTree<?>) super.getSource();
  }

  /**
   * @return a flat collection of every inserted node. The collection also contains every inserted child node even if
   * the parent is already in it.
   */
  public Collection<? extends ITreeNode> getNodes() {
    return m_nodes;
  }

  public int getType() {
    return m_type;
  }
}
