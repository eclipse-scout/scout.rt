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
package org.eclipse.scout.rt.platform.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.security.AccessController;
import java.util.concurrent.Callable;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.chain.callable.CallableChain;
import org.eclipse.scout.rt.platform.holders.Holder;
import org.junit.Test;

public class SubjectProcessorTest {

  private Subject m_subject = new Subject();

  @Test
  public void test() throws Exception {
    final Holder<Subject> actualSubject = new Holder<>();

    CallableChain<String> callableChain = new CallableChain<>();
    callableChain.add(new SubjectProcessor<String>(m_subject));
    String result = callableChain.call(new Callable<String>() {

      @Override
      public String call() throws Exception {
        actualSubject.setValue(Subject.getSubject(AccessController.getContext()));
        return "result";
      }
    });

    // VERIFY
    assertEquals("result", result);
    assertSame(m_subject, actualSubject.getValue());
  }

  @Test
  public void testWithoutSubject() throws Exception {
    final Holder<Subject> actualSubject = new Holder<>();

    CallableChain<String> callableChain = new CallableChain<>();
    callableChain.add(new SubjectProcessor<String>(null));
    String result = callableChain.call(new Callable<String>() {

      @Override
      public String call() throws Exception {
        actualSubject.setValue(Subject.getSubject(AccessController.getContext()));
        return "result";
      }
    });

    // VERIFY
    assertEquals("result", result);
    assertNull(actualSubject.getValue());
  }

  @Test
  public void testException() throws Exception {
    final Holder<Subject> actualSubject = new Holder<>();

    final Exception exception = new Exception("error");

    CallableChain<String> callableChain = new CallableChain<>();
    callableChain.add(new SubjectProcessor<String>(m_subject));
    try {
      callableChain.call(new Callable<String>() {

        @Override
        public String call() throws Exception {
          actualSubject.setValue(Subject.getSubject(AccessController.getContext()));
          throw exception;
        }
      });
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

    // RUN THE TEST
    CallableChain<String> callableChain = new CallableChain<>();
    callableChain.add(new SubjectProcessor<String>(m_subject));
    try {
      callableChain.call(new Callable<String>() {

        @Override
        public String call() throws Exception {
          actualSubject.setValue(Subject.getSubject(AccessController.getContext()));
          throw runtimeException;
        }
      });
      fail();
    }
    catch (Exception e) {
      // VERIFY
      assertSame(runtimeException, e);
      assertSame(m_subject, actualSubject.getValue());
    }
  }
}
