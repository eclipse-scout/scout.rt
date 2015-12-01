/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.status;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.scout.rt.platform.IOrdered;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.status.Status;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link Status}
 */
public class StatusTest {

  private IStatus m_infoStatus;
  private IStatus m_warningStatus;

  @Before
  public void setup() {
    m_infoStatus = new Status(IStatus.INFO);
    m_warningStatus = new Status(IStatus.WARNING);
  }

  @Test
  public void testStatusOrder() {
    assertEquals(IOrdered.DEFAULT_ORDER, m_infoStatus.getOrder(), 0.00001);
  }

  @Test
  public void testStatusCompare() {
    TestStatus highPrioError = new TestStatus();
    assertTrue(m_warningStatus.compareTo(m_infoStatus) < 0);
    assertTrue(highPrioError.compareTo(new Status(IStatus.ERROR)) < 0);
  }

  @Order(10)
  class TestStatus extends Status {
    private static final long serialVersionUID = 1L;
  }

  @Order(20)
  class TestStatus2 extends Status {
    private static final long serialVersionUID = 1L;
  }

}
