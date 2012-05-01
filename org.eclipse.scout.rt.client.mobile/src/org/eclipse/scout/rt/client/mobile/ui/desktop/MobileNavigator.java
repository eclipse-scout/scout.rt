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
package org.eclipse.scout.rt.client.mobile.ui.desktop;

import java.util.Stack;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.mobile.ui.forms.OutlineChooserForm;
import org.eclipse.scout.rt.client.ui.desktop.DesktopEvent;
import org.eclipse.scout.rt.client.ui.desktop.DesktopListener;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.navigation.INavigationHistoryService;
import org.eclipse.scout.rt.client.ui.form.FormEvent;
import org.eclipse.scout.rt.client.ui.form.FormListener;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.service.SERVICES;

/**
 * @since 3.8.0
 */
public class MobileNavigator {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(MobileNavigator.class);

  private Stack<IForm> m_formHistory = new Stack<IForm>();
  private P_FormListener m_formListener;
  private P_DeskopListener m_desktopListener;

  public MobileNavigator() {
    m_desktopListener = new P_DeskopListener();
    getDesktop().addDesktopListener(m_desktopListener);
  }

  public void stepBack() throws ProcessingException {
    IDesktop desktop = ClientSyncJob.getCurrentSession().getDesktop();
    if (desktop == null) {
      return;
    }

    IForm currentForm = getCurrentForm();

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
      if (m_formHistory.size() > 0) {
        MobileDesktopUtility.closeOpenForms();
      }
      else {
        LOG.info("Tried to step back although it is not possible because form history as well as the navigation history are empty.");
      }
    }

    //FIXME CGU If a form is openend which allows to navigate further in the tree, back won't open that form at the correct time. Similar problem with the outline chooser form exists.

  }

  public boolean steppingBackPossible() {
    return !(getCurrentForm() instanceof OutlineChooserForm);
  }

  public IForm getCurrentForm() {
    //View stack contains maximum one form
    IForm[] viewStack = getDesktop().getViewStack();
    if (viewStack.length > 0) {
      return viewStack[0];
    }

    return null;
  }

  private void makePreviousFormVisible() {
    if (m_formHistory.size() == 0) {
      return;
    }

    IForm previousForm = m_formHistory.pop();
    MobileDesktopUtility.addFormToDesktop(previousForm);

    if (m_formListener != null) {
      previousForm.removeFormListener(m_formListener);
    }
  }

  private void makePreviousFormInvisible(IForm previousForm) {
    if (previousForm.isFormClosed()) {
      return;
    }

    m_formHistory.push(previousForm);

    MobileDesktopUtility.removeFormFromDesktop(previousForm);

    if (m_formListener == null) {
      m_formListener = new P_FormListener();
    }
    previousForm.addFormListener(m_formListener);
  }

  private IDesktop getDesktop() {
    return ClientSyncJob.getCurrentSession().getDesktop();
  }

  private void destroy() {
    if (m_desktopListener != null) {
      getDesktop().removeDesktopListener(m_desktopListener);
      m_desktopListener = null;
    }

    clearFormHistory();
  }

  private void clearFormHistory() {
    if (m_formHistory == null) {
      return;
    }

    if (m_formListener != null) {
      for (IForm form : m_formHistory) {
        form.removeFormListener(m_formListener);
      }
      m_formListener = null;
    }
    m_formHistory.clear();
  }

  private class P_DeskopListener implements DesktopListener {

    @Override
    public void desktopChanged(DesktopEvent e) {
      IDesktop desktop = e.getDesktop();
      IForm form = e.getForm();
      switch (e.getType()) {
        case DesktopEvent.TYPE_FORM_ADDED: {
          final IForm[] viewStack = desktop.getViewStack();
          for (IForm previousForm : viewStack) {
            if (form != previousForm) {
              makePreviousFormInvisible(previousForm);
            }
          }

          break;
        }
        case DesktopEvent.TYPE_FORM_REMOVED: {
          final IForm[] viewStack = desktop.getViewStack();
          if (viewStack.length == 0) {
            makePreviousFormVisible();
          }

          break;
        }
        case DesktopEvent.TYPE_DESKTOP_CLOSED: {
          destroy();
          break;
        }
        default:
          break;
      }
    }
  }

  private class P_FormListener implements FormListener {
    @Override
    public void formChanged(FormEvent e) throws ProcessingException {
      if (e.getType() == FormEvent.TYPE_CLOSED) {
        IForm form = (IForm) e.getSource();

        // Make sure the form history does not contain closed forms because showing a closed form means showing a white (empty) page.
        if (m_formHistory.size() > 0 && m_formHistory.contains(form)) {
          m_formHistory.remove(form);
        }
      }
    }
  }

}
