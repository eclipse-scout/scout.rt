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
package org.eclipse.scout.rt.server.job.interceptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.security.AccessController;
import java.util.concurrent.Callable;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.job.IJob;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class SubjectCallableTest {

  @Mock
  private Callable<String> m_next;

  private Subject m_subject = new Subject();

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);
    IJob.CURRENT.set(mock(IJob.class));
  }

  @After
  public void after() {
    IJob.CURRENT.remove();
  }

  @Test
  public void test() throws Exception {
    final Holder<Subject> actualSubject = new Holder<>();
    doAnswer(new Answer<String>() {

      @Override
      public String answer(InvocationOnMock invocation) throws Throwable {
        actualSubject.setValue(Subject.getSubject(AccessController.getContext()));
        return "success";
      }
    }).when(m_next).call();

    SubjectCallable<String> runner = new SubjectCallable<String>(m_next, m_subject);

    assertEquals("success", runner.call());
    assertSame(m_subject, actualSubject.getValue());
  }

  @Test
  public void testWithoutSubject() throws Exception {
    final Holder<Subject> actualSubject = new Holder<>();
    doAnswer(new Answer<String>() {

      @Override
      public String answer(InvocationOnMock invocation) throws Throwable {
        actualSubject.setValue(Subject.getSubject(AccessController.getContext()));
        return "success";
      }
    }).when(m_next).call();

    SubjectCallable<String> runner = new SubjectCallable<String>(m_next, null);

    assertEquals("success", runner.call());
    assertNull(actualSubject.getValue());
  }

  @Test
  public void testException() throws Exception {
    final Exception exception = new Exception("error");
    final Holder<Subject> actualSubject = new Holder<>();

    doAnswer(new Answer<String>() {

      @Override
      public String answer(InvocationOnMock invocation) throws Throwable {
        actualSubject.setValue(Subject.getSubject(AccessController.getContext()));
        throw exception;
      }
    }).when(m_next).call();

    SubjectCallable<String> runner = new SubjectCallable<String>(m_next, m_subject);

    try {
      runner.call();
      fail();
    }
    catch (Exception e) {
      assertSame(exception, e);
      assertSame(m_subject, actualSubject.getValue());
    }
  }

  @Test
  public void testRuntimeException() throws Exception {
    final RuntimeException runtimeException = new RuntimeException("error");
    final Holder<Subject> actualSubject = new Holder<>();

    doAnswer(new Answer<String>() {

      @Override
      public String answer(InvocationOnMock invocation) throws Throwable {
        actualSubject.setValue(Subject.getSubject(AccessController.getContext()));
        throw runtimeException;
      }
    }).when(m_next).call();

    SubjectCallable<String> runner = new SubjectCallable<String>(m_next, m_subject);

    try {
      runner.call();
      fail();
    }
    catch (Exception e) {
      assertSame(runtimeException, e);
      assertSame(m_subject, actualSubject.getValue());
    }
  }
}
