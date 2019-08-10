/*
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
    m_properties.add(new AbstractMap.SimpleEntry<String, String>(key, value));
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
