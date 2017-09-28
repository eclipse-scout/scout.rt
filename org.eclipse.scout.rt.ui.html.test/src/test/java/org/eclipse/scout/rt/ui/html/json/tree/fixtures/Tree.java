/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.tree.fixtures;

import java.util.List;

import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;

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
