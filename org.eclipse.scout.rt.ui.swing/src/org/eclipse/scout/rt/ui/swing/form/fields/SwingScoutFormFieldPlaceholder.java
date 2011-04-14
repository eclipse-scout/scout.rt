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
package org.eclipse.scout.rt.ui.swing.form.fields;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;
import org.eclipse.scout.rt.ui.swing.ext.BorderLayoutEx;
import org.eclipse.scout.rt.ui.swing.ext.JPanelEx;
import org.eclipse.scout.rt.ui.swing.ext.JStatusLabelEx;

/**
 * used for field models that have no ui implementation
 */
public class SwingScoutFormFieldPlaceholder implements ISwingScoutFormField<IFormField> {
  private IFormField m_scoutObject;
  private ISwingEnvironment m_env;
  private JComponent m_swingContainer;
  private JStatusLabelEx m_swingStatusLabel;

  public SwingScoutFormFieldPlaceholder() {
  }

  @Override
  public void createField(IFormField model, ISwingEnvironment environment) {
    m_scoutObject = model;
    m_env = environment;
    m_swingStatusLabel = getSwingEnvironment().createStatusLabel();
    m_swingStatusLabel.setText("Placeholder for " + model.getClass().getSimpleName());
    m_swingContainer = new JPanelEx(new BorderLayoutEx(0, 0));
    m_swingContainer.add(BorderLayoutEx.CENTER, m_swingStatusLabel);
  }

  @Override
  public JComponent getSwingContainer() {
    return m_swingContainer;
  }

  @Override
  public JStatusLabelEx getSwingLabel() {
    return m_swingStatusLabel;
  }

  public JLabel getSwingStatus() {
    return null;
  }

  @Override
  public JComponent getSwingField() {
    return null;
  }

  @Override
  public IFormField getScoutObject() {
    return m_scoutObject;
  }

  @Override
  public ISwingEnvironment getSwingEnvironment() {
    return m_env;
  }

  @Override
  public void connectToScout() {
  }

  @Override
  public void disconnectFromScout() {
  }

}
