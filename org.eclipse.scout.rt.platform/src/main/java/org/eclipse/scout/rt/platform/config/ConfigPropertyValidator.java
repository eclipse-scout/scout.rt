package org.eclipse.scout.rt.platform.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.platform.BEANS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <h3>{@link ConfigPropertyValidator}</h3>
 */
public class ConfigPropertyValidator implements IConfigurationValidator {

  private static final Logger LOG = LoggerFactory.getLogger(ConfigPropertyValidator.class);

  private Map<String, IConfigProperty<?>> m_configProperties;

  protected Map<String, IConfigProperty<?>> getAllConfigProperties() {
    if (m_configProperties == null) {
      List<IConfigProperty> configProperties = BEANS.all(IConfigProperty.class);
      Map<String, IConfigProperty<?>> props = new HashMap<>(configProperties.size());
      for (IConfigProperty<?> prop : configProperties) {
        props.put(prop.getKey(), prop);
      }
      m_configProperties = props;
    }
    return m_configProperties;
  }

  protected String parseKey(String key) {
    int start = key.indexOf(PropertiesHelper.NAMESPACE_DELIMITER) + 1;
    int end = key.indexOf(PropertiesHelper.COLLECTION_DELIMITER_START, start);
    if (end < start) {
      end = key.length();
    }
    return key.substring(start, end);
  }

  @Override
  public boolean isValid(String key, String value) {
    String parsedKey = parseKey(key);
    IConfigProperty<?> property = getAllConfigProperties().get(parsedKey);
    if (property == null) {
      return false; // not found
    }

    try {
      property.getValue(); // check if the given value is valid according to the value constraints of that property class.
    }
    catch (Exception ex) {
      LOG.error("Failed parsing value of config property with key='{}'. Configured value='{}'.", parsedKey, value, ex);
      return false;
    }

    return true;
  }
}
