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
package org.eclipse.scout.rt.ui.rap.mobile.form.fields.tablefield;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.mobile.ui.basic.table.MobileTable;
import org.eclipse.scout.rt.client.mobile.ui.form.fields.table.IMobileTableField;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.ui.rap.basic.table.IRwtScoutTable;
import org.eclipse.scout.rt.ui.rap.form.fields.tablefield.IRwtTableStatus;
import org.eclipse.scout.rt.ui.rap.form.fields.tablefield.RwtScoutTableField;
import org.eclipse.scout.rt.ui.rap.window.desktop.IRwtScoutActionBar;

/**
 * @since 3.9.0
 */
public class RwtScoutMobileTableField extends RwtScoutTableField {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(RwtScoutMobileTableField.class);

  private MobileTable m_mobileTable;
  private boolean m_columnTransformationEnabled;

  public RwtScoutMobileTableField() {
    this(true);
  }

  public RwtScoutMobileTableField(boolean columnTransformationEnabled) {
    m_columnTransformationEnabled = columnTransformationEnabled;
  }

  @Override
  protected IRwtScoutTable createRwtScoutTable() {
    return new RwtScoutMobileList();
  }

  @Override
  protected void setTableFromScout(ITable table) {
    if (!m_columnTransformationEnabled) {
      super.setTableFromScout(table);
      return;
    }

    disposeMobileTable();

    if (table == null) {
      super.setTableFromScout(table);
      return;
    }
    else {
      m_mobileTable = wrapTable(table);
      super.setTableFromScout(m_mobileTable);
    }
  }

  private MobileTable wrapTable(final ITable table) {
    final Holder<MobileTable> holder = new Holder<MobileTable>(MobileTable.class);

    ClientSyncJob job = new ClientSyncJob("", getUiEnvironment().getClientSession()) {
      @Override
      protected void runVoid(IProgressMonitor monitor) throws Throwable {
        MobileTable wrapperTable = new MobileTable(table);
        try {
          wrapperTable.setTableChanging(true);
          wrapperTable.setTableRowFormDisplayHint(getScoutObject().getForm().getDisplayHint());
          wrapperTable.setTableRowFormDisplayViewId(getScoutObject().getForm().getDisplayViewId());
          wrapperTable.initTable();
        }
        finally {
          wrapperTable.setTableChanging(false);
        }
        holder.setValue(wrapperTable);
      }
    };
    job.schedule();

    try {
      //TODO CGU: this freezes the gui if initializing of the table takes a while. Async table creation possible?
      job.join();
    }
    catch (InterruptedException e) {
      LOG.error("Table wrapping interrupted. ", e);
    }

    return holder.getValue();
  }

  @Override
  protected IRwtScoutActionBar<? extends IPropertyObserver> createRwtScoutActionBar() {
    boolean actionBarVisible = true;
    if (getScoutObject() instanceof IMobileTableField) {
      actionBarVisible = ((IMobileTableField) getScoutObject()).isActionBarVisible();
    }

    if (actionBarVisible) {
      RwtScoutTableActionBar actionBar = new RwtScoutTableActionBar();
      actionBar.createUiField(getUiContainer(), getScoutObject(), getUiEnvironment());
      return actionBar;
    }

    return null;
  }

  @Override
  protected void detachScout() {
    disposeMobileTable();

    super.detachScout();
  }

  private void disposeMobileTable() {
    if (m_mobileTable == null) {
      return;
    }

    Runnable job = new Runnable() {

      @Override
      public void run() {
        if (m_mobileTable == null) {
          return;
        }

        m_mobileTable.dispose();
        m_mobileTable = null;
      }

    };

    getUiEnvironment().invokeScoutLater(job, 0);
  }

  /**
   * Returns true if the table is directly embedded into the field. In other words: Returns true if there is no parent
   * groupbox with a visible border.
   */
  @Override
  protected boolean dontCreateTableContainer() {
    if (getScoutObject() == null) {
      return false;
    }

    return !isAnyBorderVisible(getScoutObject().getParentGroupBox());
  }

  private boolean isAnyBorderVisible(IGroupBox groupBox) {
    while (groupBox != null) {
      if (groupBox.isBorderVisible()) {
        return true;
      }
      groupBox = groupBox.getParentGroupBox();
    }

    return false;
  }

  @Override
  protected IRwtTableStatus createRwtTableStatus() {
    return new RwtMobileTableStatus(getUiContainer(), getUiEnvironment(), getScoutObject());
  }
}
