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
package org.eclipse.scout.rt.client.ui.basic.table.menus;

import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.services.common.clipboard.IClipboardService;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.service.SERVICES;

public class CopyWidthsOfColumnsMenu extends AbstractMenu {
  private final ITable m_table;

  public CopyWidthsOfColumnsMenu(ITable table) {
    m_table = table;
  }

  @Override
  protected String getConfiguredText() {
    return ScoutTexts.get("CopyWidthsOfColumnsMenu");
  }

  /**
   * This menu is only visible in development mode
   */
  @Override
  protected void execPrepareAction() throws ProcessingException {
    setVisible(Platform.inDevelopmentMode());
  }

  /**
   * This menu exports the fully qualified column class names and their widths to the clipboard
   * using the following format for each column:
   * [fully qualified column class name]\t[column width]\n
   */
  @Override
  protected void execAction() {
    try {
      StringBuffer buf = new StringBuffer();
      for (IColumn<?> column : getTable().getColumnSet().getColumns()) {
        buf.append(column.getClass().getName());
        buf.append("\t");
        buf.append(column.getWidth());
        buf.append("\n");
      }

      // calling the service to write the buffer to the clipboard
      IClipboardService svc = SERVICES.getService(IClipboardService.class);
      svc.setTextContents(buf.toString());
    }
    catch (ProcessingException se) {
      se.addContextMessage(getText());
      SERVICES.getService(IExceptionHandlerService.class).handleException(se);
    }
  }

  public ITable getTable() {
    return m_table;
  }
}
