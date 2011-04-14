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
import java.util.EventListener;
import java.util.WeakHashMap;

import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;

public class SwingTreeModel implements TreeModel {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwingTreeModel.class);

  private EventListenerList m_listenerList = new EventListenerList();
  private SwingScoutTree m_swingScoutTree;
  // cache
  private WeakHashMap<Object, Object[]> m_childMap;
  private WeakHashMap<Object, Integer> m_childIndexMap;

  public SwingTreeModel(SwingScoutTree swingScoutTree) {
    m_swingScoutTree = swingScoutTree;
    clearCache();
  }

  private void clearCache() {
    m_childMap = new WeakHashMap<Object, Object[]>();
    m_childIndexMap = new WeakHashMap<Object, Integer>();
  }

  private Object[] getCachedChildren(Object parent) {
    Object[] children = m_childMap.get(parent);
    if (children == null || children.length == 0) {
      ITreeNode scoutNode = (ITreeNode) parent;
      children = scoutNode.getFilteredChildNodes();
      m_childMap.put(parent, children);
      for (int i = 0; i < children.length; i++) {
        m_childIndexMap.put(children[i], i);
      }
    }
    return children;
  }

  private int getCachedChildIndex(Object parent, Object child) {
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
    return getCachedChildren(parent).length;
  }

  @Override
  public Object getChild(Object parent, int index) {
    Object[] cachedChildren = getCachedChildren(parent);
    if (cachedChildren != null && cachedChildren.length > index) {
      return cachedChildren[index];
    }
    return null;
  }

  @Override
  public void valueForPathChanged(TreePath path, Object newValue) {
    // void
  }

  @Override
  public int getIndexOfChild(Object parent, Object child) {
    return getCachedChildIndex(parent, child);
  }

  @Override
  public void addTreeModelListener(TreeModelListener l) {
    m_listenerList.add(TreeModelListener.class, l);
  }

  @Override
  public void removeTreeModelListener(TreeModelListener l) {
    m_listenerList.remove(TreeModelListener.class, l);
  }

  protected void fireTreeNodesChanged(ITreeNode scoutParent, ITreeNode[] scoutChildren) {
    clearCache();
    EventListener[] listeners = m_listenerList.getListeners(TreeModelListener.class);
    if (listeners != null && listeners.length > 0) {
      TreeModelEvent e = new TreeModelEvent(this, SwingScoutTree.scoutNodeToTreePath(scoutParent), scoutNodesToSwingIndexes(scoutParent, scoutChildren), scoutChildren);
      for (int i = 0; i < listeners.length; i++) {
        ((TreeModelListener) listeners[i]).treeNodesChanged(e);
      }
    }
  }

  protected void fireTreeNodesInserted(ITreeNode scoutParent, ITreeNode[] scoutChildren) {
    clearCache();
    EventListener[] listeners = m_listenerList.getListeners(TreeModelListener.class);
    if (listeners != null && listeners.length > 0) {
      TreeModelEvent e = new TreeModelEvent(this, SwingScoutTree.scoutNodeToTreePath(scoutParent), scoutNodesToSwingIndexes(scoutParent, scoutChildren), scoutChildren);
      for (int i = 0; i < listeners.length; i++) {
        ((TreeModelListener) listeners[i]).treeNodesInserted(e);
      }
    }
  }

  protected void fireTreeNodesRemoved(ITreeNode scoutParent, ITreeNode[] scoutChildren) {
    clearCache();
    EventListener[] listeners = m_listenerList.getListeners(TreeModelListener.class);
    if (listeners != null && listeners.length > 0) {
      TreeModelEvent e = new TreeModelEvent(this, SwingScoutTree.scoutNodeToTreePath(scoutParent), scoutNodesToSwingIndexes(scoutParent, scoutChildren), scoutChildren);
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

  public int[] scoutNodesToSwingIndexes(Object scoutParent, Object[] scoutChildren) {
    if (scoutChildren == null || scoutChildren.length == 0) return new int[0];
    ArrayList<Integer> indexList = new ArrayList<Integer>(scoutChildren.length);
    for (Object scoutChild : scoutChildren) {
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

}
