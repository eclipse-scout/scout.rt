/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.Properties;
import java.util.ServiceLoader;

import org.eclipse.scout.rt.platform.ServiceLoaderClassLoaderMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test override of {@link IConfigFileLoader} using {@link ServiceLoader#load(Class)}
 */
public class ConfigFileLoaderTest {
  private ClassLoader m_oldContextLoader;

  @Before
  public void before() {
    m_oldContextLoader = Thread.currentThread().getContextClassLoader();
  }

  @After
  public void after() {
    Thread.currentThread().setContextClassLoader(m_oldContextLoader);
  }

  @Test
  public void testDefault() {
    PropertiesHelper p = new PropertiesHelper(ConfigUtility.CONFIG_FILE_NAME);
    assertFalse(p.getAllEntries().isEmpty());
  }

  @Test
  public void testEmptyOverride() {
    ClassLoader loader = new ServiceLoaderClassLoaderMock(getClass().getClassLoader(), IConfigFileLoader.class, FixtureEmptyConfigFileLoader.class);
    Thread.currentThread().setContextClassLoader(loader);

    PropertiesHelper p = new PropertiesHelper(ConfigUtility.CONFIG_FILE_NAME);
    assertTrue(p.getAllEntries().isEmpty());
  }

  @Test
  public void testSingleProperty() {
    ClassLoader loader = new ServiceLoaderClassLoaderMock(getClass().getClassLoader(), IConfigFileLoader.class, FixtureSinglePropertyConfigFileLoader.class);
    Thread.currentThread().setContextClassLoader(loader);

    PropertiesHelper p = new PropertiesHelper(ConfigUtility.CONFIG_FILE_NAME);
    assertEquals(1, p.getAllEntries().size());
    assertEquals("bar", p.getProperty("foo"));
  }

  public static final class FixtureEmptyConfigFileLoader implements IConfigFileLoader {
    @Override
    public void load(URL source, Properties destination) {
    }
  }

  public static final class FixtureSinglePropertyConfigFileLoader implements IConfigFileLoader {
    @Override
    public void load(URL source, Properties destination) {
      destination.put("foo", "bar");
    }
  }
}
