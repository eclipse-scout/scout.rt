/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
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

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.rt.client.ui.action.tree.IActionNode;
import org.eclipse.scout.rt.shared.extension.AbstractMoveModelObjectHandler;

/**
 * The top level list is expected to be sorted by the caller.
 */
public class MoveActionNodesHandler<T extends IActionNode<T>> extends AbstractMoveModelObjectHandler<T> {

  public MoveActionNodesHandler(List<T> actionNodes) {
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
    List<T> allModelObjects = new LinkedList<T>();
    collectAllActionNodes(getRootModelObjects(), allModelObjects);
    return allModelObjects;
  }

  private void collectAllActionNodes(List<? extends T> actionNodes, List<T> allActionNodes) {
    if (CollectionUtility.isEmpty(actionNodes)) {
      return;
    }
    allActionNodes.addAll(actionNodes);
    for (T actionNode : actionNodes) {
      if (!actionNode.hasChildActions()) {
        continue;
      }
      collectAllActionNodes(actionNode.getChildActions(), allActionNodes);
    }
  }
}
