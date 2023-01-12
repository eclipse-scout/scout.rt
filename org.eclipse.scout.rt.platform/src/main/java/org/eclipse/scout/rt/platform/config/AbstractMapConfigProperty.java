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

/**
 * A config property which represents a {@link Map}.
 * <p>
 *
 * <pre>
 * my-map-property[map-key-01]=value-01
 * my-map-property[map-key-02]=value-02
 * my-map-property[map-key-03]=value-03
 * </pre>
 *
 * @see PropertiesHelper
 */
public abstract class AbstractMapConfigProperty extends AbstractConfigProperty<Map<String, String>, Map<String, String>> {

  @Override
  public Map<String, String> readFromSource(String namespace) {
    return ConfigUtility.getPropertyMap(getKey(), null, namespace);
  }

  @Override
  protected Map<String, String> parse(Map<String, String> value) {
    return value;
  }
}
