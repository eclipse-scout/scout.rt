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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.ImmutablePair;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

/**
 * Tests for class {@link PropertiesHelper}.
 */
@RunWith(PlatformTestRunner.class)
public class PropertiesHelperTest {

  private static final String SAMPLE_CONFIG_PROPS = "classpath:org/eclipse/scout/rt/platform/config/sample-config.properties";
  private static final String HELPER_CONFIG_PROPS = "classpath:org/eclipse/scout/rt/platform/config/helper-test.properties";
  private static final String TEST_CONFIG_PROPS = "classpath:org/eclipse/scout/rt/platform/config/test-config.properties";
  private static final String MAP_CONFIG_PROPS = "classpath:org/eclipse/scout/rt/platform/config/map-test.properties";
  private static final String LOOP_IMPORT_PROPS = "classpath:org/eclipse/scout/rt/platform/config/imp1.properties";
  private static final String PLACEHOLDER_IMPORT_PROPS = "classpath:org/eclipse/scout/rt/platform/config/placeholder-imp.properties";
  private static final String LIST_PROPS = "classpath:org/eclipse/scout/rt/platform/config/list-test.properties";
  private static final String DOTPROPERTY_PROPS = "classpath:org/eclipse/scout/rt/platform/config/dotproperty-test.properties";

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

  private static final List<IBean<?>> s_beans = new ArrayList<>();

  @BeforeClass
  public static void beforeClass() throws Exception {
    s_beans.addAll(BEANS.get(BeanTestingHelper.class).registerBeans(new BeanMetaData(TestJsonPropertyReader.class)));
  }

  @Before
  public void before() {
    BEANS.get(TestJsonPropertyReader.class).clear();
  }

  @AfterClass
  public static void afterClass() throws Exception {
    BEANS.get(BeanTestingHelper.class).unregisterBeans(s_beans);
  }

  @Test
  public void testPropertiesHelper() {
    PropertiesHelper instance = new PropertiesHelper(new ConfigPropertyProvider(SAMPLE_CONFIG_PROPS));
    assertEquals(USER_HOME_VALUE, instance.getProperty(USER_HOME_KEY));
    assertEquals(OTHER_PROP_VALUE, instance.getProperty(OTHER_PROP_KEY));
    assertEquals(SPECIAL_CHARS_VALUE, instance.getProperty(SPECIAL_CHARS_KEY));
    assertEquals(RESOLVE_TEST_VALUE, instance.getProperty(RESOLVE_TEST_KEY));
    assertEquals(ATTR_USER_HOME_TEST_VALUE, instance.getProperty(ATTR_USER_HOME_TEST_KEY));
    assertTrue(instance.getAllPropertyNames().contains(EMPTY_KEY));
  }

  @Test
  public void testNamespaceProperty() {
    PropertiesHelper instance = new PropertiesHelper(new ConfigPropertyProvider(SAMPLE_CONFIG_PROPS));
    assertEquals(NAMESPACE_PROP_VAL, instance.getProperty(NAMESPACE_PROP, null, NAMESPACE));
    assertEquals(null, instance.getProperty(NAMESPACE_PROP));
    assertEquals(null, instance.getProperty(NAMESPACE_PROP + "-not-existing", null, NAMESPACE));
    assertEquals(null, instance.getProperty(NAMESPACE_PROP, null, NAMESPACE + "-not-existing"));
    assertEquals("defaultval", instance.getProperty(NAMESPACE_PROP, "defaultval", NAMESPACE + "-not-existing"));

    PropertiesHelper spiedInstance = spy(instance);
    when(spiedInstance.getEnvironmentVariable(NAMESPACE + "__" + NAMESPACE_PROP)).thenReturn(NAMESPACE_PROP_VAL + "-from-env");
    assertThat(spiedInstance.getProperty(NAMESPACE_PROP, null, NAMESPACE), is(NAMESPACE_PROP_VAL + "-from-env"));
  }

