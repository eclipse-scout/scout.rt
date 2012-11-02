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

public class OracleCongruencyFilter extends CongruencyFilter {
  protected int m_minVersion;
  protected int m_majVersion;
  protected boolean m_light;

  public OracleCongruencyFilter(int majVersion, int minVersion, boolean light) {
    m_majVersion = majVersion;
    m_minVersion = minVersion;
    m_light = light;
  }

  @Override
  public boolean allowDropTableColumn() {
    return m_majVersion >= 8;
  }

  @Override
  public String getCanonicalColumnType(ColumnDesc cd) {
    String type = cd.getTypeName().toUpperCase();
    // bugs in oracle are corrected first
    if (type.equals("DOUBLE")) {
      type = "DOUBLE PRECISION";
    }
    // map column type names to canonical names
    if (type.equals("DATE") || type.equals("TIME") || type.equals("TIMESTAMP")) {
      type = "DATE";
    }
    else if (type.equals("INT") || type.equals("INTEGER")) {
      type = "INT";
    }
    // do not use m_size even though oracle reports one for the following types
    if (type.equals("BIT") ||
        type.equals("BLOB") ||
        type.equals("CLOB") ||
        type.equals("DATE") ||
        type.equals("DOUBLE PRECISION") ||
        type.equals("LONG") ||
        type.equals("LONG RAW") ||
        type.equals("REAL") ||
        type.equals("ROWID") ||
        type.equals("INT") ||
        type.equals("BIGINT") ||
        type.equals("SMALLINT") ||
        type.equals("TINYINT")) {
      return type;
    }
    // for DECIMAL and NUMBER use only presicion and decimalDigits
    if (type.equals("NUMBER") ||
        type.equals("DECIMAL")) {
      long prec = (cd.getPrecision() > 0) ? cd.getPrecision() : 38;
      if (cd.getDecimalDigits() > 0) {
        return type + "(" + prec + "," + cd.getDecimalDigits() + ")";
      }
      else {
        return type + "(" + prec + ")";
      }
    }
    // generic, if size <>0 then use size, if also decimaldigits <>0 then also
    // use that parameter
    if (cd.getPrecision() > 0) {
      if (cd.getDecimalDigits() > 0) {
        return type + "(" + cd.getPrecision() + "," + cd.getDecimalDigits() + ")";
      }
      else {
        return type + "(" + cd.getPrecision() + ")";
      }
    }
    if (cd.getSize() > 0) {
      return type + "(" + cd.getSize() + ")";
    }
    // return raw type
    return type;
  }

}
