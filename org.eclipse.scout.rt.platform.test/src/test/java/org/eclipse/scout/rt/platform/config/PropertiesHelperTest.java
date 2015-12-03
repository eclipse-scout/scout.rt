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
package org.eclipse.scout.rt.platform.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;

import org.eclipse.scout.rt.platform.config.PropertiesHelper;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for class {@link PropertiesHelper}.
 */
public class PropertiesHelperTest {

  private static final String USER_HOME_KEY = "user.home";
  private static final String USER_HOME_VALUE = System.getProperty(USER_HOME_KEY);

  private static final String OTHER_PROP_KEY = "otherProp";
  private static final String OTHER_PROP_VALUE = "otherVal";

  private static final String RESOLVE_TEST_KEY = "a.resolve.test";
  private static final String RESOLVE_TEST_VALUE = "prefix${user.home}suffix";

  private static final String ATTR_USER_HOME_TEST_KEY = "aTestKey";
  private static final String ATTR_USER_HOME_TEST_VALUE = "@user.home/subfolder";

  private static final String ATTR_STRING_KEY = "stringKey";
  private static final String ATTR_STRING_VALUE = "stringValue";

  private static final String ATTR_INT_KEY = "intKey";
  private static final String ATTR_LONG_KEY = "longKey";
  private static final String ATTR_FLOAT_KEY = "floatKey";
  private static final String ATTR_DOUBLE_KEY = "doubleKey";
  private static final String ATTR_BOOLEAN_KEY = "booleanKey";

  private static final String ATTR_ILLEGAL_NUMBER_KEY = "invalidNumberKey";

  private static PropertiesHelper instance;

  @BeforeClass
  public static void before() throws MalformedURLException {
    URL url = new URL("configproperties", "localhost", 80, "config.properties", getConfigPropertiesContent());
    instance = new PropertiesHelper("config.properties");
    instance.parse(url);
  }

  @Test
  public void testPropertiesHelper() throws Exception {
    instance.resolveAll();

    assertEquals(USER_HOME_VALUE, instance.getProperty(USER_HOME_KEY));
    assertEquals(OTHER_PROP_VALUE, instance.getProperty(OTHER_PROP_KEY));

    assertEquals("prefix" + USER_HOME_VALUE + "suffix", instance.getProperty(RESOLVE_TEST_KEY));
    assertEquals(USER_HOME_VALUE + "/subfolder", instance.getProperty(ATTR_USER_HOME_TEST_KEY));
  }

  @Test
  public void testPropertyString() {
    assertEquals(ATTR_STRING_VALUE, instance.getProperty(ATTR_STRING_KEY));
    assertEquals(ATTR_STRING_VALUE, instance.getProperty(ATTR_STRING_KEY, "defaultValue"));

    assertNull(instance.getProperty("unknown"));
    assertEquals("defaultValue", instance.getProperty("unknown", "defaultValue"));
  }

  @Test
  public void testPropertyInt() {
    assertEquals(1, instance.getPropertyInt(ATTR_INT_KEY, 777));
    assertEquals(777, instance.getPropertyInt(ATTR_ILLEGAL_NUMBER_KEY, 777));
    assertEquals(777, instance.getPropertyInt("unknown", 777));
  }

  @Test
  public void testPropertyLong() {
    assertEquals(2L, instance.getPropertyLong(ATTR_LONG_KEY, 777L));
    assertEquals(777L, instance.getPropertyLong(ATTR_ILLEGAL_NUMBER_KEY, 777L));
    assertEquals(777L, instance.getPropertyLong("unknown", 777L));
  }

  @Test
  public void testPropertyFloat() {
    assertEquals(3f, instance.getPropertyFloat(ATTR_FLOAT_KEY, 777f), 0f);
    assertEquals(777f, instance.getPropertyFloat(ATTR_ILLEGAL_NUMBER_KEY, 777f), 0f);
    assertEquals(777f, instance.getPropertyFloat("unknown", 777f), 0f);
  }

  @Test
  public void testPropertyDouble() {
    assertEquals(4.0, instance.getPropertyDouble(ATTR_DOUBLE_KEY, 777.0), 0.0);
    assertEquals(777.0, instance.getPropertyDouble(ATTR_ILLEGAL_NUMBER_KEY, 777.0), 0.0);
    assertEquals(777.0, instance.getPropertyDouble("unknown", 777.0), 0.0);
  }

  @Test
  public void testPropertyBoolean() {
    assertTrue(instance.getPropertyBoolean(ATTR_BOOLEAN_KEY, true));
    assertTrue(instance.getPropertyBoolean(ATTR_STRING_KEY, true));
    assertTrue(instance.getPropertyBoolean("unknown", true));

    assertTrue(instance.getPropertyBoolean(ATTR_BOOLEAN_KEY, false));
    assertFalse(instance.getPropertyBoolean(ATTR_STRING_KEY, false));
    assertFalse(instance.getPropertyBoolean("unknown", false));
  }

  @Test
  public void testEntries() {
    assertEquals(11, instance.getAllPropertyNames().size());
    assertEquals(11, instance.getAllEntries().size());
  }

  private static URLStreamHandler getConfigPropertiesContent() {
    String[][] input = new String[][]{
        {USER_HOME_KEY, USER_HOME_VALUE},
        {OTHER_PROP_KEY, OTHER_PROP_VALUE},
        {RESOLVE_TEST_KEY, RESOLVE_TEST_VALUE},
        {ATTR_USER_HOME_TEST_KEY, ATTR_USER_HOME_TEST_VALUE},
        {ATTR_STRING_KEY, ATTR_STRING_VALUE},
        {ATTR_INT_KEY, "1"},
        {ATTR_LONG_KEY, "2"},
        {ATTR_FLOAT_KEY, "3"},
        {ATTR_DOUBLE_KEY, "4.0"},
        {ATTR_BOOLEAN_KEY, "TRUE"},
        {ATTR_ILLEGAL_NUMBER_KEY, "invalid"}
    };
    StringBuilder sb = new StringBuilder();
    for (String[] line : input) {
      sb.append(escape(line[0]));
      sb.append("=");
      sb.append(escape(line[1]));
      sb.append('\n');
    }

    return new P_ConfigPropertiesTestHandler(sb.toString());
  }

  private static String escape(String input) {
    return input.replace(":", "\\:").replace("=", "\\=");
  }

  private static final class P_ConfigPropertiesTestHandler extends URLStreamHandler {

    private final String m_configPropertiesContent;

    private P_ConfigPropertiesTestHandler(String configPropertiesContent) {
      m_configPropertiesContent = configPropertiesContent;
    }

    @Override
    protected URLConnection openConnection(URL u) throws IOException {
      return new URLConnection(u) {

        @Override
        public InputStream getInputStream() throws IOException {
          return new ByteArrayInputStream(m_configPropertiesContent.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public void connect() throws IOException {
        }
      };
    }
  }
}
