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
package org.eclipse.scout.rt.ui.swing.window.desktop.tray;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

import java.awt.Image;
import java.awt.PopupMenu;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;

import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for {@link SwingScoutTray}
 */
@Ignore
public class SwingScoutTrayUiTest {
  private static final String TEST_TITLE = "New Application Title";

  private TestSwingScoutTray m_tray;

  @Before
  public void setUp() {
    m_tray = new TestSwingScoutTray();
    m_tray.initializeSwing();
  }

  @Test
  public void testInitialTooltipNull() {
    assertNull("Initial tooltip must be null.", m_tray.getSwingTrayIcon().getToolTip());
  }

  @Test
  public void testSetTooltipTextFromScout() {
    String tooltipText = "New Tooltip Text";
    m_tray.setTooltipFromScout(tooltipText);
    assertEquals("New tooltip not set.", tooltipText, m_tray.getSwingTrayIcon().getToolTip());
  }

  @Test
  public void testHandleScoutPropertyChange_PropertyTitle() {
    m_tray.handleScoutPropertyChange(IDesktop.PROP_TITLE, TEST_TITLE);
    assertEquals("New tooltip not set.", TEST_TITLE, m_tray.getSwingTrayIcon().getToolTip());
  }

  @Test
  public void testHandleScoutPropertyChange_PropertyNotExisting() {
    m_tray.handleScoutPropertyChange("PROP_NOT_EXISTING", TEST_TITLE);
    assertNotEquals("New tooltip set, but it should not.", TEST_TITLE, m_tray.getSwingTrayIcon().getToolTip());
  }

  private static class TestSwingScoutTray extends SwingScoutTray {

    @Override
    protected PopupMenu createPopupMenu() {
      return null;
    }

    @Override
    protected TrayIcon createTrayIcon() {
      Image i = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
      return new TrayIcon(i);
    }
  }
}
