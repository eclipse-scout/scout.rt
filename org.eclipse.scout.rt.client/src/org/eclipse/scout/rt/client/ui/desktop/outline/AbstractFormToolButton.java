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
package org.eclipse.scout.rt.client.ui.desktop.outline;

import org.eclipse.scout.commons.annotations.ConfigPropertyValue;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.ui.action.tool.AbstractToolButton;
import org.eclipse.scout.rt.client.ui.action.tool.IToolButton;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.IForm;

/**
 * A tool button that can be used in the {@link IDesktop} to toggle a form in the tools area.
 */
public abstract class AbstractFormToolButton<FORM extends IForm> extends AbstractToolButton implements IFormToolButton<FORM> {

  private FORM m_form;
  private boolean m_previousSelectionState = false;

  @ConfigPropertyValue("true")
  @Override
  protected boolean getConfiguredToggleAction() {
    return true;
  }

  @Override
  public final FORM getForm() {
    return m_form;
  }

  @Override
  public final void setForm(FORM f) {
    setForm(f, false);
  }

  @Override
  public final void setForm(FORM f, boolean force) {
    if (force || f != m_form) {
      if (f != null) {
        decorateForm(f);
      }
      FORM oldForm = m_form;
      m_form = f;
      //single observer
      IDesktop desktop = ClientSyncJob.getCurrentSession().getDesktop();
      if (desktop != null) {
        if (m_form == null) {
          // Close the "tab", when the form is null (but remember the previous state)
          desktop.removeForm(oldForm);
          m_previousSelectionState = isSelected();
          setSelected(false);
          setEnabled(false);
        }
        else {
          setEnabled(true);
          if (!isSelected()) { // restore selection
            setSelected(m_previousSelectionState);
          }
          if (isSelected()) {
            if (oldForm != null) {
              desktop.removeForm(oldForm);
            }
            desktop.addForm(m_form);
          }
        }
      }
    }
  }

  /**
   * Called every time the tool button is selected.
   * <p>
   * Check {@link #getForm()} to see what form is currently represented.
   * <p>
   * Example code is:<code><pre>
   * if(getForm()==null){
   *   f=new MyForm();
   *   decorate(f);
   *   f.startForm()
   *   setForm(f);
   * }
   * </pre></code> Call {@link #setForm(IForm)} to change the current form.
   */
  protected void execStartForm() throws ProcessingException {
  }

  @Override
  protected void execToggleAction(boolean selected) throws ProcessingException {
    IDesktop desktop = ClientSyncJob.getCurrentSession().getDesktop();
    if (desktop == null) {
      return;
    }
    if (selected) {
      if (isToggleAction()) {
        // unselect other form tool buttons
        for (IToolButton b : desktop.getToolButtons()) {
          if (b != this && b instanceof AbstractFormToolButton && b.isSelected()) {
            b.setSelected(false);
          }
        }
      }
      // show form
      FORM oldForm = getForm();
      execStartForm();
      if (oldForm == m_form) {
        if (m_form != null) {
          m_previousSelectionState = true;
          desktop.addForm(m_form);
        }
      }
    }
    else {
      // hide form
      if (m_form != null) {
        m_previousSelectionState = false;
        desktop.removeForm(m_form);
      }
    }
  }

  protected void decorateForm(FORM f) {
    f.setAutoAddRemoveOnDesktop(false);
    f.setDisplayHint(IForm.DISPLAY_HINT_VIEW);
    f.setDisplayViewId(IForm.VIEW_ID_E);
  }
}
