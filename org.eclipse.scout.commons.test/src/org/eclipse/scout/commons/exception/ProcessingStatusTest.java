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
package org.eclipse.scout.commons.exception;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.scout.commons.status.IStatus;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link ProcessingStatus}
 */
public class ProcessingStatusTest {

  private IProcessingStatus m_infoStatus;
  private IProcessingStatus m_warningStatus;
  private IProcessingStatus m_errorStatus;
  private IProcessingStatus m_cancelStatus;
  private IProcessingStatus m_fatalStatus;
  private IProcessingStatus m_okStatus;

  @Before
  public void setup() {
    m_infoStatus = new ProcessingStatus("", IStatus.INFO);
    m_warningStatus = new ProcessingStatus("", IStatus.WARNING);
    m_errorStatus = new ProcessingStatus("", IStatus.ERROR);
    m_cancelStatus = new ProcessingStatus("", IProcessingStatus.CANCEL);
    m_fatalStatus = new ProcessingStatus("", IProcessingStatus.FATAL);
    m_okStatus = new ProcessingStatus("", IProcessingStatus.OK);
  }

  /**
   * Tests {@link ProcessingStatus#getSeverity()} ordering
   */
  @Test
  public void testSeverityOrder() {
    assertTrue(m_infoStatus.getSeverity() < m_warningStatus.getSeverity());
    assertTrue(m_warningStatus.getSeverity() < m_errorStatus.getSeverity());
    assertTrue(m_infoStatus.getSeverity() < m_errorStatus.getSeverity());
    assertTrue(m_cancelStatus.getSeverity() < m_warningStatus.getSeverity());
    assertTrue(m_errorStatus.getSeverity() < m_fatalStatus.getSeverity());
    assertTrue(m_okStatus.getSeverity() < m_infoStatus.getSeverity());
  }

  @Test
  public void testMatches() {
    assertMatchesOnly(m_infoStatus, IStatus.INFO, getKnownStatuses());
    assertMatchesOnly(m_warningStatus, IStatus.WARNING, getKnownStatuses());
    assertMatchesOnly(m_errorStatus, IStatus.ERROR, getKnownStatuses());
    assertMatchesOnly(m_fatalStatus, IProcessingStatus.FATAL, getKnownStatuses());
    assertMatchesOnly(m_cancelStatus, IProcessingStatus.CANCEL, getKnownStatuses());
    assertMatchesOnly(m_okStatus, IProcessingStatus.OK, getKnownStatuses());
  }

  private int[] getKnownStatuses() {
    return new int[]{IStatus.INFO, IStatus.ERROR, IStatus.WARNING, IProcessingStatus.FATAL, IProcessingStatus.CANCEL, IProcessingStatus.OK};
  }

  private void assertMatchesOnly(IStatus status, int match, int[] allStatuses) {
    assertTrue(status.matches(match));
    int acc = match;
    for (int s : allStatuses) {
      if (s != match) {
        assertFalse(String.format("Status %s should not match %s", status, s), status.matches(s));
        assertTrue(status.matches(s | match));
        acc = s | match;
        assertTrue(status.matches(acc));
      }
    }
  }

}
