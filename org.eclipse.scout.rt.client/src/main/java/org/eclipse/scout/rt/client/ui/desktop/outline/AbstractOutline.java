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
package org.eclipse.scout.rt.client.ui.desktop.outline;

import java.security.Permission;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.extension.ui.basic.tree.ITreeExtension;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.IOutlineExtension;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.OutlineChains.OutlineActivatedChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.OutlineChains.OutlineCreateChildPagesChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.OutlineChains.OutlineCreateRootPageChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.OutlineChains.OutlineDeactivatedChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.OutlineChains.OutlineInitDefaultDetailFormChain;
import org.eclipse.scout.rt.client.ui.AbstractEventBuffer;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TreeMenuType;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNodeFilter;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeVisitor;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeAdapter;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeEvent;
import org.eclipse.scout.rt.client.ui.desktop.outline.OutlineMenuWrapper.IMenuTypeMapper;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithNodes;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithNodes;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.ISearchForm;
import org.eclipse.scout.rt.client.ui.dnd.TransferObject;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IOrdered;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.exception.PlatformError;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.holders.Holder;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.platform.util.concurrent.OptimisticLock;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.shared.data.basic.NamedBitMaskHelper;
import org.eclipse.scout.rt.shared.dimension.IDimensions;
import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ClassId("fe822e40-23a0-4ec2-b2b0-6357df01d115")
public abstract class AbstractOutline extends AbstractTree implements IOutline {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractOutline.class);
  private static final NamedBitMaskHelper VISIBLE_BIT_HELPER = new NamedBitMaskHelper(IDimensions.VISIBLE, IDimensions.VISIBLE_GRANTED);
  private static final IMenuTypeMapper TABLE_MENU_TYPE_MAPPER = new IMenuTypeMapper() {
    @Override
    public IMenuType map(IMenuType menuType) {
      if (menuType == TableMenuType.EmptySpace || menuType == TableMenuType.SingleSelection) {
        return TreeMenuType.SingleSelection;
      }
      return menuType;
    }
  };

  /**
   * Provides 8 dimensions for visibility.<br>
   * Internally used: {@link IDimensions#VISIBLE}, {@link IDimensions#VISIBLE_GRANTED}.<br>
   * 6 dimensions remain for custom use. This Outline is visible, if all dimensions are visible (all bits set).
   */
  private byte m_visible;

  private IPage<?> m_contextPage;
  private IPageChangeStrategy m_pageChangeStrategy;
  private OptimisticLock m_contextPageOptimisticLock;
  private OutlineMediator m_outlineMediator;

  // internal usage of menus temporarily added to the tree.
  private List<IMenu> m_inheritedMenusOfPage;

  public AbstractOutline() {
    super();
  }

  public AbstractOutline(boolean callInitialzier) {
    super(callInitialzier);
  }

  @Override
  protected void callInitializer() {
    // Run the initialization on behalf of this Outline.
    ClientRunContexts.copyCurrent()
        .withOutline(this, true)
        .run(new IRunnable() {
          @Override
          public void run() throws Exception {
            AbstractOutline.super.callInitializer();
          }
        });
  }

  /*
   * Configuration
   */

  /**
   * Configures whether this outline is enabled.
   * <p>
   * Subclasses can override this method. Default is {@code true}.
   *
   * @return {@code true} if this outline is enabled, {@code false} otherwise
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(80)
  protected boolean getConfiguredEnabled() {
    return true;
  }

  /**
   * Configures the visibility of this outline.
   * <p>
   * Subclasses can override this method. Default is {@code true}.
   *
   * @return {@code true} if this outline is visible, {@code false} otherwise
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(90)
  protected boolean getConfiguredVisible() {
    return true;
  }

  /**
   * Configures the view order of this outline. The view order determines the order in which the outline appears.<br>
   * The view order of outlines with no view order configured ({@code < 0}) is initialized based on the {@link Order}
   * annotation of the class.
   * <p>
   * Subclasses can override this method. The default is {@link IOrdered#DEFAULT_ORDER}.
   *
   * @return View order of this outline.
   */
  @ConfigProperty(ConfigProperty.DOUBLE)
  @Order(120)
  protected double getConfiguredViewOrder() {
    return IOrdered.DEFAULT_ORDER;
  }

  /**
   * Configures the default detail form to be used with this page. The form is shown when no page is selected.
   * <p>
   * Subclasses can override this method. Default is {@code null}.
   *
   * @return a form type token
   * @see {@link #startDefaultDetailForm(IForm)} for details how the form gets started
   */
  @ConfigProperty(ConfigProperty.FORM)
  @Order(130)
  protected Class<? extends IForm> getConfiguredDefaultDetailForm() {
    return null;
  }

  /**
   * Configures if the outline overview is visible or not. The outline overview is rendered if no default detail form is
   * set (see {@link AbstractOutline#getConfiguredDefaultDetailForm()}. An overview of the outline is rendered in the JS
   * part.
   * <p>
   * Subclasses can override this method. Default is {@code true}.
   */
  protected boolean getConfiguredOutlineOverviewVisible() {
    return true;
  }

  /**
   * @deprecated use {@link AbstractOutline#getConfiguredToggleBreadcrumbStyleEnabled()} instead, will be removed in
   *             Scout 7.0
   */
  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  protected boolean getConfiguredAutoToggleBreadcrumbStyle() {
    return true;
  }

  /**
   * Configures whether the outline should automatically switch to the bread crumb style when getting smaller (either by
   * navigation button or moved splitter) and back when getting bigger. The threshold in case of moved splitter is
   * determined by the GUI.
   * <p>
   * Subclasses can override this method. The default is true.
   *
   * @see #getConfiguredDisplayStyle()
   */
  @Override
  protected boolean getConfiguredToggleBreadcrumbStyleEnabled() {
    return getConfiguredAutoToggleBreadcrumbStyle();
  }

  /**
   * Configures the icon of the outline.
   * <p>
   * The folder icon {@link AbstractIcons#Folder} is used as default.
   */
  @Override
  @Order(150)
  protected String getConfiguredIconId() {
    return AbstractIcons.Folder;
  }

  /**
   * Configures whether the navigate buttons are visible.
   * <p>
   * Navigation buttons give the possibility to navigate down or up the tree.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(160)
  protected boolean getConfiguredNavigateButtonsVisible() {
    return true;
  }

  /**
   * Called during initialization of this outline. Creates the root node of this outline.
   * <p>
   * Subclasses should overwrite either this method or {@link AbstractOutline#execCreateChildPages(List)}.
   * <p>
   * The default creates an {@link AbstractPageWithNodes} which is invisible according to the default of
   * {@link AbstractTree#getConfiguredRootNodeVisible()}
   *
   * @since 5.1
   */
  @ConfigOperation
  @Order(85)
  protected IPage<?> execCreateRootPage() {
    return new InvisibleRootPage();
  }

  /**
   * Called during initialization of this outline. Allows to add child pages to the outline tree. All added pages are
   * children of the invisible root node and thus roots of the visible tree.
   * <p>
   * An outline has an invisible root node unless a custom root node is provided by overwriting
   * {@link AbstractOutline#execCreateRootPage()}. <b>If a custom root node is provided, this method has no effect.<b>
   * <p>
   * Subclasses should overwrite either this method or {@link AbstractOutline#execCreateRootPage()}.
   * <p>
   * The default does nothing.
   *
   * @param pageList
   *          live collection to add pages to the outline tree
   */
  @ConfigOperation
  @Order(90)
  protected void execCreateChildPages(List<IPage<?>> pageList) {
  }

  protected void createChildPagesInternal(List<IPage<?>> pageList) {
    interceptCreateChildPages(pageList);
  }

  /**
   * By default the outline tree tries to delegate the drop to the affected page.
   */
  @Override
  protected void execDrop(ITreeNode node, TransferObject t) {
    if (node instanceof IPageWithTable) {
      ITable table = ((IPageWithTable) node).getTable();
      if (table.getDropType() != 0) {
        table.getUIFacade().fireRowDropActionFromUI(null, t);
      }
    }
  }

  /**
   * Initializes the default detail form associated with this page. This method is called before the default detail form
   * is used for the first time.
   * <p>
   * Subclasses can override this method. The default does nothing.
   *
   * @see #ensureDefaultDetailFormCreated()
   * @see #ensureDefaultDetailFormStarted()
   */
  @Order(120)
  @ConfigOperation
  protected void execInitDefaultDetailForm() {
  }

  @Order(130)
  @ConfigOperation
  protected void execActivated() {
  }

  @Order(130)
  @ConfigOperation
  protected void execDeactivated() {
  }

  @Override
  protected void initConfig() {
    m_visible = NamedBitMaskHelper.ALL_BITS_SET; // default visible
    m_contextPageOptimisticLock = new OptimisticLock();
    setPageChangeStrategy(createPageChangeStrategy());
    m_outlineMediator = createOutlineMediator();
    addTreeListener(new P_OutlineListener());
    addNodeFilter(new P_TableFilterBasedTreeNodeFilter());
    super.initConfig();
    IPage<?> rootPage = interceptCreateRootPage();
    setRootNode(rootPage);
    setEnabled(getConfiguredEnabled());
    setVisible(getConfiguredVisible());
    setOrder(calculateViewOrder());
    setNavigateButtonsVisible(getConfiguredNavigateButtonsVisible());
    setOutlineOverviewVisible(getConfiguredOutlineOverviewVisible());
    ensureDefaultDetailFormCreated();
    ensureDefaultDetailFormStarted();
  }

  @Override
  public AbstractEventBuffer<TreeEvent> createEventBuffer() {
    return new OutlineEventBuffer();
  }

  /*
   * Runtime
   */

  @Override
  public IPage<?> getActivePage() {
    return (IPage) getSelectedNode();
  }

  /**
   * Calculates the actions's view order, e.g. if the @Order annotation is set to 30.0, the method will return 30.0. If
   * no {@link Order} annotation is set, the method checks its super classes for an @Order annotation.
   *
   * @since 4.0.1
   */
  protected double calculateViewOrder() {
    double viewOrder = getConfiguredViewOrder();
    if (viewOrder == IOrdered.DEFAULT_ORDER) {
      Class<?> cls = getClass();
      while (cls != null && IOutline.class.isAssignableFrom(cls)) {
        if (cls.isAnnotationPresent(Order.class)) {
          Order order = (Order) cls.getAnnotation(Order.class);
          return order.value();
        }
        cls = cls.getSuperclass();
      }
    }
    return viewOrder;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void refreshPages(Class<?>... pageTypes) {
    if (pageTypes == null || pageTypes.length < 1) {
      return;
    }
    List<Class<? extends IPage>> list = new ArrayList<Class<? extends IPage>>(pageTypes.length);
    for (Class<?> c : pageTypes) {
      if (IPage.class.isAssignableFrom(c)) {
        list.add((Class<? extends IPage>) c);
      }
    }
    refreshPages(list);
  }

  @Override
  public void refreshPages(final List<Class<? extends IPage>> pageTypes) {
    final List<IPage<?>> candidates = new ArrayList<IPage<?>>();
    ITreeVisitor v = new ITreeVisitor() {
      @Override
      public boolean visit(ITreeNode node) {
        IPage<?> page = (IPage) node;
        if (page == null) {
          return true;
        }
        Class<? extends IPage> pageClass = page.getClass();
        for (Class<? extends IPage> c : pageTypes) {
          if (c.isAssignableFrom(pageClass)) {
            candidates.add(page);
          }
        }
        return true;
      }
    };
    visitNode(getRootNode(), v);
    for (IPage<?> page : candidates) {
      if (page.getTree() != null) {
        page.dataChanged();
      }
    }
  }

  @Override
  public void releaseUnusedPages() {
    final HashSet<IPage> preservationSet = new HashSet<IPage>();
    IPage<?> oldSelection = (IPage) getSelectedNode();
    IPage<?> p = oldSelection;
    if (p != null) {
      while (p != null) {
        preservationSet.add(p);
        p = p.getParentPage();
      }
    }
    ITreeVisitor v = new ITreeVisitor() {
      @Override
      public boolean visit(ITreeNode node) {
        IPage<?> page = (IPage) node;
        if (preservationSet.contains(page)) {
          // nop
        }
        else if (page.isChildrenLoaded() && (!page.isExpanded() || !(page.getParentPage() != null && page.getParentPage().isChildrenLoaded()))) {
          try {
            unloadNode(page);
          }
          catch (RuntimeException | PlatformError e) {
            BEANS.get(ExceptionHandler.class).handle(e);
          }
        }
        return true;
      }
    };
    try {
      setTreeChanging(true);
      visitNode(getRootNode(), v);
      if (oldSelection != null) {
        IPage<?> selectedPage = (IPage) getSelectedNode();
        if (selectedPage == null) {
          try {
            getRootNode().ensureChildrenLoaded();
            List<ITreeNode> children = getRootNode().getFilteredChildNodes();
            if (CollectionUtility.hasElements(children)) {
              selectNode(CollectionUtility.firstElement(children));
            }
          }
          catch (RuntimeException | PlatformError e) {
            LOG.warn("Exception while selecting first page in outline [{}]", getClass().getName(), e);
          }
        }
      }
    }
    finally {
      setTreeChanging(false);
    }
  }

  @Override
  public <T extends IPage> T findPage(final Class<T> pageType) {
    final Holder<T> result = new Holder<T>(pageType, null);
    ITreeVisitor v = new ITreeVisitor() {
      @Override
      @SuppressWarnings("unchecked")
      public boolean visit(ITreeNode node) {
        IPage<?> page = (IPage) node;
        Class<? extends IPage> pageClass = page.getClass();
        if (pageType.isAssignableFrom(pageClass)) {
          result.setValue((T) page);
        }
        return result.getValue() == null;
      }
    };
    visitNode(getRootNode(), v);
    return result.getValue();
  }

  @Override
  public void setVisiblePermission(Permission p) {
    boolean b;
    if (p != null) {
      b = BEANS.get(IAccessControlService.class).checkPermission(p);
    }
    else {
      b = true;
    }
    setVisibleGranted(b);
  }

  @Override
  public boolean isVisibleGranted() {
    return isVisible(IDimensions.VISIBLE_GRANTED);
  }

  @Override
  public void setVisibleGranted(boolean visible) {
    setVisible(visible, IDimensions.VISIBLE_GRANTED);
  }

  @Override
  public boolean isVisible() {
    return propertySupport.getPropertyBool(PROP_VISIBLE);
  }

  @Override
  public void setVisible(boolean visible) {
    setVisible(visible, IDimensions.VISIBLE);
  }

  private void calculateVisible() {
    propertySupport.setPropertyBool(PROP_VISIBLE, NamedBitMaskHelper.allBitsSet(m_visible));
  }

  @Override
  public void setVisible(boolean visible, String dimension) {
    m_visible = VISIBLE_BIT_HELPER.changeBit(dimension, visible, m_visible);
    calculateVisible();
  }

  @Override
  public boolean isVisible(String dimension) {
    return VISIBLE_BIT_HELPER.isBitSet(dimension, m_visible);
  }

  @Override
  public boolean isNavigateButtonsVisible() {
    return propertySupport.getPropertyBool(PROP_NAVIGATE_BUTTONS_VISIBLE);
  }

  @Override
  public void setNavigateButtonsVisible(boolean b) {
    propertySupport.setPropertyBool(PROP_NAVIGATE_BUTTONS_VISIBLE, b);
  }

  @Override
  public IForm getDefaultDetailForm() {
    return (IForm) propertySupport.getProperty(PROP_DEFAULT_DETAIL_FORM);
  }

  protected void setDefaultDetailFormInternal(IForm form) {
    propertySupport.setProperty(PROP_DEFAULT_DETAIL_FORM, form);
  }

  @Override
  public IForm getDetailForm() {
    return (IForm) propertySupport.getProperty(PROP_DETAIL_FORM);
  }

  @Override
  public void setDetailForm(IForm form) {
    propertySupport.setProperty(PROP_DETAIL_FORM, form);
  }

  @Override
  public void setOutlineOverviewVisible(boolean visible) {
    propertySupport.setPropertyBool(PROP_OUTLINE_OVERVIEW_VISIBLE, visible);
  }

  @Override
  public boolean isOutlineOverviewVisible() {
    return propertySupport.getPropertyBool(PROP_OUTLINE_OVERVIEW_VISIBLE);
  }

  @Override
  public ITable getDetailTable() {
    return (ITable) propertySupport.getProperty(PROP_DETAIL_TABLE);
  }

  @Override
  public void setDetailTable(ITable table) {
    propertySupport.setProperty(PROP_DETAIL_TABLE, table);
  }

  @Override
  public IForm getSearchForm() {
    return (IForm) propertySupport.getProperty(PROP_SEARCH_FORM);
  }

  @Override
  public void setSearchForm(IForm form) {
    propertySupport.setProperty(PROP_SEARCH_FORM, form);
  }

  @Override
  public double getOrder() {
    return propertySupport.getPropertyDouble(PROP_ORDER);
  }

  @Override
  public void setOrder(double order) {
    propertySupport.setPropertyDouble(PROP_ORDER, order);
  }

  @Override
  public IPage<?> getRootPage() {
    return (IPage) getRootNode();
  }

  @Override
  public void unloadNode(ITreeNode node) {
    try {
      setTreeChanging(true);
      //
      super.unloadNode(node);
      if (node instanceof IPageWithTable) {
        ((IPageWithTable) node).getTable().deleteAllRows();
      }
    }
    finally {
      setTreeChanging(false);
    }
  }

  @Override
  public void resetOutline() {
    if (getRootNode() == null) {
      return;
    }

    ClientRunContexts.copyCurrent()
        .withOutline(this, true)
        .run(new IRunnable() {
          @Override
          public void run() throws Exception {
            setTreeChanging(true);
            try {
              selectNode(null);
              unloadNode(getRootNode());
              getRootNode().ensureChildrenLoaded();
            }
            finally {
              setTreeChanging(false);
            }

            ITreeNode root = getRootNode();
            if (root instanceof IPageWithTable) {
              ISearchForm searchForm = ((IPageWithTable) root).getSearchFormInternal();
              if (searchForm != null) {
                searchForm.doReset();
              }
            }
            if (!isRootNodeVisible()) {
              root.setExpanded(true);
            }
            selectFirstNode();
            if (getSelectedNode() instanceof IPageWithTable) {
              getSelectedNode().setExpanded(true);
            }
          }
        });
  }

  @Override
  public void makeActivePageToContextPage() {
    IPage<?> activePage = getActivePage();
    if (activePage != null && m_contextPage != activePage) {
      m_contextPage = activePage;
      addMenusOfActivePageToContextMenu(activePage);
      activePage.pageActivatedNotify();
    }
  }

  @Override
  public List<IMenu> getMenusForPage(IPage<?> page) {
    List<IMenu> result = new ArrayList<IMenu>();
    for (IMenu m : getContextMenu().getChildActions()) {
      if (!m_inheritedMenusOfPage.contains(m)) {
        result.add(m);
      }
    }
    result.addAll(computeInheritedMenusOfPage(page));
    return result;
  }

  /**
   * @see IOutline#getMenusForPage(IPage)
   */
  protected List<IMenu> computeInheritedMenusOfPage(IPage<?> activePage) {
    List<IMenu> menus = new ArrayList<>();
    if (activePage instanceof IPageWithTable<?>) {
      // in case of a page with table the empty space actions of the table will be added to the context menu of the tree.
      IPageWithTable<?> pageWithTable = (IPageWithTable<?>) activePage;
      menus.addAll(pageWithTable.computeTableEmptySpaceMenus());
    }

    // in case of a page with nodes add the single selection menus of its parent table for the current node/row.
    IPage<?> parentPage = activePage.getParentPage();
    if (parentPage instanceof IPageWithTable<?>) {
      IPageWithTable<?> parentTablePage = (IPageWithTable<?>) parentPage;
      menus.addAll(activePage.computeParentTablePageMenus(parentTablePage));
    }

    return menus;
  }

  protected void addMenusOfActivePageToContextMenu(IPage<?> activePage) {
    List<IMenu> wrappedMenus = new ArrayList<>();
    for (IMenu m : computeInheritedMenusOfPage(activePage)) {
      wrappedMenus.add(new OutlineMenuWrapper(m, TABLE_MENU_TYPE_MAPPER));
    }
    m_inheritedMenusOfPage = wrappedMenus;
    getContextMenu().addChildActions(m_inheritedMenusOfPage);
  }

  @Override
  public void clearContextPage() {
    IPage<?> page = m_contextPage;
    if (page != null) {
      // remove menus of the active page
      removeMenusOfActivePageToContextMenu();
      m_contextPage = null;
      page.pageDeactivatedNotify();
    }
  }

  protected void removeMenusOfActivePageToContextMenu() {
    if (m_inheritedMenusOfPage != null) {
      getContextMenu().removeChildActions(m_inheritedMenusOfPage);
      m_inheritedMenusOfPage = null;
    }
  }

  @Override
  protected void nodesSelectedInternal(Set<ITreeNode> oldSelection, Set<ITreeNode> newSelection) {
    super.nodesSelectedInternal(oldSelection, newSelection);

    IPage deselectedPage = null;
    if (CollectionUtility.hasElements(oldSelection)) {
      deselectedPage = (IPage) CollectionUtility.firstElement(oldSelection);
    }
    IPage newSelectedPage = null;
    if (CollectionUtility.hasElements(newSelection)) {
      newSelectedPage = (IPage) CollectionUtility.firstElement(newSelection);
    }
    handleActivePageChanged(deselectedPage, newSelectedPage);
  }

  private void handleActivePageChanged(IPage<?> deselectedPage, IPage<?> selectedPage) {
    if (m_pageChangeStrategy == null) {
      return;
    }

    try {
      if (m_contextPageOptimisticLock.acquire()) {
        m_pageChangeStrategy.pageChanged(this, deselectedPage, selectedPage);
      }
    }
    finally {
      m_contextPageOptimisticLock.release();
    }
  }

  @Override
  public void setDefaultDetailForm(IForm form) {
    if (form != null) {
      if (form.getDisplayHint() != IForm.DISPLAY_HINT_VIEW) {
        form.setDisplayHint(IForm.DISPLAY_HINT_VIEW);
      }
      if (form.getDisplayViewId() == null) {
        form.setDisplayViewId(IForm.VIEW_ID_PAGE_DETAIL);
      }
      form.setShowOnStart(false);
    }
    setDefaultDetailFormInternal(form);
  }

  /**
   * Starts the default detail form.
   * <p>
   * The default uses {@link IForm#start()} and therefore expects a form handler to be previously set. Override to call
   * a custom start method or implement a {@link IForm#start()} on the default detail form.
   */
  protected void startDefaultDetailForm() {
    getDefaultDetailForm().start();
  }

  public void ensureDefaultDetailFormCreated() {
    if (getDefaultDetailForm() != null) {
      return;
    }
    IForm form = createDefaultDetailForm();
    if (form != null) {
      setDefaultDetailForm(form);
      interceptInitDefaultDetailForm();
    }
  }

  public void ensureDefaultDetailFormStarted() {
    if (getDefaultDetailForm() == null || !getDefaultDetailForm().isFormStartable()) {
      return;
    }
    startDefaultDetailForm();
  }

  protected void disposeDefaultDetailForm() {
    if (getDefaultDetailForm() != null) {
      getDefaultDetailForm().doClose();
      setDefaultDetailForm(null);
    }
  }

  protected IForm createDefaultDetailForm() {
    if (getConfiguredDefaultDetailForm() == null) {
      return null;
    }
    try {
      return getConfiguredDefaultDetailForm().newInstance();
    }
    catch (Exception e) {
      BEANS.get(ExceptionHandler.class).handle(new ProcessingException("error creating instance of class '" + getConfiguredDefaultDetailForm().getName() + "'.", e));
    }
    return null;
  }

  @Override
  public void activate() {
    interceptActivated();
  }

  @Override
  public void deactivate() {
    interceptDeativated();
  }

  @Override
  public OutlineMediator getOutlineMediator() {
    return m_outlineMediator;
  }

  protected OutlineMediator createOutlineMediator() {
    return new OutlineMediator();
  }

  protected IPageChangeStrategy createPageChangeStrategy() {
    return new DefaultPageChangeStrategy();
  }

  @Override
  public void setPageChangeStrategy(IPageChangeStrategy pageChangeStrategy) {
    m_pageChangeStrategy = pageChangeStrategy;
  }

  @Override
  public IPageChangeStrategy getPageChangeStrategy() {
    return m_pageChangeStrategy;
  }

  private class P_OutlineListener extends TreeAdapter {
    @Override
    public void treeChanged(TreeEvent e) {
      ITreeNode commonParentNode = e.getCommonParentNode();
      if (commonParentNode instanceof IPageWithNodes) {
        handlePageWithNodesTreeEvent(e, (IPageWithNodes) commonParentNode);
      }
      else if (commonParentNode instanceof IPageWithTable<?>) {
        handlePageWithTableTreeEvent(e, (IPageWithTable<?>) commonParentNode);
      }
    }

    private void handlePageWithNodesTreeEvent(TreeEvent e, IPageWithNodes pageWithNodes) {
      OutlineMediator outlineMediator = getOutlineMediator();
      if (outlineMediator == null) {
        return;
      }

      switch (e.getType()) {
        case TreeEvent.TYPE_CHILD_NODE_ORDER_CHANGED:
        case TreeEvent.TYPE_NODES_DELETED:
        case TreeEvent.TYPE_NODES_INSERTED:
        case TreeEvent.TYPE_NODES_UPDATED:
        case TreeEvent.TYPE_NODES_CHECKED: {
          outlineMediator.mediateTreeNodesChanged(pageWithNodes);
          break;
        }
      }
    }

    private void handlePageWithTableTreeEvent(TreeEvent e, IPageWithTable<?> pageWithTable) {
      OutlineMediator outlineMediator = getOutlineMediator();
      if (outlineMediator == null) {
        return;
      }

      switch (e.getType()) {
        case TreeEvent.TYPE_NODE_ACTION: {
          outlineMediator.mediateTreeNodeAction(e, pageWithTable);
          break;
        }
        case TreeEvent.TYPE_NODES_DRAG_REQUEST: {
          outlineMediator.mediateTreeNodesDragRequest(e, pageWithTable);
          break;
        }
        case TreeEvent.TYPE_NODE_DROP_ACTION: {
          outlineMediator.mediateTreeNodeDropAction(e, pageWithTable);
          break;
        }
      }

    }
  }

  /**
   * If the class is annotated with {@link ClassId}, the annotation value is returned.<br/>
   * Otherwise the class name.
   */
  @Override
  public String classId() {
    return ConfigurationUtility.getAnnotatedClassIdWithFallback(getClass(), false);
  }

  private class P_TableFilterBasedTreeNodeFilter implements ITreeNodeFilter {
    @Override
    public boolean accept(ITreeNode node, int level) {
      ITreeNode parentNode = node.getParentNode();
      if (parentNode != null && !parentNode.isFilterAccepted()) {
        // hide page if parent page is filtered
        node.setRejectedByUser(parentNode.isRejectedByUser());
        return false;
      }
      if (!(parentNode instanceof IPage<?>)) {
        return true;
      }
      IPage<?> parentPage = (IPage<?>) parentNode;
      ITableRow tableRow = parentPage.getTableRowFor(node);
      if (tableRow == null) {
        return true;
      }
      if (!tableRow.isFilterAccepted()) {
        node.setRejectedByUser(tableRow.isRejectedByUser());
      }
      return tableRow.isFilterAccepted();
    }
  }

  @ClassId("90c662a2-28ac-4ca3-b34c-9b58cf14b3d9")
  private class InvisibleRootPage extends AbstractPageWithNodes {
    @Override
    protected void execCreateChildPages(List<IPage<?>> pageList) {
      AbstractOutline.this.createChildPagesInternal(pageList);
    }
  }

  protected final IPage<?> interceptCreateRootPage() {
    List<? extends ITreeExtension<? extends AbstractTree>> extensions = getAllExtensions();
    OutlineCreateRootPageChain chain = new OutlineCreateRootPageChain(extensions);
    return chain.execCreateRootPage();
  }

  protected final void interceptCreateChildPages(List<IPage<?>> pageList) {
    List<? extends ITreeExtension<? extends AbstractTree>> extensions = getAllExtensions();
    OutlineCreateChildPagesChain chain = new OutlineCreateChildPagesChain(extensions);
    chain.execCreateChildPages(pageList);
  }

  protected final void interceptInitDefaultDetailForm() {
    List<? extends ITreeExtension<? extends AbstractTree>> extensions = getAllExtensions();
    OutlineInitDefaultDetailFormChain chain = new OutlineInitDefaultDetailFormChain(extensions);
    chain.execInitDefaultDetailForm();
  }

  protected final void interceptActivated() {
    List<? extends ITreeExtension<? extends AbstractTree>> extensions = getAllExtensions();
    OutlineActivatedChain chain = new OutlineActivatedChain(extensions);
    chain.execActivated();
  }

  protected final void interceptDeativated() {
    List<? extends ITreeExtension<? extends AbstractTree>> extensions = getAllExtensions();
    OutlineDeactivatedChain chain = new OutlineDeactivatedChain(extensions);
    chain.execDeactivated();
  }

  protected static class LocalOutlineExtension<OWNER extends AbstractOutline> extends LocalTreeExtension<OWNER> implements IOutlineExtension<OWNER> {

    public LocalOutlineExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public void execCreateChildPages(OutlineCreateChildPagesChain chain, List<IPage<?>> pageList) {
      getOwner().execCreateChildPages(pageList);
    }

    @Override
    public IPage<?> execCreateRootPage(OutlineCreateRootPageChain chain) {
      return getOwner().execCreateRootPage();
    }

    @Override
    public void execInitDefaultDetailForm(OutlineInitDefaultDetailFormChain chain) {
      getOwner().execInitDefaultDetailForm();
    }

    @Override
    public void execActivated(OutlineActivatedChain chain) {
      getOwner().execActivated();
    }

    @Override
    public void execDeactivated(OutlineDeactivatedChain chain) {
      getOwner().execDeactivated();
    }
  }

  @Override
  protected IOutlineExtension<? extends AbstractOutline> createLocalExtension() {
    return new LocalOutlineExtension<AbstractOutline>(this);
  }

  @Override
  public void firePageChanged(IPage<?> page) {
    if (page != null && page.isInitializing()) {
      return;
    }
    fireTreeEventInternal(new OutlineEvent(this, OutlineEvent.TYPE_PAGE_CHANGED, page, true));
  }

  @Override
  public void fireBeforeDataLoaded(IPage<?> page) {
    fireTreeEventInternal(new OutlineEvent(this, OutlineEvent.TYPE_PAGE_BEFORE_DATA_LOADED, page, false));
  }

  @Override
  public void fireAfterDataLoaded(IPage<?> page) {
    fireTreeEventInternal(new OutlineEvent(this, OutlineEvent.TYPE_PAGE_AFTER_DATA_LOADED, page, false));
  }

  @Override
  public void fireAfterTableInit(IPage<?> page) {
    fireTreeEventInternal(new OutlineEvent(this, OutlineEvent.TYPE_PAGE_AFTER_TABLE_INIT, page, false));
  }

  @Override
  public void fireAfterPageInit(IPage<?> page) {
    fireTreeEventInternal(new OutlineEvent(this, OutlineEvent.TYPE_PAGE_AFTER_PAGE_INIT, page, false));
  }

  @Override
  public void fireAfterSearchFormStart(IPage<?> page) {
    fireTreeEventInternal(new OutlineEvent(this, OutlineEvent.TYPE_PAGE_AFTER_SEARCH_FORM_START, page, false));
  }

  @Override
  public void fireAfterPageDispose(IPage<?> page) {
    fireTreeEventInternal(new OutlineEvent(this, OutlineEvent.TYPE_PAGE_AFTER_DISPOSE, page, true));
  }

  @Override
  public void firePageActivated(IPage<?> page) {
    fireTreeEventInternal(new OutlineEvent(this, OutlineEvent.TYPE_PAGE_ACTIVATED, page, false));
  }

  protected void fireTreeEventInternal(OutlineEvent e) {
    if (e.isBuffered()) {
      super.fireTreeEventInternal(e);
    }
    else {
      doFireTreeEvent(e);
    }
  }

  @Override
  protected void disposeTreeInternal() {
    super.disposeTreeInternal();
    try {
      disposeDefaultDetailForm();
    }
    catch (RuntimeException | PlatformError e) {
      BEANS.get(ExceptionHandler.class).handle(e);
    }

  }
}
