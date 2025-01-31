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

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.Platform;

/**
 * Interface for configuration validators. During the {@link Platform} startup all classes implementing this interface
 * are asked to validate configuration provided in the config.properties files (see {@link ConfigUtility} and
 * {@link PropertiesHelper}).
 * <p>
 * If there is at least one {@link IConfigurationValidator} that accepts a given key-value-pair (see
 * {@link #isValid(String, String)}) the configuration is considered to be valid. Otherwise the platform will not start.
 */
@FunctionalInterface
@Bean
public interface IConfigurationValidator {
  /**
   * Specifies if the given key-value pair is considered to be valid.<br>
   * This method should not throw any exceptions when an invalid value is found. Instead it should return {@code false}
   * and it may log some details. This helps to log all errors instead of only showing the first.
   *
   * @param key
   *     The key to validate.
   * @param value
   *     The value of the property to validate.
   * @return {@code true} if the given pair is accepted. {@code false} if it is invalid or this validator cannot decide.
   */
  boolean isValid(String key, String value);
}
