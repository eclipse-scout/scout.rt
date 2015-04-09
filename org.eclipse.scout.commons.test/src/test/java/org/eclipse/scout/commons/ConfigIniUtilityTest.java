package org.eclipse.scout.commons;

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
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for class {@link ConfigIniUtility}.
 */
public class ConfigIniUtilityTest {

  private static final String USER_HOME_KEY = "user.home";
  private static final String USER_HOME_VALUE = System.getProperty(USER_HOME_KEY);

  private static final String OTHER_PROP_KEY = "otherProp";
  private static final String OTHER_PROP_VALUE = "otherVal";

  private static final String SERVICE_CONFIG_PROP0_KEY = "svcProp0";
  private static final String SERVICE_CONFIG_PROP0_VALUE = "svcVal0";

  private static final String SERVICE_CONFIG_PROP1_KEY = "svcProp1";
  private static final String SERVICE_CONFIG_PROP1_FILTER = "/filter1";
  private static final String SERVICE_CONFIG_PROP1_VALUE = "svcVal1";

  private static final String SERVICE_CONFIG_PROP2_KEY = "svcProp2";
  private static final String SERVICE_CONFIG_PROP2_FILTER = "/filter2";
  private static final String SERVICE_CONFIG_PROP2_VALUE = "svcVal2";

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

  @Before
  public void before() throws MalformedURLException {
    URL url = new URL("configini", "localhost", 80, ConfigIniUtility.CONFIG_INI, getConfigIniContent());
    ConfigIniUtility.parseConfigIni(url);
  }

  @Test
  public void testConfigIniUtility() throws Exception {
    ConfigIniUtility.resolveAll();

    assertEquals(USER_HOME_VALUE, ConfigIniUtility.getProperty(USER_HOME_KEY));
    assertEquals(OTHER_PROP_VALUE, ConfigIniUtility.getProperty(OTHER_PROP_KEY));
    Map<String, String> props1 = ConfigIniUtility.getProperties(ConfigIniUtilityTest.class);
    assertEquals(3, props1.size());
    assertEquals(SERVICE_CONFIG_PROP0_VALUE, props1.get(SERVICE_CONFIG_PROP0_KEY));
    assertEquals(SERVICE_CONFIG_PROP1_VALUE, props1.get(SERVICE_CONFIG_PROP1_KEY));
    assertEquals(SERVICE_CONFIG_PROP2_VALUE, props1.get(SERVICE_CONFIG_PROP2_KEY));

    Map<String, String> props2 = ConfigIniUtility.getProperties(ConfigIniUtilityTest.class, SERVICE_CONFIG_PROP2_FILTER);
    assertEquals(2, props2.size());
    assertEquals(SERVICE_CONFIG_PROP2_VALUE, props2.get(SERVICE_CONFIG_PROP2_KEY));

    assertEquals("prefix" + USER_HOME_VALUE + "suffix", ConfigIniUtility.getProperty(RESOLVE_TEST_KEY));
    assertEquals(USER_HOME_VALUE + "/subfolder", ConfigIniUtility.getProperty(ATTR_USER_HOME_TEST_KEY));
  }

  @Test
  public void testGetClassProperty() {
    assertNull(ConfigIniUtility.getClassProperty(ConfigIniUtility.class.getName() + "#prop", ConfigIniUtilityTest.class, null));
    assertEquals("prop", ConfigIniUtility.getClassProperty(ConfigIniUtilityTest.class.getName() + "#prop", ConfigIniUtilityTest.class, null));
    assertNull(ConfigIniUtility.getClassProperty(ConfigIniUtilityTest.class.getName() + "#", ConfigIniUtilityTest.class, null));
    assertNull(ConfigIniUtility.getClassProperty(ConfigIniUtilityTest.class.getName(), ConfigIniUtilityTest.class, null));
    assertEquals("prop", ConfigIniUtility.getClassProperty(ConfigIniUtilityTest.class.getName() + "/filter#prop", ConfigIniUtilityTest.class, null));
    assertNull(ConfigIniUtility.getClassProperty(ConfigIniUtilityTest.class.getName() + "/filter#prop", ConfigIniUtilityTest.class, "test"));
    assertEquals("prop", ConfigIniUtility.getClassProperty(ConfigIniUtilityTest.class.getName() + "/filter#prop", ConfigIniUtilityTest.class, "/filter"));
    assertEquals("prop", ConfigIniUtility.getClassProperty(ConfigIniUtilityTest.class.getName() + "/#prop", ConfigIniUtilityTest.class, "/"));
    assertEquals("prop", ConfigIniUtility.getClassProperty(ConfigIniUtilityTest.class.getName() + "/#prop", ConfigIniUtilityTest.class, null));
    assertNull("prop", ConfigIniUtility.getClassProperty(ConfigIniUtilityTest.class.getName() + "/prop", ConfigIniUtilityTest.class, "/"));
    assertNull("prop", ConfigIniUtility.getClassProperty(ConfigIniUtilityTest.class.getName() + "/prop", ConfigIniUtilityTest.class, null));
  }

