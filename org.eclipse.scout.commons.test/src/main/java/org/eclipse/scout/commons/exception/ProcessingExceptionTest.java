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
import static org.junit.Assert.assertTrue;

import org.eclipse.scout.commons.status.IStatus;
import org.junit.Test;

/**
 * Simple tests for creating {@link ProcessingException}s
 */
public class ProcessingExceptionTest {
  private final String m_body = "testMessage";
  private final String m_title = "title";

  @Test
  public void testCreateException() {
    final Throwable testThrowable = new Throwable("cause");
    final int severity = IProcessingStatus.FATAL;
    final int errorCode = -1;

    ProcessingException pe = new ProcessingException(m_body, testThrowable, errorCode, severity);
    assertEquals(m_body, pe.getMessage());
    assertEquals(testThrowable, pe.getCause());

    IStatus errorStatus = pe.getStatus();
    assertEquals(m_body, errorStatus.getMessage());
    assertEquals(errorCode, errorStatus.getCode());
    assertEquals(severity, errorStatus.getSeverity());
  }

  @Test
  public void testDefaultStatus() {
    ProcessingException pe = new ProcessingException(m_title, m_body);
    final IProcessingStatus s = pe.getStatus();
    assertEquals(IStatus.ERROR, s.getSeverity());
    assertEquals(m_body, s.getBody());
    assertEquals(m_title, s.getTitle());
    assertEquals(String.format("%s\n%s", m_title, m_body), s.getMessage());
  }

  @Test
  public void testStatusEmptyTitle() {
    ProcessingException p2 = new ProcessingException(m_body);
    assertEquals(IStatus.ERROR, p2.getStatus().getSeverity());
    assertEquals(m_body, p2.getMessage());
    assertEquals(m_body, p2.getStatus().getMessage());
    assertEquals(m_body, p2.getStatus().getBody());
    assertNull(p2.getStatus().getTitle());
    assertFalse(p2.getStatus().isOK());
  }

  @Test
  public void testToString() {
    ProcessingException p = new ProcessingException(m_body);
    assertContainsExceptionAttributes(p.toString());
  }

  @Test
  public void testToStringWithCause() {
    final VetoException cause = new VetoException(m_title, m_body);
    ProcessingException p = new ProcessingException(m_body, cause);
    final String context = "context";
    p.addContextMessage(context);
    p.consume();
    final String exText = p.toString();
    assertContainsExceptionAttributes(exText);
    assertTrue(exText.contains(m_title));
    assertTrue(exText.contains(m_body));
    assertTrue(exText.contains("VetoException"));
    assertTrue(exText.contains("consumed"));
    assertTrue(exText.contains(context));
  }

  private void assertContainsExceptionAttributes(String exText) {
    assertTrue(exText.contains(m_body));
    assertTrue(exText.contains("ERROR"));
    assertTrue(exText.contains("0"));
  }

}
