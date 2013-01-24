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
package org.eclipse.scout.rt.client.mobile.ui.desktop;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.mobile.ui.form.outline.PageFormManager;
import org.eclipse.scout.rt.client.ui.desktop.outline.DefaultPageChangeStrategy;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.IPageChangeStrategy;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.service.SERVICES;

/**
 * @since 3.9.0
 */
public class MultiPageChangeStrategy implements IPageChangeStrategy {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(MultiPageChangeStrategy.class);

  private IOutline m_outline;
  private IPage m_mainPage;
  private IPage m_subPage;
  private PageFormManager m_pageFormManager;
  private DefaultPageChangeStrategy m_defaultPageChangeStrategy;

  public MultiPageChangeStrategy(PageFormManager pageFormManager) {
    m_defaultPageChangeStrategy = new DefaultPageChangeStrategy();
    m_pageFormManager = pageFormManager;
  }

  @Override
  public void pageChanged(IOutline outline, IPage deselectedPage, IPage selectedPage) {
    if (outline == null) {
      return;
    }

    if (outline != m_outline) {
      m_mainPage = null;
      m_subPage = null;
      m_outline = outline;
    }

    if (selectedPage == null) {
      activateMainPage(deselectedPage, selectedPage);
    }
    else {
      String pageFormSlot = m_pageFormManager.computePageFormSlot(selectedPage);
      if (m_pageFormManager.getLeftPageSlotViewId().equals(pageFormSlot)) {
        activateMainPage(deselectedPage, selectedPage);
      }
      else {
        activateMainPage(deselectedPage, selectedPage.getParentPage());
        activateSubPage(selectedPage);
      }
    }
  }

  private void activateMainPage(IPage deselectedPage, IPage selectedPage) {
    if (m_mainPage == selectedPage) {
      return;
    }
    deactivateSubPage();

    m_defaultPageChangeStrategy.pageChanged(m_outline, deselectedPage, selectedPage);
    m_mainPage = selectedPage;

    LOG.debug("Main page activated: " + selectedPage);
  }

  private void activateSubPage(IPage selectedPage) {
    if (m_subPage == selectedPage) {
      return;
    }
    deactivateSubPage();

    m_subPage = selectedPage;
    if (m_subPage != null) {
      m_subPage.pageActivatedNotify();

      try {
        m_subPage.ensureChildrenLoaded();
      }
      catch (ProcessingException e1) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(e1);
      }

      LOG.debug("Sub page activated: " + selectedPage);
    }
  }

  private void deactivateSubPage() {
    if (m_subPage == null) {
      return;
    }

    m_subPage.pageDeactivatedNotify();
    m_subPage = null;
  }

  private boolean isChildOfMainPage(IPage selectedPage) {
    if (selectedPage == null || m_mainPage == null || m_mainPage.getChildNodeCount() == 0) {
      return false;
    }

    for (IPage childPage : m_mainPage.getChildPages()) {
      if (selectedPage.equals(childPage)) {
        return true;
      }
    }

    return false;
  }

}
