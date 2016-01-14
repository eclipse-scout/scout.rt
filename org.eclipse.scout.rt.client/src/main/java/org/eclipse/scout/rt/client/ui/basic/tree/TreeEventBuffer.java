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
package org.eclipse.scout.rt.client.ui.basic.tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.scout.rt.client.ui.AbstractEventBuffer;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.CompareUtility;

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
    removeNodesContainedInPreviousEvents(events, TreeEvent.TYPE_NODES_UPDATED, TreeEvent.TYPE_NODES_INSERTED);
    removeNodesContainedInPreviousEvents(events, TreeEvent.TYPE_NODE_CHANGED, TreeEvent.TYPE_NODES_INSERTED);
    removeNodesContainedInPreviousEvents(events, TreeEvent.TYPE_NODES_INSERTED, TreeEvent.TYPE_NODES_INSERTED);
    removeEmptyEvents(events);
    removeIdenticalEvents(events);
    coalesceSameType(events);
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
      if (isIgnorePrevious(type)) {
        //remove all previous events of the same type
        remove(type, events.subList(0, i));
      }
      else if (type == TreeEvent.TYPE_NODES_DELETED || type == TreeEvent.TYPE_ALL_CHILD_NODES_DELETED) {
        List<ITreeNode> remainingNodes = removeNodesFromPreviousEvents(event.getNodes(), events.subList(0, i), TreeEvent.TYPE_NODES_INSERTED);
        events.set(i, replaceNodesInEvent(event, remainingNodes));
      }
      else if (type == TreeEvent.TYPE_NODE_EXPANDED_RECURSIVE || type == TreeEvent.TYPE_NODE_COLLAPSED_RECURSIVE) {
        remove(getExpansionRelatedEvents(), events.subList(0, i));
      }
    }
  }

  /**
   * Removes the given 'nodesToRemove' from all 'events'. Recursive child nodes are also removed (but will not be
   * returned). The event list is traversed backwards.
   *
   * @return a list with the same nodes as 'nodesToRemove', except those that were removed from an event whose type
   *         matches one of the 'creationTypes'. This allows for completely removing a node that was created and deleted
   *         in the same request.
   */
  protected List<ITreeNode> removeNodesFromPreviousEvents(Collection<ITreeNode> nodesToRemove, List<TreeEvent> events, Integer... creationTypes) {
    List<Integer> creationTypesList = Arrays.asList(creationTypes);
    List<ITreeNode> remainingNodes = new ArrayList<ITreeNode>();

    for (ITreeNode nodeToRemove : nodesToRemove) {
      // Unwrap resolved node to get the real answer to "getChildNodes()"
      nodeToRemove = TreeUtility.unwrapResolvedNode(nodeToRemove);
      Collection<ITreeNode> allChildNodes = collectAllNodesRec(nodeToRemove.getChildNodes());
      boolean nodeRemovedFromCreationEvent = false;

      for (ListIterator<TreeEvent> it = events.listIterator(events.size()); it.hasPrevious();) {
        TreeEvent event = it.previous();

        // Remove the current node from the event and check if was removed from a creation event
        TreeEvent newEvent = removeNode(event, nodeToRemove);
        if (creationTypesList.contains(event.getType())) {
          boolean removed = (event.getNodes().size() != newEvent.getNodes().size());
          if (removed) {
            nodeRemovedFromCreationEvent = true;
          }
          else {
            // Also consider it as "removed from creation event" if one of the parents of nodeToRemove
            // is a directly inserted node. The nodeToRemove will then not be contained in the insertion
            // event, but because one of its parents was inserted recently, the deletion event is not
            // required anymore (the insertion event does not contain deleted nodes).
            ITreeNode parentToCheck = nodeToRemove.getParentNode();
            while (parentToCheck != null) {
              if (containsNode(newEvent.getNodes(), parentToCheck)) {
                nodeRemovedFromCreationEvent = true;
                break;
              }
              parentToCheck = parentToCheck.getParentNode();
            }
          }
        }

        // Now remove all recursive children (without considering them for the 'remainingNodes' list)
        for (ITreeNode childNode : allChildNodes) {
          newEvent = removeNode(newEvent, childNode);
        }

        // Replace the updated event
        it.set(newEvent);

        if (nodeRemovedFromCreationEvent) {
          break;
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
      if (CompareUtility.equals(node, nodeToRemove)) {
        it.remove();
      }
    }
    return replaceNodesInEvent(event, event.getCommonParentNode(), nodes);
  }

  /**
   * Serves as a replacement for the missing "event.setNodes()" method. The same "common parent node" as in the given
   * event is used. If you want to replace the common parent node, use
   * {@link #replaceNodesInEvent(TreeEvent, ITreeNode, Collection)}.
   */
  protected TreeEvent replaceNodesInEvent(TreeEvent event, Collection<ITreeNode> nodes) {
    return replaceNodesInEvent(event, event.getCommonParentNode(), nodes);
  }

  /**
   * Serves as a replacement for the missing "event.setNodes()" method.
   */
  protected TreeEvent replaceNodesInEvent(TreeEvent event, ITreeNode commonParentNode, Collection<ITreeNode> nodes) {
    return new TreeEvent(event.getTree(), event.getType(), commonParentNode, nodes);
  }

  /**
   * Traverses the given list of events backwards, and checks for each event of type 'newType' if there is an older
   * event of type 'oldType' that already contains the same nodes. If yes, the corresponding nodes are removed from the
   * newer event.
   * <p>
   * Example: INSERT(A[B,C[E],D]), UPDATE(C[E],F) => UPDATE(C[E]) can be removed because INSERT already contains C[E] as
   * child of A.
   */
  protected void removeNodesContainedInPreviousEvents(List<TreeEvent> events, int newType, int oldType) {
    for (int j = 0; j < events.size() - 1; j++) {
      int i = events.size() - 1 - j;
      TreeEvent event = events.get(i);

      if (event.getType() == newType) {
        // Build a mutable list of nodes, filter it and then replace it in the event (if there were some nodes removed)
        List<ITreeNode> nodes = new ArrayList<>(event.getChildNodes());
        boolean removed = filterNodesByPreviousEvents(nodes, events.subList(0, i), oldType);
        if (removed) {
          events.set(i, replaceNodesInEvent(event, nodes));
        }
      }
    }
  }

  /**
   * Removes all nodes from the list 'nodes' that are contained in one of the events of 'events' matching the type
   * 'oldType'. A node is considered contained if it is a member of an events node list, or a child (at any level) of
   * such a node. Please note that 'nodes' is a live-list, i.e. it is manipulated directly by this method.
   *
   * @return <code>true</code> if one or more nodes have been removed from 'nodes'.
   */
  protected boolean filterNodesByPreviousEvents(List<ITreeNode> nodes, List<TreeEvent> events, int oldType) {
    boolean removed = false;
    for (Iterator<ITreeNode> it = nodes.iterator(); it.hasNext();) {
      ITreeNode node = it.next();
      for (TreeEvent event : events) {
        if (event.getType() == oldType && containsNodeRec(event.getNodes(), node)) {
          it.remove();
          removed = true;
          break; // no need to look further
        }
      }
    }
    return removed;
  }

  /**
   * @return <code>true</code> if 'nodes' contains 'nodeToFind'. Children of 'nodes are <b>not</b> considered, use
   *         {@link #containsNodeRec(Collection, ITreeNode)} if they should be checked as well.
   */
  protected boolean containsNode(Collection<ITreeNode> nodes, ITreeNode nodeToFind) {
    for (ITreeNode node : nodes) {
      node = TreeUtility.unwrapResolvedNode(node);
      if (CompareUtility.equals(node, nodeToFind)) {
        return true;
      }
    }
    return false;
  }

  /**
   * @return <code>true</code> if 'nodes' contains 'nodeToFind' or one of the children does so.
   */
  protected boolean containsNodeRec(Collection<ITreeNode> nodes, ITreeNode nodeToFind) {
    for (ITreeNode node : nodes) {
      // Unwrap resolved node to get the real answer to "getChildNodes()"
      node = TreeUtility.unwrapResolvedNode(node);
      if (CompareUtility.equals(node, nodeToFind)) {
        return true;
      }
      if (containsNodeRec(node.getChildNodes(), nodeToFind)) {
        return true;
      }
    }
    return false;
  }

  /**
   * @return all the given nodes, including their recursive child nodes.
   */
  protected Collection<ITreeNode> collectAllNodesRec(Collection<ITreeNode> nodes) {
    List<ITreeNode> result = new ArrayList<ITreeNode>();
    for (ITreeNode node : nodes) {
      // Unwrap resolved node to get the real answer to "getChildNodes()"
      node = TreeUtility.unwrapResolvedNode(node);
      result.add(node);
      result.addAll(collectAllNodesRec(node.getChildNodes()));
    }
    return result;
  }

  /**
   * Merge previous events of the same type into the current and delete the previous events
   */
  protected void coalesceSameType(List<TreeEvent> events) {
    for (int j = 0; j < events.size() - 1; j++) {
      int i = events.size() - 1 - j;
      TreeEvent event = events.get(i);

      if (isCoalesceConsecutivePrevious(event.getType())) {
        TreeEvent mergedEvent = coalesceConsecutivePrevious(event, events.subList(0, i));
        if (mergedEvent != event) {
          // replace in (now shorter) list
          i = events.size() - 1 - j;
          events.set(i, mergedEvent);
        }
      }
    }
  }

  /**
   * Merge events of the same type in the given list into the current and delete the other events from the list.
   *
   * @return the updated event (with other events merged into it)
   */
  protected TreeEvent coalesceConsecutivePrevious(TreeEvent event, List<TreeEvent> list) {
    for (ListIterator<TreeEvent> it = list.listIterator(list.size()); it.hasPrevious();) {
      TreeEvent previous = it.previous();
      if (event.getType() == previous.getType()) {
        if (hasSameCommonParentNode(event, previous)) {
          event = merge(previous, event);
          it.remove();
        }
      }
      else {
        return event;
      }
    }
    return event;
  }

  protected boolean hasSameCommonParentNode(TreeEvent event1, TreeEvent event2) {
    ITreeNode node1 = event1.getCommonParentNode();
    ITreeNode node2 = event2.getCommonParentNode();
    return CompareUtility.equals(node1, node2);
  }

  /**
   * @return a new event with same same property as 'second' but with the nodes of 'first' merged into
   */
  protected TreeEvent merge(TreeEvent first, TreeEvent second) {
    return replaceNodesInEvent(second, mergeNodes(first.getNodes(), second.getNodes()));
  }

  /**
   * Merge list of nodes, such that, if the same node is in both lists, only the one of the second list (later event) is
   * kept.
   */
  protected List<ITreeNode> mergeNodes(Collection<ITreeNode> first, Collection<ITreeNode> second) {
    List<ITreeNode> nodes = new ArrayList<>();
    for (ITreeNode node : first) {
      if (!second.contains(node)) {
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
      if (isNodesRequired(event.getType()) && event.getNodes().isEmpty()
          || isCommonParentNodeRequired(event.getType()) && event.getCommonParentNode() == null) {
        it.remove();
      }
    }
  }

  /**
   * Removes identical events (same type and content) when they occur consecutively (not necessarily directly, but
   * within the same type group). The oldest event is preserved.
   */
  protected void removeIdenticalEvents(List<TreeEvent> events) {
    // Please note: In contrast to all other methods in this class, this method loops through the
    // list in FORWARD direction (so the oldest event will be kept).
    for (int i = 0; i < events.size(); i++) {
      TreeEvent event = events.get(i);

      List<TreeEvent> subList = events.subList(i + 1, events.size());
      for (Iterator<TreeEvent> it = subList.iterator(); it.hasNext();) {
        TreeEvent next = it.next();
        if (next.getType() != event.getType()) {
          // Stop when a node of different type occurs
          break;
        }
        if (isIdenticalEvent(event, next)) {
          it.remove();
        }
      }
    }
  }

  @Override
  protected boolean isIdenticalEvent(TreeEvent event1, TreeEvent event2) {
    if (event1 == null && event2 == null) {
      return true;
    }
    if (event1 == null || event2 == null) {
      return false;
    }
    boolean identical = (event1.getType() == event2.getType()
        && hasSameCommonParentNode(event1, event2)
        && CollectionUtility.equalsCollection(event1.getNodes(), event2.getNodes(), true)
        && CollectionUtility.equalsCollection(event1.getDeselectedNodes(), event2.getDeselectedNodes(), true)
        && CollectionUtility.equalsCollection(event1.getNewSelectedNodes(), event2.getNewSelectedNodes(), true)
        && CollectionUtility.equalsCollection(event1.getPopupMenus(), event2.getPopupMenus())
        && event1.isConsumed() == event2.isConsumed()
        && CompareUtility.equals(event1.getDragObject(), event2.getDragObject())
        && CompareUtility.equals(event1.getDropObject(), event2.getDropObject()));
    return identical;
  }

  protected List<Integer> getExpansionRelatedEvents() {
    List<Integer> res = new ArrayList<>();
    res.add(TreeEvent.TYPE_NODE_EXPANDED);
    res.add(TreeEvent.TYPE_NODE_EXPANDED_RECURSIVE);
    res.add(TreeEvent.TYPE_NODE_COLLAPSED);
    res.add(TreeEvent.TYPE_NODE_COLLAPSED_RECURSIVE);
    return res;
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
      case TreeEvent.TYPE_NODES_UPDATED:
      case TreeEvent.TYPE_ALL_CHILD_NODES_DELETED: {
        // Multiple nodes
        return true;
      }
      case TreeEvent.TYPE_NODE_ACTION:
      case TreeEvent.TYPE_NODE_CHANGED:
      case TreeEvent.TYPE_NODE_CLICK:
      case TreeEvent.TYPE_NODE_COLLAPSED:
      case TreeEvent.TYPE_NODE_COLLAPSED_RECURSIVE:
      case TreeEvent.TYPE_NODE_DROP_ACTION:
      case TreeEvent.TYPE_NODE_DROP_TARGET_CHANGED:
      case TreeEvent.TYPE_NODE_ENSURE_VISIBLE:
      case TreeEvent.TYPE_NODE_EXPANDED:
      case TreeEvent.TYPE_NODE_EXPANDED_RECURSIVE:
      case TreeEvent.TYPE_NODE_FILTER_CHANGED: {
        // Single node
        return true;
      }
      case TreeEvent.TYPE_BEFORE_NODES_SELECTED:
      case TreeEvent.TYPE_NODES_SELECTED:
      case TreeEvent.TYPE_DRAG_FINISHED:
      case TreeEvent.TYPE_NODE_REQUEST_FOCUS:
      case TreeEvent.TYPE_REQUEST_FOCUS:
      case TreeEvent.TYPE_SCROLL_TO_SELECTION:
      case TreeEvent.TYPE_NODES_CHECKED:
      default: {
        return false;
      }
    }
  }

  protected boolean isCommonParentNodeRequired(int type) {
    switch (type) {
      case TreeEvent.TYPE_ALL_CHILD_NODES_DELETED: {
        return true;
      }
      case TreeEvent.TYPE_NODES_INSERTED:
      case TreeEvent.TYPE_CHILD_NODE_ORDER_CHANGED:
      case TreeEvent.TYPE_NODES_UPDATED:
      case TreeEvent.TYPE_NODES_DELETED:
      case TreeEvent.TYPE_NODE_FILTER_CHANGED:
      case TreeEvent.TYPE_BEFORE_NODES_SELECTED:
      case TreeEvent.TYPE_NODES_SELECTED:
      case TreeEvent.TYPE_NODE_EXPANDED:
      case TreeEvent.TYPE_NODE_COLLAPSED:
      case TreeEvent.TYPE_NODE_EXPANDED_RECURSIVE:
      case TreeEvent.TYPE_NODE_COLLAPSED_RECURSIVE:
      case TreeEvent.TYPE_NODE_ACTION:
      case TreeEvent.TYPE_NODES_DRAG_REQUEST:
      case TreeEvent.TYPE_DRAG_FINISHED:
      case TreeEvent.TYPE_NODE_DROP_ACTION:
      case TreeEvent.TYPE_NODE_REQUEST_FOCUS:
      case TreeEvent.TYPE_NODE_ENSURE_VISIBLE:
      case TreeEvent.TYPE_REQUEST_FOCUS:
      case TreeEvent.TYPE_NODE_CLICK:
      case TreeEvent.TYPE_SCROLL_TO_SELECTION:
      case TreeEvent.TYPE_NODE_CHANGED:
      case TreeEvent.TYPE_NODE_DROP_TARGET_CHANGED:
      case TreeEvent.TYPE_NODES_CHECKED:
      default: {
        return false;
      }
    }
  }
}
