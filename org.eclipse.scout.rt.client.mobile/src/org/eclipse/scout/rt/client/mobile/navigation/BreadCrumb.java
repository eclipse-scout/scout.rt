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
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientJob;
import org.eclipse.scout.rt.client.mobile.ui.desktop.MobileDesktopUtility;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.form.IForm;

/**
 * @since 3.9.0
 */
public class BreadCrumb implements IBreadCrumb {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(BreadCrumb.class);

  private IForm m_form;
  private IPage m_page;
  private IBreadCrumbsNavigation m_breadCrumbsNavigation;

  public BreadCrumb(IBreadCrumbsNavigation breadCrumbsNavigation, IForm form, IPage page) {
    m_breadCrumbsNavigation = breadCrumbsNavigation;
    m_form = form;
    m_page = page;
  }

  @Override
  public void activate() throws ProcessingException {
    List<IForm> currentNavigationForms = getBreadCrumbsNavigation().getCurrentNavigationForms();
    for (IForm form : currentNavigationForms) {
      if (form != getForm() && !getBreadCrumbsNavigation().containsFormInHistory(form)) {
        MobileDesktopUtility.closeForm(form);
      }
    }

    //Add form to desktop if it is open but has been removed
    if (getForm() != null) {
      IDesktop desktop = ClientJob.getCurrentSession().getDesktop();
      if (getForm().isFormOpen() && !desktop.isShowing(getForm())) {
        if (MobileDesktopUtility.isToolForm(getForm())) {
          MobileDesktopUtility.openToolForm(getForm());
        }
        else {
          desktop.addForm(getForm());
        }
      }
    }

    if (getPage() != null) {
      IOutline outline = getPage().getOutline();
      IDesktop desktop = getBreadCrumbsNavigation().getDesktop();
      if (desktop.getOutline() != outline) {
        desktop.setOutline(outline);
      }
      if (outline != null) {
        outline.selectNode(getPage());
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
  public IPage getPage() {
    return m_page;
  }

  @Override
  public String toString() {
    if (getPage() != null) {
      return "Page: " + getPage().toString();
    }
    else {
      String formName = getForm().getTitle();
      if (StringUtility.isNullOrEmpty(formName)) {
        formName = getForm().toString();
      }
      return "Form: " + formName;
    }
  }

  @Override
  public boolean belongsTo(IForm form, IPage page) {
    return getForm() == form && getPage() == page;
  }

}
