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
import java.util.HashSet;

import org.eclipse.scout.rt.platform.util.Assertions;

public class PropertyParameter implements Serializable {
  private static final long serialVersionUID = 1L;

  public static final String PARAM_CHARSET = "CHARSET";
  public static final String PARAM_ENCODING = "ENCODING";
  public static final String PARAM_TYPE = "TYPE";

  /**
   * Special treatment for Microsoft Outlook (2007, 2010), which can not read UTF-8 vCards otherwise.<br/>
   * NOTE: Value ("utf-8") MUST be in lower case. Outlook is too "unclever" to recognize "UTF-8" as the same. <br/>
   * <br/>
   * Also note that this property parameter is deprecated according to the vCard RFC.
   */
  public static final PropertyParameter CHARSET_UTF_8_FOR_OUTLOOK = new PropertyParameter(PARAM_CHARSET, "utf-8");

  public static final PropertyParameter ENCODING_BASE64 = new PropertyParameter(PARAM_ENCODING, "BASE64");
  public static final PropertyParameter ENCODING_QUOTED_PRINTABLE = new PropertyParameter(PARAM_ENCODING, "QUOTED-PRINTABLE");

  public static final PropertyParameter TYPE_TEXT = new PropertyParameter(PARAM_TYPE, "TEXT");
  public static final PropertyParameter TYPE_JPEG = new PropertyParameter(PARAM_TYPE, "JPEG");

  public static final PropertyParameter VALUE_BINARY = new PropertyParameter("VALUE", "BINARY");

  private final String m_name;
  private final HashSet<String> m_values = new HashSet<>();

  /**
   * @param name
   *     of the property, stored upper case
   * @param values
   *     values of the property, stored in the given case
   */
  public PropertyParameter(String name, String... values) {
    Assertions.assertNotNull(name); // name must not be null
    m_name = name.toUpperCase();
    addValues(values);
  }

  public void addValues(String... values) {
    if (values != null) {
      for (String v : values) {
        if (v != null) {
          m_values.add(v);
        }
      }
    }
  }

  public String getName() {
    return m_name;
  }

  public HashSet<String> getValueSet() {
    return m_values;
  }

  /**
   * @return a string containing all values, comma separated. The order of the values in the string may be arbitrary.
   */
  public String getValue() {
    if (m_values.isEmpty()) {
      return null;
    }
    StringBuilder sb = new StringBuilder();
    for (String s : m_values) {
      sb.append(",");
      sb.append(s);
    }
    String tmp = sb.toString();
    return tmp.substring(1);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
    for (String str : getValueSet()) {
      result += str.hashCode();
    }
    return result;
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

    PropertyParameter p = (PropertyParameter) o;
    if (!m_name.equals(p.getName())) {
      return false;
    }
    if ((getValue() == null) != (p.getValue() == null)) {
      return false;
    }
    // both values are null or not null
    if (getValueSet() != null && (getValueSet().size() != p.getValueSet().size())) {
      return false;
    }
    // boths sets are of the same size
    if ((getValueSet() != null) && !getValueSet().containsAll(p.getValueSet())) {
      return false;
    }
    // each value of this is contained in the values of p
    return true;
  }

  @Override
  public String toString() {
    return "PropertyParameter [m_name=" + m_name + ", m_value=" + getValue() + "]";
  }
}
