/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
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