  @Test
  public void testPropertyString() {
    assertEquals(ATTR_STRING_VALUE, ConfigIniUtility.getProperty(ATTR_STRING_KEY));
    assertEquals(ATTR_STRING_VALUE, ConfigIniUtility.getProperty(ATTR_STRING_KEY, "defaultValue"));

    assertNull(ConfigIniUtility.getProperty("unknown"));
    assertEquals("defaultValue", ConfigIniUtility.getProperty("unknown", "defaultValue"));
  }

  @Test
  public void testPropertyInt() {
    assertEquals(1, ConfigIniUtility.getPropertyInt(ATTR_INT_KEY, 777));
    assertEquals(777, ConfigIniUtility.getPropertyInt(ATTR_ILLEGAL_NUMBER_KEY, 777));
    assertEquals(777, ConfigIniUtility.getPropertyInt("unknown", 777));
  }

  @Test
  public void testPropertyLong() {
    assertEquals(2L, ConfigIniUtility.getPropertyLong(ATTR_LONG_KEY, 777L));
    assertEquals(777L, ConfigIniUtility.getPropertyLong(ATTR_ILLEGAL_NUMBER_KEY, 777L));
    assertEquals(777L, ConfigIniUtility.getPropertyLong("unknown", 777L));
  }

  @Test
  public void testPropertyFloat() {
    assertEquals(3f, ConfigIniUtility.getPropertyFloat(ATTR_FLOAT_KEY, 777f), 0f);
    assertEquals(777f, ConfigIniUtility.getPropertyFloat(ATTR_ILLEGAL_NUMBER_KEY, 777f), 0f);
    assertEquals(777f, ConfigIniUtility.getPropertyFloat("unknown", 777f), 0f);
  }

  @Test
  public void testPropertyDouble() {
    assertEquals(4.0, ConfigIniUtility.getPropertyDouble(ATTR_DOUBLE_KEY, 777.0), 0.0);
    assertEquals(777.0, ConfigIniUtility.getPropertyDouble(ATTR_ILLEGAL_NUMBER_KEY, 777.0), 0.0);
    assertEquals(777.0, ConfigIniUtility.getPropertyDouble("unknown", 777.0), 0.0);
  }

  @Test
  public void testPropertyBoolean() {
    assertTrue(ConfigIniUtility.getPropertyBoolean(ATTR_BOOLEAN_KEY, true));
    assertTrue(ConfigIniUtility.getPropertyBoolean(ATTR_STRING_KEY, true));
    assertTrue(ConfigIniUtility.getPropertyBoolean("unknown", true));

    assertTrue(ConfigIniUtility.getPropertyBoolean(ATTR_BOOLEAN_KEY, false));
    assertFalse(ConfigIniUtility.getPropertyBoolean(ATTR_STRING_KEY, false));
    assertFalse(ConfigIniUtility.getPropertyBoolean("unknown", false));
  }

  private URLStreamHandler getConfigIniContent() {
    String[][] input = new String[][]{
        {USER_HOME_KEY, USER_HOME_VALUE},
        {OTHER_PROP_KEY, OTHER_PROP_VALUE},
        {ConfigIniUtilityTest.class.getName() + '#' + SERVICE_CONFIG_PROP0_KEY, SERVICE_CONFIG_PROP0_VALUE},
        {ConfigIniUtilityTest.class.getName() + SERVICE_CONFIG_PROP1_FILTER + '#' + SERVICE_CONFIG_PROP1_KEY, SERVICE_CONFIG_PROP1_VALUE},
        {ConfigIniUtilityTest.class.getName() + SERVICE_CONFIG_PROP2_FILTER + '#' + SERVICE_CONFIG_PROP2_KEY, SERVICE_CONFIG_PROP2_VALUE},
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

    return new P_ConfigIniTestHandler(sb.toString());
  }

  private static String escape(String input) {
    return input.replace(":", "\\:").replace("=", "\\=");
  }

  private static final class P_ConfigIniTestHandler extends URLStreamHandler {

    private final String m_configIniContent;

    private P_ConfigIniTestHandler(String configIniContent) {
      m_configIniContent = configIniContent;
    }

    @Override
    protected URLConnection openConnection(URL u) throws IOException {
      return new URLConnection(u) {

        @Override
        public InputStream getInputStream() throws IOException {
          return new ByteArrayInputStream(m_configIniContent.getBytes("UTF-8"));
        }

        @Override
        public void connect() throws IOException {
        }
      };
    }
  }
}
