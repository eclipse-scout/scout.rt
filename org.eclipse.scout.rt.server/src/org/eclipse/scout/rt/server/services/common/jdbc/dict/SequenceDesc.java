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

import java.math.BigDecimal;

public class SequenceDesc implements java.io.Serializable {
  private static final long serialVersionUID = -4529107990427422020L;
  private String m_name;
  private long m_min;
  private long m_max;
  private long m_increment;

  // for IScoutSerializable
  private SequenceDesc() {
  }

  public SequenceDesc(String name, BigDecimal min, BigDecimal max, BigDecimal increment) {
    m_name = name;
    m_min = checkLongRange(min);
    m_max = checkLongRange(max);
    m_increment = checkLongRange(increment);
  }

  public SequenceDesc(String name, long min, long max, long increment) {
    m_name = name;
    m_min = min;
    m_max = max;
    m_increment = increment;
  }

  public String getName() {
    return m_name;
  }

  public long getMin() {
    return m_min;
  }

  public long getMax() {
    return m_max;
  }

  public long getIncrement() {
    return m_increment;
  }

  public String toXml() {
    StringBuffer buf = new StringBuffer();
    buf.append("<sequence");
    buf.append(" name=\"" + m_name + "\"");
    if (m_min != 0) buf.append(" min=\"" + m_min + "\"");
    if (m_max != 0) buf.append(" max=\"" + m_max + "\"");
    if (m_increment != 0) buf.append(" increment=\"" + m_increment + "\"");
    buf.append("/>\n");
    return buf.toString();
  }

  private long checkLongRange(BigDecimal d) {
    if (d != null && d.compareTo(BigDecimal.valueOf(0)) >= 0 && d.compareTo(BigDecimal.valueOf(Long.MAX_VALUE)) <= 0) return d.longValue();
    return 0;
  }

}
