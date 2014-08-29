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

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithNodes;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.ISearchForm;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.service.SERVICES;

public class DefaultPageChangeStrategy5 implements IPageChangeStrategy {

  @Override
  public void pageChanged(IOutline outline, IPage deselectedPage, IPage selectedPage) {
    if (outline == null) {
      return;
    }

    outline.clearContextPage();
    IForm detailForm = null;
    ITable detailTable = null;
    ISearchForm searchForm = null;
    // new active page
    outline.makeActivePageToContextPage();
    IPage activePage = outline.getActivePage();
    try {
      if (activePage != null) {
        activePage.ensureChildrenLoaded();

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
      else {
        if (outline instanceof IOutline5) {
          //FIXME remove from interface, better do it in AbstractOutline handleActivePageChanged
          ((IOutline5) outline).ensureDefaultDetailFormCreated();
          ((IOutline5) outline).ensureDefaultDetailFormStarted();
          detailForm = ((IOutline5) outline).getDefaultDetailForm();
        }
      }
    }
    catch (ProcessingException e1) {
      SERVICES.getService(IExceptionHandlerService.class).handleException(e1);
    }

    // remove first
    if (detailForm == null) {
      outline.setDetailForm(null);
    }
    if (detailTable == null) {
      outline.setDetailTable(null);
    }
    if (searchForm == null) {
      outline.setSearchForm(null);
    }
    // add new
    if (detailForm != null) {
      outline.setDetailForm(detailForm);
    }
    if (detailTable != null) {
      outline.setDetailTable(detailTable);
    }
    if (searchForm != null) {
      outline.setSearchForm(searchForm);
    }
  }

}
