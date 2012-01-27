/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.services.common.jdbc.dict;

public class ColumnDesc implements java.io.Serializable {
  private static final long serialVersionUID = 596863413691696972L;

  private String m_name;
  private String m_typeName;
  private long m_size;
  private long m_precision;
  private long m_decimalDigits;
  private boolean m_nullable;
  private String m_defaultValue;

  // for IScoutSerializable
  private ColumnDesc() {
  }

  public ColumnDesc(String name, String typeName, long size, long precision, long decimalDigits, boolean isNullable) {
    this(name, typeName, size, precision, decimalDigits, isNullable, null);
  }

  public ColumnDesc(String name, String typeName, long size, long precision, long decimalDigits, boolean isNullable, String defaultValue) {
    m_name = name;
    m_typeName = typeName;
    m_size = size;
    m_precision = precision;
    m_decimalDigits = decimalDigits;
    m_nullable = isNullable;
    if (defaultValue != null) {
      m_defaultValue = defaultValue.trim();
    }
  }

  public String getName() {
    return m_name;
  }

  public String getTypeName() {
    return m_typeName;
  }

  public long getSize() {
    return m_size;
  }

  public long getPrecision() {
    return m_precision;
  }

  public long getDecimalDigits() {
    return m_decimalDigits;
  }

  public boolean isNullable() {
    return m_nullable;
  }

  public String getDefaultValue() {
    return m_defaultValue;
  }

  public String toXml() {
    StringBuffer buf = new StringBuffer();
    buf.append("<column");
    buf.append(" name=\"" + m_name + "\"");
    buf.append(" type=\"" + m_typeName + "\"");
    buf.append(" nullable=\"" + (m_nullable ? "yes" : "no") + "\"");
    if (m_size != 0) {
      buf.append(" size=\"" + m_size + "\"");
    }
    if (m_precision != 0) {
      buf.append(" precision=\"" + m_precision + "\"");
    }
    if (m_decimalDigits != 0) {
      buf.append(" decimaldigits=\"" + m_decimalDigits + "\"");
    }
    if (m_defaultValue != null) {
      buf.append(" defaultValue=\"" + m_defaultValue + "\"");
    }
    buf.append("/>\n");
    return buf.toString();
  }
}
