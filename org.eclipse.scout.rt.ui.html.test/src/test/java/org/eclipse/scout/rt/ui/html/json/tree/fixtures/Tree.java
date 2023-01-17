/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.tree.fixtures;

import java.util.List;

import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.platform.classid.ClassId;

@ClassId("bbcaf7c6-3913-4acb-b2f3-a1f1a0db1e9b")
public class Tree extends AbstractTree {

  private List<ITreeNode> m_nodes;

  public Tree() {
    super();
  }

  public Tree(List<ITreeNode> nodes) {
    super(false);
    m_nodes = nodes;
    callInitializer();
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    addChildNodes(getRootNode(), m_nodes);
  }
}
