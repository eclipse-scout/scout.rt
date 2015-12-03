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
package org.eclipse.scout.rt.client.mobile.navigation;

import org.eclipse.scout.rt.client.mobile.Icons;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.WeakEventListener;

@Order(20)
public abstract class AbstractMobileHomeAction extends AbstractMenu {
  private P_BreadCrumbsListener m_breadCrumbsListener;

  @Override
  protected String getConfiguredText() {
    return "";
  }

  @Override
  protected boolean getConfiguredVisible() {
    return false;
  }

  @Override
  protected String getConfiguredIconId() {
    return Icons.HomeAction;
  }

  @Override
  protected void execInitAction() {
    if (m_breadCrumbsListener == null) {
      m_breadCrumbsListener = new P_BreadCrumbsListener();
      BEANS.get(IBreadCrumbsNavigationService.class).getBreadCrumbsNavigation().addBreadCrumbsListener(m_breadCrumbsListener);
    }
  }

  @Override
  protected void execAction() {
    BEANS.get(IBreadCrumbsNavigationService.class).getBreadCrumbsNavigation().goHome();
  }

  private class P_BreadCrumbsListener implements BreadCrumbsListener, WeakEventListener {

    @Override
    public void breadCrumbsChanged(BreadCrumbsEvent e) {
      setVisible(e.getBreadCrumbsNavigation().isGoingHomePossible());
    }

  }
}
