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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.extension.ui.action.tree.MoveActionNodesHandler;
import org.eclipse.scout.rt.client.extension.ui.basic.tree.ITreeNodeExtension;
import org.eclipse.scout.rt.client.extension.ui.basic.tree.TreeNodeChains.TreeNodeDecorateCellChain;
import org.eclipse.scout.rt.client.extension.ui.basic.tree.TreeNodeChains.TreeNodeDisposeChain;
import org.eclipse.scout.rt.client.extension.ui.basic.tree.TreeNodeChains.TreeNodeInitTreeNodeChain;
import org.eclipse.scout.rt.client.ui.action.ActionFinder;
import org.eclipse.scout.rt.client.ui.action.ActionUtility;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.cell.ICellObserver;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPage;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;
import org.eclipse.scout.rt.platform.util.concurrent.OptimisticLock;
import org.eclipse.scout.rt.shared.data.basic.NamedBitMaskHelper;
import org.eclipse.scout.rt.shared.dimension.IDimensions;
import org.eclipse.scout.rt.shared.extension.AbstractExtension;
import org.eclipse.scout.rt.shared.extension.ContributionComposite;
import org.eclipse.scout.rt.shared.extension.IContributionOwner;
import org.eclipse.scout.rt.shared.extension.IExtensibleObject;
import org.eclipse.scout.rt.shared.extension.IExtension;
import org.eclipse.scout.rt.shared.extension.ObjectExtensions;
import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractTreeNode implements ITreeNode, ICellObserver, IContributionOwner, IExtensibleObject {

  private static final String INITIALIZED = "INITIALIZED";
  private static final String CHILDREN_DIRTY = "CHILDREN_DIRTY";
  private static final String CHILDREN_LOADED = "CHILDREN_LOADED";
  private static final String CHILDREN_VOLATILE = "CHILDREN_VOLATILE";
  private static final String FILTER_ACCEPTED = "FILTER_ACCEPTED";
  private static final String LEAF = "LEAF";
  private static final String REJECTED_BY_USER = "REJECTED_BY_USER";
  private static final String EXPANDED = "EXPANDED";
  private static final String EXPANDED_LAZY = "EXPANDED_LAZY";
  private static final String INITIALLY_EXPANDED = "INITIALLY_EXPANDED";
  private static final String LAZY_EXPANDING_ENABLED = "LAZY_EXPANDING_ENABLED";

  private static final Logger LOG = LoggerFactory.getLogger(AbstractTreeNode.class);
  private static final NamedBitMaskHelper VISIBLE_BIT_HELPER = new NamedBitMaskHelper(IDimensions.VISIBLE, IDimensions.VISIBLE_GRANTED);
  private static final NamedBitMaskHelper ENABLED_BIT_HELPER = new NamedBitMaskHelper(IDimensions.ENABLED, IDimensions.ENABLED_GRANTED);
  private static final NamedBitMaskHelper FLAGS_BIT_HELPER = new NamedBitMaskHelper(INITIALIZED, CHILDREN_DIRTY, CHILDREN_LOADED, CHILDREN_VOLATILE, FILTER_ACCEPTED, LEAF, REJECTED_BY_USER);
  private static final NamedBitMaskHelper EXPANDED_BIT_HELPER = new NamedBitMaskHelper(EXPANDED, EXPANDED_LAZY, INITIALLY_EXPANDED, LAZY_EXPANDING_ENABLED);

  private int m_initializing = 0; // >0 is true
  private ITree m_tree;
  private ITreeNode m_parentNode;

  private final Object m_childNodeListLock;
  private final Object m_filteredChildNodesLock;
  private final OptimisticLock m_childrenLoadedLock;
  private final Cell m_cell;

  private List<ITreeNode> m_childNodeList;
  private volatile List<ITreeNode> m_filteredChildNodes;
  private int m_status;
  private List<IMenu> m_menus;
  private int m_childNodeIndex;
  private Object m_primaryKey;// user object

  /**
   * Provides 8 boolean flags.<br>
   * Currently used: {@link #INITIALIZED}, {@link #EXPANDED}, {@link #EXPANDED_LAZY}, {@link #INITIALLY_EXPANDED},
   * {@link #LAZY_EXPANDING_ENABLED}
   */
  private byte m_expanded;

  /**
   * Provides 8 dimensions for enabled state.<br>
   * Internally used: {@link IDimensions#ENABLED}, {@link IDimensions#ENABLED_GRANTED}.<br>
   * 6 dimensions remain for custom use. This TreeNode is enabled, if all dimensions are enabled (all bits set).
   */
  private byte m_enabled;

  /**
   * Provides 8 dimensions for visibility.<br>
   * Internally used: {@link IDimensions#VISIBLE}, {@link IDimensions#VISIBLE_GRANTED}.<br>
   * 6 dimensions remain for custom use. This TreeNode is visible, if all dimensions are visible (all bits set).
   */
  private byte m_visible;

  /**
   * Provides 8 boolean flags.<br>
   * Currently used: {@link #CHILDREN_DIRTY}, {@link #CHILDREN_LOADED}, {@link #CHILDREN_VOLATILE},
   * {@link #FILTER_ACCEPTED}, {@link #LEAF}, {@link #REJECTED_BY_USER}
   */
  private byte m_flags;

  protected IContributionOwner m_contributionHolder;

  private final ObjectExtensions<AbstractTreeNode, ITreeNodeExtension<? extends AbstractTreeNode>> m_objectExtensions;

  public AbstractTreeNode() {
    this(true);
  }

  public AbstractTreeNode(boolean callInitializer) {
    setFilterAccepted(true);
    m_childNodeListLock = new Object();
    m_childNodeList = new ArrayList<ITreeNode>();
    m_filteredChildNodesLock = new Object();
    m_childrenLoadedLock = new OptimisticLock();
    m_cell = new Cell(this);
    m_enabled = NamedBitMaskHelper.ALL_BITS_SET; // default enabled
    m_visible = NamedBitMaskHelper.ALL_BITS_SET; // default visible
    m_objectExtensions = new ObjectExtensions<AbstractTreeNode, ITreeNodeExtension<? extends AbstractTreeNode>>(this, this instanceof AbstractPage<?>);
    if (callInitializer) {
      callInitializer();
    }
  }

  protected void callInitializer() {
    if (!isInitialized()) {
      interceptInitConfig();
      initTreeNode();
      m_flags = FLAGS_BIT_HELPER.setBit(INITIALIZED, m_flags);
    }
  }

  protected boolean isInitialized() {
    return FLAGS_BIT_HELPER.isBitSet(INITIALIZED, m_flags);
  }

  @Override
  public final List<Object> getAllContributions() {
    return m_contributionHolder.getAllContributions();
  }

  @Override
  public final <T> List<T> getContributionsByClass(Class<T> type) {
    return m_contributionHolder.getContributionsByClass(type);
  }

  @Override
  public final <T> T getContribution(Class<T> contribution) {
    return m_contributionHolder.getContribution(contribution);
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(20)
  protected boolean getConfiguredLeaf() {
    return false;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(30)
  protected boolean getConfiguredExpanded() {
    return false;
  }

  /**
   * Configures whether child nodes should be added lazily to the tree when expanding the node.
   * <p>
   * Subclasses can override this method. Default is {@code false}.
   *
   * @see ITreeNode#isLazyExpandingEnabled()
   * @see ITreeNode#isExpandedLazy()
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(100)
  protected boolean getConfiguredLazyExpandingEnabled() {
    return false;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(10)
  protected boolean getConfiguredEnabled() {
    return true;
  }

  @ConfigOperation
  @Order(20)
  protected void execInitTreeNode() {
  }

  /**
   * called by {@link #dispose()}<br>
   */
  @ConfigOperation
  @Order(25)
  protected void execDispose() {
  }

  @ConfigOperation
  @Order(10)
  protected void execDecorateCell(Cell cell) {
  }

  protected List<Class<? extends IMenu>> getDeclaredMenus() {
    Class<?>[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<IMenu>> filtered = ConfigurationUtility.filterClasses(dca, IMenu.class);
    return ConfigurationUtility.removeReplacedClasses(filtered);
  }

  protected final void interceptInitConfig() {
    m_objectExtensions.initConfigAndBackupExtensionContext(createLocalExtension(), new Runnable() {
      @Override
      public void run() {
        initConfig();
      }
    });
  }

  protected void initConfig() {
    setLeafInternal(getConfiguredLeaf());
    setEnabled(getConfiguredEnabled(), IDimensions.ENABLED);
    setExpandedInternal(getConfiguredExpanded());
    setLazyExpandingEnabled(getConfiguredLazyExpandingEnabled());
    setExpandedLazyInternal(isLazyExpandingEnabled());
    setInitialExpanded(getConfiguredExpanded());
    m_contributionHolder = new ContributionComposite(this);
    // menus
    List<Class<? extends IMenu>> declaredMenus = getDeclaredMenus();
    List<IMenu> contributedMenus = m_contributionHolder.getContributionsByClass(IMenu.class);

    OrderedCollection<IMenu> menus = new OrderedCollection<IMenu>();
    for (Class<? extends IMenu> menuClazz : declaredMenus) {
      IMenu menu = ConfigurationUtility.newInnerInstance(this, menuClazz);
      menus.addOrdered(menu);
    }

    try {
      injectMenusInternal(menus);
    }
    catch (Exception e) {
      LOG.error("error occured while dynamically contribute menus.", e);
    }
    menus.addAllOrdered(contributedMenus);

    new MoveActionNodesHandler<IMenu>(menus).moveModelObjects();
    m_menus = menus.getOrderedList();
  }

  @Override
  public final List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> getAllExtensions() {
    return m_objectExtensions.getAllExtensions();
  }

  protected ITreeNodeExtension<? extends AbstractTreeNode> createLocalExtension() {
    return new LocalTreeNodeExtension<AbstractTreeNode>(this);
  }

  @Override
  public <T extends IExtension<?>> T getExtension(Class<T> c) {
    return m_objectExtensions.getExtension(c);
  }

  /**
   * Executes the given runnable in the extension context, in which this tree node object was created.
   *
   * @see ObjectExtensions#runInExtensionContext(Runnable)
   */
  protected void runInExtensionContext(Runnable runnable) {
    m_objectExtensions.runInExtensionContext(runnable);
  }

  /**
   * Override this internal method only in order to make use of dynamic menus<br>
   * Used to manage menu list and add/remove menus<br>
   * To change the order or specify the insert position use {@link IMenu#setOrder(double)}.
   *
   * @param menus
   *          live and mutable collection of configured menus
   */
  protected void injectMenusInternal(OrderedCollection<IMenu> menus) {
  }

  /*
   * Runtime
   */
  @Override
  public void initTreeNode() {
    setInitializing(true);
    try {
      // init menus
      try {
        ActionUtility.initActions(getMenus());
      }
      catch (RuntimeException e) {
        LOG.error("could not initialize actions.", e);
      }
      interceptInitTreeNode();
    }
    finally {
      setInitializing(false);
    }
  }

  @Override
  public boolean isInitializing() {
    return m_initializing > 0;
  }

  @Override
  public void setInitializing(boolean b) {
    if (b) {
      m_initializing++;
    }
    else {
      m_initializing--;
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
    return m_status;
  }

  /**
   * do not use this method directly use ITree.setNodeStatus...(node,b)
   */
  @Override
  public void setStatusInternal(int status) {
    m_status = status;
  }

  @Override
  public void setStatus(int status) {
    if (getTree() != null) {
      getTree().setNodeStatus(this, status);
    }
    else {
      setStatusInternal(status);
    }
  }

  @Override
  public boolean isStatusInserted() {
    return m_status == STATUS_INSERTED;
  }

  @Override
  public boolean isStatusUpdated() {
    return m_status == STATUS_UPDATED;
  }

  @Override
  public boolean isStatusDeleted() {
    return m_status == STATUS_DELETED;
  }

  @Override
  public boolean isStatusNonchanged() {
    return m_status == STATUS_NON_CHANGED;
  }

  @Override
  public boolean isSelectedNode() {
    if (getTree() != null) {
      return getTree().isSelectedNode(this);
    }
    else {
      return false;
    }
  }

  @Override
  public boolean isFilterAccepted() {
    return FLAGS_BIT_HELPER.isBitSet(FILTER_ACCEPTED, m_flags);
  }

  /**
   * do not use this method directly, use {@link ITree#addNodeFilter(ITreeNodeFilter)},
   * {@link ITree#removeNodeFilter(ITreeNodeFilter)}
   */
  @Override
  public void setFilterAccepted(boolean b) {
    if (FLAGS_BIT_HELPER.isBit(FILTER_ACCEPTED, m_flags, b)) {
      return; // no change
    }

    m_flags = FLAGS_BIT_HELPER.changeBit(FILTER_ACCEPTED, b, m_flags);
    if (getParentNode() != null) {
      getParentNode().resetFilterCache();
    }
  }

  @Override
  public void resetFilterCache() {
    synchronized (m_filteredChildNodesLock) {
      m_filteredChildNodes = null;
    }
  }

  @Override
  public boolean isRejectedByUser() {
    return FLAGS_BIT_HELPER.isBitSet(REJECTED_BY_USER, m_flags);
  }

  @Override
  public void setRejectedByUser(boolean rejectedByUser) {
    m_flags = FLAGS_BIT_HELPER.changeBit(REJECTED_BY_USER, rejectedByUser, m_flags);
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
    try {
      interceptDecorateCell(m_cell);
    }
    catch (Exception t) {
      LOG.error("node {} {}", getClass(), getCell().getText(), t);
    }
  }

  @Override
  public boolean isLeaf() {
    return FLAGS_BIT_HELPER.isBitSet(LEAF, m_flags);
  }

  /**
   * do not use this method directly use ITree.setNodeLeaf(node,b)
   */
  @Override
  public void setLeafInternal(boolean leaf) {
    m_flags = FLAGS_BIT_HELPER.changeBit(LEAF, leaf, m_flags);
  }

  @Override
  public void setLeaf(boolean b) {
    if (getTree() != null) {
      getTree().setNodeLeaf(this, b);
    }
    else {
      setLeafInternal(b);
    }
  }

  @Override
  public boolean isChecked() {
    if (getTree() != null) {
      return getTree().isNodeChecked(this);
    }
    return false;
  }

  @Override
  public void setChecked(boolean checked) {
    if (getTree() != null) {
      getTree().setNodeChecked(this, checked);
    }
  }

  @Override
  public void setChecked(boolean checked, boolean enabledNodesOnly) {
    if (getTree() != null) {
      getTree().setNodeChecked(this, checked, enabledNodesOnly);
    }
  }

  @Override
  public boolean isExpanded() {
    return EXPANDED_BIT_HELPER.isBitSet(EXPANDED, m_expanded);
  }

  @Override
  public void setExpandedInternal(boolean expanded) {
    m_expanded = EXPANDED_BIT_HELPER.changeBit(EXPANDED, expanded, m_expanded);
  }

  @Override
  public boolean isInitialExpanded() {
    return EXPANDED_BIT_HELPER.isBitSet(INITIALLY_EXPANDED, m_expanded);
  }

  @Override
  public void setInitialExpanded(boolean expanded) {
    m_expanded = EXPANDED_BIT_HELPER.changeBit(INITIALLY_EXPANDED, expanded, m_expanded);
  }

  @Override
  public void setExpanded(boolean b) {
    if (getTree() != null) {
      getTree().setNodeExpanded(this, b);
    }
    else {
      setExpandedInternal(b);
    }
  }

  @Override
  public boolean isExpandedLazy() {
    return EXPANDED_BIT_HELPER.isBitSet(EXPANDED_LAZY, m_expanded);
  }

  @Override
  public void setExpandedLazyInternal(boolean expandedLazy) {
    m_expanded = EXPANDED_BIT_HELPER.changeBit(EXPANDED_LAZY, expandedLazy, m_expanded);
  }

  @Override
  public boolean isLazyExpandingEnabled() {
    return EXPANDED_BIT_HELPER.isBitSet(LAZY_EXPANDING_ENABLED, m_expanded);
  }

  @Override
  public void setLazyExpandingEnabled(boolean lazyExpandingEnabled) {
    if (EXPANDED_BIT_HELPER.isBit(LAZY_EXPANDING_ENABLED, m_expanded, lazyExpandingEnabled)) {
      return; // no change
    }

    m_expanded = EXPANDED_BIT_HELPER.changeBit(LAZY_EXPANDING_ENABLED, lazyExpandingEnabled, m_expanded);

    // Also set state of expandedLazy as well -> if lazy expanding gets disabled, it is not expected that expandedLazy is still set to true
    // See also Tree.js _applyUpdatedNodeProperties
    setExpandedLazyInternal(lazyExpandingEnabled && getTree() != null && getTree().isLazyExpandingEnabled());

    if (getTree() != null) {
      getTree().fireNodeChanged(this);
    }
  }

  @Override
  public boolean isVisible() {
    return NamedBitMaskHelper.allBitsSet(m_visible);
  }

  @Override
  public boolean isVisibleGranted() {
    return isVisible(IDimensions.VISIBLE_GRANTED);
  }

  @Override
  public void setVisible(boolean visible, String dimension) {
    m_visible = VISIBLE_BIT_HELPER.changeBit(dimension, visible, m_visible);
  }

  @Override
  public boolean isVisible(String dimension) {
    return VISIBLE_BIT_HELPER.isBitSet(dimension, m_visible);
  }

  @Override
  public void setVisiblePermission(Permission p) {
    if (getTree() != null) {
      getTree().setNodeVisiblePermission(this, p);
    }
    else {
      setVisiblePermission(p, this);
    }
  }

  @Override
  public void setVisible(boolean visible) {
    if (getTree() != null) {
      getTree().setNodeVisible(this, visible);
    }
    else {
      setVisible(visible, IDimensions.VISIBLE);
    }
  }

  @Override
  public void setVisibleGranted(boolean visible) {
    if (getTree() != null) {
      getTree().setNodeVisibleGranted(this, visible);
    }
    else {
      setVisible(visible, IDimensions.VISIBLE_GRANTED);
    }
  }

  public static void setVisiblePermission(Permission p, ITreeNode node) {
    boolean visible = true;
    if (p != null) {
      visible = BEANS.get(IAccessControlService.class).checkPermission(p);
    }
    node.setVisible(visible, IDimensions.VISIBLE_GRANTED);
  }

  @Override
  public boolean isEnabled() {
    return NamedBitMaskHelper.allBitsSet(m_enabled);
  }

  @Override
  public boolean isEnabledGranted() {
    return isEnabled(IDimensions.ENABLED_GRANTED);
  }

  @Override
  public void setEnabled(boolean enabled, String dimension) {
    m_enabled = ENABLED_BIT_HELPER.changeBit(dimension, enabled, m_enabled);
  }

  @Override
  public boolean isEnabled(String dimension) {
    return ENABLED_BIT_HELPER.isBitSet(dimension, m_enabled);
  }

  @Override
  public void setEnabledPermission(Permission p) {
    if (getTree() != null) {
      getTree().setNodeEnabledPermission(this, p);
    }
    else {
      setEnabledPermission(p, this);
    }
  }

  @Override
  public void setEnabled(boolean enabled) {
    if (getTree() != null) {
      getTree().setNodeEnabled(this, enabled);
    }
    else {
      setEnabled(enabled, IDimensions.ENABLED);
    }
  }

  @Override
  public void setEnabledGranted(boolean enabled) {
    if (getTree() != null) {
      getTree().setNodeEnabledGranted(this, enabled);
    }
    else {
      setEnabled(enabled, IDimensions.ENABLED_GRANTED);
    }
  }

  public static void setEnabledPermission(Permission p, ITreeNode node) {
    boolean enabled = true;
    if (p != null) {
      enabled = BEANS.get(IAccessControlService.class).checkPermission(p);
    }
    node.setEnabled(enabled, IDimensions.ENABLED_GRANTED);
  }

  @Override
  public boolean isChildrenVolatile() {
    return FLAGS_BIT_HELPER.isBitSet(CHILDREN_VOLATILE, m_flags);
  }

  @Override
  public void setChildrenVolatile(boolean childrenVolatile) {
    m_flags = FLAGS_BIT_HELPER.changeBit(CHILDREN_VOLATILE, childrenVolatile, m_flags);
  }

  @Override
  public boolean isChildrenDirty() {
    return FLAGS_BIT_HELPER.isBitSet(CHILDREN_DIRTY, m_flags);
  }

  @Override
  public void setChildrenDirty(boolean dirty) {
    m_flags = FLAGS_BIT_HELPER.changeBit(CHILDREN_DIRTY, dirty, m_flags);
  }

  @Override
  public Object getPrimaryKey() {
    return m_primaryKey;
  }

  @Override
  public void setPrimaryKey(Object key) {
    m_primaryKey = key;
  }

  @Override
  public List<IMenu> getMenus() {
    return m_menus;
  }

  @Override
  public <T extends IMenu> T getMenu(Class<T> menuType) {
    // ActionFinder performs instance-of checks. Hence the menu replacement mapping is not required
    return new ActionFinder().findAction(getMenus(), menuType);
  }

  @Override
  public void setMenus(List<? extends IMenu> menus) {
    m_menus = CollectionUtility.arrayListWithoutNullElements(menus);
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
    return m_childNodeList.size();
  }

  @Override
  public int getChildNodeIndex() {
    return m_childNodeIndex;
  }

  @Override
  public void setChildNodeIndexInternal(int index) {
    m_childNodeIndex = index;
  }

  @Override
  public List<ITreeNode> getFilteredChildNodes() {
    if (m_filteredChildNodes == null) {
      synchronized (m_filteredChildNodesLock) {
        if (m_filteredChildNodes == null) {
          synchronized (m_childNodeListLock) {
            List<ITreeNode> list = new ArrayList<ITreeNode>(m_childNodeList.size());
            for (ITreeNode node : m_childNodeList) {
              if (node.isFilterAccepted()) {
                list.add(node);
              }
            }
            m_filteredChildNodes = list;
          }
        }
      }
    }
    return CollectionUtility.arrayList(m_filteredChildNodes);
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
    synchronized (m_childNodeListLock) {
      if (childIndex >= 0 && childIndex < m_childNodeList.size()) {
        return m_childNodeList.get(childIndex);
      }
      else {
        return null;
      }
    }
  }

  @Override
  public List<ITreeNode> getChildNodes() {
    synchronized (m_childNodeListLock) {
      return CollectionUtility.arrayList(m_childNodeList);
    }
  }

  @Override
  public void collectChildNodes(Set<ITreeNode> collector, boolean recursive) {
    synchronized (m_childNodeListLock) {
      for (ITreeNode node : m_childNodeList) {
        if (node == null) {
          continue;
        }
        collector.add(node);
        if (recursive) {
          node.collectChildNodes(collector, recursive);
        }
      }
    }
  }

  @Override
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
  public final void setChildNodeOrderInternal(List<ITreeNode> nodes) {
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
  public final void addChildNodesInternal(int startIndex, List<? extends ITreeNode> nodes, boolean includeSubtree) {
    for (ITreeNode node : nodes) {
      node.setTreeInternal(m_tree, true);
      node.setParentNodeInternal(this);
    }

    synchronized (m_childNodeListLock) {
      m_childNodeList.addAll(startIndex, nodes);
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
  public final void removeChildNodesInternal(Collection<? extends ITreeNode> nodes, boolean includeSubtree, boolean disposeNodes) {

    List<ITreeNode> removedNodes = new ArrayList<ITreeNode>();
    synchronized (m_childNodeListLock) {
      for (ITreeNode node : nodes) {
        if (m_childNodeList.remove(node)) {
          removedNodes.add(node);
        }
        node.setTreeInternal(null, true);
        node.setParentNodeInternal(null);
      }
      int startIndex = 0;
      int endIndex = m_childNodeList.size() - 1;
      for (int i = startIndex; i <= endIndex; i++) {
        m_childNodeList.get(i).setChildNodeIndexInternal(i);
      }
    }
    // inform nodes of remove
    for (ITreeNode removedNode : removedNodes) {
      postProcessRemoveRec(removedNode, getTree(), includeSubtree, disposeNodes);
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
    postProcessRemoveRec(oldNode, m_tree, true, true);
    postProcessAddRec(newNode, true);
    resetFilterCache();
  }

  private void postProcessAddRec(ITreeNode node, boolean includeSubtree) {
    if (node.getTree() != null) {
      try {
        node.nodeAddedNotify();
      }
      catch (Exception t) {
        LOG.error("Could not notify node added {}", node, t);
      }
      // access control after adding the page. The add triggers the
      // page.initPage() which eventually
      // changed the visible property for the page
      if (!node.isVisible()) {
        if (node instanceof AbstractTreeNode) {
          ((AbstractTreeNode) node.getParentNode()).removeChildNodesInternal(CollectionUtility.arrayList(node), false, true);
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
          LOG.warn("The node {} is child of itself!", node);
        }
      }
    }
  }

  @Override
  public void nodeAddedNotify() {
  }

  @Override
  public void nodeRemovedNotify() {
  }

  private void postProcessRemoveRec(ITreeNode node, ITree formerTree, boolean includeSubtree, boolean dispose) {
    if (includeSubtree) {
      for (ITreeNode ch : node.getChildNodes()) {
        postProcessRemoveRec(ch, formerTree, includeSubtree, dispose);
      }
    }
    if (formerTree != null) {
      try {
        node.nodeRemovedNotify();
        if (dispose) {
          node.dispose();
        }
      }
      catch (Exception t) {
        LOG.error("Error removing node", t);
      }
    }
  }

  @Override
  public boolean isChildrenLoaded() {
    return FLAGS_BIT_HELPER.isBitSet(CHILDREN_LOADED, m_flags);
  }

  /**
   * do not use this internal method
   */
  @Override
  public void setChildrenLoaded(boolean loaded) {
    m_flags = FLAGS_BIT_HELPER.changeBit(CHILDREN_LOADED, loaded, m_flags);
  }

  @Override
  public final void ensureChildrenLoaded() {
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
    if (m_tree != null && isExpanded()) {
      m_tree.setNodeExpandedInternal(this, true, isLazyExpandingEnabled());
    }
    if (includeSubtree) {
      synchronized (m_childNodeListLock) {
        for (Iterator<ITreeNode> it = m_childNodeList.iterator(); it.hasNext();) {
          (it.next()).setTreeInternal(tree, includeSubtree);
        }
      }
    }
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

  /*
   * internal cell observer
   */
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

  /**
   * The extension delegating to the local methods. This Extension is always at the end of the chain and will not call
   * any further chain elements.
   */
  protected static class LocalTreeNodeExtension<OWNER extends AbstractTreeNode> extends AbstractExtension<OWNER> implements ITreeNodeExtension<OWNER> {

    public LocalTreeNodeExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public void execDecorateCell(TreeNodeDecorateCellChain chain, Cell cell) {
      getOwner().execDecorateCell(cell);
    }

    @Override
    public void execInitTreeNode(TreeNodeInitTreeNodeChain chain) {
      getOwner().execInitTreeNode();
    }

    @Override
    public void execDispose(TreeNodeDisposeChain chain) {
      getOwner().execDispose();
    }
  }

  protected final void interceptDecorateCell(Cell cell) {
    List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions = getAllExtensions();
    TreeNodeDecorateCellChain chain = new TreeNodeDecorateCellChain(extensions);
    chain.execDecorateCell(cell);
  }

  protected final void interceptInitTreeNode() {
    List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions = getAllExtensions();
    TreeNodeInitTreeNodeChain chain = new TreeNodeInitTreeNodeChain(extensions);
    chain.execInitTreeNode();
  }

  protected final void interceptDispose() {
    List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions = getAllExtensions();
    TreeNodeDisposeChain chain = new TreeNodeDisposeChain(extensions);
    chain.execDispose();
  }

  @Override
  public final void dispose() {
    try {
      disposeInternal();
    }
    catch (RuntimeException e) {
      LOG.warn("Exception while disposing node.", e);
    }
    try {
      interceptDispose();
    }
    catch (RuntimeException e) {
      LOG.warn("Exception while disposing node.", e);
    }
  }

  protected void disposeInternal() {
    for (ITreeNode childNode : getChildNodes()) {
      childNode.dispose();
    }
    ActionUtility.disposeActions(getMenus());
  }
}
