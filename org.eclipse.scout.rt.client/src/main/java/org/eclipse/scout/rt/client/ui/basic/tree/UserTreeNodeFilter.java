/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.basic.tree;

import java.util.Collection;

import org.eclipse.scout.commons.CollectionUtility;

/**
 * @since 5.1
 */
public class UserTreeNodeFilter implements ITreeNodeFilter {
  private Collection<? extends ITreeNode> m_nodes;

  public UserTreeNodeFilter(Collection<? extends ITreeNode> nodes) {
    m_nodes = CollectionUtility.arrayList(nodes);
  }

  @Override
  public boolean accept(ITreeNode node, int level) {
    return m_nodes.contains(node);
  }

}
