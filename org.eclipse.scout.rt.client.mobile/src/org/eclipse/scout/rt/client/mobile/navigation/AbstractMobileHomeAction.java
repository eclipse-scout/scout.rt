package org.eclipse.scout.rt.client.mobile.navigation;

import org.eclipse.scout.commons.WeakEventListener;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.mobile.Icons;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.service.SERVICES;

@Order(20)
public class AbstractMobileHomeAction extends AbstractMenu {
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
  protected void execInitAction() throws ProcessingException {
    if (m_breadCrumbsListener == null) {
      m_breadCrumbsListener = new P_BreadCrumbsListener();
      SERVICES.getService(IBreadCrumbsNavigationService.class).getBreadCrumbsNavigation().addBreadCrumbsListener(m_breadCrumbsListener);
    }
  }

  @Override
  protected void execAction() throws ProcessingException {
    SERVICES.getService(IBreadCrumbsNavigationService.class).getBreadCrumbsNavigation().goHome();
  }

  private class P_BreadCrumbsListener implements BreadCrumbsListener, WeakEventListener {

    @Override
    public void breadCrumbsChanged(BreadCrumbsEvent e) {
      setVisible(e.getBreadCrumbsNavigation().isGoingHomePossible());
    }

  }
}
