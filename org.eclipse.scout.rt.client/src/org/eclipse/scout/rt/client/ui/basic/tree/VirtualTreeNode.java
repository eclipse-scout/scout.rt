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
package org.eclipse.scout.rt.client.ui.basic.tree;

import java.security.Permission;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.cell.ICellObserver;

public class VirtualTreeNode implements IVirtualTreeNode, ICellObserver {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(VirtualTreeNode.class);

  private ITree m_tree;
  private ITreeNode m_parentNode;
  private ITreeNode m_resolvedNode;
  private boolean m_filterAccepted;
  private final Cell m_cell;
  private int m_childNodeIndex;

  public VirtualTreeNode() {
    m_cell = new Cell(this);
  }

  public void initTreeNode() {
  }

  @Override
  public ITreeNode getResolvedNode() {
    return m_resolvedNode;
  }

  @Override
  public void setResolvedNode(ITreeNode resolvedNode) {
    m_resolvedNode = resolvedNode;
  }

  public String getNodeId() {
    String s = getClass().getName();
    int i = Math.max(s.lastIndexOf('$'), s.lastIndexOf('.'));
    s = s.substring(i + 1);
    return s;
  }

  public int getStatus() {
    return STATUS_NON_CHANGED;
  }

  public void setStatusInternal(int status) {
  }

  public void setStatus(int status) {
  }

  public boolean isStatusInserted() {
    return false;
  }

  public boolean isStatusUpdated() {
    return false;
  }

  public boolean isStatusDeleted() {
    return false;
  }

  public boolean isStatusNonchanged() {
    return true;
  }

  public boolean isSelectedNode() {
    return false;
  }

  public boolean isFilterAccepted() {
    return m_filterAccepted;
  }

  /**
   * do not use this method directly, use {@link ITree#addNodeFilter(ITreeNodeFilter)},
   * {@link ITree#removeNodeFilter(ITreeNodeFilter)}
   */
  public void setFilterAccepted(boolean b) {
    if (m_filterAccepted != b) {
      m_filterAccepted = b;
      if (getParentNode() != null) {
        getParentNode().resetFilterCache();
      }
    }
  }

  public void resetFilterCache() {
  }

  public ITreeNode resolveVirtualChildNode(ITreeNode node) throws ProcessingException {
    return node;
  }

  public final ICell getCell() {
    return m_cell;
  }

  public final Cell getCellForUpdate() {
    return m_cell;
  }

  public final void decorateCell() {
  }

  public boolean isLeaf() {
    return false;
  }

  public void setLeafInternal(boolean b) {
  }

  public void setLeaf(boolean b) {
  }

  public boolean isChecked() {
    return false;
  }

  public void setCheckedInternal(boolean b) {
  }

  public void setChecked(boolean b) {
  }

  public boolean isExpanded() {
    return false;
  }

  public void setExpandedInternal(boolean b) {
  }

  public boolean isInitialExpanded() {
    return false;
  }

  public void setInitialExpanded(boolean b) {
  }

  public void setExpanded(boolean b) {
  }

  public void setVisiblePermissionInternal(Permission p) {
  }

  public boolean isVisible() {
    return true;
  }

  public boolean isVisibleGranted() {
    return true;
  }

  public void setVisibleInternal(boolean b) {
  }

  public void setVisibleGrantedInternal(boolean b) {
  }

  public void setVisiblePermission(Permission p) {
  }

  public void setVisible(boolean b) {
  }

  public void setVisibleGranted(boolean b) {
  }

  public void setEnabledPermissionInternal(Permission p) {
  }

  public boolean isEnabled() {
    return true;
  }

  public boolean isEnabledGranted() {
    return true;
  }

  public void setEnabledInternal(boolean b) {
  }

  public void setEnabledGrantedInternal(boolean b) {
  }

  public void setEnabledPermission(Permission p) {
  }

  public void setEnabled(boolean b) {
  }

  public void setEnabledGranted(boolean b) {
  }

  public boolean isChildrenVolatile() {
    return false;
  }

  public void setChildrenVolatile(boolean childrenVolatile) {
  }

  public boolean isChildrenDirty() {
    return false;
  }

  public void setChildrenDirty(boolean dirty) {
  }

  public Object getPrimaryKey() {
    return null;
  }

  public void setPrimaryKey(Object key) {
  }

  public IMenu[] getMenus() {
    return new IMenu[0];
  }

  public <T extends IMenu> T getMenu(Class<T> menuType) throws ProcessingException {
    return null;
  }

  public void setMenus(IMenu[] a) {
  }

  public ITreeNode getParentNode() {
    return m_parentNode;
  }

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
  public void setParentNodeInternal(ITreeNode parent) {
    m_parentNode = parent;
  }

  public int getChildNodeCount() {
    return 0;
  }

  public void setChildNodeIndexInternal(int childNodeIndex) {
    m_childNodeIndex = childNodeIndex;
  }

  public int getChildNodeIndex() {
    return m_childNodeIndex;
  }

  public ITreeNode[] getFilteredChildNodes() {
    return new ITreeNode[0];
  }

  public int getTreeLevel() {
    int level = 0;
    ITreeNode parent = getParentNode();
    while (parent != null) {
      level++;
      parent = parent.getParentNode();
    }
    return level;
  }

  public ITreeNode getChildNode(int childIndex) {
    return null;
  }

  public ITreeNode[] getChildNodes() {
    return new ITreeNode[0];
  }

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

  public void nodeAddedNotify() {
  }

  public void nodeRemovedNotify() {
  }

  public boolean isChildrenLoaded() {
    return true;
  }

  public void setChildrenLoaded(boolean b) {
  }

  public final void ensureChildrenLoaded() throws ProcessingException {
  }

  public ITree getTree() {
    return m_tree;
  }

  /**
   * do not use this internal method
   */
  public void setTreeInternal(ITree tree, boolean includeSubtree) {
    m_tree = tree;
  }

  public void loadChildren() throws ProcessingException {
  }

  public void update() {
    getTree().updateNode(this);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + getCell() + "]";
  }

  public void cellChanged(ICell cell, int changedBit) {
  }

  public Object validateValue(ICell cell, Object value) throws ProcessingException {
    return value;
  }

}
