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
package org.eclipse.scout.rt.ui.swt.form.fields.wrappedformfield;

import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.wrappedform.IWrappedFormField;
import org.eclipse.scout.rt.ui.swt.form.ISwtScoutForm;
import org.eclipse.scout.rt.ui.swt.form.fields.SwtScoutFieldComposite;
import org.eclipse.scout.rt.ui.swt.util.SwtLayoutUtility;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

public class SwtScoutWrappedFormField extends SwtScoutFieldComposite<IWrappedFormField<? extends IForm>> implements ISwtScoutWrappedFormField {
  private ISwtScoutForm m_formComposite;

  @Override
  protected void initializeSwt(Composite parent) {
    Composite container = getEnvironment().getFormToolkit().createComposite(parent);
    setSwtContainer(container);
    // layout
    FillLayout fillLayout = new FillLayout();
    fillLayout.marginHeight = 2;
    fillLayout.marginWidth = 2;
    getSwtContainer().setLayout(fillLayout);
  }

  @Override
  public IWrappedFormField<?> getScoutObject() {
    return super.getScoutObject();
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    setInnerFormFromScout(getScoutObject().getInnerForm());
  }

  protected void setInnerFormFromScout(IForm innerForm) {
    try {
      getSwtContainer().setRedraw(false);
      if (m_formComposite != null) {
        m_formComposite.dispose();
        m_formComposite = null;
        setSwtField(null);
      }
      if (innerForm != null) {
        m_formComposite = getEnvironment().createForm(getSwtContainer(), innerForm);
        setSwtField(m_formComposite.getSwtContainer());
        m_formComposite.getSwtContainer().setLayoutData(null);
      }
      if (isConnectedToScout()) {
        SwtLayoutUtility.invalidateLayout(getSwtContainer());
      }
    }
    finally {
      getSwtContainer().setRedraw(true);
    }
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (name.equals(IWrappedFormField.PROP_INNER_FORM)) {
      setInnerFormFromScout((IForm) newValue);
    }
  }

}
