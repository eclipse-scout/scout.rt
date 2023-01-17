/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.security;

import static org.junit.Assert.*;

import java.security.AccessController;

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
    callableChain.add(new SubjectProcessor<>(m_subject));
    String result = callableChain.call(() -> {
      actualSubject.setValue(Subject.getSubject(AccessController.getContext()));
      return "result";
    });

    // VERIFY
    assertEquals("result", result);
    assertSame(m_subject, actualSubject.getValue());
  }

  @Test
  public void testWithoutSubject() throws Exception {
    final Holder<Subject> actualSubject = new Holder<>();

    CallableChain<String> callableChain = new CallableChain<>();
    callableChain.add(new SubjectProcessor<>(null));
    String result = callableChain.call(() -> {
      actualSubject.setValue(Subject.getSubject(AccessController.getContext()));
      return "result";
    });

    // VERIFY
    assertEquals("result", result);
    assertNull(actualSubject.getValue());
  }

  @Test
  public void testException() {
    final Holder<Subject> actualSubject = new Holder<>();

    final Exception exception = new Exception("expected JUnit test exception");

    CallableChain<String> callableChain = new CallableChain<>();
    callableChain.add(new SubjectProcessor<>(m_subject));
    try {
      callableChain.call(() -> {
        actualSubject.setValue(Subject.getSubject(AccessController.getContext()));
        throw exception;
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
  public void testRuntimeException() {
    final Holder<Subject> actualSubject = new Holder<>();

    final RuntimeException runtimeException = new RuntimeException("expected JUnit test exception");

    // RUN THE TEST
    CallableChain<String> callableChain = new CallableChain<>();
    callableChain.add(new SubjectProcessor<>(m_subject));
    try {
      callableChain.call(() -> {
        actualSubject.setValue(Subject.getSubject(AccessController.getContext()));
        throw runtimeException;
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
