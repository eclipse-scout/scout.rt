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

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ClientJob;
import org.eclipse.scout.rt.client.mobile.ui.desktop.MobileDesktopUtility;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.IForm;

/**
 * @since 3.9.0
 */
public class BreadCrumb implements IBreadCrumb {
  private IForm m_form;
  private IBreadCrumbsNavigation m_breadCrumbsNavigation;

  public BreadCrumb(IBreadCrumbsNavigation breadCrumbsNavigation, IForm form) {
    m_breadCrumbsNavigation = breadCrumbsNavigation;
    m_form = form;
  }

  @Override
  public void activate() throws ProcessingException {
    autoCloseNavigationForms();

    //Add form to desktop if it is open but has been removed
    if (getForm() != null) {
      IDesktop desktop = ClientJob.getCurrentSession().getDesktop();
      if (getForm().isFormOpen() && !desktop.isShowing(getForm())) {
        if (MobileDesktopUtility.isToolForm(getForm())) {
          MobileDesktopUtility.openToolForm(getForm());
        }
        else {
          MobileDesktopUtility.addFormToDesktop(getForm());
        }
      }
    }
  }

  private void autoCloseNavigationForms() throws ProcessingException {
    IDesktop desktop = ClientJob.getCurrentSession().getDesktop();

    List<IForm> currentNavigationForms = getBreadCrumbsNavigation().getCurrentNavigationForms();
    for (IForm form : currentNavigationForms) {
      //Never close the active outline table form
      if (form == desktop.getOutlineTableForm()) {
        continue;
      }
      if (form != getForm() && !getBreadCrumbsNavigation().containsFormInHistory(form)) {
        MobileDesktopUtility.closeForm(form);
      }
    }
  }

  public IBreadCrumbsNavigation getBreadCrumbsNavigation() {
    return m_breadCrumbsNavigation;
  }

  @Override
  public IForm getForm() {
    return m_form;
  }

  @Override
  public String toString() {
    String formName = getForm().getTitle();
    if (StringUtility.isNullOrEmpty(formName)) {
      formName = getForm().toString();
    }
    return "Form: " + formName;
  }

  @Override
  public boolean belongsTo(IForm form) {
    return getForm() == form;
  }

}
