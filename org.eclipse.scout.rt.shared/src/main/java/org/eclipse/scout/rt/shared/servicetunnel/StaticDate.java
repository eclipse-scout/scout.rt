/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.servicetunnel;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Static date representation as string, independent of TimeZone and daylight saving
 * <p>
 * {@link ServiceTunnelObjectReplacer}
 */
public class StaticDate implements Serializable {
  private static final long serialVersionUID = -3278051886953717195L;

  private final long m_textAsLong;

  public StaticDate(Date d) {
    Calendar cal = new GregorianCalendar();
    cal.setTime(d);
    long l = 0;
    l = cal.get(Calendar.YEAR);
    l = l * 100 + cal.get(Calendar.MONTH);
    l = l * 100 + cal.get(Calendar.DATE);
    l = l * 100 + cal.get(Calendar.HOUR_OF_DAY);
    l = l * 100 + cal.get(Calendar.MINUTE);
    l = l * 100 + cal.get(Calendar.SECOND);
    l = l * 1000 + cal.get(Calendar.MILLISECOND);
    m_textAsLong = l;
  }

  public Date getDate() {
    Calendar cal = new GregorianCalendar();
    long l = m_textAsLong;
    cal.set(Calendar.MILLISECOND, (int) (l % 1000));
    l = l / 1000;
    cal.set(Calendar.SECOND, (int) (l % 100));
    l = l / 100;
    cal.set(Calendar.MINUTE, (int) (l % 100));
    l = l / 100;
    cal.set(Calendar.HOUR_OF_DAY, (int) (l % 100));
    l = l / 100;
    cal.set(Calendar.DATE, (int) (l % 100));
    l = l / 100;
    cal.set(Calendar.MONTH, (int) (l % 100));
    l = l / 100;
    cal.set(Calendar.YEAR, (int) (l));
    return cal.getTime();
  }

  @Override
  public String toString() {
    return "StaticDate[" + m_textAsLong + "]";
  }
}
