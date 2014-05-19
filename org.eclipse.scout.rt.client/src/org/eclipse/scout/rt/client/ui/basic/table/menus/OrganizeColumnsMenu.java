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

import java.util.EnumSet;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractTableMenu;
import org.eclipse.scout.rt.client.ui.action.menu.ITableMenu;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.OrganizeColumnsForm;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.service.SERVICES;

public class OrganizeColumnsMenu extends AbstractTableMenu {
  private final ITable m_table;

  public OrganizeColumnsMenu(ITable table) {
    m_table = table;
  }

  @Override
  protected String getConfiguredText() {
    return ScoutTexts.get("OrganizeTableColumnsMenu");
  }

  @Override
  protected EnumSet<TableMenuType> getConfiguredMenuType() {
    return EnumSet.of(ITableMenu.TableMenuType.Header);
  }

  @Override
  protected void execAction() {
    try {
      OrganizeColumnsForm dlg = new OrganizeColumnsForm(m_table);
      dlg.startModify();
      dlg.waitFor();
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
