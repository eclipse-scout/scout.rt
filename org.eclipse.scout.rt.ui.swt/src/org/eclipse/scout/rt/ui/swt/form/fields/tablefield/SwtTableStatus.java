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
package org.eclipse.scout.rt.ui.swt.form.fields.tablefield;

import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.ITableField;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.scout.rt.ui.swt.LogicalGridData;
import org.eclipse.scout.rt.ui.swt.form.fields.LogicalGridDataBuilder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Default implementation using a target label
 */
public class SwtTableStatus implements ISwtTableStatus {
  private final ISwtEnvironment m_env;
  private final Composite m_container;
  private final Label m_populateLabel;
  private final Label m_selectionLabel;
  private boolean m_layoutNeeded;

  public SwtTableStatus(ISwtEnvironment env, Composite container, ITableField<?> model) {
    m_env = env;
    m_container = container;
    LogicalGridData tableGridData = LogicalGridDataBuilder.createField(model.getGridData());
    //Label 1
    m_populateLabel = m_env.getFormToolkit().createLabel(m_container, "", SWT.NONE);
    m_populateLabel.setVisible(false);
    LogicalGridData gd = new LogicalGridData();
    gd.gridx = tableGridData.gridx;
    gd.gridy = tableGridData.gridy + tableGridData.gridh;
    gd.gridw = tableGridData.gridw;
    gd.gridh = 1;
    gd.weightx = tableGridData.weightx;
    gd.weighty = 0.0;
    gd.fillHorizontal = true;
    m_populateLabel.setLayoutData(gd);
    //Label 2
    m_selectionLabel = m_env.getFormToolkit().createLabel(m_container, "", SWT.NONE);
    m_selectionLabel.setVisible(false);
    gd = new LogicalGridData();
    gd.gridx = tableGridData.gridx;
    gd.gridy = tableGridData.gridy + tableGridData.gridh + 1;
    gd.gridw = tableGridData.gridw;
    gd.gridh = 1;
    gd.weightx = tableGridData.weightx;
    gd.weighty = 0.0;
    gd.fillHorizontal = true;
    m_selectionLabel.setLayoutData(gd);
  }

  @Override
  public void dispose() {
    if (!m_populateLabel.isDisposed()) {
      m_populateLabel.dispose();
    }
    if (!m_selectionLabel.isDisposed()) {
      m_selectionLabel.dispose();
    }
  }

  @Override
  public void setStatus(IProcessingStatus populateStatus, IProcessingStatus selectionStatus) {
    setStatusImpl(m_populateLabel, populateStatus, true);
    setStatusImpl(m_selectionLabel, selectionStatus, false);
    if (m_layoutNeeded) {
      m_layoutNeeded = false;
      m_container.layout(true);
    }
  }

  private void setStatusImpl(Label field, IProcessingStatus status, boolean hideWhenNullStatus) {
    if (status == null) {
      field.setText("");
      if (hideWhenNullStatus) {
        if (field.getVisible()) {
          m_layoutNeeded = true;
          field.setVisible(false);
        }
      }
      return;
    }
    //
    if (!field.getVisible()) {
      m_layoutNeeded = true;
      field.setVisible(true);
    }
    String text = status.getMessage();
    //bsi ticket 95826: eliminate newlines
    if (text != null) {
      text = text.replaceAll("[\\s]+", " ");
    }
    field.setText(text != null ? " " + text : "");
    //style
    switch (status.getSeverity()) {
      case IProcessingStatus.ERROR:
      case IProcessingStatus.FATAL: {
        field.setForeground(m_env.getColor(new RGB(0xcc, 0, 0)));
        break;
      }
      case IProcessingStatus.WARNING: {
        field.setForeground(m_env.getColor(new RGB(0xfe, 0x9a, 0x23)));
        break;
      }
      default: {
        field.setForeground(null);
      }
    }
  }
}
