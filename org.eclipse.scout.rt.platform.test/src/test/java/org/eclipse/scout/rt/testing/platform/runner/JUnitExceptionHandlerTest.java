/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.testing.platform.runner;

import static org.junit.Assert.*;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class JUnitExceptionHandlerTest {

  /**
   * Tests that handled exceptions are used for assertion.
   */
  @Test(expected = TestException1.class)
  public void testSingleException() {
    try {
      BEANS.get(ExceptionHandler.class).handle(new TestException1());
    }
    catch (Exception e) {
      fail("exception must be swallowed");
    }
  }

  /**
   * Tests that only the first handled exception is used for assertion.
   */
  @Test(expected = TestException1.class)
  public void testMultipleExceptions() {
    try {
      BEANS.get(ExceptionHandler.class).handle(new TestException1());
    }
    catch (Exception e) {
      fail("exception must be swallowed");
    }

    try {
      BEANS.get(ExceptionHandler.class).handle(new TestException2());
    }
    catch (Exception e) {
      fail("exception must be swallowed");
    }
  }

  @Test(expected = TestProcessingException3.class)
  public void testProcessingException() {
    try {
      BEANS.get(ExceptionHandler.class).handle(new TestProcessingException3());
    }
    catch (Exception e) {
      fail("exception must be swallowed");
    }
  }

  /**
   * Tests that a consumed {@link ProcessingException} is not handled.
   */
  @Test
  public void testConsumedProcessingException() {
    try {
      ProcessingException pe = new TestProcessingException3();
      pe.consume();
      BEANS.get(ExceptionHandler.class).handle(pe);
    }
    catch (Exception e) {
      fail("exception must be swallowed");
    }
  }

  /**
   * Tests that the default Platform {@link JUnitExceptionHandler} can be overwritten.
   */
  @Test
  public void testCustomExceptionHandler() {
    IBean<Object> customExceptionHandler = Platform.get().getBeanManager().registerBean(new BeanMetaData(TestExceptionHandler.class).withReplace(true).withOrder(-1001));
    try {
      BEANS.get(ExceptionHandler.class).handle(new TestException1());
    }
    finally {
      Platform.get().getBeanManager().unregisterBean(customExceptionHandler);
    }
  }

  @Test
  public void testIgnoreExceptionOnceCorrectType() throws Exception {
    JUnitExceptionHandler exceptionHandler = BEANS.get(JUnitExceptionHandler.class);
    exceptionHandler.ignoreExceptionOnce(TestException1.class, () -> exceptionHandler.handle(new TestException1()));
    assertTrue(exceptionHandler.getErrors().isEmpty());
  }

  @Test(expected = TestException1.class)
  public void testIgnoreExceptionOnceOtherType() throws Exception {
    JUnitExceptionHandler exceptionHandler = BEANS.get(JUnitExceptionHandler.class);
    exceptionHandler.ignoreExceptionOnce(TestException2.class, () -> exceptionHandler.handle(new TestException1()));
    assertEquals(1, exceptionHandler.getErrors().size());
  }

  // === Test classes ===

  private static class TestException1 extends Exception {

    private static final long serialVersionUID = 1L;

    TestException1() {
      super("expected JUnit test exception");
    }
  }

  private static class TestException2 extends Exception {

    private static final long serialVersionUID = 1L;

    TestException2() {
      super("expected JUnit test exception");
    }
  }

  private static class TestProcessingException3 extends ProcessingException {

    private static final long serialVersionUID = 1L;

    TestProcessingException3() {
      super("expected JUnit test exception");
    }
  }

  private static class TestExceptionHandler extends ExceptionHandler {
  }
}
