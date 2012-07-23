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
package org.eclipse.scout.rt.client.mobile.transformation;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.mobile.navigation.AbstractMobileBackAction;
import org.eclipse.scout.rt.client.mobile.navigation.IBreadCrumbsNavigationService;
import org.eclipse.scout.rt.client.mobile.ui.desktop.MobileDesktopUtility;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.service.SERVICES;

/**
 * @since 3.9.0
 */
public class MobileDeviceTransformer extends AbstractDeviceTransformer {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(MobileDeviceTransformer.class);

  private ToolFormHandler m_toolFormHandler;

  public MobileDeviceTransformer(IDesktop desktop) {
    super(desktop);

    m_toolFormHandler = new ToolFormHandler(getDesktop());
    SERVICES.getService(IBreadCrumbsNavigationService.class).getBreadCrumbsNavigation(desktop).trackDisplayViewId(IForm.VIEW_ID_CENTER);
  }

  @Override
  public void tablePageLoaded(IPageWithTable<?> tablePage) throws ProcessingException {
    super.tablePageLoaded(tablePage);

    m_toolFormHandler.tablePageLoaded(tablePage);
  }

  @Override
  protected void transformDisplayHintSettings(IForm form) {
    if (form.getDisplayHint() == IForm.DISPLAY_HINT_VIEW) {
      form.setDisplayViewId(IForm.VIEW_ID_CENTER);
    }
    else {
      MobileDesktopUtility.setFormWidthHint(form, Integer.MAX_VALUE);
    }
  }

  @Override
  protected boolean shouldPageDetailFormBeEmbedded() {
    return true;
  }

  /**
   * Adds a back button if there is no other button on the left side which is able to close the form.
   */
  @Override
  public void adaptFormHeaderLeftActions(IForm form, List<IMenu> menuList) {
    super.adaptFormHeaderLeftActions(form, menuList);

    if (!containsCloseAction(menuList)) {
      menuList.add(new P_BackAction());
    }
  }

  private class P_BackAction extends AbstractMobileBackAction {

  }

}
