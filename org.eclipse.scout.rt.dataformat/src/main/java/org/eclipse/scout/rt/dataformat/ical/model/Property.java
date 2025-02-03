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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.StringUtility;

public class Property implements Serializable {
  private static final long serialVersionUID = 1L;

  private final String m_name;
  private final Map<String, PropertyParameter> m_parameters = new HashMap<>();
  private String m_value;

  /**
   * @param name
   *     Property name, not case sensitive
   */
  public Property(String name) {
    this(name, null, null);
  }

  /**
   * @param name
   *     Property name, not case sensitive
   * @param value
   *     case sensitive
   */
  public Property(String name, String value) {
    this(name, null, value);
  }

  /**
   * @param name
   *     Property name, not case sensitive (stored upper case)
   * @param parameters
   *     the order in which this parameters are added is important, as a new parameter with the same name as the
   *     existing one will overwrite the existing one
   * @param value
   *     case sensitive
   */
  public Property(String name, List<PropertyParameter> parameters, String value) {
    super();
    Assertions.assertNotNull(name); // name must not be null
    m_name = name.toUpperCase();
    if (parameters != null) {
      addParameters(parameters);
    }
    m_value = StringUtility.emptyIfNull(value);
  }

  public Collection<PropertyParameter> getParameters() {
    return m_parameters.values();
  }

  public PropertyParameter getParameter(String name) {
    return m_parameters.get(name);
  }

  public void addParameters(PropertyParameter... propertyParameters) {
    addParameters(Arrays.asList(propertyParameters));
  }

  /**
   * @param propertyParameters
   *     if a parameter already exists, all parameter values that are not already added will be added. E.g. adding
   *     "TYPE=dom" and then adding "TYPE=postal" is equivalent to adding "TYPE=dom,postal"
   */
  public void addParameters(List<PropertyParameter> propertyParameters) {
    if (propertyParameters != null) {
      for (PropertyParameter parameter : propertyParameters) {
        if (m_parameters.containsKey(parameter.getName())) {
          PropertyParameter existingParameter = m_parameters.get(parameter.getName());
          existingParameter.addValues(parameter.getValueSet().toArray(new String[0]));
        }
        else {
          m_parameters.put(parameter.getName(), parameter);
        }
      }
    }
  }

  public boolean hasParameter(String name) {
    return m_parameters.containsKey(name);
  }

  public boolean hasParameter(PropertyParameter parameter) {
    if (parameter == null) {
      return false;
    }
    if (parameter.getValue() == null) {
      return m_parameters.containsKey(parameter.getName());
    }
    PropertyParameter p = m_parameters.get(parameter.getName());
    if (p == null) {
      return false;
    }
    return p.getValueSet().containsAll(parameter.getValueSet());
  }

  public String getValue() {
    return m_value;
  }

  public void setValue(String value) {
    m_value = value;
  }

  public String getName() {
    return m_name;
  }

  public boolean equalsName(Property p) {
    if (p == null) {
      return false;
    }
    return m_name.equals(p.getName());
  }

  public boolean equalsNameParameters(Property p) {
    if (p == null) {
      return false;
    }
    if (!equalsName(p)) {
      return false;
    }
    if (p.getParameters().size() == 0) {
      return m_parameters.isEmpty();
    }
    if (p.getParameters().size() != m_parameters.size()) {
      return false;
    }
    for (PropertyParameter param : p.getParameters()) {
      if (!param.equals(m_parameters.get(param.getName()))) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o == null) {
      return false;
    }
    if (getClass() != o.getClass()) {
      return false;
    }

    Property p = (Property) o;
    if (!equalsNameParameters(p)) {
      return false;
    }
    if (m_value != null && !m_value.equals(p.m_value)) {
      return false;
    }
    if (m_value == null && p.m_value != null) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
    for (PropertyParameter prop : getParameters()) {
      result += prop.hashCode();
    }
    result += ((m_value == null) ? 0 : m_value.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return "Property [m_name=" + m_name + ", m_parameters=" + m_parameters + ", m_value=" + m_value + "]";
  }
}
