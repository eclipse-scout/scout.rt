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
package org.eclipse.scout.rt.client.mobile.ui.forms;

import java.util.Stack;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.mobile.ui.desktop.MobileDesktopUtility;
import org.eclipse.scout.rt.client.ui.desktop.DesktopEvent;
import org.eclipse.scout.rt.client.ui.desktop.DesktopListener;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.FormEvent;
import org.eclipse.scout.rt.client.ui.form.FormListener;
import org.eclipse.scout.rt.client.ui.form.IForm;

/**
 * @since 3.8.0
 */
//FIXME CGU if a stacked form gets removed on the desktop it does not automatically gets removed on the stack.
//For example crm svg is open, a tools form gets open and svg stacked.
//Changing the page should remove the svg form which does not happen because it is not on the desktop anymore. Should we change remove FormStack and handle stacking only in UI?
public class FormStack {
  private Stack<IForm> m_formHistory = new Stack<IForm>();
  private P_FormListener m_formListener;
  private P_DeskopListener m_desktopListener;
  private String m_displayViewId;

  public FormStack(String displayViewId) {
    m_displayViewId = displayViewId;

    m_desktopListener = new P_DeskopListener();
    getDesktop().addDesktopListener(m_desktopListener);
  }

  protected IDesktop getDesktop() {
    return ClientSyncJob.getCurrentSession().getDesktop();
  }

  public void makePreviousFormVisible() {
    if (m_formHistory.size() == 0) {
      return;
    }

    IForm previousForm = m_formHistory.pop();
    MobileDesktopUtility.addFormToDesktop(previousForm);

    if (m_formListener != null) {
      previousForm.removeFormListener(m_formListener);
    }
  }

  public void makePreviousFormInvisible(IForm previousForm) {
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

  public void clearFormHistory() {
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

  public int size() {
    return m_formHistory.size();
  }

  public boolean belongsToThisStack(IForm form) {
    return m_displayViewId.equals(form.getDisplayViewId());
  }

  private void destroy() {
    if (m_desktopListener != null) {
      getDesktop().removeDesktopListener(m_desktopListener);
      m_desktopListener = null;
    }

    clearFormHistory();
  }

  private class P_DeskopListener implements DesktopListener {

    @Override
    public void desktopChanged(DesktopEvent e) {

      switch (e.getType()) {
        case DesktopEvent.TYPE_FORM_ADDED: {
          handleFormAdded(e);
          break;
        }
        case DesktopEvent.TYPE_FORM_REMOVED: {
          handleFormRemoved(e);
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

    private void handleFormAdded(DesktopEvent e) {
      IDesktop desktop = e.getDesktop();
      IForm form = e.getForm();
      if (!belongsToThisStack(form)) {
        return;
      }

      IForm[] viewStack = desktop.getViewStack();
      for (IForm previousForm : viewStack) {
        if (belongsToThisStack(previousForm) && form != previousForm) {
          makePreviousFormInvisible(previousForm);
        }
      }

    }

    private void handleFormRemoved(DesktopEvent e) {
      IForm form = e.getForm();
      if (!belongsToThisStack(form)) {
        return;
      }

      if (!MobileDesktopUtility.isAnyViewVisible(form.getDisplayViewId())) {
        makePreviousFormVisible();
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
