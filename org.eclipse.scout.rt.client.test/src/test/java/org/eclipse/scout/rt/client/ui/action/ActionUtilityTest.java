/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
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

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link ActionUtility}
 *
 * @since 4.0.0-M6
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
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
    assertEquals(Collections.emptyList(), cleanList);
  }

  @Test
  public void testCleanupWithLeadingSeparators() {
    IMenu s1 = createMenu("s1", true, true);
    IMenu s2 = createMenu("s2", true, true);
    IMenu s3 = createMenu("s3", true, true);
    IMenu m4 = createMenu("m4", false, true);
    List<IMenu> cleanList = ActionUtility.visibleNormalizedActions(CollectionUtility.arrayList(s1, s2, s3, m4));
    assertEquals(Arrays.asList(m4), cleanList);
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
    assertEquals(Arrays.asList(m4), cleanList);
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
    assertEquals(Arrays.asList(m4, s5, m8), cleanList);
  }

  @Test
  public void testDispose() {
    Menu m0 = createMenu("m0");
    Menu m1 = createMenu("m1");
    Menu m2 = createMenu("m2");
    Menu m3 = createMenu("m3");
    Menu m4 = createMenu("m4");
    m1.addChildAction(m2);
    m2.addChildAction(m3);
    m2.addChildAction(m4);
    List<IMenu> rootActions = new LinkedList<>();
    rootActions.add(m0);
    rootActions.add(m1);
    ActionUtility.disposeActions(rootActions);
    assertTrue(m0.isExecDisposeCalled());
    assertTrue(m1.isExecDisposeCalled());
    assertTrue(m2.isExecDisposeCalled());
    assertTrue(m3.isExecDisposeCalled());
    assertTrue(m4.isExecDisposeCalled());
  }

  private Menu createMenu(String label) {
    return new Menu(label);
  }

  private Menu createMenu(String label, boolean separator, boolean visible) {
    return new Menu(label, separator, visible);
  }

  private static class Menu extends AbstractMenu {
    private String m_label;
    private boolean m_separator;
    private boolean m_visible;
    private boolean m_execDisposeCalled;

    public Menu(String label) {
      this(label, false, true);
    }

    public Menu(String label, boolean separator, boolean visible) {
      super(false);
      m_label = label;
      m_separator = separator;
      m_visible = visible;
      callInitializer();
    }

    @Override
    protected boolean getConfiguredVisible() {
      return m_visible;
    }

    @Override
    protected boolean getConfiguredSeparator() {
      return m_separator;
    }

    @Override
    protected String getConfiguredText() {
      return m_label;
    }

    @Override
    protected void execOwnerValueChanged(Object newOwnerValue) {

    }

    @Override
    protected void execDispose() {
      super.execDispose();
      m_execDisposeCalled = true;
    }

    public boolean isExecDisposeCalled() {
      return m_execDisposeCalled;
    }

    @Override
    public String toString() {
      return getText();
    }
  }
}
