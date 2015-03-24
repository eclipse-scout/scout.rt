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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.holders.IHolder;
import org.eclipse.scout.rt.client.ui.AbstractEventBuffer;

/**
 * A buffer for tree events ({@link TreeEvent}s) with coalesce functionality:
 * <p>
 * <ul>
 * <li>Unnecessary events are removed.
 * <li>Events are merged, if possible.
 * </ul>
 * </p>
 * Not thread safe, to be accessed in client model job.
 */
public class TreeEventBuffer extends AbstractEventBuffer<TreeEvent> {

  /**
   * Removes unnecessary events or combines events in the list.
   */
  @Override
  protected List<TreeEvent> coalesce(List<TreeEvent> events) {
    removeObsolete(events);
    replacePrevious(events, TreeEvent.TYPE_NODES_INSERTED, TreeEvent.TYPE_NODES_UPDATED);
    coalesceSameType(events);
    removeEmptyEvents(events);
    return events;
  }

  /**
   * Remove previous events that are now obsolete.
   */
  protected void removeObsolete(List<TreeEvent> events) {
    //traverse the list in reversed order
    //previous events may be deleted from the list
    for (int j = 0; j < events.size() - 1; j++) {
      int i = events.size() - 1 - j;

      TreeEvent event = events.get(i);
      int type = event.getType();

      //remove all previous events of the same type
      if (isIgnorePrevious(type)) {
        remove(type, events.subList(0, i));
      }
      else if (type == TreeEvent.TYPE_NODES_DELETED) {
        List<ITreeNode> remainingNodes = removeNodesFromPreviousEvents(event.getNodes(), events.subList(0, i), TreeEvent.TYPE_NODES_INSERTED);
        events.set(i, replaceNodesInEvent(event, remainingNodes));
      }
    }
  }

  /**
   * Removes the given 'nodesToRemove' from all 'events'. The event list is traversed backwards.
   *
   * @return a list with the same nodes as 'nodesToRemove', except those that were removed from an
   *         event whose type matches one of the 'creationTypes'. This allows for completely removing
   *         a node that was created and deleted in the same request.
   */
  protected List<ITreeNode> removeNodesFromPreviousEvents(Collection<ITreeNode> nodesToRemove, List<TreeEvent> events, Integer... creationTypes) {
    List<Integer> creationTypesList = Arrays.asList(creationTypes);
    List<ITreeNode> remainingNodes = new ArrayList<ITreeNode>();

    for (ITreeNode nodeToRemove : nodesToRemove) {
      boolean nodeRemovedFromCreationEvent = false;

      for (ListIterator<TreeEvent> it = events.listIterator(events.size()); it.hasPrevious();) {
        TreeEvent event = it.previous();
        TreeEvent newEvent = removeNode(event, nodeToRemove);
        it.set(newEvent);
        boolean removed = (event.getNodes().size() != newEvent.getNodes().size());
        if (removed && creationTypesList.contains(event.getType())) {
          nodeRemovedFromCreationEvent = true;
        }
      }

      if (!nodeRemovedFromCreationEvent) {
        remainingNodes.add(nodeToRemove);
      }
    }

    return remainingNodes;
  }

  protected TreeEvent removeNode(TreeEvent event, ITreeNode nodeToRemove) {
    Collection<ITreeNode> nodes = event.getNodes();
    for (Iterator<ITreeNode> it = nodes.iterator(); it.hasNext();) {
      ITreeNode node = it.next();
      if (node == nodeToRemove) {
        it.remove();
      }
    }
    return replaceNodesInEvent(event, nodes);
  }

  /**
   * Serves as a replacement for the missing "event.setNodes()" method.
   */
  protected TreeEvent replaceNodesInEvent(TreeEvent event, Collection<ITreeNode> nodes) {
    return new TreeEvent(event.getTree(), event.getType(), event.getCommonParentNode(), nodes);
  }

  /**
   * Update a previous event of given type and removes a newer one of another type.
   */
  protected void replacePrevious(List<TreeEvent> events, int oldType, int newType) {
    for (int j = 0; j < events.size() - 1; j++) {
      int i = events.size() - 1 - j;

      TreeEvent event = events.get(i);
      int type = event.getType();

      //merge current update event with previous insert event of the same row
      if (type == newType) {
        events.set(i, updatePreviousNode(event, events.subList(0, i), oldType));
      }
    }
  }

  /**
   * Merge previous events of the same type (rows and columns) into the current and delete the previous events
   */
  protected void coalesceSameType(List<TreeEvent> events) {
    for (int j = 0; j < events.size() - 1; j++) {
      int i = events.size() - 1 - j;

      TreeEvent event = events.get(i);
      int type = event.getType();

      if (isCoalesceConsecutivePrevious(type)) {
        coalesceConsecutivePrevious(event, events.subList(0, i));
      }
    }
  }

  /**
   * Updates previous rows in the list, if it is of the given type.
   */
  protected TreeEvent updatePreviousNode(TreeEvent event, List<TreeEvent> events, int type) {
    Collection<ITreeNode> nodes = event.getNodes();
    for (ListIterator<TreeEvent> it = events.listIterator(events.size()); it.hasPrevious();) {
      TreeEvent previous = it.previous();
      if (previous.getType() == type) {
        it.set(replaceNodes(previous, nodes));
      }
    }
    return replaceNodesInEvent(event, nodes);
  }

