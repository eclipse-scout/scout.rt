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
package org.eclipse.scout.rt.extension.client.ui.action.menu.internal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.extension.client.ui.action.menu.IMenuExtensionFilter;
import org.junit.Test;

/**
 * Test cases for {@link AbstractMenuExtension}.
 * <p/>
 * <b>Note</b>: This class must not be called <em>Abstract</em>MenuExtensionTest because the JUnit test case browser
 * ignores classes starting with <em>Abstract</em>.
 * 
 * @since 3.9.0
 */
public class MenuExtensionTest {

  @Test
  public void testAcceptWithoutFilter() {
    P_MenuExtension menuExtension = new P_MenuExtension(P_Menu.class, null);
    assertFalse(menuExtension.accept(null, null, null));
    assertFalse(menuExtension.accept(new Object(), null, null));
    assertFalse(menuExtension.accept(null, new Object(), null));
    assertTrue(menuExtension.accept(new Object(), new Object(), null));
  }

  @Test
  public void testAcceptWithAcceptFilter() {
    P_MenuExtension menuExtension = new P_MenuExtension(P_Menu.class, new P_AcceptingMenuExtensionFilter());
    assertTrue(menuExtension.accept(new Object(), new Object(), null));
  }

  @Test
  public void testAcceptWithRejectFilter() {
    P_MenuExtension menuExtension = new P_MenuExtension(P_Menu.class, new P_RejectingMenuExtensionFilter());
    assertFalse(menuExtension.accept(new Object(), new Object(), null));
  }

  private static class P_MenuExtension extends AbstractMenuExtension {
    public P_MenuExtension(Class<? extends IMenu> menuClass, IMenuExtensionFilter filter) {
      super(menuClass, filter);
    }
  }

  private static class P_AcceptingMenuExtensionFilter implements IMenuExtensionFilter {
    @Override
    public boolean accept(Object anchor, Object container, IMenu menu) {
      return true;
    }
  }

  private static class P_RejectingMenuExtensionFilter implements IMenuExtensionFilter {
    @Override
    public boolean accept(Object anchor, Object container, IMenu menu) {
      return false;
    }
  }

  private static class P_Menu extends AbstractMenu {
  }
}
