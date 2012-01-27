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
package org.eclipse.scout.rt.ui.swing.form.fields.tablefield;

import java.awt.Color;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.BorderUIResource;

import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.ITableField;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;
import org.eclipse.scout.rt.ui.swing.LogicalGridData;
import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.ext.JLabelEx;
import org.eclipse.scout.rt.ui.swing.form.fields.LogicalGridDataBuilder;

/**
 * Default implementation using a target label
 */
public class SwingTableStatus implements ISwingTableStatus {
  private final ISwingEnvironment m_env;
  private final JLabel m_populateLabel;
  private final JLabel m_selectionLabel;

  public SwingTableStatus(ISwingEnvironment env, JComponent container, ITableField<?> model) {
    m_env = env;
    //Label 1
    m_populateLabel = new JLabelEx();
    if (!SwingUtility.isSynth()) {
      m_populateLabel.setBorder(new BorderUIResource(new EmptyBorder(0, 4, 0, 0)));
    }
    //set synth name AFTER setting ui border
    if (model.getForm() != null && IForm.VIEW_ID_PAGE_TABLE.equals(model.getForm().getDisplayViewId())) {
      m_populateLabel.setName("Synth.WideTableStatus");
    }
    else {
      m_populateLabel.setName("Synth.TableStatus");
    }
    LogicalGridData tableGridData = LogicalGridDataBuilder.createField(m_env, model.getGridData());
    LogicalGridData gd = new LogicalGridData();
    gd.gridx = tableGridData.gridx;
    gd.gridy = tableGridData.gridy + tableGridData.gridh;
    gd.gridw = tableGridData.gridw;
    gd.gridh = 1;
    gd.weightx = tableGridData.weightx;
    gd.weighty = 0.0;
    gd.fillHorizontal = true;
    m_populateLabel.putClientProperty(LogicalGridData.CLIENT_PROPERTY_NAME, gd);
    container.add(m_populateLabel);
    //Label 2
    m_selectionLabel = new JLabelEx();
    if (!SwingUtility.isSynth()) {
      m_selectionLabel.setBorder(new BorderUIResource(new EmptyBorder(0, 4, 0, 0)));
    }
    //set synth name AFTER setting ui border
    if (model.getForm() != null && IForm.VIEW_ID_PAGE_TABLE.equals(model.getForm().getDisplayViewId())) {
      m_selectionLabel.setName("Synth.WideTableStatus");
    }
    else {
      m_selectionLabel.setName("Synth.TableStatus");
    }
    tableGridData = LogicalGridDataBuilder.createField(m_env, model.getGridData());
    gd = new LogicalGridData();
    gd.gridx = tableGridData.gridx;
    gd.gridy = tableGridData.gridy + tableGridData.gridh + 1;
    gd.gridw = tableGridData.gridw;
    gd.gridh = 1;
    gd.weightx = tableGridData.weightx;
    gd.weighty = 0.0;
    gd.fillHorizontal = true;
    m_selectionLabel.putClientProperty(LogicalGridData.CLIENT_PROPERTY_NAME, gd);
    container.add(m_selectionLabel);
  }

  @Override
  public void setStatus(IProcessingStatus populateStatus, IProcessingStatus selectionStatus) {
    setStatusImpl(m_populateLabel, populateStatus, true);
    setStatusImpl(m_selectionLabel, selectionStatus, true);
  }

  private void setStatusImpl(JLabel field, IProcessingStatus status, boolean hideWhenNullStatus) {
    if (status == null) {
      field.setText(null);
      if (hideWhenNullStatus) {
        field.setVisible(false);
      }
      return;
    }
    field.setVisible(true);
    String text = status.getMessage();
    //bsi ticket 95826: eliminate newlines
    if (text != null) {
      text = text.replaceAll("[\\s]+", " ");
    }
    field.setText(text);
    //style
    switch (status.getSeverity()) {
      case IProcessingStatus.ERROR:
      case IProcessingStatus.FATAL: {
        field.setForeground(new Color(0xcc0000));
        break;
      }
      case IProcessingStatus.WARNING: {
        field.setForeground(new Color(0xfe9a23));
        break;
      }
      default: {
        field.setForeground(new Color(0x264159));
      }
    }
  }
}
