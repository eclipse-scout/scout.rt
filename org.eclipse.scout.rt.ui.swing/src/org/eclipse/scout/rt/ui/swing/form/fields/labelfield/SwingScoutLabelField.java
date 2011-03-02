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
package org.eclipse.scout.rt.ui.swing.form.fields.labelfield;

import javax.swing.JLabel;

import org.eclipse.scout.rt.client.ui.form.fields.labelfield.ILabelField;
import org.eclipse.scout.rt.ui.swing.LogicalGridData;
import org.eclipse.scout.rt.ui.swing.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swing.SingleLayout;
import org.eclipse.scout.rt.ui.swing.SwingLayoutUtility;
import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.ext.JLabelEx;
import org.eclipse.scout.rt.ui.swing.ext.JPanelEx;
import org.eclipse.scout.rt.ui.swing.ext.JStatusLabelEx;
import org.eclipse.scout.rt.ui.swing.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.swing.form.fields.SwingScoutValueFieldComposite;

public class SwingScoutLabelField extends SwingScoutValueFieldComposite<ILabelField> implements ISwingScoutLabelField {
  private static final long serialVersionUID = 1L;

  private JPanelEx m_fieldPanel;

  @Override
  protected void initializeSwing() {
    JPanelEx container = new JPanelEx();
    container.setOpaque(false);
    JStatusLabelEx label = getSwingEnvironment().createStatusLabel();
    container.add(label);
    m_fieldPanel = new JPanelEx(new SingleLayout());
    LogicalGridData fieldData = LogicalGridDataBuilder.createField(getSwingEnvironment(), getScoutObject().getGridData());
    fieldData.topInset = SwingLayoutUtility.getTextFieldTopInset();
    m_fieldPanel.putClientProperty(LogicalGridData.CLIENT_PROPERTY_NAME, fieldData);
    JLabelEx labelField = new JLabelEx();
    m_fieldPanel.add(labelField);
    container.add(m_fieldPanel);
    //
    setSwingContainer(container);
    setSwingLabel(label);
    setSwingField(labelField);
    // layout
    LogicalGridLayout layout = new LogicalGridLayout(getSwingEnvironment(), 1, 0);
    getSwingContainer().setLayout(layout);
  }

  public JLabelEx getSwingLabelField() {
    return (JLabelEx) getSwingField();
  }

  /*
   * scout properties
   */

  @Override
  protected void attachScout() {
    super.attachScout();
    ILabelField f = getScoutObject();
    setTextWrapFromScout(f.isWrapText());
  }

  protected void setTextWrapFromScout(boolean b) {
    // nop
  }

  @Override
  protected void setDisplayTextFromScout(String s) {
    JLabel swingField = getSwingLabelField();
    String oldText = swingField.getText();
    if (s == null) s = "";
    if (oldText == null) oldText = "";
    if (oldText.equals(s)) {
      return;
    }
    swingField.setText(s);
  }

  @Override
  protected void setHorizontalAlignmentFromScout(int scoutAlign) {
    getSwingLabelField().setHorizontalAlignment(SwingUtility.createHorizontalAlignment(scoutAlign));
  }

  /**
   * scout property handler override
   */
  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (name.equals(ILabelField.PROP_WRAP_TEXT)) {
      setTextWrapFromScout(((Boolean) newValue).booleanValue());
    }
  }

}
