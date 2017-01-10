package org.eclipse.scout.rt.platform.config;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.Platform;

/**
 * <h3>{@link IConfigurationValidator}</h3><br>
 * Interface for configuration validators. During the {@link Platform} startup all classes implementing this interface
 * are asked to validate configuration provided in the config.properties files (see {@link ConfigUtility} and
 * {@link PropertiesHelper}).<br>
 * If there is at least one {@link IConfigurationValidator} that accepts a given key-value-pair (see
 * {@link #isValid(String, String)}) the configuration is considered to be valid. Otherwise the platform will not start.
 */
@Bean
public interface IConfigurationValidator {
  /**
   * Specifies if the given key-value pair is considered to be valid.<br>
   * This method should not throw any exceptions when an invalid value is found. Instead it should return {@code false}
   * and it may log some details. This helps to log all errors instead of only showing the first.
   *
   * @param key
   *          The key to valiate.
   * @param value
   *          The value of the property to validate.
   * @return {@code true} if the given pair is accepted. {@code false} if it is invalid or this validator cannot decide.
   */
  boolean isValid(String key, String value);
}
