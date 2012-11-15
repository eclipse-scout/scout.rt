/*******************************************************************************
 * Copyright (c) 2012 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.extension.client.ui.basic.table;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.extension.client.Replace;
import org.junit.Test;

/**
 * @since 3.9.0
 */
public class ExtensibleTableTest {

  @Test
  public void testGetDefaultMenu() {
    P_ExtensibleTableWithDefaultMenu table = new P_ExtensibleTableWithDefaultMenu();
    assertSame(P_ExtensibleTableWithDefaultMenu.DefaultMenu.class, table.getDefaultMenuInternal());
  }

  @Test
  public void testGetReplacedDefaultMenu() {
    P_ExtensibleTableWithReplacedDefaultMenu table = new P_ExtensibleTableWithReplacedDefaultMenu();
    assertSame(P_ExtensibleTableWithDefaultMenu.DefaultMenu.class, table.getConfiguredDefaultMenu());
    assertSame(P_ExtensibleTableWithReplacedDefaultMenu.ExtendedMenu.class, table.getDefaultMenuInternal());
  }

  @Test
  public void testGetReplacedDefaultMenuNotExtendingConfiguredDefaultMenu() {
    P_ExtensibleTableWithReplacedDefaultMenuNotExtending table = new P_ExtensibleTableWithReplacedDefaultMenuNotExtending();
    assertSame(P_ExtensibleTableWithDefaultMenu.DefaultMenu.class, table.getConfiguredDefaultMenu());
    assertNull(table.getDefaultMenuInternal());
  }

  private static class P_ExtensibleTableWithDefaultMenu extends AbstractExtensibleTable {

    @Override
    protected Class<? extends IMenu> getConfiguredDefaultMenu() {
      return DefaultMenu.class;
    }

    @Order(10)
    public class DefaultMenu extends AbstractMenu {

    }
  }

  private static class P_ExtensibleTableWithReplacedDefaultMenu extends P_ExtensibleTableWithDefaultMenu {

    @Order(10)
    @Replace
    public class ExtendedMenu extends DefaultMenu {

    }
  }

  private static class P_ExtensibleTableWithReplacedDefaultMenuNotExtending extends P_ExtensibleTableWithDefaultMenu {

    @Order(10)
    @Replace(P_ExtensibleTableWithDefaultMenu.DefaultMenu.class)
    public class ExtendedMenu extends AbstractMenu {

    }
  }
}
