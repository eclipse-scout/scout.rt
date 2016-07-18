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

import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

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
    removeNodesContainedInPreviousInsertEvents(events, CollectionUtility.hashSet(
        //why these types? This information is included on each inserted node;
        //once the event buffer is flushed, the individual inserted tree nodes will be
        //sent with their current (=latest) state.
        TreeEvent.TYPE_NODE_EXPANDED,
        TreeEvent.TYPE_NODE_EXPANDED_RECURSIVE,
        TreeEvent.TYPE_NODE_COLLAPSED,
        TreeEvent.TYPE_NODE_COLLAPSED_RECURSIVE,
        TreeEvent.TYPE_NODE_CHANGED,
        TreeEvent.TYPE_NODES_UPDATED,
        TreeEvent.TYPE_NODES_INSERTED));
    removeEmptyEvents(events);
    removeIdenticalEvents(events);
    coalesceSameType(events);
    return events;
  }

  /**
   * Remove previous events that are now obsolete.
   */
  protected void removeObsolete(List<TreeEvent> events) {
    if (events.size() < 2) {
      return;
    }

    //traverse the list in reversed order
    //previous events may be deleted from the list
    final Set<Integer> typesToDelete = new HashSet<>();
    final List<DeletedNodesRemover> deletedNodesRemoverList = new LinkedList<>();

    for (ListIterator<TreeEvent> it = events.listIterator(events.size()); it.hasPrevious();) {
      final TreeEvent event = it.previous();
      final int type = event.getType();

      // process deleted nodes remover first so that unused nodes are removed from delete events
      if (!deletedNodesRemoverList.isEmpty()) {
        for (Iterator<DeletedNodesRemover> removerIt = deletedNodesRemoverList.iterator(); removerIt.hasNext();) {
          DeletedNodesRemover remover = removerIt.next();
          boolean finished = remover.removeDeletedNodes(event);
          if (finished) {
            remover.complete();
            removerIt.remove();
          }
        }
      }

      // handle types to delete
      if (typesToDelete.contains(type)) {
        it.remove();
        continue;
      }

      if (isIgnorePrevious(type)) {
        typesToDelete.add(type);
      }
      else if (type == TreeEvent.TYPE_NODE_EXPANDED_RECURSIVE || type == TreeEvent.TYPE_NODE_COLLAPSED_RECURSIVE) {
        typesToDelete.addAll(getExpansionRelatedEvents());
      }
      else if (type == TreeEvent.TYPE_NODES_DELETED || type == TreeEvent.TYPE_ALL_CHILD_NODES_DELETED) {
        if (event.hasNodes()) {
          deletedNodesRemoverList.add(new DeletedNodesRemover(event));
        }
      }
    }

    for (DeletedNodesRemover remover : deletedNodesRemoverList) {
      remover.complete();
    }
  }

  /**
   * Traverses the given list of events in forward direction, and checks for each event of type 'newType' if there is an
   * older insert event that already contains the same nodes. If yes, the corresponding nodes are removed from the newer
   * event.
   * <p>
   * Example: INSERT(A[B,C[E],D]), UPDATE(C[E],F) => UPDATE(C[E]) can be removed because INSERT already contains C[E] as
   * child of A.
   */
  protected void removeNodesContainedInPreviousInsertEvents(List<TreeEvent> events, Set<Integer> newTypes) {
    if (events.size() < 2) {
      return;
    }

    final Set<ITreeNode> insertedTreeNodes = new HashSet<>();
    for (Iterator<TreeEvent> it = events.iterator(); it.hasNext();) {
      final TreeEvent event = it.next();
      final int type = event.getType();

      if (!event.hasNodes()) {
        continue;
      }

      if (newTypes.contains(type) && !insertedTreeNodes.isEmpty()) {
        event.removeNodes(insertedTreeNodes, null);
      }

      if (type == TreeEvent.TYPE_NODES_INSERTED && event.hasNodes()) {
        Collection<ITreeNode> nodes = event.getNodes();
        for (ITreeNode node : nodes) {
          node = TreeUtility.unwrapResolvedNode(node);
          if (node != null && insertedTreeNodes.add(node)) {
            node.collectChildNodes(insertedTreeNodes, true);
          }
        }
      }
    }
  }

  /**
   * Merge previous events of the same type into the current and delete the previous events
   */
  protected void coalesceSameType(List<TreeEvent> events) {
    if (events.size() < 2) {
      return;
    }

    final Map<ITreeNode, TreeEvent> initialEventByParentNode = new HashMap<>();
    final Map<ITreeNode, TreeEventMerger> eventMergerByParent = new HashMap<>();
    int previousEventType = -1;

    for (ListIterator<TreeEvent> it = events.listIterator(events.size()); it.hasPrevious();) {
      final TreeEvent event = it.previous();
      final int type = event.getType();

      // clean-up initial event and event merger maps
      if (previousEventType != type && !initialEventByParentNode.isEmpty()) {
        for (TreeEventMerger merger : eventMergerByParent.values()) {
          merger.complete();
        }
        initialEventByParentNode.clear();
        eventMergerByParent.clear();
      }

      if (!isCoalesceConsecutivePrevious(type)) {
        continue;
      }

      previousEventType = type;
      final ITreeNode parentNode = event.getCommonParentNode();

      final TreeEvent initialEvent = initialEventByParentNode.get(event.getCommonParentNode());
      if (initialEvent == null) {
        // this is the first event with given common parent node.
        // put it into the initial event cache and continue with the next event
        initialEventByParentNode.put(parentNode, event);
        continue;
      }

      // there is already an initial event.
      // check if there is already an event merger or create one
      TreeEventMerger eventMerger = eventMergerByParent.get(parentNode);
      if (eventMerger == null) {
        eventMerger = new TreeEventMerger(initialEvent);
        eventMergerByParent.put(parentNode, eventMerger);
      }

      // merge current event and remove it from the original event list
      eventMerger.merge(event);
      it.remove();
    }

    // complete "open" event mergers
    for (TreeEventMerger eventMerger : eventMergerByParent.values()) {
      eventMerger.complete();
    }
  }

  protected void removeEmptyEvents(List<TreeEvent> events) {
    for (Iterator<TreeEvent> it = events.iterator(); it.hasNext();) {
      TreeEvent event = it.next();
      if (isNodesRequired(event.getType()) && !event.hasNodes()
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
    if (events.size() < 2) {
      return;
    }

    // Please note: In contrast to other methods in this class, this method loops through the
    // list in FORWARD direction (so the oldest event will be kept).
    Map<Integer, List<TreeEvent>> predecessorEventsOfSameType = new HashMap<>();
    int currentEventGroupType = -1;

    for (ListIterator<TreeEvent> it = events.listIterator(); it.hasNext();) {
      final TreeEvent event = it.next();

      if (event.getType() != currentEventGroupType) {
        // first event of next group. Initialize group related data.
        currentEventGroupType = event.getType();
        predecessorEventsOfSameType.clear();
        if (lookAheadEventType(it) == currentEventGroupType) {
          predecessorEventsOfSameType.put(identicalEventHashCode(event), CollectionUtility.arrayList(event));
        }
        continue;
      }

      // event belongs to the same group. Check whether it is identical to one of its predecessors.
      boolean removed = false;
      int treeEventHashCode = identicalEventHashCode(event);
      List<TreeEvent> identicalEventList = predecessorEventsOfSameType.get(treeEventHashCode);
      if (identicalEventList != null) {
        for (TreeEvent predecessorEvent : identicalEventList) {
          if (isIdenticalEvent(event, predecessorEvent)) {
            it.remove();
            removed = true;
            break;
          }
        }
      }
      if (!removed) {
        if (identicalEventList == null && lookAheadEventType(it) == currentEventGroupType) {
          identicalEventList = new ArrayList<>();
          predecessorEventsOfSameType.put(treeEventHashCode, identicalEventList);
        }
        if (identicalEventList != null) {
          identicalEventList.add(event);
        }
      }
    }
  }

  /**
   * @return Returns the next event's type or <code>-1</code> if {@link ListIterator#hasNext()} returns
   *         <code>false</code>. The iterator is moved back to its initial position (i.e.
   *         {@link ListIterator#previous()}).
   */
  private int lookAheadEventType(ListIterator<TreeEvent> it) {
    if (!it.hasNext()) {
      return -1;
    }
    try {
      return it.next().getType();
    }
    finally {
      it.previous();
    }
  }

  public int identicalEventHashCode(TreeEvent event) {
    final int prime = 31;
    int result = 1;
    result = prime * result + event.getType();
    result = prime * result + (event.isConsumed() ? 1231 : 1237);

    final Object commonParentNode = event.getCommonParentNode();
    result = prime * result + ((commonParentNode == null) ? 0 : commonParentNode.hashCode());

    final Object nodes = event.getNodes();
    result = prime * result + ((nodes == null) ? 0 : nodes.hashCode());

    final Object deselectedNodes = event.getDeselectedNodes();
    result = prime * result + ((deselectedNodes == null) ? 0 : deselectedNodes.hashCode());

    final Object newSelectedNodes = event.getNewSelectedNodes();
    result = prime * result + ((newSelectedNodes == null) ? 0 : newSelectedNodes.hashCode());

    final Object dragObject = event.getDragObject();
    result = prime * result + ((dragObject == null) ? 0 : dragObject.hashCode());

    final Object popupMenus = event.getPopupMenus();
    result = prime * result + ((popupMenus == null) ? 0 : popupMenus.hashCode());

    final Object dropObject = event.getDropObject();
    result = prime * result + ((dropObject == null) ? 0 : dropObject.hashCode());
    return result;
  }

  @Override
  protected boolean isIdenticalEvent(TreeEvent event1, TreeEvent event2) {
    if (event1 == null && event2 == null) {
      return true;
    }
    if (event1 == null || event2 == null) {
      return false;
    }
    boolean identical = event1.getType() == event2.getType()
        && event1.isConsumed() == event2.isConsumed()
        && event1.getNodeCount() == event2.getNodeCount()
        && CompareUtility.equals(event1.getCommonParentNode(), event2.getCommonParentNode())
        && CollectionUtility.equalsCollection(event1.getNodes(), event2.getNodes(), true)
        && CollectionUtility.equalsCollection(event1.getDeselectedNodes(), event2.getDeselectedNodes(), true)
        && CollectionUtility.equalsCollection(event1.getNewSelectedNodes(), event2.getNewSelectedNodes(), true)
        && CollectionUtility.equalsCollection(event1.getPopupMenus(), event2.getPopupMenus())
        && CompareUtility.equals(event1.getDragObject(), event2.getDragObject())
        && CompareUtility.equals(event1.getDropObject(), event2.getDropObject());
    return identical;
  }

  protected Set<Integer> getExpansionRelatedEvents() {
    Set<Integer> res = new HashSet<>();
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

  /**
   * Helper for merging nodes form other {@link TreeEvent}s into the initial target event. <br/>
   * <b>Note</b>: The {@link #merge(TreeEvent)} method does not check any rules. The given event's nodes are merged in
   * any case.<br/>
   * <b>Usage</b>:
   *
   * <pre>
   * TreeEventMerger eventMerger = new TreeEventMerger(targetEvent);
   * eventMerger.merge(e1);
   * eventMerger.merge(e2);
   * eventMerger.complete();
   * </pre>
   */
  protected static class TreeEventMerger {

    private final TreeEvent m_targetEvent;
    private Collection<ITreeNode> m_targetNodes;
    private HashSet<ITreeNode> m_targetNodeSet;
    private List<ITreeNode> m_mergedNodes;

    public TreeEventMerger(TreeEvent targetEvent) {
      m_targetEvent = assertNotNull(targetEvent, "targetEvent must not be null");
      m_mergedNodes = new LinkedList<>();
    }

    /**
     * Merges nodes. Using this method after invoking {@link #complete()} throws an {@link IllegalStateException}.
     */
    public void merge(TreeEvent event) {
      if (m_mergedNodes == null) {
        throw new IllegalStateException("Invocations of merge is not allowed after complete has been invoked.");
      }
      if (!event.hasNodes()) {
        return;
      }
      ensureInitialized();
      mergeCollections(event.getNodes(), m_mergedNodes, m_targetNodeSet);
    }

    protected void ensureInitialized() {
      if (m_targetNodes != null) {
        return;
      }
      m_targetNodes = m_targetEvent.getNodes();
      m_targetNodeSet = new HashSet<>(m_targetNodes);
    }

    /**
     * Completes the merge process. Subsequent invocations of this method does not have any effects.
     */
    public void complete() {
      if (m_mergedNodes == null) {
        return;
      }
      if (m_targetNodes != null) {
        m_mergedNodes.addAll(m_targetNodes);
        m_targetEvent.setNodes(m_mergedNodes);
      }
      m_mergedNodes = null;
    }

    /**
     * Merge collections, such that, if an element is in both collections, only the one of the second collection (later
     * event) is kept.
     */
    protected <TYPE> void mergeCollections(Collection<TYPE> source, List<TYPE> target, HashSet<TYPE> targetSet) {
      for (Iterator<TYPE> it = source.iterator(); it.hasNext();) {
        TYPE sourceElement = it.next();
        if (!targetSet.add(sourceElement)) { // returns true, if the sourceElement has been added; false, if it was already in the set.
          it.remove();
        }
      }
      target.addAll(0, source);
    }
  }

  /**
   * Removes the nodes from the initial delete event from all events passed to the
   * {@link DeletedNodesRemover#removeDeletedNodes(TreeEvent)} method. If a node to delete is part of an insert event,
   * it is removed from the initial delete event as well. <br/>
   * This implementation uses lazy initialization of helper data structure for performance reasons.
   */
  protected static class DeletedNodesRemover {

    private final TreeEvent m_deleteEvent;
    private Map<ITreeNode, Set<ITreeNode>> m_childNodesByNodeToRemove;
    private Set<ITreeNode> m_nodesToRemove;
    private Set<ITreeNode> m_allNodesToRemove;
    private Set<ITreeNode> m_removedNodesCollector;

    public DeletedNodesRemover(TreeEvent deleteEvent) {
      m_deleteEvent = deleteEvent;
    }

    public boolean removeDeletedNodes(TreeEvent event) {
      ensureInitialized();
      if (m_allNodesToRemove.isEmpty()) {
        return true;
      }

      final boolean insertEvent = event.getType() == TreeEvent.TYPE_NODES_INSERTED;
      event.removeNodes(m_allNodesToRemove, insertEvent ? m_removedNodesCollector : null);

      if (!insertEvent) {
        return false;
      }

      for (Iterator<ITreeNode> it = m_nodesToRemove.iterator(); it.hasNext();) {
        final ITreeNode nodeToRemove = it.next();

        if (m_removedNodesCollector.contains(nodeToRemove)) {
          it.remove();
          updateNodesToRemove(nodeToRemove);
          continue;
        }

        // Also consider it as "removed from creation event" if one of the parents of nodeToRemove
        // is a directly inserted node. The nodeToRemove will then not be contained in the insertion
        // event, but because one of its parents was inserted recently, the deletion event is not
        // required anymore (the insertion event does not contain deleted nodes).
        ITreeNode parentToCheck = nodeToRemove.getParentNode();
        while (parentToCheck != null) {
          if (event.containsNode(parentToCheck)) {
            it.remove();
            m_removedNodesCollector.add(nodeToRemove);
            updateNodesToRemove(nodeToRemove);
            break;
          }
          parentToCheck = parentToCheck.getParentNode();
        }
      }
      return m_allNodesToRemove.isEmpty();
    }

    protected void updateNodesToRemove(final ITreeNode nodeToRemove) {
      m_allNodesToRemove.remove(nodeToRemove);
      Set<ITreeNode> childNodesToRemove = m_childNodesByNodeToRemove.remove(nodeToRemove);
      if (childNodesToRemove != null) {
        m_allNodesToRemove.removeAll(childNodesToRemove);
      }
    }

    protected void ensureInitialized() {
      if (m_removedNodesCollector != null) {
        return;
      }
      m_nodesToRemove = new HashSet<>();
      m_allNodesToRemove = new HashSet<>();
      m_childNodesByNodeToRemove = new HashMap<>();
      // collect nodes to remove and their child nodes
      for (ITreeNode node : m_deleteEvent.getNodesSet()) {
        if (node == null) {
          continue;
        }
        node = TreeUtility.unwrapResolvedNode(node);
        m_nodesToRemove.add(node);
        m_allNodesToRemove.add(node);
        if (node.getChildNodeCount() > 0) {
          Set<ITreeNode> collector = new HashSet<>();
          node.collectChildNodes(collector, true);
          m_childNodesByNodeToRemove.put(node, collector);
          m_allNodesToRemove.addAll(collector);
        }
      }
      m_removedNodesCollector = new HashSet<>();
    }

    public void complete() {
      if (CollectionUtility.isEmpty(m_removedNodesCollector)) {
        // the original delete event must not be modified
        return;
      }

      List<ITreeNode> remainingNodes = new ArrayList<>();
      for (ITreeNode node : m_deleteEvent.getNodes()) {
        if (!m_removedNodesCollector.contains(node)) {
          remainingNodes.add(node);
        }
      }
      m_deleteEvent.setNodes(remainingNodes);
    }
  }
}
