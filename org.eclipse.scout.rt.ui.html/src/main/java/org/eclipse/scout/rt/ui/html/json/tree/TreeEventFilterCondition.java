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

import java.util.List;

import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeEvent;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

public class TreeEventFilterCondition {

  private final int m_type;
  private List<ITreeNode> m_nodes;
  private List<ITreeNode> m_checkedNodes;
  private List<ITreeNode> m_uncheckedNodes;

  private boolean m_checkNodes;
  private boolean m_checkCheckedNodes;

  /**
   * @param type
   *          event type (see {@link TreeEvent})
   */
  public TreeEventFilterCondition(int type) {
    m_type = type;
  }

  public int getType() {
    return m_type;
  }

  public List<ITreeNode> getNodes() {
    return CollectionUtility.arrayList(m_nodes);
  }

  public void setNodes(List<ITreeNode> nodes) {
    m_nodes = CollectionUtility.arrayList(nodes);
    m_checkNodes = true;
  }

  public List<ITreeNode> getCheckedNodes() {
    return CollectionUtility.arrayList(m_checkedNodes);
  }

  public List<ITreeNode> getUncheckedNodes() {
    return CollectionUtility.arrayList(m_uncheckedNodes);
  }

  public void setCheckedNodes(List<ITreeNode> checkedNodes, List<ITreeNode> uncheckedNodes) {
    m_checkedNodes = CollectionUtility.arrayList(checkedNodes);
    m_uncheckedNodes = CollectionUtility.arrayList(uncheckedNodes);
    m_checkCheckedNodes = true;
  }

  public boolean checkNodes() {
    return m_checkNodes;
  }

  public boolean checkCheckedNodes() {
    return m_checkCheckedNodes;
  }
}
