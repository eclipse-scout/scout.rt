/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.exception;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.scout.rt.platform.status.IStatus;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link ProcessingStatus}
 */
public class ProcessingStatusTest {

  private IProcessingStatus m_infoStatus;
  private IProcessingStatus m_warningStatus;
  private IProcessingStatus m_errorStatus;
  private IProcessingStatus m_fatalStatus;
  private IProcessingStatus m_okStatus;

  private static final String BODY = "testMessage";
  private static final String TITLE = "title";

  @Before
  public void setup() {
    m_infoStatus = new ProcessingStatus("", IStatus.INFO);
    m_warningStatus = new ProcessingStatus("", IStatus.WARNING);
    m_errorStatus = new ProcessingStatus("", IStatus.ERROR);
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
    assertTrue(m_errorStatus.getSeverity() < m_fatalStatus.getSeverity());
    assertTrue(m_okStatus.getSeverity() < m_infoStatus.getSeverity());
  }

  @Test
  public void testMatches() {
    assertMatchesOnly(m_infoStatus, IStatus.INFO, getKnownStatuses());
    assertMatchesOnly(m_warningStatus, IStatus.WARNING, getKnownStatuses());
    assertMatchesOnly(m_errorStatus, IStatus.ERROR, getKnownStatuses());
    assertMatchesOnly(m_fatalStatus, IProcessingStatus.FATAL, getKnownStatuses());
    assertMatchesOnly(m_okStatus, IProcessingStatus.OK, getKnownStatuses());
  }

  @Test
  public void testToString() {
    final String title = "title";
    final String body = "body";
    final String exceptionMessage = "ex";
    assertTrue(m_infoStatus.toString().contains("INFO"));
    String fullStatus = new ProcessingStatus(title, body, new ProcessingException(exceptionMessage), 0, IProcessingStatus.FATAL).toString();
    assertTrue(fullStatus.contains(title));
    assertTrue(fullStatus.contains(body));
  }

  @Test
  public void testToStringWithCause() {
    final String exceptionName = "NPE";
    final int code = 22;
    final ProcessingStatus ps = new ProcessingStatus(TITLE, BODY, new NullPointerException(exceptionName), code, IStatus.INFO);
    final String psString = ps.toString();
    assertContainsStatusStrings(code, psString);
    assertTrue(psString.contains("NullPointerException"));
    assertTrue(psString.contains(exceptionName));
  }

  @Test
  public void testToStringWithProcessingException() {
    final int code = 22;
    final String exceptionMessage = "ex";
    final ProcessingException pe = new ProcessingException(exceptionMessage);
    final ProcessingStatus ps = new ProcessingStatus(TITLE, BODY, pe, code, IStatus.INFO);
    final String psString = ps.toString();
    assertContainsStatusStrings(code, psString);
    assertContainsExceptionStrings(exceptionMessage, psString);
  }

  @Test
  public void testToStringWithProcessingExceptionStatus() {
    final int code = 22;
    final String exceptionMessage = "ex";
    final String innerStatusMessage = "innerStatus";
    final ProcessingException pe = new ProcessingException(exceptionMessage, new ProcessingException(new ProcessingStatus(innerStatusMessage, IStatus.OK))).withTitle("ExTitle");
    final ProcessingStatus ps = new ProcessingStatus(TITLE, BODY, pe, code, IStatus.INFO);
    final String psString = ps.toString();
    assertContainsStatusStrings(code, psString);
    assertContainsExceptionStrings(exceptionMessage, psString);
    assertFalse(psString.contains(innerStatusMessage));
    assertTrue(psString.contains("INFO"));
  }

  private void assertContainsStatusStrings(final int code, final String psString) {
    assertTrue(psString.contains(TITLE));
    assertTrue(psString.contains(BODY));
    assertTrue(psString.contains("" + code));
    assertTrue(psString.contains("INFO"));
  }

  private void assertContainsExceptionStrings(final String exceptionMessage, final String psString) {
    assertTrue(psString.contains("ProcessingException"));
    assertTrue(psString.contains(exceptionMessage));
  }

  private int[] getKnownStatuses() {
    return new int[]{IStatus.INFO, IStatus.ERROR, IStatus.WARNING, IProcessingStatus.FATAL, IProcessingStatus.OK};
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