  protected TreeEvent replaceNodes(TreeEvent event, Collection<ITreeNode> newRows) {
    for (Iterator<ITreeNode> it = newRows.iterator(); it.hasNext();) {
      ITreeNode newRow = it.next();
      IHolder<TreeEvent> eventHolder = new Holder<>(event);
      boolean replaced = tryReplaceRow(eventHolder, newRow);
      if (replaced) {
        event = eventHolder.getValue();
        it.remove();
      }
    }
    return event;
  }

  /**
   * Replaces the row in the event, if it is contained.
   *
   * @return <code>true</code> if successful.
   */
  protected boolean tryReplaceRow(IHolder<TreeEvent> eventHolder, ITreeNode newNode) {
    TreeEvent event = eventHolder.getValue();
    List<ITreeNode> targetRows = new ArrayList<>();
    boolean replaced = false;
    for (ITreeNode node : event.getNodes()) {
      if (node == newNode) {
        node = newNode;
        replaced = true;
      }
      targetRows.add(node);
    }
    eventHolder.setValue(replaceNodesInEvent(event, targetRows));
    return replaced;
  }

  /**
   * Merge events of the same type in the given list into the current and delete the other events
   * from the list.
   */
  protected void coalesceConsecutivePrevious(TreeEvent event, List<TreeEvent> list) {
    for (ListIterator<TreeEvent> it = list.listIterator(list.size()); it.hasPrevious();) {
      TreeEvent previous = it.previous();
      if (event.getType() == previous.getType() && hasSameCommonParentNode(event, previous)) {
        merge(previous, event);
        it.remove();
      }
      else {
        return;
      }
    }
  }

  protected boolean hasSameCommonParentNode(TreeEvent event1, TreeEvent event2) {
    ITreeNode node1 = event1.getCommonParentNode();
    ITreeNode node2 = event2.getCommonParentNode();
    if (node1 == null && node2 == null) {
      return true;
    }
    else if (node1 != null && node2 != null) {
      return (node1 == node2);
    }
    return false;
  }

  /**
   * @return a new event with same same property as 'second' but with the nodes of 'first' merged into
   */
  protected TreeEvent merge(TreeEvent first, TreeEvent second) {
    return replaceNodesInEvent(second, mergeNodes(first.getNodes(), second.getNodes()));
  }

  /**
   * Merge list of nodes, such that, if the same node is in both lists, only the one of the second list (later
   * event) is kept.
   */
  protected List<ITreeNode> mergeNodes(Collection<ITreeNode> first, Collection<ITreeNode> second) {
    List<ITreeNode> nodes = new ArrayList<>();
    for (ITreeNode node : first) {
      if (second.contains(node)) {
        nodes.add(node);
      }
    }
    for (ITreeNode node : second) {
      nodes.add(node);
    }
    return nodes;
  }

  protected void removeEmptyEvents(List<TreeEvent> events) {
    for (Iterator<TreeEvent> it = events.iterator(); it.hasNext();) {
      TreeEvent event = it.next();
      if (isNodesRequired(event.getType()) && event.getNodes().isEmpty()) {
        it.remove();
      }
    }
  }

  /**
   * @param type
   *          {@link TreeEvent} type
   * @return <code>true</code>, if previous events of the same type can be ignored. <code>false</code> otherwise
   */
  protected boolean isIgnorePrevious(int type) {
    switch (type) {
      case TreeEvent.TYPE_NODES_SELECTED:
      case TreeEvent.TYPE_BEFORE_NODES_SELECTED:
      case TreeEvent.TYPE_SCROLL_TO_SELECTION: {
        return true;
      }
      default: {
        return false;
      }
    }
  }

  /**
   * @return true, if previous consecutive events of the same type can be coalesced.
   */
  protected boolean isCoalesceConsecutivePrevious(int type) {
    switch (type) {
      case TreeEvent.TYPE_NODES_UPDATED:
      case TreeEvent.TYPE_NODES_INSERTED:
      case TreeEvent.TYPE_NODES_DELETED:
      case TreeEvent.TYPE_NODES_CHECKED: {
        return true;
      }
      default: {
        return false;
      }
    }
  }

  protected boolean isNodesRequired(int type) {
    switch (type) {
      case TreeEvent.TYPE_CHILD_NODE_ORDER_CHANGED:
      case TreeEvent.TYPE_NODES_DELETED:
      case TreeEvent.TYPE_NODES_DRAG_REQUEST:
      case TreeEvent.TYPE_NODES_INSERTED:
      case TreeEvent.TYPE_NODES_UPDATED: {
        // Multiple nodes
        return true;
      }
      case TreeEvent.TYPE_NODE_ACTION:
      case TreeEvent.TYPE_NODE_CHANGED:
      case TreeEvent.TYPE_NODE_CLICK:
      case TreeEvent.TYPE_NODE_COLLAPSED:
      case TreeEvent.TYPE_NODE_DROP_ACTION:
      case TreeEvent.TYPE_NODE_DROP_TARGET_CHANGED:
      case TreeEvent.TYPE_NODE_ENSURE_VISIBLE:
      case TreeEvent.TYPE_NODE_EXPANDED:
      case TreeEvent.TYPE_NODE_FILTER_CHANGED: {
        // Single node
        return true;
      }
      default: {
        return false;
      }
    }
  }
}
