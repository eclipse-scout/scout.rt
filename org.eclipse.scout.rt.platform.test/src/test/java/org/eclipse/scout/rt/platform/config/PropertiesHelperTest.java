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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.platform.util.IOUtility;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for class {@link PropertiesHelper}.
 */
public class PropertiesHelperTest {

  private static final String SAMPLE_CONFIG_PROPS = "org/eclipse/scout/rt/platform/config/sample-config.properties";
  private static final String HELPER_CONFIG_PROPS = "org/eclipse/scout/rt/platform/config/helper-test.properties";
  private static final String TEST_CONFIG_PROPS = "org/eclipse/scout/rt/platform/config/test-config.properties";
  private static final String MAP_CONFIG_PROPS = "org/eclipse/scout/rt/platform/config/map-test.properties";
  private static final String REC_CONFIG_PROPS = "org/eclipse/scout/rt/platform/config/recursion-test.properties";
  private static final String LOOP_IMPORT_PROPS = "org/eclipse/scout/rt/platform/config/imp1.properties";
  private static final String PLACEHOLDER_IMPORT_PROPS = "org/eclipse/scout/rt/platform/config/placeholder-imp.properties";
  private static final String LIST_PROPS = "org/eclipse/scout/rt/platform/config/list-test.properties";

  private static final String USER_HOME_KEY = "user.home";
  private static final String USER_HOME_VALUE = System.getProperty("user.home");
  private static final String OTHER_PROP_KEY = "otherProp";
  private static final String OTHER_PROP_VALUE = "otherVal";
  private static final String RESOLVE_TEST_KEY = "a.resolve.test";
  private static final String RESOLVE_TEST_VALUE = "prefix" + USER_HOME_VALUE + "suffix";
  private static final String ATTR_USER_HOME_TEST_KEY = "aTestKey";
  private static final String ATTR_USER_HOME_TEST_VALUE = USER_HOME_VALUE + "/subfolder";
  private static final String ATTR_STRING_KEY = "stringKey";
  private static final String ATTR_STRING_VALUE = "stringValue";
  private static final String ATTR_INT_KEY = "intKey";
  private static final String ATTR_LONG_KEY = "longKey";
  private static final String ATTR_FLOAT_KEY = "floatKey";
  private static final String ATTR_DOUBLE_KEY = "doubleKey";
  private static final String ATTR_BOOLEAN_KEY = "booleanKey";
  private static final String NAMESPACE = "ns";
  private static final String NAMESPACE_PROP = "nsProperty";
  private static final String NAMESPACE_PROP_VAL = "nsval";
  private static final String ATTR_LIST_KEY = "listKey";
  private static final String ATTR_ILLEGAL_NUMBER_KEY = "invalidNumberKey";
  private static final String MAP_KEY = "mapKey";
  private static final String EMPTY_KEY = "emptyKey";

  @Test
  public void testPropertiesHelper() throws Exception {
    PropertiesHelper instance = new PropertiesHelper(SAMPLE_CONFIG_PROPS);
    assertEquals(USER_HOME_VALUE, instance.getProperty(USER_HOME_KEY));
    assertEquals(OTHER_PROP_VALUE, instance.getProperty(OTHER_PROP_KEY));
    assertEquals(RESOLVE_TEST_VALUE, instance.getProperty(RESOLVE_TEST_KEY));
    assertEquals(ATTR_USER_HOME_TEST_VALUE, instance.getProperty(ATTR_USER_HOME_TEST_KEY));
    assertTrue(instance.getAllPropertyNames().contains(EMPTY_KEY));
  }

  @Test
  public void testNamespaceProperty() {
    PropertiesHelper instance = new PropertiesHelper(SAMPLE_CONFIG_PROPS);
    assertEquals(NAMESPACE_PROP_VAL, instance.getProperty(NAMESPACE_PROP, null, NAMESPACE));
    assertEquals(null, instance.getProperty(NAMESPACE_PROP));
    assertEquals(null, instance.getProperty(NAMESPACE_PROP + "-not-existing", null, NAMESPACE));
    assertEquals(null, instance.getProperty(NAMESPACE_PROP, null, NAMESPACE + "-not-existing"));
    assertEquals("defaultval", instance.getProperty(NAMESPACE_PROP, "defaultval", NAMESPACE + "-not-existing"));
  }

