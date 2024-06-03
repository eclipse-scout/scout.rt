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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.annotation.PostConstruct;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigPropertyValidator implements IConfigurationValidator {

  private static final Logger LOG = LoggerFactory.getLogger(ConfigPropertyValidator.class);

  private Map<String, IConfigProperty<?>> m_configProperties;
  private Set<String> m_specialValidKeys = new HashSet<>();

  @PostConstruct
  public void init() {
    m_specialValidKeys.add(PropertiesHelper.IMPORT_KEY);// 'import' key should be accepted although there is no IConfigProperty class for this key.
    m_specialValidKeys.add(PropertiesHelper.IMPORTENV_KEY);
  }

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
      return m_specialValidKeys.contains(parsedKey);
    }

    try {
      property.getValue(); // check if the given value is valid according to the value constraints of that property class.
      checkDefaultValueConfiguration(parsedKey, property, value);
    }
    catch (Exception ex) {
      LOG.error("Failed parsing value of config property with key='{}'. Configured value='{}'.", parsedKey, value, ex);
      return false;
    }

    return true;
  }

  /**
   * Check if configured value matches the default value
   */
  protected void checkDefaultValueConfiguration(String parsedKey, IConfigProperty<?> property, String configuredValue) {
    Object actualValue = property.getValue();
    Object defaultValue = property.getDefaultValue();
    if (ObjectUtility.equals(actualValue, defaultValue)) {
      String msg = "Config property with key='{}' has configured value='{}'. This results in an actual value of '{}' which is equal to the default value. Remove config entry for this key to minimize properties file.";
      if (Platform.get().inDevelopmentMode()) {
        LOG.warn(msg, parsedKey, configuredValue, actualValue);
      }
      else {
        LOG.info(msg, parsedKey, configuredValue, actualValue);
      }
    }
  }
}
