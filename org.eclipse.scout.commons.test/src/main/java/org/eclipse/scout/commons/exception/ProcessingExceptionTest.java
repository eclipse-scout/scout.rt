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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import org.eclipse.scout.commons.status.IStatus;
import org.junit.Test;

/**
 * Simple tests for creating {@link ProcessingException}s
 */
public class ProcessingExceptionTest {
  private final String m_testMessageBody = "testMessage";
  private final String m_title = "title";

  @Test
  public void testCreateException() {
    final Throwable testThrowable = new Throwable("cause");
    final int severity = IProcessingStatus.FATAL;
    final int errorCode = -1;

    ProcessingException pe = new ProcessingException(m_testMessageBody, testThrowable, errorCode, severity);
    assertEquals(m_testMessageBody, pe.getMessage());
    assertEquals(testThrowable, pe.getCause());

    IStatus errorStatus = pe.getStatus();
    assertEquals(m_testMessageBody, errorStatus.getMessage());
    assertEquals(errorCode, errorStatus.getCode());
    assertEquals(severity, errorStatus.getSeverity());
  }

  @Test
  public void testDefaultStatus() {
    ProcessingException pe = new ProcessingException(m_title, m_testMessageBody);
    final IProcessingStatus s = pe.getStatus();
    assertEquals(IStatus.ERROR, s.getSeverity());
    assertEquals(m_testMessageBody, s.getBody());
    assertEquals(m_title, s.getTitle());
    assertEquals(String.format("%s\n%s", m_title, m_testMessageBody), s.getMessage());
  }

  @Test
  public void testStatusEmptyTitle() {
    ProcessingException p2 = new ProcessingException(m_testMessageBody);
    assertEquals(IStatus.ERROR, p2.getStatus().getSeverity());
    assertEquals(m_testMessageBody, p2.getMessage());
    assertEquals(m_testMessageBody, p2.getStatus().getMessage());
    assertEquals(m_testMessageBody, p2.getStatus().getBody());
    assertNull(p2.getStatus().getTitle());
    assertFalse(p2.getStatus().isOK());
  }

}
