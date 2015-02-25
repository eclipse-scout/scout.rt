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
package org.eclipse.scout.commons.job.internal.callable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.security.AccessController;
import java.util.concurrent.Callable;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.holders.Holder;
import org.junit.Test;

public class SubjectCallableTest {

  private Subject m_subject = new Subject();

  @Test
  public void test() throws Exception {
    final Holder<Subject> actualSubject = new Holder<>();

    Callable<String> callable = new Callable<String>() {

      @Override
      public String call() throws Exception {
        actualSubject.setValue(Subject.getSubject(AccessController.getContext()));
        return "result";
      }
    };

    // RUN THE TEST
    SubjectCallable<String> testee = new SubjectCallable<String>(callable, m_subject);
    String result = testee.call();

    // VERIFY
    assertEquals("result", result);
    assertSame(m_subject, actualSubject.getValue());
  }

  @Test
  public void testWithoutSubject() throws Exception {
    final Holder<Subject> actualSubject = new Holder<>();

    Callable<String> callable = new Callable<String>() {

      @Override
      public String call() throws Exception {
        actualSubject.setValue(Subject.getSubject(AccessController.getContext()));
        return "result";
      }
    };

    // RUN THE TEST
    SubjectCallable<String> testee = new SubjectCallable<String>(callable, null);
    String result = testee.call();

    // VERIFY
    assertEquals("result", result);
    assertNull(actualSubject.getValue());
  }

  @Test
  public void testException() throws Exception {
    final Holder<Subject> actualSubject = new Holder<>();

    final Exception exception = new Exception("error");

    Callable<String> callable = new Callable<String>() {

      @Override
      public String call() throws Exception {
        actualSubject.setValue(Subject.getSubject(AccessController.getContext()));
        throw exception;
      }
    };

    // RUN THE TEST
    SubjectCallable<String> testee = new SubjectCallable<String>(callable, m_subject);
    try {
      testee.call();
      fail();
    }
    catch (Exception e) {
      // VERIFY
      assertSame(exception, e);
      assertSame(m_subject, actualSubject.getValue());
    }
  }

  @Test
  public void testRuntimeException() throws Exception {
    final Holder<Subject> actualSubject = new Holder<>();

    final RuntimeException runtimeException = new RuntimeException("error");

    Callable<String> callable = new Callable<String>() {

      @Override
      public String call() throws Exception {
        actualSubject.setValue(Subject.getSubject(AccessController.getContext()));
        throw runtimeException;
      }
    };

    // RUN THE TEST
    SubjectCallable<String> testee = new SubjectCallable<String>(callable, m_subject);
    try {
      testee.call();
      fail();
    }
    catch (Exception e) {
      // VERIFY
      assertSame(runtimeException, e);
      assertSame(m_subject, actualSubject.getValue());
    }
  }
}
