/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.config;

import static org.junit.Assert.*;

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
    PropertiesHelper p = new PropertiesHelper(new ConfigPropertyProvider(ConfigUtility.DEFAULT_PATH_TO_CONFIG_FILE));
    assertFalse(p.getAllEntries().isEmpty());
  }

  @Test
  public void testEmptyOverride() {
    ClassLoader loader = new ServiceLoaderClassLoaderMock(getClass().getClassLoader(), IConfigFileLoader.class, FixtureEmptyConfigFileLoader.class);
    Thread.currentThread().setContextClassLoader(loader);

    PropertiesHelper p = new PropertiesHelper(new ConfigPropertyProvider(ConfigUtility.DEFAULT_PATH_TO_CONFIG_FILE));
    assertTrue(p.getAllEntries().isEmpty());
  }

  @Test
  public void testSingleProperty() {
    ClassLoader loader = new ServiceLoaderClassLoaderMock(getClass().getClassLoader(), IConfigFileLoader.class, FixtureSinglePropertyConfigFileLoader.class);
    Thread.currentThread().setContextClassLoader(loader);

    PropertiesHelper p = new PropertiesHelper(new ConfigPropertyProvider(ConfigUtility.DEFAULT_PATH_TO_CONFIG_FILE));
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
