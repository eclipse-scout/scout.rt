package org.eclipse.scout.rt.client.mobile.navigation;

import org.eclipse.scout.commons.WeakEventListener;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.mobile.Icons;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.service.SERVICES;

public class AbstractMobileBackAction extends AbstractMenu {
  private P_BreadCrumbsListener m_breadCrumbsListener;

  @Override
  protected String getConfiguredText() {
    return "";
  }

  @Override
  protected void execInitAction() throws ProcessingException {
    IBreadCrumbsNavigation breadCrumbsNavigation = SERVICES.getService(IBreadCrumbsNavigationService.class).getBreadCrumbsNavigation();
    if (m_breadCrumbsListener == null) {
      m_breadCrumbsListener = new P_BreadCrumbsListener();
      breadCrumbsNavigation.addBreadCrumbsListener(m_breadCrumbsListener);
    }
    setVisible(breadCrumbsNavigation.isSteppingBackPossible());
  }

  @Override
  protected String getConfiguredIconId() {
    return Icons.BackAction;
  }

  @Override
  protected void execAction() throws ProcessingException {
    SERVICES.getService(IBreadCrumbsNavigationService.class).getBreadCrumbsNavigation().stepBack();
  }

  private class P_BreadCrumbsListener implements BreadCrumbsListener, WeakEventListener {

    @Override
    public void breadCrumbsChanged(BreadCrumbsEvent e) {
      setVisible(e.getBreadCrumbsNavigation().isSteppingBackPossible());
    }

  }
}
