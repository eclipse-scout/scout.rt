/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.config;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * A config property which represents {@link Map} entries.
 * <p>
 *
 * <pre>
 * example: property=(key1)->value1\
 *                   (key2)->value2\
 *                   (key3)->value3
 * </pre>
 *
 * Use a trailing backslash to spread the map entries across multiple lines.
 */
public abstract class AbstractMapConfigProperty extends AbstractConfigProperty<Map<String, String>> {

  private static final Pattern ENTRY_PATTERN;

  static {
    // Example of Map Entries in config.properties:
    // prop=(key1)->value1\
    //      (key2)->value2\
    //      (key3)->value3
    //
    // -> The Java Properties framework returns the property's value as following: (key1)->value1(key2)->value2(key3)->value3

    // The following expression captures the entry key.
    final String mapEntryKey = "\\(([^\\)]+?)\\)->";
    // The following expression captures the entry value.
    // To identify the value's end, a positive lookahead (?=) for the next key (or the input's end) is used. Those characters are not consumed, and are available in the next match.
    final String mapEntryValue = "(.+?)(?=" + mapEntryKey + "|$)";
    ENTRY_PATTERN = Pattern.compile(mapEntryKey + mapEntryValue);
  }

  @Override
  protected Map<String, String> parse(final String value) {
    if (!StringUtility.hasText(value)) {
      return null;
    }

    final Map<String, String> map = new HashMap<>();
    final Matcher matcher = ENTRY_PATTERN.matcher(value);
    while (matcher.find()) {
      map.put(matcher.group(1), matcher.group(2));
    }
    return map;
  }
}
