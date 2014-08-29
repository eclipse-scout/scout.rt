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
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.AbstractExtensibleOutline;

public class AbstractOutline5 extends AbstractExtensibleOutline implements IOutline5 {
  private IForm m_defaultDetailForm;

  @Override
  protected IPageChangeStrategy createPageChangeStrategy() {
    return new DefaultPageChangeStrategy5();
  }

  @Override
  public IForm getDefaultDetailForm() {
    return m_defaultDetailForm;
  }

  public void setDefaultDetailForm(IForm form) {
    if (form != null) {
      if (form.getDisplayHint() != IForm.DISPLAY_HINT_VIEW) {
        form.setDisplayHint(IForm.DISPLAY_HINT_VIEW);
      }
      if (form.getDisplayViewId() == null) {
        form.setDisplayViewId(IForm.VIEW_ID_PAGE_DETAIL);
      }
      form.setAutoAddRemoveOnDesktop(false);
    }
    m_defaultDetailForm = form;
  }

  protected IForm execCreateDefaultDetailForm() throws ProcessingException {
    return null;
  }

  protected void execStartDefaultDetailForm(IForm form) throws ProcessingException {
  }

  @Override
  public void ensureDefaultDetailFormCreated() throws ProcessingException {
    if (getDefaultDetailForm() != null) {
      return;
    }
    IForm form = execCreateDefaultDetailForm();
    setDefaultDetailForm(form);
  }

  @Override
  public void ensureDefaultDetailFormStarted() throws ProcessingException {
    if (getDefaultDetailForm() == null || getDefaultDetailForm().isFormOpen()) {
      return;
    }
    execStartDefaultDetailForm(getDefaultDetailForm());
  }
}
