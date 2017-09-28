/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.extension.ui.action.tree;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.scout.rt.client.ui.action.tree.IActionNode;
import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;
import org.eclipse.scout.rt.shared.extension.AbstractMoveModelObjectHandler;

/**
 * The top level list is expected to be sorted by the caller.
 */
public class MoveActionNodesHandler<T extends IActionNode<T>> extends AbstractMoveModelObjectHandler<T> {

  public MoveActionNodesHandler(OrderedCollection<T> actionNodes) {
    super("action node", actionNodes);
  }

  @Override
  protected T getParent(T child) {
    return child.getParent();
  }

  @Override
  protected void removeChild(T parent, T child) {
    parent.removeChildAction(child);
  }

  @Override
  protected void addChild(T parent, T child) {
    parent.addChildAction(child);
  }

  @Override
  protected void sortChildren(T parent) {
    parent.setChildActions(parent.getChildActions());
  }

  @Override
  protected List<T> collectAllModelObjects() {
    List<T> allModelObjects = new LinkedList<>();
    collectAllActionNodes(getRootModelObjects(), allModelObjects);
    return allModelObjects;
  }

  private void collectAllActionNodes(Iterable<? extends T> actionNodes, List<T> allActionNodes) {
    if (actionNodes == null) {
      return;
    }
    for (T actionNode : actionNodes) {
      allActionNodes.add(actionNode);
      if (actionNode.hasChildActions()) {
        collectAllActionNodes(actionNode.getChildActions(), allActionNodes);
      }
    }
  }
}
