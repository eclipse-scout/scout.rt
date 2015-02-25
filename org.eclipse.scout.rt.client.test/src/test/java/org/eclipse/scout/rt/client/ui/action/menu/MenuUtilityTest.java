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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collection;
import java.util.List;

import org.easymock.EasyMock;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
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
    List<IMenu> menus = CollectionUtility.arrayList(menu);
    Collection<IKeyStroke> keyStrokes = MenuUtility.getKeyStrokesFromMenus(menus);
    assertEquals("should return 1 menu", keyStrokes.size(), 1);
    assertEquals("keyStroke should be f11", CollectionUtility.firstElement(keyStrokes).getKeyStroke(), "f1");

    keyStrokes = MenuUtility.getKeyStrokesFromMenus(null);
    assertNotNull(keyStrokes);
    assertEquals("null Paramter should return empty array", keyStrokes.size(), 0);

    menu = EasyMock.createNiceMock(IMenu.class);
    EasyMock.expect(menu.getKeyStroke()).andReturn(" "); //invalid keyStroke definition
    EasyMock.replay(menu);
    menus = CollectionUtility.arrayList(menu);
    keyStrokes = MenuUtility.getKeyStrokesFromMenus(menus);
    assertNotNull(keyStrokes);
    assertEquals("KeyStrokes should be empty, since ' ' is an invalid keyStroke", keyStrokes.size(), 0);
  }

}