  @Test
  public void testGetPropertiesFileUrl() throws IOException {
    PropertiesHelper h = new PropertiesHelper((URL) null);
    assertFalse(h.isInitialized());

    PropertiesHelper h2 = new PropertiesHelper("classpath:not-existing.properties");
    assertFalse(h2.isInitialized());

    try {
      new PropertiesHelper(new URL("http://www.whatever-not-existing-scout-domain.org/config.properties"));
      Assert.fail();
    }
    catch (IllegalArgumentException e) {
      assertNotNull(e);
    }

    PropertiesHelper h4 = new PropertiesHelper("classpath:" + TEST_CONFIG_PROPS);
    assertTrue(h4.isInitialized());
    assertEquals(2, h4.getAllPropertyNames().size());

    String key = "myconfig.properties";
    try {
      System.setProperty(key, "classpath:" + TEST_CONFIG_PROPS);
      PropertiesHelper h5 = new PropertiesHelper(key);
      assertTrue(h5.isInitialized());
      assertEquals(2, h5.getAllPropertyNames().size());
    }
    finally {
      System.clearProperty(key);
    }

    assertFalse(new PropertiesHelper((String) null).isInitialized());
    assertFalse(new PropertiesHelper("classpath:").isInitialized());

    try {
      new PropertiesHelper("blubi:test");
      Assert.fail();
    }
    catch (IllegalArgumentException e) {
      assertNotNull(e);
    }
  }

  @Test
  public void testPropertyMap() {
    PropertiesHelper h = new PropertiesHelper(MAP_CONFIG_PROPS);
    String thirdKey = "mapKey[third]";
    String fourthKey = "mapKey[fourth]";
    System.setProperty(fourthKey, "four");
    System.setProperty(thirdKey, "changed");
    try {
      Map<String, String> map1 = h.getPropertyMap(MAP_KEY);
      assertEquals(5, map1.size());
      assertEquals("one", map1.get("first"));
      assertEquals("two", map1.get("second"));
      assertEquals("changed", map1.get("third"));
      assertEquals("four", map1.get("fourth"));
      assertEquals("last", map1.get("last"));

      try {
        h.getPropertyMap(MAP_KEY, "namespace");
        Assert.fail();
      }
      catch (IllegalArgumentException e) {
        Assert.assertNotNull(e);
      }
      assertEquals(Collections.emptyMap(), h.getPropertyMap(null));
      assertEquals(Collections.emptyMap(), h.getPropertyMap(null, "namespace"));

      Map<String, String> defaultValue = new HashMap<>(0);
      Map<String, String> propertyMap = h.getPropertyMap("not-existing-whatever", defaultValue);
      Assert.assertSame(defaultValue, propertyMap);
    }
    finally {
      System.clearProperty(fourthKey);
      System.clearProperty(thirdKey);
    }
  }

  @Test
  public void testPropertyList() {
    PropertiesHelper instance = new PropertiesHelper(SAMPLE_CONFIG_PROPS);
    String key = ATTR_LIST_KEY + "[2]";
    try {
      System.setProperty(key, "3");
      List<String> list = instance.getPropertyList(ATTR_LIST_KEY);
      assertNotNull(list);
      assertEquals(Arrays.asList("1", "2", "3", "4"), list);
    }
    finally {
      System.clearProperty(key);
    }

    assertEquals(Collections.singletonList("1"), instance.getPropertyList(ATTR_INT_KEY));
    assertEquals(Arrays.asList("a", "b"), instance.getPropertyList("not.existing.key", Arrays.asList("a", "b")));

    assertEquals(0, instance.getPropertyList("not-existing").size());
    assertEquals(0, instance.getPropertyList(null).size());
    assertEquals(0, instance.getPropertyList("").size());

    PropertiesHelper listProps = new PropertiesHelper(LIST_PROPS);
    List<String> list = listProps.getPropertyList("listWithValidIndices");
    assertEquals(4, list.size());
    assertEquals(Arrays.asList("a", null, null, "b"), list);
    try {
      listProps.getPropertyList("listKeyWithNegIndex");
      Assert.fail();
    }
    catch (IllegalArgumentException e) {
      Assert.assertNotNull(e);
    }

    try {
      listProps.getPropertyList("listKeyWithNonNum");
      Assert.fail();
    }
    catch (IllegalArgumentException e) {
      Assert.assertNotNull(e);
    }

    try {
      listProps.getPropertyList("listKeyWithMissingIndex");
      Assert.fail();
    }
    catch (IllegalArgumentException e) {
      Assert.assertNotNull(e);
    }
  }

