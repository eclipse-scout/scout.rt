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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataDictionary implements Serializable {
  private static final long serialVersionUID = -485573072845041815L;
  private transient boolean m_valid = true;
  private int m_sourceType = 100;
  private String m_schema;
  private HashMap<String, TableDesc> m_tables;
  private HashMap<String, ViewDesc> m_views;
  private HashMap<String, SequenceDesc> m_sequences;

  // for IScoutSerializable
  private DataDictionary() {
  }

  public boolean isValid() {
    return m_valid;
  }

  public void invalidate() {
    m_valid = false;
  }

  public DataDictionary(String schema) {
    m_schema = schema.toUpperCase();
    m_tables = new HashMap<String, TableDesc>();
    m_views = new HashMap<String, ViewDesc>();
    m_sequences = new HashMap<String, SequenceDesc>();
  }

  public void setSourceType(int t) {
    m_sourceType = t;
  }

  public int getSourceType() {
    return m_sourceType;
  }

  public String getSchema() {
    return m_schema;
  }

  public List<TableDesc> getTables() {
    return new ArrayList<TableDesc>(m_tables.values());
  }

  public void addTable(TableDesc td) {
    m_tables.put(td.getName(), td);
  }

  public void removeTable(String name) {
    m_tables.remove(name);
  }

  /**
   * can be full table name or raw table name
   */
  public TableDesc getTable(String name) {
    // remove qualification
    if (name.startsWith(getSchema() + ".")) {
      name = name.substring(getSchema().length() + 1);
    }
    TableDesc td = m_tables.get(name);
    return td;
  }

  public List<ViewDesc> getViews() {
    return new ArrayList<ViewDesc>(m_views.values());
  }

  public void addView(ViewDesc vd) {
    m_views.put(vd.getName(), vd);
  }

  public ViewDesc getView(String name) {
    return m_views.get(name);
  }

  public List<SequenceDesc> getSequences() {
    return new ArrayList<SequenceDesc>(m_sequences.values());
  }

  public void addSequence(SequenceDesc sd) {
    m_sequences.put(sd.getName(), sd);
  }

  public SequenceDesc getSequence(String name) {
    return m_sequences.get(name);
  }

  public String toXml() {
    StringBuffer buf = new StringBuffer();
    buf.append("<datadictionary schema=\"" + m_schema + "\">\n");
    buf.append("<tables>\n");
    for (TableDesc td : m_tables.values()) {
      buf.append(td.toXml() + "\n");
    }
    buf.append("</tables>\n");
    buf.append("<views>\n");
    for (ViewDesc vd : m_views.values()) {
      buf.append(vd.toXml() + "\n");
    }
    buf.append("</views>\n");
    buf.append("<sequences>\n");
    for (SequenceDesc sd : m_sequences.values()) {
      buf.append(sd.toXml() + "\n");
    }
    buf.append("</sequences>\n");
    buf.append("</datadictionary>\n");
    return buf.toString();
  }

}
