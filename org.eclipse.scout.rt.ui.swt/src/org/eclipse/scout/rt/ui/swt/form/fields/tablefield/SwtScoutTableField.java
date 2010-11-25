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

import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.ITableField;
import org.eclipse.scout.rt.ui.swt.LogicalGridData;
import org.eclipse.scout.rt.ui.swt.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swt.basic.table.ISwtScoutTable;
import org.eclipse.scout.rt.ui.swt.basic.table.SwtScoutTable;
import org.eclipse.scout.rt.ui.swt.ext.StatusLabelEx;
import org.eclipse.scout.rt.ui.swt.extension.UiDecorationExtensionPoint;
import org.eclipse.scout.rt.ui.swt.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.swt.form.fields.SwtScoutFieldComposite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

public class SwtScoutTableField extends SwtScoutFieldComposite<ITableField<? extends ITable>> implements ISwtScoutTableField {

  private ISwtScoutTable m_tableComposite;
  private Label m_swtTableStatus;

  @Override
  protected void initializeSwt(Composite parent) {
    Composite container = getEnvironment().getFormToolkit().createComposite(parent);
    int labelStyle = UiDecorationExtensionPoint.getLookAndFeel().getFormFieldLabelAlignment();
    StatusLabelEx label = new StatusLabelEx(container, labelStyle, getEnvironment());
    getEnvironment().getFormToolkit().getFormToolkit().adapt(label, false, false);
    //
    setSwtContainer(container);
    setSwtLabel(label);

    // layout
    LogicalGridLayout containerLayout = new LogicalGridLayout(1, 0);
    container.setLayout(containerLayout);
  }

  /**
   * complete override
   */
  @Override
  protected void setFieldEnabled(Control swtField, boolean b) {
    if (m_tableComposite != null) {
      m_tableComposite.setEnabledFromScout(b);
    }
  }

  @Override
  public Control getSwtField() {
    return super.getSwtField();
  }

  @Override
  protected void attachScout() {
    setTableFromScout(getScoutObject().getTable());
    super.attachScout();
    setTableStatusFromScout();
  }

  protected void setTableFromScout(ITable table) {
    if (m_tableComposite != null && !m_tableComposite.isDisposed()) {
      m_tableComposite.dispose();
    }
    if (m_swtTableStatus != null && !m_swtTableStatus.isDisposed()) {
      m_swtTableStatus.dispose();
    }
    m_tableComposite = null;
    m_swtTableStatus = null;
    if (table != null) {
      LogicalGridData tableGridData = LogicalGridDataBuilder.createField(getScoutObject().getGridData());
      //table
      m_tableComposite = createSwtScoutTable();
      m_tableComposite.createField(getSwtContainer(), getScoutObject().getTable(), getEnvironment());
      m_tableComposite.getSwtField().setLayoutData(tableGridData);
      //table status
      if (getScoutObject().isTableStatusVisible()) {
        m_swtTableStatus = new Label(getSwtContainer(), SWT.NONE);
        LogicalGridData gd = new LogicalGridData();
        gd.gridx = tableGridData.gridx;
        gd.gridy = tableGridData.gridy + tableGridData.gridh;
        gd.gridw = tableGridData.gridw;
        gd.gridh = 1;
        gd.weightx = tableGridData.weightx;
        gd.weighty = 0.0;
        gd.fillHorizontal = true;
        m_swtTableStatus.setLayoutData(gd);
      }
      setSwtField(m_tableComposite.getSwtField());
    }
    if (!getSwtContainer().isDisposed()) {
      getSwtContainer().layout(true);
    }
  }

  protected void setTableStatusFromScout() {
    if (m_swtTableStatus != null) {
      String s = getScoutObject().getTableStatus();
      //bsi ticket 95826: eliminate newlines
      if (s != null) {
        s = s.replaceAll("[\\s]+", " ");
      }
      m_swtTableStatus.setText(s != null ? s : "");
    }
  }

  protected ISwtScoutTable createSwtScoutTable() {
    return new SwtScoutTable();
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (name.equals(ITableField.PROP_TABLE)) {
      setTableFromScout((ITable) newValue);
      if (isConnectedToScout()) {
        setLayoutDirty();
      }
    }
    else if (name.equals(ITableField.PROP_TABLE_STATUS)) {
      setTableStatusFromScout();
    }
  }
}
