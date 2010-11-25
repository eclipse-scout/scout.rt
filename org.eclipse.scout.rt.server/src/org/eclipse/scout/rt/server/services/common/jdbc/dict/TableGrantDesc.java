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

public class TableGrantDesc implements java.io.Serializable {
  private static final long serialVersionUID = -4877847488885163712L;
  private String m_grantee;
  private String m_type;
  private boolean m_grantOption;

  // for IScoutSerializable
  private TableGrantDesc() {
  }

  public TableGrantDesc(String grantee, String type, boolean grantOption) {
    m_grantee = grantee;
    m_type = type;
    m_grantOption = grantOption;
  }

  public String getGrantee() {
    return m_grantee;
  }

  public String getType() {
    return m_type;
  }

  public boolean withGrantOption() {
    return m_grantOption;
  }

  public String toXml() {
    StringBuffer buf = new StringBuffer();
    buf.append("<tablegrant");
    buf.append(" grantee=\"" + m_grantee + "\"");
    buf.append(" type=\"" + m_type + "\"");
    buf.append(" grantoption=\"" + (m_grantOption ? "yes" : "no") + "\"");
    buf.append("/>\n");
    return buf.toString();
  }

}
