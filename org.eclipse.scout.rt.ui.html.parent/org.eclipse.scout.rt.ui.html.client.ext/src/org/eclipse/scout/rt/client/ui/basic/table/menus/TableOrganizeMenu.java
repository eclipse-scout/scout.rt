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

import org.eclipse.scout.commons.annotations.ClassId;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;

@ClassId("b4810839-8bfd-42ba-b295-5df38241bb3c")
public class TableOrganizeMenu extends OrganizeColumnsMenu {

  public TableOrganizeMenu(ITable table) {
    super(table);
  }

  @Override
  protected String getConfiguredIconId() {
    return "\uf013";
  }

  @Override
  protected String getConfiguredText() {
    return null;
  }

  //FIXME implement table customizer features (if table has

  @Override
  protected final void execAction() {
    //FIXME this is a GUI only item. How can we mark that? Or do we not care and execute the action nevertheless?
  }

}
