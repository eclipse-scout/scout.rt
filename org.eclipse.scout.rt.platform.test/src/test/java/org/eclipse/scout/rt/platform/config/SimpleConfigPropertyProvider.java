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

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

public class SimpleConfigPropertyProvider implements IPropertyProvider {

  private final Object m_identifier;
  private List<Entry<String, String>> m_properties = new ArrayList<>();

  public SimpleConfigPropertyProvider(Object identifier) {
    m_identifier = identifier;
  }

  public SimpleConfigPropertyProvider withProperty(String key, String value) {
    m_properties.add(new AbstractMap.SimpleEntry<>(key, value));
    return this;
  }

  @Override
  public Object getPropertiesIdentifier() {
    return m_identifier;
  }

  @Override
  public List<Entry<String, String>> readProperties() {
    return m_properties;
  }
}
