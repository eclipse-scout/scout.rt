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

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.ui.action.tool.AbstractToolButton;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.IForm;

/**
 * A tool button that can be used in the {@link IDesktop} to toggle a form in the tools area.
 */
public abstract class AbstractFormToolButton2 extends AbstractToolButton implements IFormToolButton2 {

  private boolean m_previousSelectionState = false;

  @Override
  protected boolean getConfiguredToggleAction() {
    return true;
  }

  @Override
  public final IForm getForm() {
    return (IForm) propertySupport.getProperty(PROP_FORM);
  }

  @Override
  public final void setForm(IForm f) {
    propertySupport.setProperty(PROP_FORM, f);
    setForm(f, false);
  }

  @Override
  public final void setForm(IForm form, boolean force) {
    IForm oldForm = getForm();
    if (force || form != oldForm) {
      if (form != null) {
        decorateForm(form);
      }
      //single observer
      IDesktop desktop = ClientSyncJob.getCurrentSession().getDesktop();
      if (desktop != null) {
        if (form == null) {
          // Close the "tab", when the form is null (but remember the previous state)
//          desktop.removeForm(oldForm);
          m_previousSelectionState = isSelected();
          setSelected(false);
          setEnabled(false);
        }
        else {
          setEnabled(true);
          if (!isSelected()) { // restore selection
            setSelected(m_previousSelectionState);
          }
//          if (isSelected()) {
//            if (oldForm != null) {
//              desktop.removeForm(oldForm);
//            }
//            desktop.addForm(form);
//          }
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
//        for (IToolButton b : desktop.getToolButtons()) {
//          if (b != this && b instanceof AbstractFormToolButton && b.isSelected()) {
//            b.setSelected(false);
//          }
//        }
      }
      // show form
      IForm oldForm = getForm();
      execStartForm();
      IForm newForm = getForm();
      if (oldForm == newForm) {
        if (newForm != null) {
          m_previousSelectionState = true;
//          desktop.addForm(newForm);
        }
      }
    }
    else {
//      // hide form
//      IForm form = getForm();
//      if (form != null) {
//        m_previousSelectionState = false;
//        desktop.removeForm(form);
//      }
    }
  }

  protected void decorateForm(IForm f) {
//    f.setAutoAddRemoveOnDesktop(false);
//    f.setDisplayHint(IForm.DISPLAY_HINT_VIEW);
//    f.setDisplayViewId(IForm.VIEW_ID_E);
  }
}
