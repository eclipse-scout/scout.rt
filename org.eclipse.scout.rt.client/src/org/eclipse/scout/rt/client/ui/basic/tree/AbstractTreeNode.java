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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.OptimisticLock;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.ConfigPropertyValue;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.ActionFinder;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.cell.ICellObserver;
import org.eclipse.scout.rt.client.ui.profiler.DesktopProfiler;
import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;
import org.eclipse.scout.service.SERVICES;

public abstract class AbstractTreeNode implements ITreeNode, ICellObserver {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractTreeNode.class);

  private boolean m_initialized;
  private ITree m_tree;
  private ITreeNode m_parentNode;
  private final Object m_childNodeListLock;
  private List<ITreeNode> m_childNodeList;
  private final Object m_filteredChildNodesLock;
  private ITreeNode[] m_filteredChildNodes;
  private int m_status;
  private final Cell m_cell;
  private IMenu[] m_menus;
  private int m_childNodeIndex;
  private boolean m_childrenLoaded;
  private final OptimisticLock m_childrenLoadedLock = new OptimisticLock();
  private boolean m_leaf;
  private boolean m_checked;
  private boolean m_defaultExpanded;
  private boolean m_expanded;
  private boolean m_childrenVolatile;
  private boolean m_childrenDirty;
  private boolean m_filterAccepted;
  private Object m_primaryKey;// user object
  // enabled is defined as: enabledGranted && enabledProperty
  private boolean m_enabledGranted;
  private boolean m_enabledProperty;
  // visible is defined as: visibleGranted && visibleProperty
  private boolean m_visible;
  private boolean m_visibleGranted;
  private boolean m_visibleProperty;
  // hash code is received from a virtual tree node when resolving to this node
  // this node is not attached to a virtual node if m_hashCode is null
  private Integer m_hashCode = null;

  public AbstractTreeNode() {
    this(true);
  }

  public AbstractTreeNode(boolean callInitializer) {
    if (DesktopProfiler.getInstance().isEnabled()) {
      DesktopProfiler.getInstance().registerTreeNode(this);
    }
    m_filterAccepted = true;
    m_visibleGranted = true;
    m_visibleProperty = true;
    calculateVisible();
    m_enabledGranted = true;
    m_childNodeListLock = new Object();
    m_childNodeList = new ArrayList<ITreeNode>(0);
    m_filteredChildNodesLock = new Object();
    m_cell = new Cell(this);
    if (callInitializer) {
      callInitializer();
    }
  }

  protected void callInitializer() {
    if (!m_initialized) {
      initConfig();
      initTreeNode();
      m_initialized = true;
    }
  }

  protected void ensureInitialized() {
    if (!m_initialized) {
      callInitializer();
    }
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(20)
  @ConfigPropertyValue("false")
  protected boolean getConfiguredLeaf() {
    return false;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(30)
  @ConfigPropertyValue("false")
  protected boolean getConfiguredExpanded() {
    return false;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(10)
  @ConfigPropertyValue("true")
  protected boolean getConfiguredEnabled() {
    return true;
  }

  @ConfigOperation
  @Order(20)
  protected void execInitTreeNode() {
  }

  @ConfigOperation
  @Order(10)
  protected void execDecorateCell(Cell cell) {
  }

  @ConfigOperation
  @Order(30)
  protected ITreeNode execResolveVirtualChildNode(IVirtualTreeNode node) throws ProcessingException {
    return node;
  }

  private Class<? extends IMenu>[] getConfiguredMenus() {
    Class<?>[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    return ConfigurationUtility.sortFilteredClassesByOrderAnnotation(dca, IMenu.class);
  }

  protected void initConfig() {
    setLeafInternal(getConfiguredLeaf());
    setEnabledInternal(getConfiguredEnabled());
    setExpandedInternal(getConfiguredExpanded());
    m_defaultExpanded = getConfiguredExpanded();
    // menus
    ArrayList<IMenu> menuList = new ArrayList<IMenu>();
    Class<? extends IMenu>[] ma = getConfiguredMenus();
    for (int i = 0; i < ma.length; i++) {
      try {
        IMenu menu = ConfigurationUtility.newInnerInstance(this, ma[i]);
        menuList.add(menu);
      }
      catch (Exception e) {
        LOG.warn(null, e);
      }
    }

    try {
      injectMenusInternal(menuList);
    }
    catch (Exception e) {
      LOG.error("error occured while dynamically contribute menus.", e);
    }
    m_menus = menuList.toArray(new IMenu[0]);
  }

  /**
   * Override this internal method only in order to make use of dynamic menus<br>
   * Used to manage menu list and add/remove menus
   * 
   * @param menuList
   *          live and mutable list of configured menus
   */
  protected void injectMenusInternal(List<IMenu> menuList) {
  }

  @Override
  public int hashCode() {
    if (m_hashCode != null) {
      return m_hashCode.intValue();
    }
    return super.hashCode();
  }

  /**
   * This method sets the internally used hash code. Should only be used by {@link VirtualTreeNode} when resolving this
   * real node.
   * 
   * @param hashCode
   */
  void setHashCode(int hashCode) {
    if (m_hashCode != null) {
      LOG.warn("Overriding the hash code of an object will lead to inconsistent behavior of hash maps etc." +
          " setHashCode() must not be called more than once.");
    }
    m_hashCode = Integer.valueOf(hashCode);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof IVirtualTreeNode && ((IVirtualTreeNode) obj).getResolvedNode() == this) {
      return true;
    }
    return super.equals(obj);
  }

  /*
   * Runtime
   */
  public void initTreeNode() {
    execInitTreeNode();
  }

  public String getNodeId() {
    String s = getClass().getName();
    int i = Math.max(s.lastIndexOf('$'), s.lastIndexOf('.'));
    s = s.substring(i + 1);
    return s;
  }

  public int getStatus() {
    return m_status;
  }

  /**
   * do not use this method directly use ITree.setNodeStatus...(node,b)
   */
  public void setStatusInternal(int status) {
    m_status = status;
  }

  public void setStatus(int status) {
    if (getTree() != null) {
      getTree().setNodeStatus(this, status);
    }
    else {
      setStatusInternal(status);
    }
  }

  public boolean isStatusInserted() {
    return m_status == STATUS_INSERTED;
  }

  public boolean isStatusUpdated() {
    return m_status == STATUS_UPDATED;
  }

  public boolean isStatusDeleted() {
    return m_status == STATUS_DELETED;
  }

  public boolean isStatusNonchanged() {
    return m_status == STATUS_NON_CHANGED;
  }

  public boolean isSelectedNode() {
    if (getTree() != null) {
      return getTree().isSelectedNode(this);
    }
    else {
      return false;
    }
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
    synchronized (m_filteredChildNodesLock) {
      m_filteredChildNodes = null;
    }
  }

  public ITreeNode resolveVirtualChildNode(ITreeNode node) throws ProcessingException {
    if (m_tree != null) {
      if (node instanceof IVirtualTreeNode) {
        if (node.getTree() == m_tree && node.getParentNode() == this) {
          try {
            m_tree.setTreeChanging(true);
            //
            ITreeNode resolvedNode = execResolveVirtualChildNode((IVirtualTreeNode) node);
            if (node != resolvedNode) {
              if (resolvedNode == null) {
                m_tree.removeChildNode(this, node);
              }
              else {
                replaceChildNodeInternal(node.getChildNodeIndex(), resolvedNode);
                m_tree.updateNode(resolvedNode);
              }
              return resolvedNode;
            }
          }
          finally {
            m_tree.setTreeChanging(false);
          }
        }
      }
    }
    return node;
  }

  public final ICell getCell() {
    return m_cell;
  }

  public final Cell getCellForUpdate() {
    return m_cell;
  }

  public final void decorateCell() {
    try {
      execDecorateCell(m_cell);
    }
    catch (Throwable t) {
      LOG.warn("node " + getClass() + " " + getCell().getText(), t);
    }
  }

  public boolean isLeaf() {
    return m_leaf;
  }

  /**
   * do not use this method directly use ITree.setNodeLeaf(node,b)
   */
  public void setLeafInternal(boolean b) {
    m_leaf = b;
  }

  public void setLeaf(boolean b) {
    if (getTree() != null) {
      getTree().setNodeLeaf(this, b);
    }
    else {
      setLeafInternal(b);
    }
  }

  public boolean isChecked() {
    return m_checked;
  }

  /**
   * do not use this method directly use ITree.setNodeLeaf(node,b)
   */
  public void setCheckedInternal(boolean b) {
    m_checked = b;
  }

  public void setChecked(boolean b) {
    if (getTree() != null) {
      getTree().setNodeChecked(this, b);
    }
    else {
      setCheckedInternal(b);
    }
  }

  public boolean isExpanded() {
    return m_expanded;
  }

  public void setExpandedInternal(boolean b) {
    m_expanded = b;
  }

  public boolean isInitialExpanded() {
    return m_defaultExpanded;
  }

  public void setInitialExpanded(boolean b) {
    m_defaultExpanded = b;
  }

  public void setExpanded(boolean b) {
    if (getTree() != null) {
      getTree().setNodeExpanded(this, b);
    }
    else {
      setExpandedInternal(b);
    }
  }

  public void setVisiblePermissionInternal(Permission p) {
    boolean b;
    if (p != null) {
      b = SERVICES.getService(IAccessControlService.class).checkPermission(p);
    }
    else {
      b = true;
    }
    setVisibleGrantedInternal(b);
  }

  public boolean isVisible() {
    return m_visible;
  }

  public boolean isVisibleGranted() {
    return m_visibleGranted;
  }

  public void setVisibleInternal(boolean b) {
    m_visibleProperty = b;
    calculateVisible();
  }

  public void setVisibleGrantedInternal(boolean b) {
    m_visibleGranted = b;
    calculateVisible();
  }

  private void calculateVisible() {
    m_visible = m_visibleGranted && m_visibleProperty;
  }

  public void setVisiblePermission(Permission p) {
    if (getTree() != null) {
      getTree().setNodeVisiblePermission(this, p);
    }
    else {
      setVisiblePermissionInternal(p);
    }
  }

  public void setVisible(boolean b) {
    if (getTree() != null) {
      getTree().setNodeVisible(this, b);
    }
    else {
      setVisibleInternal(b);
    }
  }

  public void setVisibleGranted(boolean b) {
    if (getTree() != null) {
      getTree().setNodeVisibleGranted(this, b);
    }
    else {
      setVisibleGrantedInternal(b);
    }
  }

  public void setEnabledPermissionInternal(Permission p) {
    boolean b;
    if (p != null) {
      b = SERVICES.getService(IAccessControlService.class).checkPermission(p);
    }
    else {
      b = true;
    }
    setEnabledGrantedInternal(b);
  }

  public boolean isEnabled() {
    return m_cell.isEnabled();
  }

  public boolean isEnabledGranted() {
    return m_enabledGranted;
  }

  public void setEnabledInternal(boolean b) {
    m_enabledProperty = b;
    calculateEnabled();
  }

  public void setEnabledGrantedInternal(boolean b) {
    m_enabledGranted = b;
    calculateEnabled();
  }

  private void calculateEnabled() {
    m_cell.setEnabled(m_enabledGranted && m_enabledProperty);
  }

  public void setEnabledPermission(Permission p) {
    if (getTree() != null) {
      getTree().setNodeEnabledPermission(this, p);
    }
    else {
      setEnabledPermissionInternal(p);
    }
  }

  public void setEnabled(boolean b) {
    if (getTree() != null) {
      getTree().setNodeEnabled(this, b);
    }
    else {
      setEnabledInternal(b);
    }
  }

  public void setEnabledGranted(boolean b) {
    if (getTree() != null) {
      getTree().setNodeEnabledGranted(this, b);
    }
    else {
      setEnabledGrantedInternal(b);
    }
  }

  public boolean isChildrenVolatile() {
    return m_childrenVolatile;
  }

  public void setChildrenVolatile(boolean childrenVolatile) {
    m_childrenVolatile = childrenVolatile;
  }

  public boolean isChildrenDirty() {
    return m_childrenDirty;
  }

  public void setChildrenDirty(boolean dirty) {
    m_childrenDirty = dirty;
  }

  public Object getPrimaryKey() {
    return m_primaryKey;
  }

  public void setPrimaryKey(Object key) {
    m_primaryKey = key;
  }

  public IMenu[] getMenus() {
    return m_menus;
  }

  public <T extends IMenu> T getMenu(Class<T> menuType) throws ProcessingException {
    return new ActionFinder().findAction(getMenus(), menuType);
  }

  public void setMenus(IMenu[] a) {
    m_menus = a;
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
    return m_childNodeList.size();
  }

  public int getChildNodeIndex() {
    return m_childNodeIndex;
  }

  public void setChildNodeIndexInternal(int index) {
    m_childNodeIndex = index;
  }

  public ITreeNode[] getFilteredChildNodes() {
    synchronized (m_filteredChildNodesLock) {
      if (m_filteredChildNodes == null) {
        synchronized (m_childNodeListLock) {
          ArrayList<ITreeNode> list = new ArrayList<ITreeNode>(m_childNodeList.size());
          for (ITreeNode node : m_childNodeList) {
            if (node.isFilterAccepted()) {
              list.add(node);
            }
          }
          m_filteredChildNodes = list.toArray(new ITreeNode[list.size()]);
        }
      }
    }
    return m_filteredChildNodes;
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
    synchronized (m_childNodeListLock) {
      if (childIndex >= 0 && childIndex < m_childNodeList.size()) {
        return m_childNodeList.get(childIndex);
      }
      else {
        return null;
      }
    }
  }

  public ITreeNode[] getChildNodes() {
    synchronized (m_childNodeListLock) {
      return m_childNodeList.toArray(new ITreeNode[m_childNodeList.size()]);
    }
  }

  public ITreeNode findParentNode(Class<?> interfaceType) {
    ITreeNode test = getParentNode();
    while (test != null) {
      if (interfaceType.isInstance(test)) {
        break;
      }
      test = test.getParentNode();
    }
    return test;
  }

  /**
   * do not use this internal method
   */
  public final void setChildNodeOrderInternal(ITreeNode[] nodes) {
    synchronized (m_childNodeListLock) {
      ArrayList<ITreeNode> newList = new ArrayList<ITreeNode>(m_childNodeList.size());
      int index = 0;
      for (ITreeNode n : nodes) {
        n.setChildNodeIndexInternal(index);
        newList.add(n);
        index++;
      }
      m_childNodeList = newList;
    }
    resetFilterCache();
  }

  /**
   * do not use this internal method
   */
  public final void addChildNodesInternal(int startIndex, ITreeNode[] nodes, boolean includeSubtree) {
    for (int i = 0; i < nodes.length; i++) {
      nodes[i].setTreeInternal(m_tree, true);
      nodes[i].setParentNodeInternal(this);
    }
    synchronized (m_childNodeListLock) {
      m_childNodeList.addAll(startIndex, Arrays.asList(nodes));
      int endIndex = m_childNodeList.size() - 1;
      for (int i = startIndex; i <= endIndex; i++) {
        m_childNodeList.get(i).setChildNodeIndexInternal(i);
      }
    }
    // traverse subtree for add / remove notify
    for (ITreeNode node : nodes) {
      postProcessAddRec(node, includeSubtree);
    }
    resetFilterCache();
  }

  /**
   * do not use this internal method
   */
  public final void removeChildNodesInternal(ITreeNode[] nodes, boolean includeSubtree) {
    boolean[] removed = new boolean[nodes.length];
    synchronized (m_childNodeListLock) {
      for (int i = 0; i < nodes.length; i++) {
        removed[i] = m_childNodeList.remove(nodes[i]);
        nodes[i].setTreeInternal(null, true);
        nodes[i].setParentNodeInternal(null);
      }
      int startIndex = 0;
      int endIndex = m_childNodeList.size() - 1;
      for (int i = startIndex; i <= endIndex; i++) {
        m_childNodeList.get(i).setChildNodeIndexInternal(i);
      }
    }
    // inform nodes of remove
    for (int i = 0; i < nodes.length; i++) {
      if (removed[i]) {
        postProcessRemoveRec(nodes[i], getTree(), includeSubtree);
      }
    }
    resetFilterCache();
  }

  /**
   * do not use this internal method
   */
  public final void replaceChildNodeInternal(int index, ITreeNode newNode) {
    ITreeNode oldNode;
    synchronized (m_childNodeListLock) {
      //remove old
      oldNode = m_childNodeList.get(index);
      oldNode.setTreeInternal(null, true);
      oldNode.setParentNodeInternal(null);
      //add new
      m_childNodeList.set(index, newNode);
      newNode.setTreeInternal(m_tree, true);
      newNode.setParentNodeInternal(this);
      m_childNodeList.get(index).setChildNodeIndexInternal(index);
    }
    postProcessRemoveRec(oldNode, m_tree, true);
    postProcessAddRec(newNode, true);
    resetFilterCache();
  }

  private void postProcessAddRec(ITreeNode node, boolean includeSubtree) {
    if (node.getTree() != null) {
      try {
        node.nodeAddedNotify();
      }
      catch (Throwable t) {
        t.printStackTrace();
      }
      // access control after adding the page. The add triggers the
      // page.initPage() which eventually
      // changed the visible property for the page
      if (!node.isVisible()) {
        if (node instanceof AbstractTreeNode) {
          ((AbstractTreeNode) node.getParentNode()).removeChildNodesInternal(new ITreeNode[]{node}, false);
        }
        return;
      }
    }
    if (node.isChildrenLoaded()) {
      node.setLeafInternal(node.getChildNodeCount() == 0);
    }
    if (includeSubtree) {
      for (ITreeNode ch : node.getChildNodes()) {
        //quick-check: is node child of itself
        if (ch != node) {
          postProcessAddRec(ch, includeSubtree);
        }
        else {
          LOG.warn("The node " + node + " is child of itself!");
        }
      }
    }
  }

  public void nodeAddedNotify() {
  }

  public void nodeRemovedNotify() {
  }

  private void postProcessRemoveRec(ITreeNode node, ITree formerTree, boolean includeSubtree) {
    if (includeSubtree) {
      for (ITreeNode ch : node.getChildNodes()) {
        postProcessRemoveRec(ch, formerTree, includeSubtree);
      }
    }
    if (formerTree != null) {
      try {
        node.nodeRemovedNotify();
      }
      catch (Throwable t) {
        t.printStackTrace();
      }
    }
  }

  public boolean isChildrenLoaded() {
    return m_childrenLoaded;
  }

  /**
   * do not use this internal method
   */
  public void setChildrenLoaded(boolean b) {
    m_childrenLoaded = b;
  }

  public final void ensureChildrenLoaded() throws ProcessingException {
    if (!isChildrenLoaded()) {
      // avoid loop
      try {
        if (m_childrenLoadedLock.acquire()) {
          loadChildren();
        }
      }
      finally {
        m_childrenLoadedLock.release();
      }
    }
  }

  public ITree getTree() {
    return m_tree;
  }

  /**
   * do not use this internal method
   */
  public void setTreeInternal(ITree tree, boolean includeSubtree) {
    m_tree = tree;
    if (includeSubtree) {
      synchronized (m_childNodeListLock) {
        for (Iterator<ITreeNode> it = m_childNodeList.iterator(); it.hasNext();) {
          (it.next()).setTreeInternal(tree, includeSubtree);
        }
      }
    }
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

  /*
   * internal cell observer
   */
  public void cellChanged(ICell cell, int changedBit) {
  }

  public Object validateValue(ICell cell, Object value) throws ProcessingException {
    return value;
  }
}
