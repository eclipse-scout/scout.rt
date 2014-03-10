/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swing.basic.tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EventListener;
import java.util.List;
import java.util.WeakHashMap;

import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;

public class SwingTreeModel implements TreeModel {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwingTreeModel.class);

  private EventListenerList m_listenerList = new EventListenerList();
  private SwingScoutTree m_swingScoutTree;
  // cache
  private WeakHashMap<Object, List<ITreeNode>> m_childMap;
  private WeakHashMap<Object, Integer> m_childIndexMap;

  public SwingTreeModel(SwingScoutTree swingScoutTree) {
    m_swingScoutTree = swingScoutTree;
    clearCache();
  }

  private void clearCache() {
    m_childMap = new WeakHashMap<Object, List<ITreeNode>>();
    m_childIndexMap = new WeakHashMap<Object, Integer>();
  }

  private List<ITreeNode> getCachedChildren(ITreeNode parent) {
    List<ITreeNode> children = m_childMap.get(parent);
    if (!CollectionUtility.hasElements(children)) {
      ITreeNode scoutNode = (ITreeNode) parent;
      children = scoutNode.getFilteredChildNodes();
      m_childMap.put(parent, children);
      for (int i = 0; i < children.size(); i++) {
        m_childIndexMap.put(children.get(i), i);
      }
    }
    return Collections.unmodifiableList(children);
  }

  private int getCachedChildIndex(ITreeNode parent, ITreeNode child) {
    getCachedChildren(parent);
    Integer index = m_childIndexMap.get(child);
    return index != null ? index.intValue() : -1;
  }

  public void notifyScoutModelChanged() {
    fireStructureChanged((ITreeNode) getRoot());
  }

  @Override
  public boolean isLeaf(Object node) {
    ITreeNode scoutNode = (ITreeNode) node;
    return scoutNode.isLeaf();
  }

  @Override
  public Object getRoot() {
    Object root = null;
    if (m_swingScoutTree.getScoutObject() != null) {
      root = m_swingScoutTree.getScoutObject().getRootNode();
    }
    return root;
  }

  @Override
  public int getChildCount(Object parent) {
    List<ITreeNode> cachedChildren = getCachedChildren((ITreeNode) parent);
    if (cachedChildren != null) {
      return cachedChildren.size();
    }
    return 0;
  }

  @Override
  public Object getChild(Object parent, int index) {
    List<ITreeNode> cachedChildren = getCachedChildren((ITreeNode) parent);
    if (cachedChildren != null && cachedChildren.size() > index) {
      return cachedChildren.get(index);
    }
    return null;
  }

  @Override
  public void valueForPathChanged(TreePath path, Object newValue) {
    // void
  }

  @Override
  public int getIndexOfChild(Object parent, Object child) {
    return getCachedChildIndex((ITreeNode) parent, (ITreeNode) child);
  }

  @Override
  public void addTreeModelListener(TreeModelListener l) {
    m_listenerList.add(TreeModelListener.class, l);
  }

  @Override
  public void removeTreeModelListener(TreeModelListener l) {
    m_listenerList.remove(TreeModelListener.class, l);
  }

  /**
   * @param scoutParent
   * @param scoutChildren
   *          If <code>scoutParam</code> is the root node, <code>scoutChildren</code> can be null to indicate the root
   *          node has changed
   */
  protected void fireTreeNodesChanged(ITreeNode scoutParent, List<ITreeNode> scoutChildren) {
    clearCache();
    EventListener[] listeners = m_listenerList.getListeners(TreeModelListener.class);
    if (listeners != null && listeners.length > 0) {
      TreeModelEvent e = new TreeModelEvent(this, SwingScoutTree.scoutNodeToTreePath(scoutParent), scoutNodesToSwingIndexes(scoutParent, scoutChildren), (scoutChildren != null) ? scoutChildren.toArray() : null);
      for (int i = 0; i < listeners.length; i++) {
        ((TreeModelListener) listeners[i]).treeNodesChanged(e);
      }
    }
  }

  protected void fireTreeNodesInserted(ITreeNode scoutParent, List<ITreeNode> scoutChildren) {
    clearCache();
    EventListener[] listeners = m_listenerList.getListeners(TreeModelListener.class);
    if (listeners != null && listeners.length > 0) {
      TreeModelEvent e = new TreeModelEvent(this, SwingScoutTree.scoutNodeToTreePath(scoutParent), scoutNodesToSwingIndexes(scoutParent, scoutChildren), scoutChildren.toArray());
      for (int i = 0; i < listeners.length; i++) {
        ((TreeModelListener) listeners[i]).treeNodesInserted(e);
      }
    }
  }

  protected void fireTreeNodesRemoved(ITreeNode scoutParent, List<ITreeNode> scoutChildren) {
    clearCache();
    EventListener[] listeners = m_listenerList.getListeners(TreeModelListener.class);
    if (listeners != null && listeners.length > 0) {
      TreeModelEvent e = new TreeModelEvent(this, SwingScoutTree.scoutNodeToTreePath(scoutParent), scoutNodesToSwingIndexes(scoutParent, scoutChildren), scoutChildren.toArray());
      for (int i = 0; i < listeners.length; i++) {
        ((TreeModelListener) listeners[i]).treeNodesRemoved(e);
      }
    }
  }

  protected void fireStructureChanged(ITreeNode scoutParent) {
    clearCache();
    EventListener[] listeners = m_listenerList.getListeners(TreeModelListener.class);
    if (listeners != null && listeners.length > 0) {
      TreeModelEvent e = new TreeModelEvent(this, SwingScoutTree.scoutNodeToTreePath(scoutParent));
      for (int i = 0; i < listeners.length; i++) {
        ((TreeModelListener) listeners[i]).treeStructureChanged(e);
      }
    }
  }

  public int[] scoutNodesToSwingIndexes(ITreeNode scoutParent, List<ITreeNode> scoutChildren) {
    if (!CollectionUtility.hasElements(scoutChildren)) {
      return new int[0];
    }
    List<Integer> indexList = new ArrayList<Integer>(scoutChildren.size());
    for (ITreeNode scoutChild : scoutChildren) {
      int i = getCachedChildIndex(scoutParent, scoutChild);
      if (i >= 0) {
        indexList.add(i);
      }
    }
    int[] a = new int[indexList.size()];
    for (int i = 0; i < a.length; i++) {
      a[i] = indexList.get(i);
    }
    Arrays.sort(a);
    return a;
  }

  /**
   * Updates the given node.
   * 
   * @since 3.10.0-M5
   */
  public void updateNode(ITreeNode node) {
    if (node != null) {
      /**
       * To indicate the root has changed, childIndices and children will be null
       * http://docs.oracle.com/javase/7/docs/api/javax/swing/event/TreeModelListener.html
       */
      if (node.getParentNode() == null) {
        fireTreeNodesChanged(node, null);
      }
      else {
        fireTreeNodesChanged(node.getParentNode(), CollectionUtility.arrayList(node));
      }
    }
  }

}
