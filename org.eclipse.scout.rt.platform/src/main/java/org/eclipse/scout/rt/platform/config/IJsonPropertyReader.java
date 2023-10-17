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

import java.util.Map;

import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * This interface allows for the integration different implementations into the reading process of property maps being
 * defined as JSON strings.
 */
@ApplicationScoped
public interface IJsonPropertyReader {

  /**
   * This method receives a JSON string and is expected to return a Map holding the JSON's attribute names as keys and
   * their respective values in their string representation. In addition, <code>null</code> values in the JSON object
   * must be preserved as Java <code>null</code> values. Values which are JSON objects or arrays must be preserved in
   * their string representation.<br>
   * Consumers of this method should catch {@link RuntimeException} in order to handle errors that occur while parsing
   * the provided String as a JSON object.<br>
   * Although {@link PropertiesHelper} will never call this method with a <code>null</code> or empty string argument,
   * implementers are still expected to handle these cases as follows:
   * <ul>
   * <li><code>null</code> > <code>null</code></li>
   * <li>empty string > empty map
   * </ul>
   * Calls with a value not representing a JSON map must fail, e.g. arguments such as: a, true, &quot;a&quot;. This is
   * required to support reading a list property with only a single entry, in which case the reading of the JSON map
   * must fail to use the fallback instead.
   * <p>
   * Example:
   *
   * <pre>
   * {
   *     "key1": "value1",
   *     "key2": null,
   *     "key3": "",
   *     "key4": 1,
   *     "key5": "1",
   *     "key6": true,
   *     "key7": {"key71": "value71", "key72": "value72", "key73": "value73"},
   *     "key8": ["value81", "value82", "value83"]
   * }
   * </pre>
   *
   * must yield the following Map
   *
   * <pre>
   * [
   *
   *     "key1": "value1",
   *     "key2": null,
   *     "key3": "",
   *     "key4": "1",
   *     "key5": "1"
   *     "key6": "true",
   *     "key7": "{\"key71\": \"value71\", \"key72\": \"value72\", \"key73\": \"value73\"}",
   *     "key8": "[\"value81\", \"value82\", \"value83\"]"
   * ]
   * </pre>
   *
   * @param propertyValue
   *          The JSON string to parse into a {@link Map}. May be <code>null</code> or an empty string.
   * @throws RuntimeException
   *           Thrown in case of errors that occur while parsing the provided string as a JSON object (or anything
   *           else).
   */
  Map<String, String> readJsonPropertyValue(String propertyValue);
}
