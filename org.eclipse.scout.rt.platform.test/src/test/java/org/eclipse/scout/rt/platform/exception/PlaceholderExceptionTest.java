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
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.eclipse.scout.rt.platform.exception.fixture.CustomProcessingException;
import org.eclipse.scout.rt.platform.exception.fixture.CustomProcessingExceptionWithStringConstructor;
import org.eclipse.scout.rt.platform.exception.fixture.CustomProcessingStatus;
import org.ietf.jgss.GSSException;
import org.junit.Test;
import org.xml.sax.SAXNotRecognizedException;

/**
 * JUnit tests for {@link PlaceholderException}
 */
public class PlaceholderExceptionTest {

  private static final String TITLE = "title";
  private static final String MESSAGE = "message";

  @Test
  public void testNull() {
    assertNull(PlaceholderException.transformException(null));
  }

  @Test
  public void testStringConstructorPkgJava() {
    NullPointerException e = new NullPointerException(MESSAGE);
    Throwable t = PlaceholderException.transformException(e);
    assertEqualExceptionAndCause(e, t);
  }

  @Test
  public void testCauseConstructorPkgJava() {
    NullPointerException cause = new NullPointerException();
    IllegalArgumentException e = new IllegalArgumentException(cause);
    Throwable t = PlaceholderException.transformException(e);
    assertEqualExceptionAndCause(e, t);
  }

  @Test
  public void testStringCauseConstructorPkgJava() {
    NullPointerException cause = new NullPointerException();
    IllegalArgumentException e = new IllegalArgumentException(MESSAGE, cause);
    Throwable t = PlaceholderException.transformException(e);
    assertEqualExceptionAndCause(e, t);
  }

  @Test
  public void testDefaultConstructorNonPkgJava() {
    GSSException e = new GSSException(15);
    Throwable t = PlaceholderException.transformException(e);
    assertEqualExceptionAndCause(new PlaceholderException(e, null), t);
  }

  @Test
  public void testStringConstructorNonPkgJava() {
    SAXNotRecognizedException e = new SAXNotRecognizedException(MESSAGE);
    Throwable t = PlaceholderException.transformException(e);
    assertEqualExceptionAndCause(new PlaceholderException(e, null), t);
  }

  @Test
  public void testProcessingException() {
    ProcessingException e = new ProcessingException(MESSAGE).withTitle(TITLE);
    Throwable t = PlaceholderException.transformException(e);
    assertEqualExceptionAndCause(e, t);
  }

  @Test
  public void testProcessingExceptionWithCausePkgJava() {
    NullPointerException cause = new NullPointerException();
    ProcessingException e = new ProcessingException(MESSAGE, cause).withTitle(TITLE);
    Throwable t = PlaceholderException.transformException(e);
    assertEqualExceptionAndCause(e, t);
  }

  @Test
  public void testProcessingExceptionWithCauseNonPkgJava() {
    SAXNotRecognizedException cause = new SAXNotRecognizedException(MESSAGE);
    ProcessingException e = new ProcessingException(MESSAGE, cause).withTitle(TITLE);
    Throwable t = PlaceholderException.transformException(e);
    ProcessingException expected = new ProcessingException(MESSAGE, new PlaceholderException(cause, null)).withTitle(TITLE);
    expected.setStackTrace(e.getStackTrace());
    assertEqualExceptionAndCause(expected, t);
  }

  @Test
  public void testProcessingExceptionWithCauseNonPkgJavaAndCustomStatus() {
    SAXNotRecognizedException cause = new SAXNotRecognizedException(MESSAGE);
    CustomProcessingStatus customProcessingStatus = new CustomProcessingStatus(MESSAGE, cause);
    ProcessingException e = new ProcessingException(customProcessingStatus);
    Throwable t = PlaceholderException.transformException(e);
    ProcessingException expected = new ProcessingException(MESSAGE, new PlaceholderException(cause, null));
    expected.setStackTrace(e.getStackTrace());
    assertEqualExceptionAndCause(expected, t);
  }

  @Test
  public void testCustomProcessingException() {
    CustomProcessingException e = new CustomProcessingException();
    Throwable t = PlaceholderException.transformException(e);
    assertEqualExceptionAndCause(e, t);
  }

  @Test
  public void testCustomProcessingExceptionWithStringConstructor() {
    CustomProcessingExceptionWithStringConstructor e = new CustomProcessingExceptionWithStringConstructor("test");
    Throwable t = PlaceholderException.transformException(e);
    assertEqualExceptionAndCause(e, t);
  }

  private static void assertEqualExceptionAndCause(Throwable expected, Throwable actual) {
    assertTrue((expected == null && actual == null) || (expected != null && actual != null));
    if (expected != null && actual != null) {
      assertNotSame(expected, actual);
      assertEquals(expected.getClass(), actual.getClass());
      assertEquals(expected.getMessage(), actual.getMessage());
      assertTrue("Stacktraces are not equal", Arrays.equals(expected.getStackTrace(), actual.getStackTrace()));
      assertEqualExceptionAndCause(expected.getCause(), actual.getCause());
    }
  }
}
