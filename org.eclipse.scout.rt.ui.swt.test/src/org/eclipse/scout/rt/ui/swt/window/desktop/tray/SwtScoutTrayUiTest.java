/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swt.window.desktop.tray;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link SwtScoutTray}
 */
public class SwtScoutTrayUiTest {
  private static final String TEST_TITLE = "New Application Title";

  private TestSwtScoutTray m_tray;

  @Before
  public void setUp() {
    m_tray = new TestSwtScoutTray();
    m_tray.initializeSwt(null);
  }

  @After
  public void tearDown() {
    m_tray.getSwtTrayItem().dispose();
  }

  @Test
  public void testInitialTooltipNull() {
    assertNull("Initial tooltip must be null.", m_tray.getSwtTrayItem().getToolTipText());
  }

  @Test
  public void testSetTooltipTextFromScout() {
    String tooltipText = "New Tooltip Text";
    m_tray.setTooltipFromScout(tooltipText);
    assertEquals("New tooltip not set.", tooltipText, m_tray.getSwtTrayItem().getToolTipText());
  }

  @Test
  public void testHandleScoutPropertyChange_PropertyTitle() {
    m_tray.handleScoutPropertyChange(IDesktop.PROP_TITLE, TEST_TITLE);
    assertEquals("New tooltip not set.", TEST_TITLE, m_tray.getSwtTrayItem().getToolTipText());
  }

  @Test
  public void testHandleScoutPropertyChange_PropertyNotExisting() {
    m_tray.handleScoutPropertyChange("PROP_NOT_EXISTING", TEST_TITLE);
    assertNotEquals("New tooltip set, but it should not.", TEST_TITLE, m_tray.getSwtTrayItem().getToolTipText());
  }

  private static class TestSwtScoutTray extends SwtScoutTray {

    @Override
    protected Menu createPopupMenu() {
      return null;
    }

    @Override
    protected TrayItem createTrayItem() {
      Tray systemTray = Display.getDefault().getSystemTray();
      return new TrayItem(systemTray, SWT.NONE);
    }
  }
}
