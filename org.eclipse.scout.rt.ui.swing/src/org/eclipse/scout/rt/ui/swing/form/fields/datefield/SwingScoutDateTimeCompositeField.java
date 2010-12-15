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
package org.eclipse.scout.rt.ui.swing.form.fields.datefield;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.IDateField;
import org.eclipse.scout.rt.ui.swing.LogicalGridData;
import org.eclipse.scout.rt.ui.swing.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swing.ext.JPanelEx;
import org.eclipse.scout.rt.ui.swing.ext.JStatusLabelEx;
import org.eclipse.scout.rt.ui.swing.form.fields.ISwingScoutFormField;
import org.eclipse.scout.rt.ui.swing.form.fields.SwingScoutFieldComposite;

public class SwingScoutDateTimeCompositeField extends SwingScoutFieldComposite<IDateField> implements ISwingScoutFormField<IDateField> {
  private static final long serialVersionUID = 1L;
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwingScoutDateTimeCompositeField.class);

  private SwingScoutDateField m_dateField;
  private SwingScoutTimeField m_timeField;

  @Override
  protected void initializeSwing() {
    JPanelEx container = new JPanelEx();
    container.setOpaque(false);
    JStatusLabelEx label = getSwingEnvironment().createStatusLabel();
    container.add(label);
    m_dateField = new SwingScoutDateField();
    m_dateField.setIgnoreLabel(true);
    m_dateField.createField(getScoutObject(), getSwingEnvironment());
    m_dateField.getSwingContainer().putClientProperty(LogicalGridData.CLIENT_PROPERTY_NAME, createDateFieldGridData());
    container.add(m_dateField.getSwingContainer());
    m_timeField = new SwingScoutTimeField();
    m_timeField.setIgnoreLabel(true);
    m_timeField.createField(getScoutObject(), getSwingEnvironment());
    m_timeField.getSwingContainer().putClientProperty(LogicalGridData.CLIENT_PROPERTY_NAME, createTimeFieldGridData());
    container.add(m_timeField.getSwingContainer());
    //
    setSwingContainer(container);
    setSwingLabel(label);
    // layout
    getSwingContainer().setLayout(new LogicalGridLayout(getSwingEnvironment(), 1, 0));
  }

  protected LogicalGridData createDateFieldGridData() {
    LogicalGridData data = new LogicalGridData();
    data.gridx = 1;
    data.gridy = 0;
    data.gridw = 1;
    data.gridh = 1;
    data.weightx = 1.0;
    data.weighty = 0;
    data.useUiWidth = true;
    return data;
  }

  protected LogicalGridData createTimeFieldGridData() {
    LogicalGridData data = new LogicalGridData();
    data.gridx = 2;
    data.gridy = 0;
    data.gridw = 1;
    data.gridh = 1;
    data.weightx = 1.0;
    data.weighty = 0;
    data.useUiWidth = true;
    return data;
  }

}
