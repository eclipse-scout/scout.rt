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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Properties;

import org.eclipse.scout.rt.platform.util.StringUtility;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for class {@link PropertiesUtility}.
 */
public class PropertiesUtilityTest {
  private static final String SAMPLE_CONFIG_PROPS = "org/eclipse/scout/rt/platform/config/sample-config.properties";

  private static final String USER_HOME_KEY = "user.home";
  private static final String USER_HOME_VALUE = System.getProperty("user.home");
  private static final String OTHER_PROP_KEY = "otherProp";
  private static final String OTHER_PROP_VALUE = "otherVal";
  private static final String SPECIAL_CHARS_KEY = "specialChars";
  private static final String SPECIAL_CHARS_VALUE = "-$-\\-";
  private static final String RESOLVE_TEST_KEY = "a.resolve.test";
  private static final String RESOLVE_TEST_VALUE = "prefix" + USER_HOME_VALUE + "suffix";
  private static final String ATTR_USER_HOME_TEST_KEY = "aTestKey";
  private static final String ATTR_USER_HOME_TEST_VALUE = USER_HOME_VALUE + "/subfolder";
  private static final String ATTR_STRING_KEY = "stringKey";
  private static final String ATTR_LONG_KEY = "longKey";
  private static final String EMPTY_KEY = "emptyKey";

  private static Properties load(String path) throws Exception {
    try (InputStream inputStream = PropertiesUtility.class.getResourceAsStream("/" + path);
         InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
      Properties props = new Properties();
      props.load(reader);
      PropertiesUtility.resolveVariables(props, true);
      return props;
    }
  }

  @Test
  public void testPropertiesUtility() throws Exception {
    Properties props = load(SAMPLE_CONFIG_PROPS);
    assertEquals(USER_HOME_VALUE, props.getProperty(USER_HOME_KEY));
    assertEquals(OTHER_PROP_VALUE, props.getProperty(OTHER_PROP_KEY));
    assertEquals(SPECIAL_CHARS_VALUE, props.getProperty(SPECIAL_CHARS_KEY));
    assertEquals(RESOLVE_TEST_VALUE, props.getProperty(RESOLVE_TEST_KEY));
    assertEquals(ATTR_USER_HOME_TEST_VALUE, props.getProperty(ATTR_USER_HOME_TEST_KEY));
    assertTrue(Collections.list(props.propertyNames()).contains(EMPTY_KEY));
  }

  @Test
  public void testLoopOnSelf() {
    Properties props = new Properties();
    props.setProperty("prop1", "a${prop1}b");
    try {
      PropertiesUtility.resolveVariables(props, true);
      Assert.fail();
    }
    catch (IllegalArgumentException e) {
      assertEquals("resolving expression 'a${prop1}b': loop detected (the resolved value contains the original expression): a${prop1}b", e.getMessage());
    }
  }

  @Test
  public void testUndefinedPlaceholder() {
    Properties props = new Properties();
    props.setProperty("prop1", "a${prop1}b");
    props.setProperty("prop1", "a${prop2}b");
    props.setProperty("prop2", "a${prop33}b");
    try {
      PropertiesUtility.resolveVariables(props, true);
      Assert.fail();
    }
    catch (IllegalArgumentException e) {
      assertEquals("resolving expression 'a${prop33}b': variable ${prop33} is not defined in the context.", e.getMessage());
    }
  }

  @Test
  public void testLoop() {
    Properties props = new Properties();
    props.setProperty("prop1", "a${prop2}b");
    props.setProperty("prop2", "a${prop3}b");
    props.setProperty("prop3", "a${prop4}b");
    props.setProperty("prop4", "a${prop5}b");
    props.setProperty("prop5", "a${prop4}b");
    try {
      PropertiesUtility.resolveVariables(props, true);
      Assert.fail();
    }
    catch (IllegalArgumentException e) {
      assertTrue(StringUtility.containsString(e.getMessage(), "loop detected"));
    }
  }

  @Test
  public void testSystemResolveProperty() throws Exception {
    final String attrOtherSystemPropertyKey = "attrOtherSystemPropertyKey";
    try {
      System.setProperty(ATTR_STRING_KEY, "property ${" + attrOtherSystemPropertyKey + "}");
      System.setProperty(attrOtherSystemPropertyKey, "resolved");

      Properties props = load(SAMPLE_CONFIG_PROPS);
      PropertiesUtility.resolveVariables(props, true);
      assertEquals("property resolved", props.getProperty(ATTR_STRING_KEY));

      System.setProperty(ATTR_STRING_KEY, "property ${" + ATTR_LONG_KEY + "}");
      System.setProperty(attrOtherSystemPropertyKey, "property " + 777);
    }
    finally {
      System.clearProperty(ATTR_STRING_KEY);
      System.clearProperty(attrOtherSystemPropertyKey);
    }
  }
}
