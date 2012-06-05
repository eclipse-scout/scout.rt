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
package org.eclipse.scout.rt.client.mobile.navigation;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.mobile.ui.desktop.MobileDesktopUtility;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.form.IForm;

/**
 * @since 3.9.0
 */
public class NavigationPoint implements INavigationPoint {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(NavigationPoint.class);

  private IForm m_form;
  private IPage m_page;
  private IDeviceNavigator m_deviceNavigator;

  public NavigationPoint(IDeviceNavigator deviceNavigator, IForm form, IPage page) {
    m_deviceNavigator = deviceNavigator;
    m_form = form;
    m_page = page;
  }

  @Override
  public void activate() throws ProcessingException {
    List<IForm> currentNavigationForms = getDeviceNavigator().getCurrentNavigationForms();
    for (IForm form : currentNavigationForms) {
      if (form != getForm() && !getDeviceNavigator().containsFormInHistory(form)) {
        MobileDesktopUtility.closeForm(form);
      }
    }

    if (getPage() != null) {
      IOutline outline = getPage().getOutline();
      outline.selectNode(getPage());
    }
  }

  public IDeviceNavigator getDeviceNavigator() {
    return m_deviceNavigator;
  }

  @Override
  public IForm getForm() {
    return m_form;
  }

  @Override
  public IPage getPage() {
    return m_page;
  }

  @Override
  public String toString() {
    return "Form: " + getForm() + ". Page: " + getPage();
  }

}
