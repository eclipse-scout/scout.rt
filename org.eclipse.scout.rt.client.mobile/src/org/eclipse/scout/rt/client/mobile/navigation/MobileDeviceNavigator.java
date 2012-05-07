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

import java.util.Map;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.mobile.ui.desktop.MobileDesktopUtility;
import org.eclipse.scout.rt.client.mobile.ui.forms.FormStack;
import org.eclipse.scout.rt.client.mobile.ui.forms.OutlineChooserForm;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.navigation.INavigationHistoryService;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.service.SERVICES;

/**
 * @since 3.8.0
 */
public class MobileDeviceNavigator implements IDeviceNavigator {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(MobileDeviceNavigator.class);

  private FormStack m_navigationFormStack;

  public MobileDeviceNavigator(FormStack navigationFormStack) {
    m_navigationFormStack = navigationFormStack;
  }

  protected void initFormStacks(Map<String, FormStack> formStacks) {

  }

  protected FormStack getNavigationFormStack() {
    return m_navigationFormStack;
  }

  @Override
  public void stepBack() throws ProcessingException {
    IDesktop desktop = ClientSyncJob.getCurrentSession().getDesktop();
    if (desktop == null) {
      return;
    }

    IForm currentForm = getCurrentNavigationForm();

    if (MobileDesktopUtility.isToolForm(currentForm)) {
      MobileDesktopUtility.closeToolForm(currentForm);
      return;
    }

    //Forms with isAutoAddRemoveOnDesktop = true are ordinary forms like a dialog and can be closed normally
    if (currentForm != null && currentForm.isAutoAddRemoveOnDesktop()) {
      MobileDesktopUtility.closeOpenForms();
      return;
    }

    //Other forms like outline table forms or page detail forms should not be closed.
    //Instead the navigation history is used to properly step back
    INavigationHistoryService navigation = SERVICES.getService(INavigationHistoryService.class);
    if (navigation.hasBackwardBookmarks()) {
      SERVICES.getService(INavigationHistoryService.class).stepBackward();
    }
    else {
      if (getNavigationFormStack().size() > 0) {
        MobileDesktopUtility.closeOpenForms();
      }
      else {
        LOG.info("Tried to step back although it is not possible because form history as well as the navigation history are empty.");
      }
    }

    //FIXME CGU If a form is openend which allows to navigate further in the tree, back won't open that form at the correct time. Similar problem with the outline chooser form exists.

  }

  @Override
  public boolean isSteppingBackPossible() {
    return !(getCurrentNavigationForm() instanceof OutlineChooserForm);
  }

  @Override
  public IForm getCurrentNavigationForm() {
    IForm[] viewStack = getDesktop().getViewStack();
    for (IForm form : viewStack) {
      if (getNavigationFormStack().belongsToThisStack(form)) {
        return form;
      }
    }

    return null;
  }

  @Override
  public boolean isOutlineTreeAvailable() {
    return false;
  }

  protected IDesktop getDesktop() {
    return ClientSyncJob.getCurrentSession().getDesktop();
  }

}
