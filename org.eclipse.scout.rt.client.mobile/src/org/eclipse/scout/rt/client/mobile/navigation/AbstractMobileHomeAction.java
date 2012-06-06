package org.eclipse.scout.rt.client.mobile.navigation;

import org.eclipse.scout.commons.WeakEventListener;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.mobile.Icons;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.service.SERVICES;

@Order(20)
public class AbstractMobileHomeAction extends AbstractMenu {
  private P_BreadCrumbsListener m_breadCrumbsListener;

  @Override
  protected String getConfiguredText() {
    return "";
  }

  @Override
  protected String getConfiguredTooltipText() {
    return "Home"; //TODO rst Texts
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
    IClientSession session = ClientSyncJob.getCurrentSession();
    if (session != null && session.getDesktop() != null) {
      init();
    }
  }

  protected void init() {
    init(null);
  }

  protected void init(IDesktop desktop) {
    if (m_breadCrumbsListener == null) {
      m_breadCrumbsListener = new P_BreadCrumbsListener();
      SERVICES.getService(IBreadCrumbsNavigationService.class).addBreadCrumbsListener(desktop, m_breadCrumbsListener);
    }
  }

  @Override
  protected void execAction() throws ProcessingException {
    SERVICES.getService(IBreadCrumbsNavigationService.class).goHome();
  }

  private class P_BreadCrumbsListener implements BreadCrumbsListener, WeakEventListener {

    @Override
    public void breadCrumbsChanged(BreadCrumbsEvent e) {
      setVisible(e.getBreadCrumbsNavigation().isGoingHomePossible());
    }

  }
}
