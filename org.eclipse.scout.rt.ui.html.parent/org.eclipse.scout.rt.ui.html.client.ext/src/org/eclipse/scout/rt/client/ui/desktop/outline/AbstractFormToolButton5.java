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
import org.eclipse.scout.rt.client.ui.action.tool.AbstractToolButton;
import org.eclipse.scout.rt.client.ui.form.IForm;

//FIXME CGU Harmonize form creation with AbstractTableControl and AbstractPageWithNodes5 (searchform / detailform) -> Never add to desktop, form should be managed by the responsible control -> allows javascript button to directly show the form without the need to go to the server first
public abstract class AbstractFormToolButton5 extends AbstractToolButton implements IFormToolButton5 {

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
  }

  protected IForm execStartForm() throws ProcessingException {
    return null;
  }

  @Override
  protected void execSelectionChanged(boolean selected) throws ProcessingException {
    if (selected && getForm() == null) {
      IForm form = execStartForm();
      if (form != null) {
        setForm(form);
      }
    }
  }

}
