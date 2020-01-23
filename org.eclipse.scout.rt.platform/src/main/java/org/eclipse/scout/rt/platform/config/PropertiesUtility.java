/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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

/**
 * Convenience utility used to replace variable patterns in property files or maps
 * <p>
 * The variable pattern is by default <code>${name}</code> but custom code can also use patterns like
 * <code>#name#</code> or <code>@@name@@</code>
 *
 * @since 10.0
 */
public class PropertiesUtility {
  public static final Pattern DEFAULT_VARIABLE_PATTERN = Pattern.compile("\\$\\{([^}]+)}");

  /**
   * Replace all variables in the {@link Properties} using the properties as the value domain
   *
   * @param properties
   */
  public static void resolveVariables(Properties properties) {
    for (String key : properties.stringPropertyNames()) {
      properties.setProperty(key, resolveValue(key, properties.getProperty(key), DEFAULT_VARIABLE_PATTERN, (key2, variableName) -> properties.getProperty(variableName)));
    }
  }

  /**
   * Replace all variables in the {@link Properties} using the properties as the value domain
   *
   * @param properties
   */
  public static void resolveVariables(Map<String, String> properties) {
    properties.replaceAll((key, value) -> resolveValue(key, value, DEFAULT_VARIABLE_PATTERN, (key2, variableName) -> properties.get(variableName)));
  }

  /**
   * Resolves all variables of format <code>${variableName}</code> in the given expression according to the current
   * application context.
   *
   * @param value
   *          The expression to resolve.
   * @param variablePattern
   *          The pattern for variables, such as <code>${var}</code>. The pattern must contain group(1) as the variable name upon a match.
   * @param variableReplacer
   *          maps a variable name to its variable value
   * @return A {@link String} where all variables have been replaced with their values.
   * @throws IllegalArgumentException
   *           if a variable could not be resolved in the current context.
   */
  @SuppressWarnings("squid:S1149")
  public static String resolveValue(String propertyKey, String value, Pattern variablePattern, BinaryOperator<String> variableReplacer) {
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
          throw new IllegalArgumentException("resolving expression '" + value + "': variable ${" + variableName + "} is not defined in the context.");
        }
        if (replacement.contains(value)) {
          throw new IllegalArgumentException("resolving expression '" + value + "': loop detected (the resolved value contains the original expression): " + replacement);
        }
        m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        stageKeys.add(variableName);
        if (loopDetection.contains(variableName)) {
          throw new IllegalArgumentException("resolving expression '" + value + "': loop detected: " + loopDetection);
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

}
