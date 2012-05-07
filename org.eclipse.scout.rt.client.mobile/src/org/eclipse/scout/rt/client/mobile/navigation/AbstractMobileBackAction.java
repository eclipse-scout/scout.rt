package org.eclipse.scout.rt.client.mobile.navigation;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.mobile.Icons;
import org.eclipse.scout.rt.client.mobile.services.IMobileNavigationService;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.service.SERVICES;

public class AbstractMobileBackAction extends AbstractMenu {

  @Override
  protected String getConfiguredText() {
    return "";
  }

  @Override
  protected String getConfiguredTooltipText() {
    return "Zurück"; //TODO rst Texts
  }

  @Override
  protected String getConfiguredIconId() {
    return Icons.BackAction;
  }

  @Override
  protected void execAction() throws ProcessingException {
    SERVICES.getService(IMobileNavigationService.class).stepBack();
  }
}
