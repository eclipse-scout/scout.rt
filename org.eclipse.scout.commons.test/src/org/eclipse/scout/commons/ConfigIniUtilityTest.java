package org.eclipse.scout.commons;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for class {@link ConfigIniUtility}.
 */
public class ConfigIniUtilityTest {

  private static final String USER_HOME_KEY = "user.home";
  private static final String USER_HOME_VALUE = "C:/user/home";

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

  private static final String AT_USER_HOME_TEST_KEY = "aTestKey";
  private static final String AT_USER_HOME_TEST_VALUE = "@user.home/subfolder";

  @Test
  public void testConfigIniUtility() throws Exception {
    URL url = new URL("configini", "localhost", 80, ConfigIniUtility.CONFIG_INI, getConfigIniContent());
    Set<String> externalConfigPaths = new HashSet<>();
    ConfigIniUtility.parseConfigIni(url, externalConfigPaths);
    ConfigIniUtility.resolveAll();

    Assert.assertEquals(USER_HOME_VALUE, ConfigIniUtility.getProperty(USER_HOME_KEY));
    Assert.assertEquals(OTHER_PROP_VALUE, ConfigIniUtility.getProperty(OTHER_PROP_KEY));
    Map<String, String> props1 = ConfigIniUtility.getProperties(ConfigIniUtilityTest.class);
    Assert.assertEquals(3, props1.size());
    Assert.assertEquals(SERVICE_CONFIG_PROP0_VALUE, props1.get(SERVICE_CONFIG_PROP0_KEY));
    Assert.assertEquals(SERVICE_CONFIG_PROP1_VALUE, props1.get(SERVICE_CONFIG_PROP1_KEY));
    Assert.assertEquals(SERVICE_CONFIG_PROP2_VALUE, props1.get(SERVICE_CONFIG_PROP2_KEY));

    Map<String, String> props2 = ConfigIniUtility.getProperties(ConfigIniUtilityTest.class, SERVICE_CONFIG_PROP2_FILTER);
    Assert.assertEquals(2, props2.size());
    Assert.assertEquals(SERVICE_CONFIG_PROP2_VALUE, props2.get(SERVICE_CONFIG_PROP2_KEY));

    Assert.assertEquals("prefix" + USER_HOME_VALUE + "suffix", ConfigIniUtility.getProperty(RESOLVE_TEST_KEY));
    Assert.assertEquals(USER_HOME_VALUE + "/subfolder", ConfigIniUtility.getProperty(AT_USER_HOME_TEST_KEY));
  }

  @Test
  public void testGetClassProperty() {
    Assert.assertNull(ConfigIniUtility.getClassProperty(ConfigIniUtility.class.getName() + "#prop", ConfigIniUtilityTest.class, null));
    Assert.assertEquals("prop", ConfigIniUtility.getClassProperty(ConfigIniUtilityTest.class.getName() + "#prop", ConfigIniUtilityTest.class, null));
    Assert.assertNull(ConfigIniUtility.getClassProperty(ConfigIniUtilityTest.class.getName() + "#", ConfigIniUtilityTest.class, null));
    Assert.assertNull(ConfigIniUtility.getClassProperty(ConfigIniUtilityTest.class.getName(), ConfigIniUtilityTest.class, null));
    Assert.assertEquals("prop", ConfigIniUtility.getClassProperty(ConfigIniUtilityTest.class.getName() + "/filter#prop", ConfigIniUtilityTest.class, null));
    Assert.assertNull(ConfigIniUtility.getClassProperty(ConfigIniUtilityTest.class.getName() + "/filter#prop", ConfigIniUtilityTest.class, "test"));
    Assert.assertEquals("prop", ConfigIniUtility.getClassProperty(ConfigIniUtilityTest.class.getName() + "/filter#prop", ConfigIniUtilityTest.class, "/filter"));
    Assert.assertEquals("prop", ConfigIniUtility.getClassProperty(ConfigIniUtilityTest.class.getName() + "/#prop", ConfigIniUtilityTest.class, "/"));
    Assert.assertEquals("prop", ConfigIniUtility.getClassProperty(ConfigIniUtilityTest.class.getName() + "/#prop", ConfigIniUtilityTest.class, null));
    Assert.assertNull("prop", ConfigIniUtility.getClassProperty(ConfigIniUtilityTest.class.getName() + "/prop", ConfigIniUtilityTest.class, "/"));
    Assert.assertNull("prop", ConfigIniUtility.getClassProperty(ConfigIniUtilityTest.class.getName() + "/prop", ConfigIniUtilityTest.class, null));
  }

  private URLStreamHandler getConfigIniContent() {
    String[][] input = new String[][]{
        {USER_HOME_KEY, USER_HOME_VALUE},
        {OTHER_PROP_KEY, OTHER_PROP_VALUE},
        {ConfigIniUtilityTest.class.getName() + '#' + SERVICE_CONFIG_PROP0_KEY, SERVICE_CONFIG_PROP0_VALUE},
        {ConfigIniUtilityTest.class.getName() + SERVICE_CONFIG_PROP1_FILTER + '#' + SERVICE_CONFIG_PROP1_KEY, SERVICE_CONFIG_PROP1_VALUE},
        {ConfigIniUtilityTest.class.getName() + SERVICE_CONFIG_PROP2_FILTER + '#' + SERVICE_CONFIG_PROP2_KEY, SERVICE_CONFIG_PROP2_VALUE},
        {RESOLVE_TEST_KEY, RESOLVE_TEST_VALUE},
        {AT_USER_HOME_TEST_KEY, AT_USER_HOME_TEST_VALUE}
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
