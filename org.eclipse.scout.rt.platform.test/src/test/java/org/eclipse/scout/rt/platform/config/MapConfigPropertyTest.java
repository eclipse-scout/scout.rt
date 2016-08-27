package org.eclipse.scout.rt.platform.config;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.rt.platform.IgnoreBean;
import org.junit.BeforeClass;
import org.junit.Test;

public class MapConfigPropertyTest {

  private static final String PROP_NAME = "mapconfigproperty";
  private static final String PROP_VALUE = ""
      + PROP_NAME + " = (key1)->value1\\\n"
      + "               (key2)->value2\\\n"
      + "               (key3)->vm:(broker:(tcp://localhost:8229)?persistent=false)?jms.prefetchPolicy.queuePrefetch=1\\\n"
      + "               (key4)->failover:(tcp://local1:61616,tcp://local2:61616,tcp://remote:61616)?randomize=false&priorityBackup=true&priorityURIs=tcp://local1:61616,tcp://local2:61616\\\n"
      + "               (key5)->value4";

  private static PropertiesHelper PROPERTIES_HELPER;

  @BeforeClass
  public static void beforeClass() throws MalformedURLException {
    final URL url = new URL("configproperties", "localhost", 80, "config.properties", new P_ConfigPropertiesTestHandler(PROP_VALUE));
    PROPERTIES_HELPER = new PropertiesHelper("config.properties");
    PROPERTIES_HELPER.parse(url);
    PROPERTIES_HELPER.resolveAll();
  }

  @Test
  public void test() throws MalformedURLException {
    SampleMapConfigProperty testee = new SampleMapConfigProperty();

    Map<String, String> expected = new HashMap<>();
    expected.put("key1", "value1");
    expected.put("key2", "value2");
    expected.put("key3", "vm:(broker:(tcp://localhost:8229)?persistent=false)?jms.prefetchPolicy.queuePrefetch=1");
    expected.put("key4", "failover:(tcp://local1:61616,tcp://local2:61616,tcp://remote:61616)?randomize=false&priorityBackup=true&priorityURIs=tcp://local1:61616,tcp://local2:61616");
    expected.put("key5", "value4");
    assertEquals(expected, testee.getValue());
  }

  @IgnoreBean
  private static class SampleMapConfigProperty extends AbstractMapConfigProperty {

    @Override
    public String getKey() {
      return PROP_NAME;
    }

    @Override
    protected Map<String, String> createValue() {
      return parse(PROPERTIES_HELPER.getProperty(PROP_NAME));
    }
  }

  private static final class P_ConfigPropertiesTestHandler extends URLStreamHandler {

    private final String m_configPropertiesContent;

    private P_ConfigPropertiesTestHandler(final String configPropertiesContent) {
      m_configPropertiesContent = configPropertiesContent;
    }

    @Override
    protected URLConnection openConnection(final URL u) throws IOException {
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
