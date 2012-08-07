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
package org.eclipse.scout.rt.client.mobile.ui.basic.table;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ClientJob;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.mobile.ui.basic.table.form.TableRowForm;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.form.FormEvent;
import org.eclipse.scout.rt.client.ui.form.FormListener;
import org.eclipse.scout.rt.client.ui.form.IForm;

/**
 * @since 3.9.0
 */
public abstract class AbstractMobileTable extends AbstractTable implements IMobileTable {
  private DrillDownStyleMap m_drillDownStyleMap;

  public AbstractMobileTable() {
    this(true);
  }

  public AbstractMobileTable(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected void initConfig() {
    super.initConfig();

    m_drillDownStyleMap = new DrillDownStyleMap();
    setAutoCreateTableRowForm(execIsAutoCreateTableRowForm());
  }

  @Override
  public boolean isAutoCreateTableRowForm() {
    return propertySupport.getPropertyBool(PROP_AUTO_CREATE_TABLE_ROW_FORM);
  }

  @Override
  public void setAutoCreateTableRowForm(boolean autoCreateTableRowForm) {
    propertySupport.setPropertyBool(PROP_AUTO_CREATE_TABLE_ROW_FORM, autoCreateTableRowForm);
  }

  public void putDrillDownStyle(ITableRow row, String drillDownStyle) {
    m_drillDownStyleMap.put(row, drillDownStyle);
  }

  public String getDrillDownStyle(ITableRow row) {
    return m_drillDownStyleMap.get(row);
  }

  @Override
  public void setDrillDownStyleMap(DrillDownStyleMap drillDownStyleMap) {
    m_drillDownStyleMap = drillDownStyleMap;
    if (m_drillDownStyleMap == null) {
      m_drillDownStyleMap = new DrillDownStyleMap();
    }
  }

  @Override
  public DrillDownStyleMap getDrillDownStyleMap() {
    return m_drillDownStyleMap;
  }

  protected boolean execIsAutoCreateTableRowForm() {
    if (isCheckable()) {
      return false;
    }

    return true;
  }

  protected void startTableRowForm(ITableRow row) throws ProcessingException {
    TableRowForm form = new TableRowForm(row);
    form.setDisplayHint(IForm.DISPLAY_HINT_DIALOG);
    form.setModal(true);
    form.start();
    form.addFormListener(new FormListener() {

      @Override
      public void formChanged(FormEvent e) throws ProcessingException {
        if (FormEvent.TYPE_CLOSED == e.getType()) {
          clearSelection();
        }
      }

    });
  }

  protected boolean isClearingSelectionNecessary() {
    if (getSelectedRow() == null) {
      return false;
    }

    return (IRowSummaryColumn.DRILL_DOWN_STYLE_ICON.equals(getDrillDownStyle(getSelectedRow())));
  }

  protected void clearSelectionDelayed() {
    ClientSyncJob job = new ClientSyncJob("Clearing selection", ClientJob.getCurrentSession()) {

      @Override
      protected void runVoid(IProgressMonitor monitor) throws Throwable {
        clearSelection();
      }

    };
    job.schedule();
  }

  protected void clearSelection() {
    if (isClearingSelectionNecessary()) {
      selectRow(null);
    }
  }
}
