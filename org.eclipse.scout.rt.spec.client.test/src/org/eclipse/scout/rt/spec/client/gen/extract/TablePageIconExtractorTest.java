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
package org.eclipse.scout.rt.spec.client.gen.extract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.FileUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.AbstractClientSession;
import org.eclipse.scout.rt.client.ClientJob;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.services.common.icon.IconSpec;
import org.eclipse.scout.rt.client.ui.IIconLocator;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithTable;
import org.eclipse.scout.rt.spec.client.config.ConfigRegistry;
import org.eclipse.scout.rt.spec.client.config.SpecFileConfig;
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test for {@link TablePageIconExtractor}
 */
@RunWith(ScoutClientTestRunner.class)
public class TablePageIconExtractorTest {

  private static final String TEST_ICON_NAME = "testIcon";
  private static final String TEST_ICON_FILENAME = "testIcon.png";
  private IClientSession m_clientSession;
  private IIconLocator m_originalIconLocator;
  private Field m_iconLocatorField;
  private static final byte[] ICON_BYTES = "DUMMY_ICON_BYTES".getBytes();
  private Object m_originalSpecFileConfigBundle;
  private Field m_specFileConfigBundleField;

  @Before
  public void before() throws InterruptedException, SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
    m_specFileConfigBundleField = SpecFileConfig.class.getDeclaredField("m_bundle");
    m_specFileConfigBundleField.setAccessible(true);
    m_originalSpecFileConfigBundle = m_specFileConfigBundleField.get(ConfigRegistry.getSpecFileConfigInstance());
    m_specFileConfigBundleField.set(ConfigRegistry.getSpecFileConfigInstance(), Platform.getBundle("org.eclipse.scout.rt.spec.client.test"));
    // setup icon locator
    m_clientSession = ClientJob.getCurrentSession();
    m_originalIconLocator = m_clientSession.getIconLocator();
    m_iconLocatorField = AbstractClientSession.class.getDeclaredField("m_iconLocator");
    m_iconLocatorField.setAccessible(true);
    m_iconLocatorField.set(m_clientSession, new P_IconLocator());
  }

  @After
  public void after() throws IllegalArgumentException, IllegalAccessException {
    m_specFileConfigBundleField.set(ConfigRegistry.getSpecFileConfigInstance(), m_originalSpecFileConfigBundle);
    m_iconLocatorField.set(m_clientSession, m_originalIconLocator);
  }

  @Test
  public void testGetText() throws ProcessingException, IOException {
    AbstractPageWithTable<ITable> page = createTestPage(TEST_ICON_NAME);
    String extractedText = new TablePageIconExtractor().getText(page);
    assertTrue(extractedText.startsWith("[["));
    assertTrue(extractedText.endsWith("]]"));

    File iconsDir = new File(ConfigRegistry.getSpecFileConfigInstance().getImageDir(), "icons");
    byte[] copiedIconContent = FileUtility.readFile(new File(iconsDir, TEST_ICON_FILENAME));
    Assert.assertArrayEquals(ICON_BYTES, copiedIconContent);
  }

  @Test
  public void testGetTextNoIcon() throws ProcessingException, IOException {
    AbstractPageWithTable<ITable> page = createTestPage(null);
    assertEquals("", new TablePageIconExtractor().getText(page));
  }

  @Test
  public void testGetTextIconNotFound() throws ProcessingException, IOException {
    AbstractPageWithTable<ITable> page = createTestPage("INVALID_ICON_NAME");
    assertEquals("", new TablePageIconExtractor().getText(page));
  }

  private AbstractPageWithTable<ITable> createTestPage(String iconName) {
    AbstractPageWithTable<ITable> page = new AbstractPageWithTable<ITable>() {
    };
    page.getCellForUpdate().setIconId(iconName);
    return page;
  }

  private static class P_IconLocator implements IIconLocator {

    @Override
    public IconSpec getIconSpec(String iconName) {
      if (iconName == TEST_ICON_NAME) {
        return new IconSpec(TEST_ICON_FILENAME, ICON_BYTES);
      }
      return null;
    }

  }

}
