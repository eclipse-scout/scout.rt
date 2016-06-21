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

import java.security.Permission;
import java.util.List;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.cell.ICellObserver;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

public class VirtualTreeNode implements IVirtualTreeNode, ICellObserver {
  private ITree m_tree;
  private ITreeNode m_parentNode;
  private ITreeNode m_resolvedNode;
  private boolean m_filterAccepted;
  private boolean m_rejectedByUser;
  private boolean m_enabled;
  private final Cell m_cell;
  private int m_childNodeIndex;

  public VirtualTreeNode() {
    m_cell = new Cell(this);
    m_enabled = true;
  }

  /**
   * do NOT change this hashCode
   */
  @Override
  public final int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj != null && obj == m_resolvedNode) {
      return true;
    }
    return super.equals(obj);
  }

  @Override
  public void initTreeNode() {
  }

  @Override
  public boolean isInitializing() {
    return false;
  }

  @Override
  public void setInitializing(boolean b) {
  }

  @Override
  public ITreeNode getResolvedNode() {
    return m_resolvedNode;
  }

  @Override
  public void setResolvedNode(ITreeNode resolvedNode) {
    m_resolvedNode = resolvedNode;
    if (resolvedNode instanceof AbstractTreeNode) {
      ((AbstractTreeNode) resolvedNode).setHashCode(this.hashCode());
    }
  }

  @Override
  public String getNodeId() {
    String s = getClass().getName();
    int i = Math.max(s.lastIndexOf('$'), s.lastIndexOf('.'));
    s = s.substring(i + 1);
    return s;
  }

  @Override
  public int getStatus() {
    return STATUS_NON_CHANGED;
  }

  @Override
  public void setStatusInternal(int status) {
  }

  @Override
  public void setStatus(int status) {
  }

  @Override
  public boolean isStatusInserted() {
    return false;
  }

  @Override
  public boolean isStatusUpdated() {
    return false;
  }

  @Override
  public boolean isStatusDeleted() {
    return false;
  }

  @Override
  public boolean isStatusNonchanged() {
    return true;
  }

  @Override
  public boolean isSelectedNode() {
    return false;
  }

  @Override
  public boolean isFilterAccepted() {
    return m_filterAccepted;
  }

  /**
   * do not use this method directly, use {@link ITree#addNodeFilter(ITreeNodeFilter)},
   * {@link ITree#removeNodeFilter(ITreeNodeFilter)}
   */
  @Override
  public void setFilterAccepted(boolean b) {
    if (m_filterAccepted != b) {
      m_filterAccepted = b;
      if (getParentNode() != null) {
        getParentNode().resetFilterCache();
      }
    }
  }

  @Override
  public void resetFilterCache() {
  }

  @Override
  public boolean isRejectedByUser() {
    return m_rejectedByUser;
  }

  @Override
  public void setRejectedByUser(boolean rejectedByUser) {
    m_rejectedByUser = rejectedByUser;
  }

  @Override
  public ITreeNode resolveVirtualChildNode(ITreeNode node) {
    return node;
  }

  @Override
  public final ICell getCell() {
    return m_cell;
  }

  @Override
  public final Cell getCellForUpdate() {
    return m_cell;
  }

  @Override
  public final void decorateCell() {
  }

  @Override
  public boolean isLeaf() {
    return false;
  }

  @Override
  public void setLeafInternal(boolean b) {
  }

  @Override
  public void setLeaf(boolean b) {
  }

  @Override
  public boolean isChecked() {
    return false;
  }

  @Override
  public void setChecked(boolean b) {
  }

  @Override
  public boolean isExpanded() {
    return false;
  }

  @Override
  public void setExpandedInternal(boolean b) {
  }

  @Override
  public boolean isExpandedLazy() {
    return false;
  }

  @Override
  public void setExpandedLazyInternal(boolean expandedLazy) {
  }

  @Override
  public boolean isLazyExpandingEnabled() {
    return false;
  }

  @Override
  public void setLazyExpandingEnabled(boolean lazyExpandingEnabled) {
  }

  @Override
  public boolean isInitialExpanded() {
    return false;
  }

  @Override
  public void setInitialExpanded(boolean b) {
  }

  @Override
  public void setExpanded(boolean b) {
  }

  @Override
  public void setVisiblePermissionInternal(Permission p) {
  }

  @Override
  public boolean isVisible() {
    return true;
  }

  @Override
  public boolean isVisibleGranted() {
    return true;
  }

  @Override
  public void setVisibleInternal(boolean b) {
  }

  @Override
  public void setVisibleGrantedInternal(boolean b) {
  }

  @Override
  public void setVisiblePermission(Permission p) {
  }

  @Override
  public void setVisible(boolean b) {
  }

  @Override
  public void setVisibleGranted(boolean b) {
  }

  @Override
  public void setEnabledPermissionInternal(Permission p) {
  }

  @Override
  public boolean isEnabled() {
    return m_enabled;
  }

  @Override
  public boolean isEnabledGranted() {
    return true;
  }

  @Override
  public void setEnabledInternal(boolean b) {
    m_enabled = b;
  }

  @Override
  public void setEnabledGrantedInternal(boolean b) {
  }

  @Override
  public void setEnabledPermission(Permission p) {
  }

  @Override
  public void setEnabled(boolean b) {
    setEnabledInternal(b);
  }

  @Override
  public void setEnabledGranted(boolean b) {
  }

  @Override
  public boolean isChildrenVolatile() {
    return false;
  }

  @Override
  public void setChildrenVolatile(boolean childrenVolatile) {
  }

  @Override
  public boolean isChildrenDirty() {
    return false;
  }

  @Override
  public void setChildrenDirty(boolean dirty) {
  }

  @Override
  public Object getPrimaryKey() {
    return null;
  }

  @Override
  public void setPrimaryKey(Object key) {
  }

  @Override
  public List<IMenu> getMenus() {
    return CollectionUtility.emptyArrayList();
  }

  @Override
  public <T extends IMenu> T getMenu(Class<T> menuType) {
    return null;
  }

  @Override
  public void setMenus(List<? extends IMenu> a) {
  }

  @Override
  public ITreeNode getParentNode() {
    return m_parentNode;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends ITreeNode> T getParentNode(Class<T> type) {
    ITreeNode node = getParentNode();
    if (node != null && type.isAssignableFrom(node.getClass())) {
      return (T) node;
    }
    else {
      return null;
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends ITreeNode> T getParentNode(Class<T> type, int backCount) {
    ITreeNode node = this;
    while (node != null && backCount > 0) {
      node = node.getParentNode();
      backCount--;
    }
    if (backCount == 0 && node != null && type.isAssignableFrom(node.getClass())) {
      return (T) node;
    }
    else {
      return null;
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends ITreeNode> T getAncestorNode(Class<T> type) {
    ITreeNode node = getParentNode();
    while (node != null && !type.isAssignableFrom(node.getClass())) {
      node = node.getParentNode();
    }
    return (T) node;
  }

  /**
   * do not use this internal method
   */
  @Override
  public void setParentNodeInternal(ITreeNode parent) {
    m_parentNode = parent;
  }

  @Override
  public int getChildNodeCount() {
    return 0;
  }

  @Override
  public void setChildNodeIndexInternal(int childNodeIndex) {
    m_childNodeIndex = childNodeIndex;
  }

  @Override
  public int getChildNodeIndex() {
    return m_childNodeIndex;
  }

  @Override
  public List<ITreeNode> getFilteredChildNodes() {
    return CollectionUtility.emptyArrayList();
  }

  @Override
  public int getTreeLevel() {
    int level = 0;
    ITreeNode parent = getParentNode();
    while (parent != null) {
      level++;
      parent = parent.getParentNode();
    }
    return level;
  }

  @Override
  public ITreeNode getChildNode(int childIndex) {
    return null;
  }

  @Override
  public List<ITreeNode> getChildNodes() {
    return CollectionUtility.emptyArrayList();
  }

  @Override
  public boolean containsChildNode(ITreeNode node, boolean recursive) {
    if (equals(node)) {
      return true;
    }
    if (recursive) {
      final ITreeNode resolvedNode = getResolvedNode();
      if (resolvedNode != null) {
        return resolvedNode.containsChildNode(node, recursive);
      }
    }
    return false;
  }

  @Override
  public ITreeNode findParentNode(Class interfaceType) {
    ITreeNode test = getParentNode();
    while (test != null) {
      if (interfaceType.isInstance(test)) {
        break;
      }
      test = test.getParentNode();
    }
    return test;
  }

  @Override
  public void nodeAddedNotify() {
  }

  @Override
  public void nodeRemovedNotify() {
  }

  @Override
  public boolean isChildrenLoaded() {
    return true;
  }

  @Override
  public void setChildrenLoaded(boolean b) {
  }

  @Override
  public final void ensureChildrenLoaded() {
  }

  @Override
  public ITree getTree() {
    return m_tree;
  }

  /**
   * do not use this internal method
   */
  @Override
  public void setTreeInternal(ITree tree, boolean includeSubtree) {
    m_tree = tree;
  }

  @Override
  public void loadChildren() {
  }

  @Override
  public void update() {
    getTree().updateNode(this);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + getCell() + "]";
  }

  @Override
  public void cellChanged(ICell cell, int changedBit) {
    if (getTree() != null) {
      getTree().fireNodeChanged(this);
    }
  }

  @Override
  public Object validateValue(ICell cell, Object value) {
    return value;
  }

  @Override
  public void dispose() {
  }

}