  @Test
  public void testPropertyString() {
    PropertiesHelper instance = new PropertiesHelper(SAMPLE_CONFIG_PROPS);
    assertEquals(ATTR_STRING_VALUE, instance.getProperty(ATTR_STRING_KEY));
    assertEquals(ATTR_STRING_VALUE, instance.getProperty(ATTR_STRING_KEY, "defaultValue"));
    assertEquals(null, instance.getProperty(null));
    assertFalse(instance.hasProperty("not-existing"));
    assertTrue(instance.hasProperty(ATTR_STRING_KEY));

    assertNull(instance.getProperty("unknown"));
    assertEquals("defaultValue", instance.getProperty("unknown", "defaultValue"));
  }

  @Test
  public void testRecursions() {
    PropertiesHelper h = new PropertiesHelper(REC_CONFIG_PROPS);
    assertEquals("aaaaaaaaaaaainnerbbbbbbbbbbbb", h.getProperty("prop1"));
  }

  @Test
  public void testLoopOnSelf() throws MalformedURLException {
    try {
      new PropertiesHelper(new URL("http://www.whatever.org/config.properties")) {
        @Override
        protected void parse(URL propertiesFileUrl) {
          getConfigPropertyMap().put("prop1", "a${prop1}b");
        }
      };
      Assert.fail();
    }
    catch (IllegalArgumentException e) {
      assertEquals("resolving expression 'a${prop1}b': loop detected (the resolved value contains the original expression): a${prop1}b", e.getMessage());
    }
  }

  @Test
  public void testUndefinedPlaceholder() throws MalformedURLException {
    try {
      new PropertiesHelper(new URL("http://www.whatever.org/config.properties")) {
        @Override
        protected void parse(URL propertiesFileUrl) {
          getConfigPropertyMap().put("prop1", "a${prop2}b");
          getConfigPropertyMap().put("prop2", "a${prop33}b");
        }
      };
      Assert.fail();
    }
    catch (IllegalArgumentException e) {
      assertEquals("resolving expression 'a${prop33}b': variable ${prop33} is not defined in the context.", e.getMessage());
    }
  }

  @Test
  public void testLoop() throws MalformedURLException {
    try {
      new PropertiesHelper(new URL("http://www.whatever.org/config.properties")) {
        @Override
        protected void parse(URL propertiesFileUrl) {
          getConfigPropertyMap().put("prop1", "a${prop2}b");
          getConfigPropertyMap().put("prop2", "a${prop3}b");
          getConfigPropertyMap().put("prop3", "a${prop4}b");
          getConfigPropertyMap().put("prop4", "a${prop5}b");
          getConfigPropertyMap().put("prop5", "a${prop4}b");
        }
      };
      Assert.fail();
    }
    catch (IllegalArgumentException e) {
      assertEquals("resolving expression 'a${prop3}b': loop detected: [prop3, prop4, prop5]", e.getMessage());
    }
  }

  @Test
  public void testImportWithPlaceholder() {
    PropertiesHelper h = new PropertiesHelper(PLACEHOLDER_IMPORT_PROPS);
    assertTrue(h.isInitialized());
    assertEquals(4, h.getPropertyMap(MAP_KEY).size());
    try {
      h.getPropertyMap(MAP_KEY, "namespace");
      Assert.fail();
    }
    catch (IllegalArgumentException e) {
      Assert.assertNotNull(e);
    }
  }

  @Test
  public void testImportLoop() {
    PropertiesHelper h = new PropertiesHelper(LOOP_IMPORT_PROPS);
    assertEquals("value1", h.getProperty("key1"));
  }

