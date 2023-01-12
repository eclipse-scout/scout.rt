/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataformat.ical.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  private List<Property> m_properties = new ArrayList<>();

  public List<Property> getProperties() {
    return m_properties;
  }

  public List<Property> getProperties(String propertyName) {
    List<Property> found = new ArrayList<>();
    for (Property property : m_properties) {
      if (property.getName().equals(propertyName)) {
        found.add(property);
      }
    }
    return found;
  }

  public Property getProperty(String propertyName) {
    for (Property property : m_properties) {
      if (property.getName().equals(propertyName)) {
        return property;
      }
    }
    return null;
  }

  public void addProperty(Property property) {
    if (property != null) {
      m_properties.add(property);
    }
  }
}
