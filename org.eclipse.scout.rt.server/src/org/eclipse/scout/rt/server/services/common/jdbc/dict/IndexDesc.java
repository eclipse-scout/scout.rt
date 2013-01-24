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

public class IndexDesc implements java.io.Serializable {
  private static final long serialVersionUID = 8473853730094122830L;
  private String m_indexName;
  private boolean m_uniqueness;
  private ArrayList<String> m_columnNames;

  // for IScoutSerializable
  private IndexDesc() {
  }

  public IndexDesc(String indexName, boolean uniqueness) {
    m_indexName = indexName;
    m_uniqueness = uniqueness;
    m_columnNames = new ArrayList<String>();
  }

  public String getName() {
    return m_indexName;
  }

  public boolean isUnique() {
    return m_uniqueness;
  }

  public ArrayList/* of String */getColumnNames() {
    return m_columnNames;
  }

  public void addColumnName(String s) {
    m_columnNames.add(s);
  }

  public String toXml() {
    StringBuffer buf = new StringBuffer();
    buf.append("<index name=\"" + m_indexName + "\" uniqueness=\"" + m_uniqueness + "\">\n");
    for (Iterator it = m_columnNames.iterator(); it.hasNext();) {
      buf.append("<columnref name=\"" + it.next() + "\"/>\n");
    }
    buf.append("</index>\n");
    return buf.toString();
  }

}
