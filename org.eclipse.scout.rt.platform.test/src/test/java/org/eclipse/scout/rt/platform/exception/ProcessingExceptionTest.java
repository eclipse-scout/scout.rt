/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.exception;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
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

    ProcessingException pe = new ProcessingException(m_body, testThrowable).withCode(errorCode).withSeverity(severity);
    assertEquals(m_body + " [severity=FATAL, code=-1]", pe.getMessage());
    assertEquals(m_body, pe.getDisplayMessage());
    assertEquals(testThrowable, pe.getCause());

    IStatus errorStatus = pe.getStatus();
    assertEquals(m_body, errorStatus.getMessage());
    assertEquals(errorCode, errorStatus.getCode());
    assertEquals(severity, errorStatus.getSeverity());
  }

  @Test
  public void testDefaultStatus() {
    ProcessingException pe = new ProcessingException(m_body).withTitle(m_title);
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
    assertEquals(m_body, p2.getDisplayMessage());
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
    final VetoException cause = new VetoException(m_body).withTitle(m_title);
    ProcessingException processingException = new ProcessingException(m_body, cause);
    processingException
        .withContextInfo("key1", "value1")
        .withContextInfo("key2", "value2")
        .consume();

    final String exText = processingException.toString();
    assertContainsExceptionAttributes(exText);
    assertFalse(exText.contains(m_title));
    assertTrue(exText.contains(m_body));
    assertFalse(exText.contains("VetoException"));
    assertTrue(exText.contains("key1=value1, key2=value2"));
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

  @Test
  public void testGetMessageDefaultConstructor() {
    ProcessingException processingException = new ProcessingException();
    assertEquals(new ProcessingStatus("undefined", processingException, 0, IStatus.ERROR), processingException.getStatus());
    assertEquals("undefined [severity=ERROR]", processingException.getMessage());
    assertEquals("undefined", processingException.getDisplayMessage());
  }

  @Test
  public void testGetMessageDefaultConstructorWithStatus() {
    ProcessingStatus status = new ProcessingStatus("title", "body", IStatus.ERROR);
    ProcessingException processingException = new ProcessingException().withStatus(status);
    assertEquals(status, processingException.getStatus());
    assertEquals("title: body [severity=ERROR]", processingException.getMessage());
    assertEquals("title: body", processingException.getDisplayMessage());
  }

  @Test
  public void testGetMessageStatusConstructor() {
    ProcessingStatus status = new ProcessingStatus("title", "body", IStatus.ERROR);
    ProcessingException processingException = new ProcessingException(status);
    assertEquals(status, processingException.getStatus());
    assertEquals("title: body [severity=ERROR]", processingException.getMessage());
    assertEquals("title: body", processingException.getDisplayMessage());
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
      if (trimmedLine.startsWith("at org.junit.")) {
        // junit runner chain
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
