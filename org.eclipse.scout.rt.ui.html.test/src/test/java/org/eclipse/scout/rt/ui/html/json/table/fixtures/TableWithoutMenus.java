/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.table.fixtures;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.annotations.OrderedCollection;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;

public class TableWithoutMenus extends AbstractTable {

  @Order(10)
  public class Col1Column extends AbstractStringColumn {

    @Override
    protected String getConfiguredHeaderText() {
      return "col1";
    }
  }

  @Override
  protected void addHeaderMenus(OrderedCollection<IMenu> menus) {
    // don't add menus
  }
}
