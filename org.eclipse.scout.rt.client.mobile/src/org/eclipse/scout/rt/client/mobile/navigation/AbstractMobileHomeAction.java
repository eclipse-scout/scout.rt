package org.eclipse.scout.rt.client.mobile.navigation;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.mobile.Icons;
import org.eclipse.scout.rt.client.mobile.services.IMobileNavigationService;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.service.SERVICES;

@Order(20)
public class AbstractMobileHomeAction extends AbstractMenu {

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
  protected void execAction() throws ProcessingException {
    SERVICES.getService(IMobileNavigationService.class).goHome();
  }
}
