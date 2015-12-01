package org.eclipse.scout.rt.client.mobile.navigation;

import org.eclipse.scout.rt.client.mobile.Icons;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.WeakEventListener;

public abstract class AbstractMobileBackAction extends AbstractMenu {
  private P_BreadCrumbsListener m_breadCrumbsListener;

  @Override
  protected String getConfiguredText() {
    return "";
  }

  @Override
  protected void execInitAction() {
    IBreadCrumbsNavigation breadCrumbsNavigation = BEANS.get(IBreadCrumbsNavigationService.class).getBreadCrumbsNavigation();
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
  protected void execAction() {
    BEANS.get(IBreadCrumbsNavigationService.class).getBreadCrumbsNavigation().stepBack();
  }

  private class P_BreadCrumbsListener implements BreadCrumbsListener, WeakEventListener {

    @Override
    public void breadCrumbsChanged(BreadCrumbsEvent e) {
      setVisible(e.getBreadCrumbsNavigation().isSteppingBackPossible());
    }

  }
}
