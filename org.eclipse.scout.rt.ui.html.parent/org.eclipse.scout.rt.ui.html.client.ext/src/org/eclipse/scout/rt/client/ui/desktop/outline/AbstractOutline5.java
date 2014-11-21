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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.AbstractClientSession;
import org.eclipse.scout.rt.client.ClientJob;
import org.eclipse.scout.rt.client.Scout5ExtensionUtil;
import org.eclipse.scout.rt.client.ui.action.ActionUtility;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.MenuSeparator;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuType;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.AbstractExtensibleOutline;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.service.SERVICES;

public abstract class AbstractOutline5 extends AbstractExtensibleOutline implements IOutline5 {

  private IForm m_defaultDetailForm;

  @Override
  protected IPageChangeStrategy createPageChangeStrategy() {
    return new DefaultPageChangeStrategy5();
  }

  @Override
  protected void initConfig() {
    super.initConfig();

    try {
      ensureDefaultDetailFormCreated();
      ensureDefaultDetailFormStarted();
    }
    catch (ProcessingException e) {
      SERVICES.getService(IExceptionHandlerService.class).handleException(e);
    }
  }

  @Override
  public IForm getDefaultDetailForm() {
    return m_defaultDetailForm;
  }

  protected boolean getConfiguredSelectFirstPageOnOutlineChange() {
    return true;
  }

  @Override
  public void selectNodes(Collection<? extends ITreeNode> nodes, boolean append) {
    if (!getConfiguredSelectFirstPageOnOutlineChange() && Scout5ExtensionUtil.IDesktop_isOutlineChanging(ClientJob.getCurrentSession(AbstractClientSession.class).getDesktop())) {
      //Prevent selecting the first node when changing the outline
      return;
    }
    super.selectNodes(nodes, append);
  }

  public void setDefaultDetailForm(IForm form) {
    if (form != null) {
      if (form.getDisplayHint() != IForm.DISPLAY_HINT_VIEW) {
        form.setDisplayHint(IForm.DISPLAY_HINT_VIEW);
      }
      if (form.getDisplayViewId() == null) {
        form.setDisplayViewId(IForm.VIEW_ID_PAGE_DETAIL);
      }
      form.setAutoAddRemoveOnDesktop(false);
    }
    m_defaultDetailForm = form;
  }

  protected IForm execCreateDefaultDetailForm() throws ProcessingException {
    return null;
  }

  protected void execStartDefaultDetailForm(IForm form) throws ProcessingException {
  }

  @Override
  protected List<IMenu> computeInheritedMenusOfPage(IPage activePage) {
    List<IMenu> menus = new ArrayList<IMenu>();
    if (activePage instanceof IPageWithTable<?>) {
      // in case of a page with table the empty space actions of the table will be added to the context menu of the tree.
      IPageWithTable<?> pageWithTable = (IPageWithTable<?>) activePage;
      if (pageWithTable.isShowEmptySpaceMenus()) {
        ITable table = pageWithTable.getTable();
        List<IMenu> emptySpaceMenus = ActionUtility.getActions(table.getMenus(),
            ActionUtility.createMenuFilterMenuTypes(CollectionUtility.hashSet(TableMenuType.EmptySpace), false));
        if (emptySpaceMenus.size() > 0) {
          menus.add(new MenuSeparator());
          for (IMenu menu : emptySpaceMenus) {
            menus.add(menu);
          }
        }
      }
    }

    // in case of a page with nodes add the single selection menus of its parent table for the current node/row.
    IPage parentPage = activePage.getParentPage();
    if (parentPage instanceof IPageWithTable<?>) {
      IPageWithTable<?> pageWithTable = (IPageWithTable<?>) parentPage;
      ITableRow row = pageWithTable.getTableRowFor(activePage);
      ITable table = pageWithTable.getTable();
      if (row != null) {
        table.getUIFacade().setSelectedRowsFromUI(CollectionUtility.arrayList(row));
        List<IMenu> parentTableMenus = ActionUtility.getActions(table.getContextMenu().getChildActions(), ActionUtility.createMenuFilterMenuTypes(CollectionUtility.hashSet(TableMenuType.SingleSelection), false));
        if (parentTableMenus.size() > 0) {
          menus.add(new MenuSeparator());
          for (IMenu menu : parentTableMenus) {
            menus.add(menu);
          }
        }
      }
    }
    return menus;
  }

  @Override
  public void ensureDefaultDetailFormCreated() throws ProcessingException {
    if (getDefaultDetailForm() != null) {
      return;
    }
    IForm form = execCreateDefaultDetailForm();
    setDefaultDetailForm(form);
  }

  @Override
  public void ensureDefaultDetailFormStarted() throws ProcessingException {
    if (getDefaultDetailForm() == null || getDefaultDetailForm().isFormOpen()) {
      return;
    }
    execStartDefaultDetailForm(getDefaultDetailForm());
  }

  @Override
  public void fireOutlineEvent(OutlineEvent event) {
    fireTreeEventInternal(event);
  }

}
