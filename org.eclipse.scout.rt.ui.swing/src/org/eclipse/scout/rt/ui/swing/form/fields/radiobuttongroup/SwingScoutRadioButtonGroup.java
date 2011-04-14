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
package org.eclipse.scout.rt.ui.swing.form.fields.radiobuttongroup;

import javax.swing.JPanel;

import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.radiobuttongroup.IRadioButtonGroup;
import org.eclipse.scout.rt.ui.swing.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swing.ext.JPanelEx;
import org.eclipse.scout.rt.ui.swing.ext.JStatusLabelEx;
import org.eclipse.scout.rt.ui.swing.form.fields.ISwingScoutFormField;
import org.eclipse.scout.rt.ui.swing.form.fields.SwingScoutValueFieldComposite;
import org.eclipse.scout.rt.ui.swing.form.fields.radiobuttongroup.layout.RadioButtonGroupLayout;

public class SwingScoutRadioButtonGroup extends SwingScoutValueFieldComposite<IRadioButtonGroup<?>> implements ISwingScoutRadioButtonGroup {

  @Override
  protected void initializeSwing() {
    super.initializeSwing();
    JPanelEx container = new JPanelEx();
    container.setName("SwingScoutRadioButtonGroup.container");
    container.setOpaque(false);
    JStatusLabelEx label = getSwingEnvironment().createStatusLabel();
    container.add(label);
    JPanel buttonPanel = new JPanelEx();
    buttonPanel.setName("SwingScoutRadioButtonGroup.buttonPanel");
    buttonPanel.setOpaque(false);
    GridData scoutGridData = getScoutObject().getGridData();
    boolean usesLogicalGrid = (getScoutObject().getGridRowCount() == scoutGridData.h && !scoutGridData.useUiHeight);
    if (usesLogicalGrid) {
      buttonPanel.setLayout(new RadioButtonGroupLayout(getScoutObject(), getSwingEnvironment().getFormColumnGap(), getSwingEnvironment().getFormRowGap()));
    }
    else {
      buttonPanel.setLayout(new RadioButtonGroupLayout(getScoutObject(), 0, 0));
    }
    // add all radio buttons
    IFormField[] scoutFields = getScoutObject().getFields();
    for (int i = 0; i < scoutFields.length; i++) {
      ISwingScoutFormField comp = getSwingEnvironment().createFormField(buttonPanel, scoutFields[i]);
      buttonPanel.add(comp.getSwingContainer());
    }
    container.add(buttonPanel);
    // register
    setSwingLabel(label);
    setSwingField(buttonPanel);
    setSwingContainer(container);
    // layout
    container.setLayout(new LogicalGridLayout(getSwingEnvironment(), 1, 0));
  }

  @Override
  public JPanel getSwingRadioButtonPanel() {
    return (JPanel) getSwingField();
  }

}
