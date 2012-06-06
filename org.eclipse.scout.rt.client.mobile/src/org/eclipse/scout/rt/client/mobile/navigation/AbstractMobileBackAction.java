package org.eclipse.scout.rt.client.mobile.navigation;

import org.eclipse.scout.commons.WeakEventListener;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.mobile.Icons;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.service.SERVICES;

public class AbstractMobileBackAction extends AbstractMenu {
  private P_BreadCrumbsListener m_breadCrumbsListener;

  @Override
  protected String getConfiguredText() {
    return "";
  }

  @Override
  protected String getConfiguredTooltipText() {
    return "Zurück"; //TODO rst Texts
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
  protected String getConfiguredIconId() {
    return Icons.BackAction;
  }

  @Override
  protected void execAction() throws ProcessingException {
    SERVICES.getService(IBreadCrumbsNavigationService.class).stepBack();
  }

  private class P_BreadCrumbsListener implements BreadCrumbsListener, WeakEventListener {

    @Override
    public void breadCrumbsChanged(BreadCrumbsEvent e) {
      setVisible(e.getBreadCrumbsNavigation().isSteppingBackPossible());
    }

  }
}