  @Test
  public void testImport() throws IOException {
    String virtualPropsName = "virtual-test.properties";
    System.setProperty(virtualPropsName, "classpath:" + TEST_CONFIG_PROPS);
    System.setProperty(PropertiesHelper.IMPORT_KEY + "[0]", virtualPropsName);

    Path tmpFile = Files.createTempFile("properties-helper-test", ".properties");
    try {
      Files.write(tmpFile, IOUtility.readBytes(PropertiesHelperTest.class.getClassLoader().getResourceAsStream(SAMPLE_CONFIG_PROPS)));
      System.setProperty(PropertiesHelper.IMPORT_KEY + "[1]", tmpFile.toString());

      PropertiesHelper h6 = new PropertiesHelper(HELPER_CONFIG_PROPS);
      assertEquals("aotherValb", h6.getProperty("keyWithPlaceholderFromImport"));
      assertEquals(19, h6.getAllPropertyNames().size());
      assertEquals(-11, h6.getPropertyLong("longKey", -1));
    }
    finally {
      Files.delete(tmpFile);
      System.clearProperty(virtualPropsName);
      System.clearProperty(PropertiesHelper.IMPORT_KEY + "[0]");
      System.clearProperty(PropertiesHelper.IMPORT_KEY + "[1]");
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNonExistingImport() {
    try {
      System.setProperty("import", "non-existing-file-url");
      new PropertiesHelper(HELPER_CONFIG_PROPS);
    }
    finally {
      System.clearProperty("import");
    }
  }

  @Test
  public void testPropertyInt() {
    PropertiesHelper instance = new PropertiesHelper(SAMPLE_CONFIG_PROPS);
    assertEquals(1, instance.getPropertyInt(ATTR_INT_KEY, 777));
    try {
      instance.getPropertyInt(ATTR_ILLEGAL_NUMBER_KEY, 777);
      Assert.fail();
    }
    catch (IllegalArgumentException e) {
      Assert.assertNotNull(e);
    }
    assertEquals(777, instance.getPropertyInt("unknown", 777));
  }

  @Test
  public void testPropertyLong() {
    PropertiesHelper instance = new PropertiesHelper(SAMPLE_CONFIG_PROPS);
    assertEquals(2L, instance.getPropertyLong(ATTR_LONG_KEY, 777L));
    try {
      instance.getPropertyLong(ATTR_ILLEGAL_NUMBER_KEY, 777L);
      Assert.fail();
    }
    catch (IllegalArgumentException e) {
      Assert.assertNotNull(e);
    }
    assertEquals(777L, instance.getPropertyLong("unknown", 777L));
  }

  @Test
  public void testPropertyFloat() {
    PropertiesHelper instance = new PropertiesHelper(SAMPLE_CONFIG_PROPS);
    assertEquals(3.23f, instance.getPropertyFloat(ATTR_FLOAT_KEY, 777f), 0f);
    try {
      instance.getPropertyFloat(ATTR_ILLEGAL_NUMBER_KEY, 777f);
      Assert.fail();
    }
    catch (IllegalArgumentException e) {
      Assert.assertNotNull(e);
    }
    assertEquals(777f, instance.getPropertyFloat("unknown", 777f), 0f);
  }

  @Test
  public void testPropertyDouble() {
    PropertiesHelper instance = new PropertiesHelper(SAMPLE_CONFIG_PROPS);
    assertEquals(4.01, instance.getPropertyDouble(ATTR_DOUBLE_KEY, 777.0), 0.0);
    try {
      instance.getPropertyDouble(ATTR_ILLEGAL_NUMBER_KEY, 777.0);
      Assert.fail();
    }
    catch (IllegalArgumentException e) {
      Assert.assertNotNull(e);
    }
    assertEquals(777.0, instance.getPropertyDouble("unknown", 777.0), 0.0);
  }

  @Test
  public void testPropertyBoolean() {
    PropertiesHelper instance = new PropertiesHelper(SAMPLE_CONFIG_PROPS);
    assertTrue(instance.getPropertyBoolean(ATTR_BOOLEAN_KEY, true));
    try {
      instance.getPropertyBoolean(ATTR_STRING_KEY, true);
      Assert.fail();
    }
    catch (IllegalArgumentException e) {
      Assert.assertNotNull(e);
    }

    assertTrue(instance.getPropertyBoolean(ATTR_BOOLEAN_KEY, false));
    assertTrue(instance.getPropertyBoolean("unknown", true));
    assertFalse(instance.getPropertyBoolean("unknown", false));
  }

  @Test
  public void testEntries() {
    PropertiesHelper instance = new PropertiesHelper(SAMPLE_CONFIG_PROPS);
    assertEquals(16, instance.getAllPropertyNames().size());
    assertEquals(16, instance.getAllEntries().size());
  }
}
