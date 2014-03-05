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
package org.eclipse.scout.rt.client.ui.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.easymock.EasyMock;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.junit.Test;

/**
 * Tests for {@link ActionUtility}
 * 
 * @since 4.0.0-M6
 */
public class ActionUtilityTest {

  @Test
  public void testCleanupWithEmptyList() {
    List<IMenu> cleanList = ActionUtility.visibleNormalizedActions(Collections.<IMenu> emptyList());
    assertTrue(cleanList.isEmpty());
  }

  @Test
  public void testCleanupWithOnlySeparators() {
    IMenu s1 = createMenu("s1", true, true);
    IMenu s2 = createMenu("s2", true, true);
    IMenu s3 = createMenu("s3", true, true);
    List<IMenu> cleanList = ActionUtility.visibleNormalizedActions(CollectionUtility.arrayList(s1, s2, s3));
    assertTrue(cleanList.isEmpty());
  }

  @Test
  public void testCleanupWithLeadingSeparators() {
    IMenu s1 = createMenu("s1", true, true);
    IMenu s2 = createMenu("s2", true, true);
    IMenu s3 = createMenu("s3", true, true);
    IMenu m4 = createMenu("m4", false, true);
    List<IMenu> cleanList = ActionUtility.visibleNormalizedActions(CollectionUtility.arrayList(s1, s2, s3, m4));
    assertEquals(1, cleanList.size());
    assertEquals("m4", cleanList.get(0).getText());
  }

  @Test
  public void testCleanupWithEndingSeparators() {

    IMenu s1 = createMenu("s1", true, true);
    IMenu s2 = createMenu("s2", true, true);
    IMenu s3 = createMenu("s3", true, true);
    IMenu m4 = createMenu("m4", false, true);
    IMenu s5 = createMenu("s5", true, true);
    IMenu s6 = createMenu("s6", true, true);
    IMenu s7 = createMenu("s7", true, true);
    List<IMenu> cleanList = ActionUtility.visibleNormalizedActions(CollectionUtility.arrayList(s1, s2, s3, m4, s5, s6, s7));
    assertEquals(1, cleanList.size());
    assertEquals("m4", cleanList.get(0).getText());
  }

  @Test
  public void testCleanupWithDoubleSeparators() {
    IMenu s1 = createMenu("s1", true, true);
    IMenu s2 = createMenu("s2", true, true);
    IMenu s3 = createMenu("s3", true, true);
    IMenu m4 = createMenu("m4", false, true);
    IMenu s5 = createMenu("s5", true, true);
    IMenu s6 = createMenu("s6", true, true);
    IMenu s7 = createMenu("s7", true, true);
    IMenu m8 = createMenu("m8", false, true);
    List<IMenu> cleanList = ActionUtility.visibleNormalizedActions(CollectionUtility.arrayList(s1, s2, s3, m4, s5, s6, s7, m8));
    assertEquals(3, cleanList.size());
    assertEquals("m4", cleanList.get(0).getText());
    assertEquals("s5", cleanList.get(1).getText());
    assertEquals("m8", cleanList.get(2).getText());

  }

  private IMenu createMenu(String label, boolean separator, boolean visible) {
    IMenu menu = mock(IMenu.class);
    when(menu.isVisible()).thenReturn(visible);
    when(menu.isSeparator()).thenReturn(separator);
    when(menu.getText()).thenReturn(label);

    return menu;
  }

  private void setChildMenus(IMenu parentMenu, List<IMenu> childMenus) {
    EasyMock.expect(parentMenu.getChildActions()).andReturn(childMenus);
    EasyMock.expect(parentMenu.getChildActionCount()).andReturn(childMenus.size());
  }

}
