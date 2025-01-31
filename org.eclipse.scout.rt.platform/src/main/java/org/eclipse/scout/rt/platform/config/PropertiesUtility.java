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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convenience utility used to replace variable patterns in property files or maps
 * <p>
 * The variable pattern is by default <code>${name}</code> but custom code can also use patterns like
 * <code>#name#</code> or <code>@@name@@</code>
 *
 * @since 10.0
 */
public class PropertiesUtility {
  private static final Logger LOG = LoggerFactory.getLogger(PropertiesHelper.class);
  private static final char ENVIRONMENT_VARIABLE_DOT_REPLACEMENT = '_';

  public static final Pattern DEFAULT_VARIABLE_PATTERN = Pattern.compile("\\$\\{([^}]+)}");

  /**
   * Replace all variables in the {@link Properties} using the properties as the value domain. Order of evaluation is
   * <ol>
   * <li>System property</li>
   * <li>Environment variable</li>
   * <li>key/value in properties</li>
   * </ol>
   *
   * @param properties
   *     {@link Properties} object
   * @param failOnProblems
   *     true to throw {@link IllegalArgumentException} on problems, false to just log as debug
   */
  public static void resolveVariables(Properties properties, boolean failOnProblems) {
    BinaryOperator<String> variableReplacer = (key, variableName) -> {
      String value = defaultReplaceSystemPropertyAndEnv(variableName);
      if (value != null) {
        return value;
      }
      return properties.getProperty(variableName);
    };
    for (String key : properties.stringPropertyNames()) {
      properties.setProperty(key, resolveKeyOverride(key, variableReplacer));
      properties.setProperty(key, resolveValue(key, properties.getProperty(key), DEFAULT_VARIABLE_PATTERN, variableReplacer, failOnProblems));
    }
  }

  /**
   * Replace all variables in the {@link Properties} using the properties as the value domain. Order of evaluation is
   * <ol>
   * <li>System property</li>
   * <li>Environment variable</li>
   * <li>key/value in properties</li>
   * </ol>
   *
   * @param properties
   *     properties map (key -> value)
   * @param failOnProblems
   *     true to throw {@link IllegalArgumentException} on problems, false to just log as debug
   */
  public static void resolveVariables(Map<String, String> properties, boolean failOnProblems) {
    BinaryOperator<String> variableReplacer = (key, variableName) -> {
      String value = defaultReplaceSystemPropertyAndEnv(variableName);
      if (value != null) {
        return value;
      }
      return properties.get(variableName);
    };
    properties.replaceAll((key, value) -> resolveKeyOverride(key, variableReplacer));
    properties.replaceAll((key, value) -> resolveValue(key, value, DEFAULT_VARIABLE_PATTERN, variableReplacer, failOnProblems));
  }

  /**
   * Find override value of ky using the variable replacer
   *
   * @param propertyKey
   *     The property key.
   * @param variableReplacer
   *     maps a variable name to its variable value
   * @return A {@link String} with the original value of this property key or the replaced value due to a System
   * property or env value override with the same name as the property key.
   */
  public static String resolveKeyOverride(String propertyKey, BinaryOperator<String> variableReplacer) {
    return variableReplacer.apply(propertyKey, propertyKey);
  }

