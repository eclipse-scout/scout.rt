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
package org.eclipse.scout.rt.ui.swing.form.fields.wrappedformfield;

import javax.swing.JComponent;

import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.wrappedform.IWrappedFormField;
import org.eclipse.scout.rt.ui.swing.SingleLayout;
import org.eclipse.scout.rt.ui.swing.ext.JPanelEx;
import org.eclipse.scout.rt.ui.swing.form.ISwingScoutForm;
import org.eclipse.scout.rt.ui.swing.form.fields.SwingScoutFieldComposite;

public class SwingScoutWrappedFormField extends SwingScoutFieldComposite<IWrappedFormField<?>> implements ISwingScoutWrappedFormField {
  private ISwingScoutForm m_formComposite;
  // cache
  private IForm m_currentInnerFormModel;

  @Override
  protected void initializeSwing() {
    JPanelEx container = new JPanelEx(new SingleLayout());
    container.setName(getScoutObject().getClass().getSimpleName());
    container.setOpaque(false);
    setSwingContainer(container);
    setInnerFormFromScout();
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    setInnerFormFromScout();
  }

  protected void setInnerFormFromScout() {
    IForm f = getScoutObject().getInnerForm();
    if (f != m_currentInnerFormModel) {
      JComponent container = getSwingContainer();
      container.removeAll();
      m_formComposite = null;
      m_currentInnerFormModel = f;
      if (f != null) {
        m_formComposite = getSwingEnvironment().createForm(container, f);
        JComponent comp = m_formComposite.getSwingFormPane();
        container.add(comp);
      }
      if (container != null && container.getRootPane() != null) {
        container.getRootPane().revalidate();
      }
    }
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (name.equals(IWrappedFormField.PROP_INNER_FORM)) {
      setInnerFormFromScout();
    }
  }

}
