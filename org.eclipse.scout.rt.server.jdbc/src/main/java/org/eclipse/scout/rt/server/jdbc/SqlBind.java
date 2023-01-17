/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.jdbc;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Types;

public class SqlBind {
  private final int m_sqlType;
  private final Object m_value;

  public SqlBind(int sqlType, Object value) {
    m_sqlType = sqlType;
    m_value = value;
  }

  public Object getValue() {
    return m_value;
  }

  public int getSqlType() {
    return m_sqlType;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + decodeJdbcType(m_sqlType) + " " + m_value + "]";
  }

  public static String decodeJdbcType(int i) {
    try {
      Field[] fields = Types.class.getFields();
      for (Field f : fields) {
        int flags = f.getModifiers();
        if (Modifier.isPublic(flags) && Modifier.isStatic(flags) && Modifier.isFinal(flags) && ((Integer) f.get(null)) == i) {
          return f.getName();
        }
      }
    }
    catch (Throwable t) { // NOSONAR
      // nop
    }
    return "type=" + i;
  }
}
