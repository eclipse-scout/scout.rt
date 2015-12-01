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
package org.eclipse.scout.rt.testing.platform.runner;

import static org.junit.Assert.fail;

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

  // === Test classes ===

  private static class TestException1 extends Exception {

    private static final long serialVersionUID = 1L;
  }

  private static class TestException2 extends Exception {

    private static final long serialVersionUID = 1L;
  }

  private static class TestProcessingException3 extends ProcessingException {

    private static final long serialVersionUID = 1L;
  }

  private static class TestExceptionHandler extends ExceptionHandler {
  }
}
