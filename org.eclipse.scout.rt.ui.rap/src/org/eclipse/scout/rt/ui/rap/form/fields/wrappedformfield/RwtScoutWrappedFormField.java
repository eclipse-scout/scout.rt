/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.form.fields.wrappedformfield;

import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.wrappedform.IWrappedFormField;
import org.eclipse.scout.rt.ui.rap.form.IRwtScoutForm;
import org.eclipse.scout.rt.ui.rap.form.fields.RwtScoutFieldComposite;
import org.eclipse.scout.rt.ui.rap.util.RwtLayoutUtility;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

public class RwtScoutWrappedFormField extends RwtScoutFieldComposite<IWrappedFormField<? extends IForm>> implements IRwtScoutWrappedFormField {
  private IRwtScoutForm m_formComposite;

  @Override
  protected void initializeUi(Composite parent) {
    Composite container = getUiEnvironment().getFormToolkit().createComposite(parent);
    setUiContainer(container);
    // layout
    FillLayout fillLayout = new FillLayout();
    fillLayout.marginHeight = 2;
    fillLayout.marginWidth = 2;
    getUiContainer().setLayout(fillLayout);
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
      getUiContainer().setRedraw(false);
      if (m_formComposite != null) {
        m_formComposite.dispose();
        m_formComposite = null;
        setUiField(null);
      }
      if (innerForm != null) {
        m_formComposite = getUiEnvironment().createForm(getUiContainer(), innerForm);
        setUiField(m_formComposite.getUiContainer());
        m_formComposite.getUiContainer().setLayoutData(null);
      }
      if (isCreated()) {
        RwtLayoutUtility.invalidateLayout(getUiEnvironment(), getUiContainer());
      }
    }
    finally {
      getUiContainer().setRedraw(true);
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
