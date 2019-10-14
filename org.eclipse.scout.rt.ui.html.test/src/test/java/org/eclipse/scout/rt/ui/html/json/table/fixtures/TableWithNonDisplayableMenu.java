/*
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.ui.html.json.table.fixtures;

import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;

@ClassId("fb3f5f32-4278-4d9b-8ddf-65e35ee8ddd7")
public class TableWithNonDisplayableMenu extends AbstractTable {

  @Order(10)
  @ClassId("32202d0e-7362-478f-a4f8-a0375e0274f4")
  public class Col1Column extends AbstractStringColumn {

    @Override
    protected String getConfiguredHeaderText() {
      return "col1";
    }
  }

  @Override
  protected void addHeaderMenus(OrderedCollection<IMenu> menus) {
    // don't add additional menus
  }

  @Order(10)
  @ClassId("0420fe5a-15f3-4522-a3fb-268e2106fb0f")
  public class DisplayableMenu extends AbstractMenu {
  }

  @Order(20)
  @ClassId("1ab1b81e-edcf-47e4-aebb-604e34cde13e")
  public class NonDisplayableMenu extends AbstractMenu {

    @Override
    protected void execInitAction() {
      setVisibleGranted(false);
    }
  }
}