  /**
   * Resolves all variables of format <code>${variableName}</code> in the given expression according to the current
   * application context.
   *
   * @param propertyKey
   *     The key of the property to resolve.
   * @param value
   *     The expression to resolve.
   * @param variablePattern
   *     The pattern for variables, such as <code>${var}</code>. The pattern must contain group(1) as the variable
   *     name upon a match.
   * @param variableReplacer
   *     maps a variable name to its variable value
   * @return A {@link String} where all variables have been replaced with their values.
   */
  @SuppressWarnings("squid:S1149")
  public static String resolveValue(String propertyKey, String value, Pattern variablePattern, BinaryOperator<String> variableReplacer, boolean failOnProblems) {
    Matcher m = variablePattern.matcher(value);
    boolean found = m.find();
    if (!found) {
      return value;
    }
    String t = value;
    Set<String> loopDetection = new LinkedHashSet<>();
    while (found) {
      StringBuffer sb = new StringBuffer();
      List<String> stageKeys = new ArrayList<>();
      while (found) {
        String variableName = m.group(1);
        String replacement = variableReplacer.apply(propertyKey, variableName);
        if (!StringUtility.hasText(replacement)) {
          if (failOnProblems) {
            throw new IllegalArgumentException("resolving expression '" + value + "': variable ${" + variableName + "} is not defined in the context.");
          }
          else {
            LOG.debug("resolving expression '" + value + "': variable ${" + variableName + "} is not defined in the context.");
            return t;
          }
        }
        if (replacement.contains(value)) {
          if (failOnProblems) {
            throw new IllegalArgumentException("resolving expression '" + value + "': loop detected (the resolved value contains the original expression): " + replacement);
          }
          else {
            LOG.debug("resolving expression '" + value + "': loop detected (the resolved value contains the original expression): " + replacement);
            return t;
          }
        }
        m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        stageKeys.add(variableName);
        if (loopDetection.contains(variableName)) {
          if (failOnProblems) {
            throw new IllegalArgumentException("resolving expression '" + value + "': loop detected: " + loopDetection);
          }
          else {
            LOG.debug("resolving expression '" + value + "': loop detected: " + loopDetection);
            return t;
          }
        }
        found = m.find();
      }
      m.appendTail(sb);
      loopDetection.addAll(stageKeys);
      // next
      t = sb.toString();
      m = variablePattern.matcher(t);
      found = m.find();
    }
    return t;
  }

  /**
   * @return the replacement or null if there was no replacement
   */
  public static String defaultReplaceSystemPropertyAndEnv(String variableName) {
    // system config
    String value = System.getProperty(variableName);
    if (StringUtility.hasText(value)) {
      return value;
    }
    // environment config
    value = lookupEnvironmentVariableValue(variableName);
    if (StringUtility.hasText(value)) {
      return value;
    }
    return null;
  }

  /**
   * Returns the environment variable value corresponding to the property specified by the key, or <code>null</code>.
   * <p>
   * Attempts to find them by resolving the property name in the following order:
   * <ol>
   * <li>Original: <code>my.property</code></li>
   * <li>Periods replaced: <code>my_property</code></li>
   * <li>Original in uppercase: <code>MY.PROPERTY</code></li>
   * <li>Periods replaced, in uppercase: <code>MY_PROPERTY</code></li>
   * </ol>
   */
  public static String lookupEnvironmentVariableValue(String variableName) {
    // 1. Original
    String value = getenv(variableName);
    if (value != null) {
      return value;
    }

    // Periods in environment variable names are not POSIX compliant (See IEEE Standard 1003.1-2017, Chapter 8.1 "Environment Variable Definition"),
    // but supported by some shells. To allow overriding via environment variables (Bugzilla 541099) in any shell, convert them to underscores.
    // 2. With periods replaced
    String keyWithoutDots = variableName.replace('.', ENVIRONMENT_VARIABLE_DOT_REPLACEMENT);
    value = getenv(keyWithoutDots);
    if (value != null) {
      logInexactEnvNameMatch(variableName, keyWithoutDots);
      return value;
    }

    // Applications may define environment variable names with lower case, but only upper case is POSIX compliant for the environment.
    // To override from a shell, we should also check for upper case.
    // 3. In Uppercase, original periods
    String uppercaseKey = variableName.toUpperCase();
    value = getenv(uppercaseKey);
    if (value != null) {
      logInexactEnvNameMatch(variableName, uppercaseKey);
      return value;
    }

    // 4. In Uppercase, with periods replaced
    String keyWithoutDotsUppercase = keyWithoutDots.toUpperCase();
    value = getenv(keyWithoutDotsUppercase);
    if (value != null) {
      logInexactEnvNameMatch(variableName, keyWithoutDotsUppercase);
      return value;
    }

    return null;
  }

  private static void logInexactEnvNameMatch(String variableName, String actualEnvVariableName) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Property '{}' resolved to environment variable '{}' by inexact match.", variableName, actualEnvVariableName);
    }
  }

  private static String getenv(String key) {
    return System.getenv(key);
  }
}
