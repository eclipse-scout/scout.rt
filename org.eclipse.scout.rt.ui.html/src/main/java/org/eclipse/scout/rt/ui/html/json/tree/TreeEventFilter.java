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
package org.eclipse.scout.rt.ui.html.json.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeEvent;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.ui.html.json.AbstractEventFilter;

public class TreeEventFilter extends AbstractEventFilter<TreeEvent, TreeEventFilterCondition> {

  private final JsonTree<? extends ITree> m_jsonTree;

  public TreeEventFilter(JsonTree<? extends ITree> jsonTree) {
    m_jsonTree = jsonTree;
  }

  /**
   * Computes whether the event should be returned to the GUI. There are three cases:
   * <ul>
   * <li>No filtering happens: The original event is returned. <br>
   * This is the case if the conditions don't contain an event with the same type as the original event.</li>
   * <li>Partial filtering happens (if condition.checkNodes is true): A new event with a subset of tree nodes is
   * returned.<br>
   * This is the case if the conditions contain a relevant event but has different nodes than the original event.
   * <li>Complete filtering happens: Null is returned.<br>
   * This is the case if the event should be filtered for every node in the original event
   */
  @Override
  public TreeEvent filter(TreeEvent event) {
    for (TreeEventFilterCondition condition : getConditions()) {
      if (condition.getType() == event.getType()) {

        if (condition.checkNodes()) {
          Collection<ITreeNode> nodes = new ArrayList<>(event.getNodes());
          nodes.removeAll(condition.getNodes());
          if (nodes.isEmpty()) {
            // Ignore event if no nodes remain (or if the event contained no nodes at all)
            return null;
          }
          return new TreeEvent(m_jsonTree.getModel(), event.getType(), event.getCommonParentNode(), nodes);
        }

        if (condition.checkCheckedNodes()) {
          List<ITreeNode> nodes = new ArrayList<>(event.getNodes());
          List<ITreeNode> checkedNodes = new ArrayList<>();
          List<ITreeNode> uncheckedNodes = new ArrayList<>();
          for (ITreeNode node : nodes) {
            if (node.isChecked()) {
              checkedNodes.add(node);
            }
            else {
              uncheckedNodes.add(node);
            }
          }
          if (CollectionUtility.equalsCollection(checkedNodes, condition.getCheckedNodes()) &&
              CollectionUtility.equalsCollection(uncheckedNodes, condition.getUncheckedNodes())) {
            // Ignore event if the checked and the unchecked nodes have not changes
            return null;
          }
          // Otherwise, send nodes that have a different checked state than before
          checkedNodes.removeAll(condition.getCheckedNodes());
          uncheckedNodes.removeAll(condition.getUncheckedNodes());
          nodes = CollectionUtility.combine(checkedNodes, uncheckedNodes);
          TreeEvent newEvent = new TreeEvent(m_jsonTree.getModel(), event.getType(), event.getCommonParentNode(), nodes);
          return newEvent;
        }

        // Ignore event if only type should be checked
        return null;
      }
    }
    return event;
  }
}
