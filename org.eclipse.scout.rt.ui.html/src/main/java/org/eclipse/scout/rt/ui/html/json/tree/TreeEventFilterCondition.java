/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.tree;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

public class TreeEventFilterCondition {

  private int m_type;
  private List<ITreeNode> m_nodes;
  private boolean m_checkNodes;

  public TreeEventFilterCondition(int type) {
    this(type, new ArrayList<ITreeNode>());
    m_checkNodes = false;
  }

  public TreeEventFilterCondition(int type, List<? extends ITreeNode> nodes) {
    m_nodes = CollectionUtility.arrayList(nodes);
    m_type = type;
    m_checkNodes = true;
  }

  public int getType() {
    return m_type;
  }

  public List<ITreeNode> getNodes() {
    return CollectionUtility.arrayList(m_nodes);
  }

  public boolean checkNodes() {
    return m_checkNodes;
  }
}
