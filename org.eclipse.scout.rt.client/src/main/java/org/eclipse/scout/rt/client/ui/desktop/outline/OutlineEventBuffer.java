/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.desktop.outline;

import java.util.Collection;

import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeEvent;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeEventBuffer;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.CompareUtility;

/**
 * A buffer for outline events ({@link OutlineEvent}s and {@link TreeEvent}s)
 */
public class OutlineEventBuffer extends TreeEventBuffer {

  @Override
  protected TreeEvent removeNode(TreeEvent event, ITreeNode nodeToRemove) {
    if (event instanceof OutlineEvent) {
      if (CompareUtility.equals(event.getCommonParentNode(), nodeToRemove)) {
        // Replace by empty event, because setting "commonParentNode" to null would not have an
        // effect (TreeEvent will recalculate the common parent node from the nodes list).
        return new OutlineEvent(event.getTree(), event.getType());
      }
    }
    return super.removeNode(event, nodeToRemove);
  }

  @Override
  protected TreeEvent replaceNodesInEvent(TreeEvent event, ITreeNode commonParentNode, Collection<ITreeNode> nodes) {
    if (event instanceof OutlineEvent) {
      // Outline events can have max. one node, "nodes" should not contain more
      return new OutlineEvent(event.getTree(), event.getType(), CollectionUtility.firstElement(nodes));
    }
    return super.replaceNodesInEvent(event, commonParentNode, nodes);
  }

  @Override
  protected boolean isNodesRequired(int type) {
    if (type == OutlineEvent.TYPE_PAGE_CHANGED) {
      return true;
    }
    return super.isNodesRequired(type);
  }
}
