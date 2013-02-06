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

import java.util.ArrayList;
import java.util.Iterator;

public class PrimaryKeyDesc implements java.io.Serializable {
  private static final long serialVersionUID = 9164041758420662753L;
  private String m_name;
  private ArrayList<String> m_columnNames;

  // for IScoutSerializable
  private PrimaryKeyDesc() {
  }

  public PrimaryKeyDesc(String name) {
    m_name = name;
    m_columnNames = new ArrayList<String>();
  }

  public String getName() {
    return m_name;
  }

  public void addColumnName(String col) {
    m_columnNames.add(col);
  }

  public ArrayList/* of String */getColumnNames() {
    return m_columnNames;
  }

  public String toXml() {
    StringBuffer buf = new StringBuffer();
    buf.append("<primarykey name=\"" + m_name + "\">\n");
    for (Iterator it = m_columnNames.iterator(); it.hasNext();) {
      buf.append("<columnref name=\"" + it.next() + "\"/>\n");
    }
    buf.append("</primarykey>\n");
    return buf.toString();
  }
}
