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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.zip.CRC32;

public class TableDesc implements java.io.Serializable {
  private static final long serialVersionUID = -8904163472086670246L;
  private String m_name, m_owner;
  private String m_internalShortName;
  private HashMap<String, ColumnDesc> m_columns;
  private Collection<TableGrantDesc> m_grants;
  private Collection<IndexDesc> m_indices;
  private PrimaryKeyDesc m_pk;

  // for IScoutSerializable
  private TableDesc() {
  }

  public TableDesc(String name, String owner) {
    m_name = name;
    m_owner = owner;
    m_columns = new HashMap<String, ColumnDesc>();
    m_indices = new HashSet<IndexDesc>();
    m_grants = new HashSet<TableGrantDesc>();
    rebuildInternalShortName();
  }

  public String getName() {
    return m_name;
  }

  public String getOwner() {
    return m_owner;
  }

  public String getFullName() {
    return m_owner + "." + m_name;
  }

  /**
   * Short name, shorted and (if necessary) hashed to 25 characters (name limit
   * 30 chars - 5 = 25)
   */
  public String getInternalShortName() {
    return m_internalShortName;
  }

  public List<ColumnDesc> getColumns() {
    return new ArrayList<ColumnDesc>(m_columns.values());
  }

  public ColumnDesc getColumn(String name) {
    return m_columns.get(name);
  }

  public void addColumn(ColumnDesc cd) {
    m_columns.put(cd.getName(), cd);
  }

  public Collection<IndexDesc> getIndices() {
    return m_indices;
  }

  public void addIndex(IndexDesc id) {
    m_indices.add(id);
  }

  public Collection<TableGrantDesc> getGrants() {
    return m_grants;
  }

  public void addGrant(TableGrantDesc gd) {
    m_grants.add(gd);
  }

  public PrimaryKeyDesc getPrimaryKey() {
    return m_pk;
  }

  public void setPrimaryKey(PrimaryKeyDesc pk) {
    m_pk = pk;
  }

  public String toXml() {
    StringBuffer buf = new StringBuffer();
    buf.append("<table name=\"" + getName() + "\">\n");
    buf.append("<columns>\n");
    for (Iterator it = m_columns.values().iterator(); it.hasNext();) {
      ColumnDesc cd = (ColumnDesc) it.next();
      buf.append(cd.toXml());
    }
    buf.append("</columns>\n");
    if (m_pk != null) {
      buf.append(m_pk.toXml());
    }
    buf.append("<indexes>\n");
    for (Iterator it = m_indices.iterator(); it.hasNext();) {
      IndexDesc id = (IndexDesc) it.next();
      buf.append(id.toXml());
    }
    buf.append("</indexes>\n");
    buf.append("<grants>\n");
    for (Iterator it = m_grants.iterator(); it.hasNext();) {
      TableGrantDesc gd = (TableGrantDesc) it.next();
      buf.append(gd.toXml());
    }
    buf.append("</grants>\n");
    buf.append("</table>\n");
    return buf.toString();
  }

  private void rebuildInternalShortName() {
    m_internalShortName = createInternalShortName(m_name);
  }

  public static String createInternalShortName(String name) {
    String s = name;
    if (s.length() > 25) {
      CRC32 crc = new CRC32();
      crc.update(s.getBytes());
      // too long, shorten to 25-4 characters (21 name and 4 hashcode)
      s = s.substring(0, 21) + Integer.toHexString((int) (crc.getValue() & 0xffff));
    }
    return s;
  }

  public static String removeOwner(String fullName) {
    String s = fullName;
    int i = s.indexOf('.');
    if (i >= 0) {
      s = s.substring(i + 1);
    }
    return s;
  }

}
