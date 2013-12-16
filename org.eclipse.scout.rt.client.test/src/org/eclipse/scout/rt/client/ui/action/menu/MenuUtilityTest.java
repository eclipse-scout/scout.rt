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
package org.eclipse.scout.rt.client.ui.action.menu;

import org.easymock.EasyMock;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link MenuUtility}
 * 
 * @since 3.10.0-M4
 */
public class MenuUtilityTest {
  @Test
  public void testGetKeyStrokesFromMenus() {
    IMenu menu = EasyMock.createNiceMock(IMenu.class);
    EasyMock.expect(menu.getKeyStroke()).andReturn("f1");
    EasyMock.replay(menu);
    IMenu[] menus = new IMenu[]{menu};
    IKeyStroke[] keyStrokes = MenuUtility.getKeyStrokesFromMenus(menus);
    Assert.assertEquals("should return 1 menu", keyStrokes.length, 1);
    Assert.assertEquals("keyStroke should be f11", keyStrokes[0].getKeyStroke(), "f1");

    keyStrokes = MenuUtility.getKeyStrokesFromMenus(null);
    Assert.assertNotNull(keyStrokes);
    Assert.assertEquals("null Paramter should return empty array", keyStrokes.length, 0);

    menu = EasyMock.createNiceMock(IMenu.class);
    EasyMock.expect(menu.getKeyStroke()).andReturn(" "); //invalid keyStroke definition
    EasyMock.replay(menu);
    menus = new IMenu[]{menu};
    keyStrokes = MenuUtility.getKeyStrokesFromMenus(menus);
    Assert.assertNotNull(keyStrokes);
    Assert.assertEquals("KeyStrokes should be empty, since ' ' is an invalid keyStroke", keyStrokes.length, 0);
  }
}
