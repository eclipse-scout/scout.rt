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

import java.util.Set;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuType;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.OrganizeColumnsForm;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.shared.ScoutTexts;

public class OrganizeColumnsMenu extends AbstractMenu {
  private final ITable m_table;

  public OrganizeColumnsMenu(ITable table) {
    m_table = table;
  }

  @Override
  protected String getConfiguredText() {
    return ScoutTexts.get("OrganizeTableColumnsMenu");
  }

  @Override
  protected boolean getConfiguredInheritAccessibility() {
    return false;
  }

  @Override
  protected Set<IMenuType> getConfiguredMenuTypes() {
    return CollectionUtility.<IMenuType> hashSet(TableMenuType.Header);
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
      BEANS.get(ExceptionHandler.class).handle(se);
    }
  }

  public ITable getTable() {
    return m_table;
  }

}