  @Test
  public void testGetPropertiesFileUrl() {
    PropertiesHelper h = new PropertiesHelper(null);
    assertFalse(h.hasProviderProperties());

    PropertiesHelper h2 = new PropertiesHelper(new ConfigPropertyProvider("classpath:not-existing.properties"));
    assertFalse(h2.hasProviderProperties());

    try {
      new PropertiesHelper(new ConfigPropertyProvider("http://www.whatever-not-existing-scout-domain.org/config.properties"));
      Assert.fail();
    }
    catch (IllegalArgumentException e) {
      assertNotNull(e);
    }

    PropertiesHelper file4 = new PropertiesHelper(new ConfigPropertyProvider(TEST_CONFIG_PROPS));
    assertTrue(file4.hasProviderProperties());
    assertEquals(2, file4.getAllPropertyNames().size());

    String key = "myconfig.properties";
    try {
      System.setProperty(key, TEST_CONFIG_PROPS);
      PropertiesHelper file5 = new PropertiesHelper(new ConfigPropertyProvider(key, null));
      assertTrue(file5.hasProviderProperties());
      assertEquals(2, file5.getAllPropertyNames().size());
    }
    finally {
      System.clearProperty(key);
    }

    PropertiesHelper file6 = new PropertiesHelper(null);
    assertFalse(file6.hasProviderProperties());

    PropertiesHelper file7 = new PropertiesHelper(new ConfigPropertyProvider("classpath:"));
    assertFalse(file7.hasProviderProperties());

    try {
      new PropertiesHelper(new ConfigPropertyProvider("blubi:test"));
      Assert.fail();
    }
    catch (IllegalArgumentException e) {
      assertNotNull(e);
    }
  }

