/*******************************************************************************
 * Copyright (c) 2010, 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.basic.activitymap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Calendar;
import java.util.Date;

import org.eclipse.scout.commons.DateUtility;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link org.eclipse.scout.rt.client.ui.basic.activitymap.TimeScale}
 * 
 * @since 3.10.0-M6
 */
public class TimeScaleTest {

  private TimeScale m_timeScale = null;

  @Before
  public void setUp() {
    m_timeScale = new TimeScale();
  }

  @Test
  public void testGetRangeOfMajorTimeColumn() {
    Date now = Calendar.getInstance().getTime();
    MajorTimeColumn majorColumn = new MajorTimeColumn(m_timeScale);

    double[] result = m_timeScale.getRangeOf(majorColumn);
    assertNull(result);

    MinorTimeColumn minorColumn1 = new MinorTimeColumn(majorColumn, now, DateUtility.addHours(now, 1));
    m_timeScale.addMinorColumnNotify(majorColumn, minorColumn1);

    result = m_timeScale.getRangeOf(majorColumn);
    assertNotNull(result);
    assertEquals(2, result.length);
    assertEquals(0.0d, result[0], 0.0d);
    assertEquals(1.0d, result[1], 0.0d);

    MinorTimeColumn minorColumn2 = new MinorTimeColumn(majorColumn, DateUtility.addHours(now, 1), DateUtility.addHours(now, 2));
    m_timeScale.addMinorColumnNotify(majorColumn, minorColumn2);

    result = m_timeScale.getRangeOf(majorColumn);
    assertNotNull(result);
    assertEquals(2, result.length);
    assertEquals(0.0d, result[0], 0.0d);
    assertEquals(1.0d, result[1], 0.0d);

    // set minorColumn1 to null on timescale
    MinorTimeColumn[] minorColumns = m_timeScale.getMinorTimeColumns();
    assertNotNull(minorColumns);
    assertEquals(2, minorColumns.length);
    minorColumns[0] = null;
    result = m_timeScale.getRangeOf(majorColumn);
    assertNull(result);

    // minorColumn2 is null
    minorColumns[0] = minorColumn1;
    minorColumns[1] = null;
    result = m_timeScale.getRangeOf(majorColumn);
    assertNull(result);

    // both are null
    minorColumns[0] = null;
    result = m_timeScale.getRangeOf(majorColumn);
    assertNull(result);
  }
}
