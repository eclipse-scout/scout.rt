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
package org.eclipse.scout.rt.client.mobile.ui.form.outline;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ClientJob;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.mobile.ui.desktop.MobileDesktopUtility;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;

public class MainPageForm extends PageForm implements IMainPageForm {

  public MainPageForm(IPage page, PageFormManager manager, boolean tablePagesAllowed, boolean detailFormVisible) throws ProcessingException {
    super(page, manager, tablePagesAllowed, detailFormVisible);

    setKeepSelection(true);
  }

  public MainPageForm(IPage page, PageFormManager manager) throws ProcessingException {
    this(page, manager, true, false);
  }

  @Override
  public void formAddedNotify() throws ProcessingException {
    super.formAddedNotify();

    ITable pageTable = MobileDesktopUtility.getPageTable(getPage());
    if (pageTable == null) {
      return;
    }

    ITableRow selectedRow = pageTable.getSelectedRow();
    if (selectedRow != null && !isDrillDownPage(MobileDesktopUtility.getPageFor(getPage(), selectedRow))) {
      handleTableRowSelected(selectedRow);
    }
  }

  @Override
  protected void setPageTable(ITable table) throws ProcessingException {
    super.setPageTable(table);

    selectPageTableRowIfNecessary(table);
  }

  private void selectPageTableRowIfNecessary(final ITable pageDetailTable) throws ProcessingException {
    if (pageDetailTable == null || pageDetailTable.getRowCount() == 0) {
      return;
    }

    IPage activePage = getDesktop().getOutline().getActivePage();
    IPage pageToSelect = MobileDesktopUtility.getPageFor(activePage, pageDetailTable.getRow(0));
    if (pageDetailTable.getSelectedRow() == null) {
      if (!isDrillDownPage(pageToSelect)) {
//        FIXME CGU There are some strange behaviours Without client sync job, why?
        ClientSyncJob job = new ClientSyncJob("Auto selecting node", ClientJob.getCurrentSession()) {
          @Override
          protected void runVoid(IProgressMonitor monitor) throws Throwable {
//        FIXME CGU Does not really select the first row (MobileTable?)
            pageDetailTable.selectFirstRow();
          }
        };
        job.schedule();
      }
    }

  }

}
