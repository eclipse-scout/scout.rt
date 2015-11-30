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
import static org.junit.Assert.fail;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.StringUtility;
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
    assertEquals(m_body + " [severity=FATAL, code=-1]", pe.getMessage());
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
    assertEquals(String.format("%s: %s", m_title, m_body), s.getMessage());
  }

  @Test
  public void testStatusEmptyTitle() {
    ProcessingException p2 = new ProcessingException(m_body);
    assertEquals(IStatus.ERROR, p2.getStatus().getSeverity());
    assertEquals(m_body + " [severity=ERROR]", p2.getMessage());
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
    p.addContextMessage("context1");
    p.addContextMessage("context2");
    p.consume();
    final String exText = p.toString();
    assertContainsExceptionAttributes(exText);
    assertFalse(exText.contains(m_title));
    assertTrue(exText.contains(m_body));
    assertFalse(exText.contains("VetoException"));
    assertTrue(exText.contains("{context2, context1}"));
  }

  @Test
  public void testAddContextMessage1() {
    ProcessingException e = new ProcessingException("exception");
    e.addContextMessage("3");
    e.addContextMessage("2");
    e.addContextMessage("1");

    assertEquals(Arrays.asList("1", "2", "3"), e.getStatus().getContextMessages());
  }

  @Test
  public void testAddContextMessage2() {
    ProcessingException e = new ProcessingException("exception");
    e.addContextMessage("position=%s", 3);
    e.addContextMessage("position=%s", 2);
    e.addContextMessage("position=%s", 1);

    assertEquals(Arrays.asList("position=1", "position=2", "position=3"), e.getStatus().getContextMessages());
  }

  @Test
  public void testStackTraceNoCause() {
    ProcessingException pe = new ProcessingException();
    assertUniqueLinesOnStackTrace(pe);
  }

  @Test
  public void testStackTraceCausedByNullPointerException() {
    // processing exception references processing status and vice versa
    ProcessingException pe = new ProcessingException("message", new NullPointerException());
    assertUniqueLinesOnStackTrace(pe);
  }

  @Test
  public void testStackTraceCausedByAnotherProcessingException() {
    // pe1 references status of pe1 and vice versa
    // pe2 references status of pe2, but status of pe2 references pe1
    ProcessingException pe1 = new ProcessingException("pe1");
    ProcessingException pe2 = new ProcessingException("pe2", pe1);
    assertUniqueLinesOnStackTrace(pe2);
  }

  private void assertUniqueLinesOnStackTrace(Throwable t) {
    StringWriter writer = new StringWriter();
    PrintWriter pw = new PrintWriter(writer);
    t.printStackTrace(pw);
    String stackTrace = writer.toString();

    Set<String> processedLines = new HashSet<>();
    List<String> duplicateLines = new ArrayList<>();
    for (String line : StringUtility.getLines(stackTrace)) {
      String trimmedLine = StringUtility.trim(line);
      if (trimmedLine.startsWith("...")) {
        // ignore omitted line counter
        continue;
      }
      if (!processedLines.add(trimmedLine) && !duplicateLines.contains(trimmedLine)) {
        duplicateLines.add(trimmedLine);
      }
    }

    if (!duplicateLines.isEmpty()) {
      fail("given string contains duplicate entries:\n" + CollectionUtility.format(duplicateLines, "\n"));
    }
  }

  private void assertContainsExceptionAttributes(String exText) {
    assertTrue(exText.contains(m_body));
    assertTrue(exText.contains("ERROR"));
    assertFalse(exText.contains("0"));
  }
}
