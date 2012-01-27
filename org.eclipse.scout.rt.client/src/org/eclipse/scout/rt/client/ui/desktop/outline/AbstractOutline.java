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
package org.eclipse.scout.rt.client.ui.desktop.outline;

import java.security.Permission;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.eclipse.scout.commons.OptimisticLock;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.ConfigPropertyValue;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.dnd.TransferObject;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNodeFilter;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeVisitor;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeAdapter;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeEvent;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithNodes;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithNodes;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.ISearchForm;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;
import org.eclipse.scout.service.SERVICES;

public abstract class AbstractOutline extends AbstractTree implements IOutline {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractOutline.class);

  // visible is defined as: visibleGranted && visibleProperty
  private boolean m_visibleGranted;
  private boolean m_visibleProperty;
  private IPage m_contextPage;
  private OptimisticLock m_contextPageOptimisticLock;

  public AbstractOutline() {
    super();
  }

  /*
   * Configuration
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(80)
  @ConfigPropertyValue("true")
  protected boolean getConfiguredEnabled() {
    return true;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(90)
  @ConfigPropertyValue("true")
  protected boolean getConfiguredVisible() {
    return true;
  }

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(100)
  @ConfigPropertyValue("0")
  protected int getConfiguredSortNo() {
    return 0;
  }

  @ConfigProperty(ConfigProperty.DOC)
  @Order(110)
  @ConfigPropertyValue("null")
  protected String getConfiguredDoc() {
    return null;
  }

  /**
   * @param pageList
   *          is used to add pages to the tree. All these pages are roots of the
   *          visible tree
   */
  @ConfigOperation
  @Order(90)
  protected void execCreateChildPages(Collection<IPage> pageList) throws ProcessingException {
  }

  /**
   * By default the outline tree tries to delegate the drop to the affected
   * page.
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

  @Override
  protected void initConfig() {
    m_visibleGranted = true;
    m_contextPageOptimisticLock = new OptimisticLock();
    addTreeListener(new P_OutlineListener());
    addNodeFilter(new P_TableFilterBasedTreeNodeFilter());
    super.initConfig();
    setRootNodeVisible(false);
    IPage rootPage = new InvisibleRootPage();
    setRootNode(rootPage);
    setEnabled(getConfiguredEnabled());
    setVisible(getConfiguredVisible());
  }

  /*
   * Runtime
   */

  @Override
  public IPage getActivePage() {
    return (IPage) getSelectedNode();
  }

  @Override
  public void refreshPages(final Class... pageTypes) {
    final ArrayList<IPage> candidates = new ArrayList<IPage>();
    ITreeVisitor v = new ITreeVisitor() {
      @Override
      public boolean visit(ITreeNode node) {
        IPage page = (IPage) node;
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
    for (IPage page : candidates) {
      if (page.getTree() != null) {
        page.dataChanged();
      }
    }
  }

  @Override
  public void releaseUnusedPages() {
    final HashSet<IPage> preservationSet = new HashSet<IPage>();
    IPage oldSelection = (IPage) getSelectedNode();
    IPage p = oldSelection;
    if (p != null) {
      while (p != null) {
        preservationSet.add(p);
        p = p.getParentPage();
      }
    }
    ITreeVisitor v = new ITreeVisitor() {
      @Override
      public boolean visit(ITreeNode node) {
        IPage page = (IPage) node;
        if (preservationSet.contains(page)) {
          // nop
        }
        else if (page.isChildrenLoaded() && (!page.isExpanded() || !(page.getParentPage() != null && page.getParentPage().isChildrenLoaded()))) {
          try {
            unloadNode(page);
          }
          catch (ProcessingException e) {
            SERVICES.getService(IExceptionHandlerService.class).handleException(e);
          }
        }
        return true;
      }
    };
    try {
      setTreeChanging(true);
      visitNode(getRootNode(), v);
      if (oldSelection != null) {
        IPage selectedPage = (IPage) getSelectedNode();
        if (selectedPage == null) {
          try {
            getRootNode().ensureChildrenLoaded();
            ITreeNode[] children = getRootNode().getFilteredChildNodes();
            if (children.length > 0) {
              selectNode(children[0]);
            }
          }
          catch (ProcessingException e) {
            LOG.warn(null, e);
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
        IPage page = (IPage) node;
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
      b = SERVICES.getService(IAccessControlService.class).checkPermission(p);
    }
    else {
      b = true;
    }
    setVisibleGranted(b);
  }

  @Override
  public boolean isVisibleGranted() {
    return m_visibleGranted;
  }

  @Override
  public void setVisibleGranted(boolean b) {
    m_visibleGranted = b;
    calculateVisible();
  }

  @Override
  public boolean isVisible() {
    return propertySupport.getPropertyBool(PROP_VISIBLE);
  }

  @Override
  public void setVisible(boolean b) {
    m_visibleProperty = b;
    calculateVisible();
  }

  private void calculateVisible() {
    propertySupport.setPropertyBool(PROP_VISIBLE, m_visibleGranted && m_visibleProperty);
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
  public IPage getRootPage() {
    return (IPage) getRootNode();
  }

  @Override
  public void unloadNode(ITreeNode node) throws ProcessingException {
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
  public void resetOutline() throws ProcessingException {
    if (getRootNode() != null) {
      try {
        setTreeChanging(true);
        //
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
  }

  @Override
  public void makeActivePageToContextPage() {
    IPage activePage = getActivePage();
    if (activePage != null && m_contextPage != activePage) {
      m_contextPage = activePage;
      activePage.pageActivatedNotify();
    }
  }

  @Override
  public void clearContextPage() {
    IPage page = m_contextPage;
    if (page != null) {
      m_contextPage = null;
      page.pageDeactivatedNotify();
    }
  }

  private void handleActivePageChanged() {
    try {
      if (m_contextPageOptimisticLock.acquire()) {
        clearContextPage();
        IForm detailForm = null;
        ITable detailTable = null;
        ISearchForm searchForm = null;
        // new active page
        makeActivePageToContextPage();
        IPage activePage = getActivePage();
        if (activePage != null) {
          try {
            activePage.ensureChildrenLoaded();
          }
          catch (ProcessingException e1) {
            SERVICES.getService(IExceptionHandlerService.class).handleException(e1);
          }
          if (activePage instanceof IPageWithTable) {
            IPageWithTable tablePage = (IPageWithTable) activePage;
            detailForm = activePage.getDetailForm();
            if (activePage.isTableVisible()) {
              detailTable = tablePage.getTable();
            }
            if (tablePage.isSearchActive()) {
              searchForm = tablePage.getSearchFormInternal();
            }
          }
          else if (activePage instanceof IPageWithNodes) {
            IPageWithNodes nodePage = (IPageWithNodes) activePage;
            detailForm = activePage.getDetailForm();
            if (activePage.isTableVisible()) {
              detailTable = nodePage.getInternalTable();
            }
          }
        }

        // remove first
        if (detailForm == null) {
          setDetailForm(null);
        }
        if (detailTable == null) {
          setDetailTable(null);
        }
        if (searchForm == null) {
          setSearchForm(null);
        }
        // add new
        if (detailForm != null) {
          setDetailForm(detailForm);
        }
        if (detailTable != null) {
          setDetailTable(detailTable);
        }
        if (searchForm != null) {
          setSearchForm(searchForm);
        }
      }
    }
    finally {
      m_contextPageOptimisticLock.release();
    }
  }

  private class P_OutlineListener extends TreeAdapter {
    @Override
    public void treeChanged(TreeEvent e) {
      switch (e.getType()) {
        case TreeEvent.TYPE_NODES_SELECTED: {
          handleActivePageChanged();
          break;
        }
      }
    }
  }

  private class P_TableFilterBasedTreeNodeFilter implements ITreeNodeFilter {
    @Override
    public boolean accept(ITreeNode node, int level) {
      ITreeNode parentNode = node.getParentNode();
      if (parentNode != null && !parentNode.isFilterAccepted()) {
        // hide page if parent page is filtered
        return false;
      }
      if (parentNode instanceof IPageWithTable<?>) {
        ITableRow tableRow = ((IPageWithTable<?>) parentNode).getTableRowFor(node);
        return tableRow == null || tableRow.isFilterAccepted();
      }
      else if (parentNode instanceof IPageWithNodes) {
        for (ITreeNode child : parentNode.getChildNodes()) {
          if (child.equals(node)) {
            return ((IPageWithNodes) parentNode).isFilterAcceptedForChildNode(node);
          }
        }
      }
      return true;
    }
  }

  private class InvisibleRootPage extends AbstractPageWithNodes {
    @Override
    protected void execCreateChildPages(Collection<IPage> pageList) throws ProcessingException {
      AbstractOutline.this.execCreateChildPages(pageList);
    }
  }

}
