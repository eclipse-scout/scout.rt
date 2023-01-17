/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
  @SuppressWarnings("unchecked")
  protected T getParent(T child) {
    return (T) child.getParentOfType(IActionNode.class);
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