  @Test
  public void testPropertyMap() {
    PropertiesHelper h = new PropertiesHelper(new ConfigPropertyProvider(MAP_CONFIG_PROPS));
    String thirdKey = "mapKey[third]";
    String fourthKey = "mapKey[fourth]";
    System.setProperty(fourthKey, "four");
    System.setProperty(thirdKey, "changed");
    try {
      Map<String, String> map1 = h.getPropertyMap(MAP_KEY);
      assertEquals(6, map1.size());
      assertEquals("one", map1.get("first"));
      assertEquals("two", map1.get("second"));
      assertEquals("changed", map1.get("third"));
      assertEquals("four", map1.get("fourth"));
      assertNull(map1.get("empty"));
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
    PropertiesHelper instance = new PropertiesHelper(new ConfigPropertyProvider(SAMPLE_CONFIG_PROPS));
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

    PropertiesHelper listProps = new PropertiesHelper(new ConfigPropertyProvider(LIST_PROPS));
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
  public void testReadPropertyMapFromEnvironment() {
    PropertiesHelper originalInstance = new PropertiesHelper(new ConfigPropertyProvider(SAMPLE_CONFIG_PROPS));
    PropertiesHelper spiedInstance = spy(originalInstance);

    String mapNotInFilePropertyValue = "{\"keya\": \"valuea\",\"keyb\": \"valueb\",\"keyc\": \"valuec\"}";

    BEANS.get(TestJsonPropertyReader.class).putMapping(mapNotInFilePropertyValue, CollectionUtility.hashMap(
        new ImmutablePair<>("keya", "valuea"),
        new ImmutablePair<>("keyb", "valueb"),
        new ImmutablePair<>("keyc", "valuec")));

    when(spiedInstance.getEnvironmentVariable("map_not_in_file")).thenReturn(mapNotInFilePropertyValue);

    assertThat(spiedInstance.getPropertyMap("map.not.in.file"), is(CollectionUtility.hashMap(
        new ImmutablePair<>("keya", "valuea"),
        new ImmutablePair<>("keyb", "valueb"),
        new ImmutablePair<>("keyc", "valuec"))));
  }

  @Test
  public void testReadPropertyMapFromEnvironmentWithVariableReference() {
    PropertiesHelper originalInstance = new PropertiesHelper(new ConfigPropertyProvider(SAMPLE_CONFIG_PROPS), new SimpleConfigPropertyProvider("envFile")
        .withProperty("envFileKey", "value-from-envfile"));
    PropertiesHelper spiedInstance = spy(originalInstance);
    try {
      System.setProperty("sysProp", "sysPropVal");
      System.setProperty("stringKey", "stringKeyValueFromSystemProperty");

      String mapNotInFilePropertyValue = "{" +
          "\"propFromConfigProperties\": \"${otherProp}\"," +
          "\"propFromSystemProperties\": \"${sysProp}\"," +
          "\"propFromEnvironment\": \"${envProp}\"," +
          "\"propFromConfigPropertiesOverriddenByEnv\": \"${intKey}\"," +
          "\"propFromConfigPropertiesOverriddenBySystemProperty\": \"${stringKey}\"," +
          "\"propFromConfigPropertiesInString\": \"test${longKey}testtest\"," +
          "\"propFromEnvFileInString\": \"test${envFileKey}testtest\"}";

      BEANS.get(TestJsonPropertyReader.class).putMapping(mapNotInFilePropertyValue, CollectionUtility.hashMap(
          new ImmutablePair<>("propFromConfigProperties", "${otherProp}"),
          new ImmutablePair<>("propFromSystemProperties", "${sysProp}"),
          new ImmutablePair<>("propFromEnvironment", "${envProp}"),
          new ImmutablePair<>("propFromConfigPropertiesOverriddenByEnv", "${intKey}"),
          new ImmutablePair<>("propFromConfigPropertiesOverriddenBySystemProperty", "${stringKey}"),
          new ImmutablePair<>("propFromConfigPropertiesInString", "test${longKey}testtest"),
          new ImmutablePair<>("propFromEnvFileInString", "test${envFileKey}testtest")));

      when(spiedInstance.getEnvironmentVariable("envProp")).thenReturn("envPropVal");
      when(spiedInstance.getEnvironmentVariable("intKey")).thenReturn("intKeyFromEnv");
      when(spiedInstance.getEnvironmentVariable("map_not_in_file")).thenReturn(mapNotInFilePropertyValue);

      assertThat(spiedInstance.getPropertyMap("map.not.in.file"), is(CollectionUtility.hashMap(
          new ImmutablePair<>("propFromConfigProperties", "otherVal"),
          new ImmutablePair<>("propFromSystemProperties", "sysPropVal"),
          new ImmutablePair<>("propFromEnvironment", "envPropVal"),
          new ImmutablePair<>("propFromConfigPropertiesOverriddenByEnv", "intKeyFromEnv"),
          new ImmutablePair<>("propFromConfigPropertiesOverriddenBySystemProperty", "stringKeyValueFromSystemProperty"),
          new ImmutablePair<>("propFromConfigPropertiesInString", "test2testtest"),
          new ImmutablePair<>("propFromEnvFileInString", "testvalue-from-envfiletesttest"))));
    }
    finally {
      System.clearProperty("sysProp");
      System.clearProperty("stringKey");
    }
  }

  @Test
  public void testReadPropertyMapFromEnvironmentMixingWithOtherPropertyMapSources() {
    PropertiesHelper originalInstance = new PropertiesHelper(new ConfigPropertyProvider(MAP_CONFIG_PROPS));
    PropertiesHelper spiedInstance = spy(originalInstance);

    String mapKeyPropertyValue = "{\"second\": \"zwei\", \"third\": null}";

    BEANS.get(TestJsonPropertyReader.class).putMapping(mapKeyPropertyValue, CollectionUtility.hashMap(
        new ImmutablePair<>("second", "zwei"),
        new ImmutablePair<>("third", null)));

    when(spiedInstance.getEnvironmentVariable("mapKey")).thenReturn(mapKeyPropertyValue);

    assertThat(spiedInstance.getPropertyMap("mapKey"), is(CollectionUtility.hashMap(
        new ImmutablePair<>("first", "one"),
        new ImmutablePair<>("second", "zwei"),
        new ImmutablePair<>("empty", null),
        new ImmutablePair<>("last", "last"))));
  }

  @Test
  public void testReadPropertyMapRespectingPrecedence() {
    PropertiesHelper originalInstance = new PropertiesHelper(new ConfigPropertyProvider(MAP_CONFIG_PROPS), new SimpleConfigPropertyProvider("envFile")
        .withProperty("mapKey[third]", "third-from-env-file")
        .withProperty("mapKey[empty]", "really-nothing-from-env-file"));
    PropertiesHelper spiedInstance = spy(originalInstance);
    try {
      System.setProperty("mapKey[first]", "one-from-system-property");

      String mapKeyPropertyValue = "{\"first\": \"one-from-env\", \"second\": \"two-from-env\", \"empty\": \"really-empty-from-env\"}";

      BEANS.get(TestJsonPropertyReader.class).putMapping(mapKeyPropertyValue, CollectionUtility.hashMap(
          new ImmutablePair<>("first", "one-from-env"),
          new ImmutablePair<>("second", "two-from-env")));

      when(spiedInstance.getEnvironmentVariable("mapKey")).thenReturn(mapKeyPropertyValue);

      assertThat(spiedInstance.getPropertyMap("mapKey"), is(CollectionUtility.hashMap(
          new ImmutablePair<>("first", "one-from-system-property"),
          new ImmutablePair<>("second", "two-from-env"),
          new ImmutablePair<>("third", "third-from-env-file"),
          new ImmutablePair<>("empty", "really-nothing-from-env-file"),
          new ImmutablePair<>("last", "last"))));
    }
    finally {
      System.clearProperty("mapKey[first]");
    }
  }

  @Test
  public void testReadPropertyMapFromEnvironmentWithNamespace() {
    PropertiesHelper originalInstance = new PropertiesHelper(new ConfigPropertyProvider(MAP_CONFIG_PROPS));
    PropertiesHelper spiedInstance = spy(originalInstance);

    String mapKeyPropertyValue = "{\"second\": \"zwei\", \"third\": null}";
    String namespace1MapKeyPropertyValue = "{\"a\": null, \"b\": \"b\", \"c\": \"ce\"}";
    String namespace2MapKeyPropertyValue = "{\"d\": \"de\", \"e\": \"ee\"}";

    BEANS.get(TestJsonPropertyReader.class).putMapping(mapKeyPropertyValue, CollectionUtility.hashMap(
        new ImmutablePair<>("second", "zwei"),
        new ImmutablePair<>("third", null)));

    BEANS.get(TestJsonPropertyReader.class).putMapping(namespace1MapKeyPropertyValue, CollectionUtility.hashMap(
        new ImmutablePair<>("a", null),
        new ImmutablePair<>("b", "b"),
        new ImmutablePair<>("c", "ce")));

    BEANS.get(TestJsonPropertyReader.class).putMapping(namespace2MapKeyPropertyValue, CollectionUtility.hashMap(
        new ImmutablePair<>("d", "de"),
        new ImmutablePair<>("e", "ee")));

    when(spiedInstance.getEnvironmentVariable("mapKey")).thenReturn(mapKeyPropertyValue);
    when(spiedInstance.getEnvironmentVariable("namespace1__mapKey")).thenReturn(namespace1MapKeyPropertyValue);
    when(spiedInstance.getEnvironmentVariable("namespace2__mapKey")).thenReturn(namespace2MapKeyPropertyValue);

    assertThat(spiedInstance.getPropertyMap("mapKey"), is(CollectionUtility.hashMap(
        new ImmutablePair<>("first", "one"),
        new ImmutablePair<>("second", "zwei"),
        new ImmutablePair<>("empty", null),
        new ImmutablePair<>("last", "last"))));
    assertThat(spiedInstance.getPropertyMap("mapKey", "namespace1"), is(CollectionUtility.hashMap(
        new ImmutablePair<>("b", "b"),
        new ImmutablePair<>("c", "ce"))));
    assertThat(spiedInstance.getPropertyMap("mapKey", "namespace2"), is(CollectionUtility.hashMap(
        new ImmutablePair<>("d", "de"),
        new ImmutablePair<>("e", "ee"))));
  }

  @Test
  public void testExpectedExceptionOnPlatformExceptionFromIJsonPropertyReader() {
    PropertiesHelper originalInstance = new PropertiesHelper(new ConfigPropertyProvider(MAP_CONFIG_PROPS));
    PropertiesHelper spiedInstance = spy(originalInstance);

    BEANS.get(TestJsonPropertyReader.class).setThrowExceptionOnRead(true);

    when(spiedInstance.getEnvironmentVariable("mapKey")).thenReturn("{\"second\": \"zwei\" \"third\": null}"); // missing comma after "zwei"

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> spiedInstance.getPropertyMap("mapKey"));
    assertThat(exception.getMessage(), is("Error parsing value of property map 'mapKey' as JSON value from an environment variable."));
  }

  @Test
  public void testCorrectBehaviorWhenNoIJsonPropertyReaderCouldBeFound() {
    PropertiesHelper originalInstance = new PropertiesHelper(new ConfigPropertyProvider(MAP_CONFIG_PROPS));
    PropertiesHelper spiedInstance = spy(originalInstance);

    // unregister IJsonPropertyReader Bean
    Platform.get().getBeanManager().unregisterClass(TestJsonPropertyReader.class);

    when(spiedInstance.getEnvironmentVariable("mapKey")).thenReturn("{\"second\": \"zwei\", \"third\": null}");
    try {
      PlatformException exception = assertThrows(PlatformException.class, () -> spiedInstance.getPropertyMap("mapKey"));
      assertThat(exception.getMessage(), is("No IJsonPropertyReader instance found while trying to decode the value of property map 'mapKey' from an environment variable. " +
          "Make sure to provide an appropriate implementation or unset the respective environment variable."));
    }
    finally {
      BeanTestingHelper.get().registerBean(new BeanMetaData(TestJsonPropertyReader.class));
    }
  }

  @Test
  public void testCorrectBehaviorWhenIJsonPropertyReaderIsNotPresentButAlsoNotNeeded() throws Exception {
    PropertiesHelper helper = new PropertiesHelper(new ConfigPropertyProvider(MAP_CONFIG_PROPS));

    Platform.get().getBeanManager().unregisterClass(TestJsonPropertyReader.class);

    try {
      assertThat(helper.getPropertyMap("mapKey"), is(CollectionUtility.hashMap(
          new ImmutablePair<>("first", "one"),
          new ImmutablePair<>("second", "two"),
          new ImmutablePair<>("third", "three"),
          new ImmutablePair<>("empty", null),
          new ImmutablePair<>("last", "last"))));
    }
    finally {
      BeanTestingHelper.get().registerBean(new BeanMetaData(TestJsonPropertyReader.class));
    }
  }

  @Test
  public void testReadPropertyListFromEnvironment() {
    PropertiesHelper originalInstance = new PropertiesHelper(new ConfigPropertyProvider(LIST_PROPS));
    PropertiesHelper spiedInstance = spy(originalInstance);

    String listPropertyValue = "{\"0\": \"zero\", \"1\": \"one\", \"2\": \"two\", \"3\": \"three\"}";
    String listWithValidIndicesPropertyValue = "{\"2\": \"two\", \"4\": \"four\", \"5\": \"five\"}";

    BEANS.get(TestJsonPropertyReader.class).putMapping(listPropertyValue, CollectionUtility.hashMap(
        new ImmutablePair<>("0", "zero"),
        new ImmutablePair<>("1", "one"),
        new ImmutablePair<>("2", "two"),
        new ImmutablePair<>("3", "three")));

    BEANS.get(TestJsonPropertyReader.class).putMapping(listWithValidIndicesPropertyValue, CollectionUtility.hashMap(
        new ImmutablePair<>("2", "two"),
        new ImmutablePair<>("4", "four"),
        new ImmutablePair<>("5", "five")));

    when(spiedInstance.getEnvironmentVariable("list")).thenReturn(listPropertyValue);
    when(spiedInstance.getEnvironmentVariable("listWithValidIndices")).thenReturn(listWithValidIndicesPropertyValue);

    assertThat(spiedInstance.getPropertyList("list"), is(Arrays.asList("zero", "one", "two", "three")));
    assertThat(spiedInstance.getPropertyList("listWithValidIndices"), is(Arrays.asList("a", null, "two", "b", "four", "five")));
  }

  @Test
  public void testPropertyString() {
    PropertiesHelper instance = new PropertiesHelper(new ConfigPropertyProvider(SAMPLE_CONFIG_PROPS));
    assertEquals(ATTR_STRING_VALUE, instance.getProperty(ATTR_STRING_KEY));
    assertEquals(ATTR_STRING_VALUE, instance.getProperty(ATTR_STRING_KEY, "defaultValue"));
    assertEquals(null, instance.getProperty(null));
    assertFalse(instance.hasProperty("not-existing"));
    assertTrue(instance.hasProperty(ATTR_STRING_KEY));

    assertNull(instance.getProperty("unknown"));
    assertEquals("defaultValue", instance.getProperty("unknown", "defaultValue"));
  }

  @Test
  public void testImportRecursion() {
    IPropertyProvider properties1 = new SimpleConfigPropertyProvider("ID1")
        .withProperty("prop1", "abc")
        .withProperty("import[props2]", "propFile2");
    IPropertyProvider properties2 = new SimpleConfigPropertyProvider("ID2")
        .withProperty("prop2", "def")
        .withProperty("import[props1]", "propFile1");

    PropertiesHelper h = new PropertiesHelper(properties1) {
      @Override
      protected IPropertyProvider getPropertyProvider(String configUrl) {
        if ("propFile1".equals(configUrl)) {
          return properties1;
        }
        else if ("propFile2".equals(configUrl)) {
          return properties2;
        }
        return super.getPropertyProvider(configUrl);
      }
    };
    assertEquals("abc", h.getProperty("prop1"));
    assertEquals("def", h.getProperty("prop2"));
  }

  @Test
  public void testLoopOnSelf() {
    try {
      new PropertiesHelper(new SimpleConfigPropertyProvider("ID")
          .withProperty("prop1", "a${prop1}b"));
      Assert.fail();
    }
    catch (IllegalArgumentException e) {
      assertEquals("resolving expression 'a${prop1}b': loop detected (the resolved value contains the original expression): a${prop1}b", e.getMessage());
    }
  }

  @Test
  public void testUndefinedPlaceholder() {
    try {
      new PropertiesHelper(new SimpleConfigPropertyProvider("ID")
          .withProperty("prop1", "a${prop2}b")
          .withProperty("prop2", "a${prop33}b"));
      Assert.fail();
    }
    catch (IllegalArgumentException e) {
      assertEquals("resolving expression 'a${prop33}b': variable ${prop33} is not defined in the context.", e.getMessage());
    }
  }

  @Test
  public void testLoop() {
    try {
      new PropertiesHelper(new SimpleConfigPropertyProvider("ID")
          .withProperty("prop1", "a${prop2}b")
          .withProperty("prop2", "a${prop3}b")
          .withProperty("prop3", "a${prop4}b")
          .withProperty("prop4", "a${prop5}b")
          .withProperty("prop5", "a${prop4}b"));

      Assert.fail();
    }
    catch (IllegalArgumentException e) {
      assertEquals("resolving expression 'a${prop3}b': loop detected: [prop3, prop4, prop5]", e.getMessage());
    }
  }

  @Test
  public void testImportWithPlaceholder() {
    PropertiesHelper h = new PropertiesHelper(new ConfigPropertyProvider(PLACEHOLDER_IMPORT_PROPS));
    assertTrue(h.hasProviderProperties());
    assertEquals(5, h.getPropertyMap(MAP_KEY).size());
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
    PropertiesHelper h = new PropertiesHelper(new ConfigPropertyProvider(LOOP_IMPORT_PROPS));
    assertEquals("value1", h.getProperty("key1"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNonExistingImport() {
    try {
      System.setProperty("import", "non-existing-file-url");
      new PropertiesHelper(new ConfigPropertyProvider(HELPER_CONFIG_PROPS));
    }
    finally {
      System.clearProperty("import");
    }
  }

  @Test
  public void testPropertyInt() {
    PropertiesHelper instance = new PropertiesHelper(new ConfigPropertyProvider(SAMPLE_CONFIG_PROPS));
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
    PropertiesHelper instance = new PropertiesHelper(new ConfigPropertyProvider(SAMPLE_CONFIG_PROPS));
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
    PropertiesHelper instance = new PropertiesHelper(new ConfigPropertyProvider(SAMPLE_CONFIG_PROPS));
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
    PropertiesHelper instance = new PropertiesHelper(new ConfigPropertyProvider(SAMPLE_CONFIG_PROPS));
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
    PropertiesHelper instance = new PropertiesHelper(new ConfigPropertyProvider(SAMPLE_CONFIG_PROPS));
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
    PropertiesHelper instance = new PropertiesHelper(new ConfigPropertyProvider(SAMPLE_CONFIG_PROPS));
    assertEquals(17, instance.getAllPropertyNames().size());
    assertEquals(17, instance.getAllEntries().size());
  }

  @Test
  public void testSystemResolveProperty() {
    PropertiesHelper instance = new PropertiesHelper(new ConfigPropertyProvider(SAMPLE_CONFIG_PROPS));
    final String attrOtherSystemPropertyKey = "attrOtherSystemPropertyKey";
    try {
      System.setProperty(ATTR_STRING_KEY, "property ${" + attrOtherSystemPropertyKey + "}");
      System.setProperty(attrOtherSystemPropertyKey, "resolved");

      assertEquals("property resolved", instance.getProperty(ATTR_STRING_KEY));

      System.setProperty(ATTR_STRING_KEY, "property ${" + ATTR_LONG_KEY + "}");
      System.setProperty(attrOtherSystemPropertyKey, "property " + 777);
    }
    finally {
      System.clearProperty(ATTR_STRING_KEY);
      System.clearProperty(attrOtherSystemPropertyKey);
    }
  }

  /**
   * Only some shells support environment variables with dots. Test that these override properties in the config file
   * correctly.
   */
  @Test
  public void testEnvironmentOverrideWithDotSupport() {
    final String overridableProperty = "overridable.with.dotproperty";

    // Since Environment variables with dots (.) are not posix-compliant,
    PropertiesHelper realInstance = new PropertiesHelper(new ConfigPropertyProvider(DOTPROPERTY_PROPS));
    PropertiesHelper spiedInstance = Mockito.spy(realInstance);
    Mockito.when(spiedInstance.getEnvironmentVariable(overridableProperty)).thenReturn("2");

    assertEquals("2", spiedInstance.getProperty(overridableProperty));
  }

  /**
   * Only some shells support environment variables with dots. Test the alternative override using underscores.
   */
  @Test
  public void testEnvironmentOverrideWithoutDotSupport() {
    // Replaced with underscore.
    final String overridableProperty = "overridable_with_dotproperty";

    // Mock environment variables
    PropertiesHelper realInstance = new PropertiesHelper(new ConfigPropertyProvider(DOTPROPERTY_PROPS));
    PropertiesHelper spiedInstance = Mockito.spy(realInstance);
    Mockito.when(spiedInstance.getEnvironmentVariable(overridableProperty)).thenReturn("2");

    final String originalProperty = "overridable.with.dotproperty";
    assertEquals("2", spiedInstance.getProperty(originalProperty));
  }

  /**
   * Test override using uppercase
   */
  @Test
  public void testEnvironmentOverrideWithUppercase() {
    // Replaced with underscore.
    final String overridableProperty = "OVERRIDABLE.WITH.DOTPROPERTY";

    // Since Environment variables with dots (.) are not posix-compliant,
    PropertiesHelper realInstance = new PropertiesHelper(new ConfigPropertyProvider(DOTPROPERTY_PROPS));
    PropertiesHelper spiedInstance = Mockito.spy(realInstance);
    Mockito.when(spiedInstance.getEnvironmentVariable(overridableProperty)).thenReturn("2");

    final String originalProperty = "overridable.with.dotproperty";
    assertEquals("2", spiedInstance.getProperty(originalProperty));
  }

  /**
   * Test override using uppercase
   */
  @Test
  public void testEnvironmentOverrideWithUppercaseWithoutDotSupport() {
    // Replaced with underscore.
    final String overridableProperty = "OVERRIDABLE_WITH_DOTPROPERTY";

    // Since Environment variables with dots (.) are not posix-compliant,
    PropertiesHelper realInstance = new PropertiesHelper(new ConfigPropertyProvider(DOTPROPERTY_PROPS));
    PropertiesHelper spiedInstance = Mockito.spy(realInstance);
    Mockito.when(spiedInstance.getEnvironmentVariable(overridableProperty)).thenReturn("2");

    final String originalProperty = "overridable.with.dotproperty";
    assertEquals("2", spiedInstance.getProperty(originalProperty));
  }

  @Test
  public void testOverrideImportWithSystemProperty() {
    String IMPORT_DB = "import[db]";
    try {
      System.setProperty(IMPORT_DB, "propSetSystem");

      IPropertyProvider properties1 = new SimpleConfigPropertyProvider("ID1")
          .withProperty("prop1", "prop1");
      IPropertyProvider dbSystemPropertySet = new SimpleConfigPropertyProvider("ID2")
          .withProperty("prop1", "sys.prop1");

      PropertiesHelper h = new PropertiesHelper(properties1) {
        @Override
        protected IPropertyProvider getPropertyProvider(String configUrl) {
          if ("propSetSystem".equals(configUrl)) {
            return dbSystemPropertySet;
          }
          return super.getPropertyProvider(configUrl);
        }
      };
      assertEquals("sys.prop1", h.getProperty("prop1"));
    }
    finally {
      System.clearProperty(IMPORT_DB);
    }
  }

  @Test
  public void testReplacementOfKeyAlreadyUsed() {
    IPropertyProvider properties = new SimpleConfigPropertyProvider("ID1")
        .withProperty("prop1", "prop1")
        .withProperty("prop2", "${prop1}")
        .withProperty("prop1", "prop3")
        .withProperty("prop4", "${prop1}");

    PropertiesHelper h = new PropertiesHelper(properties);
    assertEquals("prop3", h.getProperty("prop1"));
    assertEquals("prop3", h.getProperty("prop2"));
    assertEquals("prop3", h.getProperty("prop4"));
  }

  @Test
  public void testReferenceInImport() {
    IPropertyProvider properties1 = new SimpleConfigPropertyProvider("ID1")
        .withProperty("prop1", "aValue")
        .withProperty("import[sub]", "properties2");
    IPropertyProvider properties2 = new SimpleConfigPropertyProvider("ID2")
        .withProperty("prop2", "${prop1}");

    PropertiesHelper h = new PropertiesHelper(properties1) {
      @Override
      protected IPropertyProvider getPropertyProvider(String configUrl) {
        if ("properties2".equals(configUrl)) {
          return properties2;
        }
        return super.getPropertyProvider(configUrl);
      }
    };
    assertEquals("aValue", h.getProperty("prop1"));
    assertEquals("aValue", h.getProperty("prop2"));
  }

  @Test
  public void testReplaceImportWithSystemProperty() {
    String IMPORT_DB = "import[db]";
    try {
      System.setProperty(IMPORT_DB, "dbSystemProperties");

      IPropertyProvider properties1 = new SimpleConfigPropertyProvider("ID1")
          .withProperty("import[db]", "dbProperties");

      IPropertyProvider dbProperties = new SimpleConfigPropertyProvider("ID2")
          .withProperty("dbName", "postgres")
          .withProperty("dbPort", "222");

      IPropertyProvider dbSystemProperties = new SimpleConfigPropertyProvider("ID3")
          .withProperty("dbName", "derby");

      PropertiesHelper h = new PropertiesHelper(properties1) {
        @Override
        protected IPropertyProvider getPropertyProvider(String configUrl) {
          if ("dbProperties".equals(configUrl)) {
            return dbProperties;
          }
          else if ("dbSystemProperties".equals(configUrl)) {
            return dbSystemProperties;
          }
          return super.getPropertyProvider(configUrl);
        }
      };
      assertEquals(null, h.getProperty("dbPort"));
      assertEquals("derby", h.getProperty("dbName"));
    }
    finally {
      System.clearProperty(IMPORT_DB);
    }
  }

  @Test
  public void testReplaceImportWithSystemProperty1() {
    String IMPORT_DB = "import_db";
    try {
      System.setProperty(IMPORT_DB, "dbSystemProperties");

      IPropertyProvider properties1 = new SimpleConfigPropertyProvider("ID1")
          .withProperty("import[db]", "dbProperties");

      IPropertyProvider dbProperties = new SimpleConfigPropertyProvider("ID2")
          .withProperty("dbName", "postgres")
          .withProperty("dbPort", "222");

      IPropertyProvider dbSystemProperties = new SimpleConfigPropertyProvider("ID3")
          .withProperty("dbName", "derby")
          .withProperty("dbPort", "111");

      PropertiesHelper h = new PropertiesHelper(properties1) {
        @Override
        protected IPropertyProvider getPropertyProvider(String configUrl) {
          if ("dbProperties".equals(configUrl)) {
            return dbProperties;
          }
          else if ("dbSystemProperties".equals(configUrl)) {
            return dbSystemProperties;
          }
          return super.getPropertyProvider(configUrl);
        }
      };
      assertEquals("111", h.getProperty("dbPort"));
      assertEquals("derby", h.getProperty("dbName"));
    }
    finally {
      System.clearProperty(IMPORT_DB);
    }
  }

  /**
   * Used in {@link PropertiesHelperTest} as a test implementation for {@link IJsonPropertyReader}.
   */
  @IgnoreBean
  public static class TestJsonPropertyReader implements IJsonPropertyReader {

    private final Map<String, Map<String, String>> m_propertyValueToMap = new HashMap<>();

    private boolean m_throwExceptionOnRead = false;

    @Override
    public Map<String, String> readJsonPropertyValue(String propertyValue) {
      if (m_throwExceptionOnRead) {
        throw new PlatformException("Simulated exception from {}", this.getClass());
      }
      Assertions.assertTrue(m_propertyValueToMap.containsKey(propertyValue), "Prepare mapping for value {} before using this {}.", propertyValue, this.getClass());
      return m_propertyValueToMap.get(propertyValue);
    }

    public void clear() {
      m_propertyValueToMap.clear();
      m_throwExceptionOnRead = false;
    }

    public void putMapping(String propertyValue, Map<String, String> parsedMap) {
      m_propertyValueToMap.put(propertyValue, parsedMap);
    }

    public void setThrowExceptionOnRead(boolean throwExceptionOnRead) {
      m_throwExceptionOnRead = throwExceptionOnRead;
    }
  }
}
