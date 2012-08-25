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
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.ITableField;
import org.eclipse.scout.rt.ui.swt.LogicalGridData;
import org.eclipse.scout.rt.ui.swt.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swt.basic.table.ISwtScoutTable;
import org.eclipse.scout.rt.ui.swt.basic.table.SwtScoutTable;
import org.eclipse.scout.rt.ui.swt.ext.StatusLabelEx;
import org.eclipse.scout.rt.ui.swt.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.swt.form.fields.SwtScoutFieldComposite;
import org.eclipse.scout.rt.ui.swt.util.SwtLayoutUtility;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class SwtScoutTableField extends SwtScoutFieldComposite<ITableField<? extends ITable>> implements ISwtScoutTableField {

  private ISwtScoutTable m_tableComposite;
  private ISwtTableStatus m_swtTableStatus;

  @Override
  protected void initializeSwt(Composite parent) {
    Composite container = getEnvironment().getFormToolkit().createComposite(parent);
    StatusLabelEx label = getEnvironment().getFormToolkit().createStatusLabel(container, getEnvironment(), getScoutObject());
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
    super.attachScout();
    setTableFromScout(getScoutObject().getTable());
  }

  protected synchronized void setTableFromScout(ITable table) {
    try {
      getSwtContainer().setRedraw(false);
      if (m_tableComposite != null && !m_tableComposite.isDisposed()) {
        m_tableComposite.dispose();
      }
      if (m_swtTableStatus != null) {
        m_swtTableStatus.dispose();
      }
      m_tableComposite = null;
      m_swtTableStatus = null;
      if (table != null) {
        //table
        LogicalGridData tableGridData = LogicalGridDataBuilder.createField(getScoutObject().getGridData());
        m_tableComposite = createSwtScoutTable();
        m_tableComposite.createField(getSwtContainer(), getScoutObject().getTable(), getEnvironment());
        m_tableComposite.getSwtField().setLayoutData(tableGridData);
        //table status
        if (getScoutObject().isTableStatusVisible()) {
          m_swtTableStatus = createSwtTableStatus();
        }
        setSwtField(m_tableComposite.getSwtField());
        setTableStatusFromScout();
      }
    }
    finally {
      getSwtContainer().setRedraw(true);
    }

    if (!getSwtContainer().isDisposed()) {
      getSwtContainer().layout(true, true);
    }
  }

  protected void setTableStatusFromScout() {
    if (m_swtTableStatus != null) {
      IProcessingStatus dataStatus = getScoutObject().getTablePopulateStatus();
      IProcessingStatus selectionStatus = getScoutObject().getTableSelectionStatus();
      m_swtTableStatus.setStatus(dataStatus, selectionStatus);
    }
  }

  protected ISwtScoutTable createSwtScoutTable() {
    return new SwtScoutTable();
  }

  protected ISwtTableStatus createSwtTableStatus() {
    return new SwtTableStatus(getEnvironment(), getSwtContainer(), getScoutObject());
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (name.equals(ITableField.PROP_TABLE)) {
      setTableFromScout((ITable) newValue);
      if (isConnectedToScout()) {
        SwtLayoutUtility.invalidateLayout(getSwtContainer());
      }
    }
    else if (name.equals(ITableField.PROP_TABLE_SELECTION_STATUS)) {
      setTableStatusFromScout();
    }
    else if (name.equals(ITableField.PROP_TABLE_POPULATE_STATUS)) {
      setTableStatusFromScout();
    }
  }
}
