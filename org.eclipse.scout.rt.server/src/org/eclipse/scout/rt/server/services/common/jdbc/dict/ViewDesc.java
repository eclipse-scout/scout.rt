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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

public class ViewDesc implements java.io.Serializable {
  private static final long serialVersionUID = 5293982375734275306L;
  private String m_name, m_owner;
  private String m_statement;
  private Collection<TableGrantDesc> m_grants;

  // for IScoutSerializable
  private ViewDesc() {
  }

  public ViewDesc(String name, String owner, String statement) {
    m_name = name;
    m_owner = owner;
    m_statement = statement;
    m_grants = new HashSet<TableGrantDesc>();
  }

  public String getName() {
    return m_name;
  }

  public String getOwner() {
    return m_owner;
  }

  public Collection<TableGrantDesc> getGrants() {
    return m_grants;
  }

  public void addGrant(TableGrantDesc gd) {
    m_grants.add(gd);
  }

  public String getFullName() {
    return m_owner + "." + m_name;
  }

  public String getStatement() {
    return m_statement;
  }

  public String toXml() {
    StringBuffer buf = new StringBuffer();
    buf.append("<view name=\"" + getName() + "\">\n");
    buf.append("<statement>\n");
    buf.append(m_statement);
    buf.append("</statement>\n");
    buf.append("<grants>\n");
    for (Iterator it = m_grants.iterator(); it.hasNext();) {
      TableGrantDesc gd = (TableGrantDesc) it.next();
      buf.append(gd.toXml());
    }
    buf.append("</grants>\n");
    buf.append("</view>\n");
    return buf.toString();
  }
}
