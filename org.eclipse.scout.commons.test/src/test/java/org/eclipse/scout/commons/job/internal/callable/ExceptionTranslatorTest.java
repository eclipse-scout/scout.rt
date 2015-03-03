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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.security.PrivilegedAction;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.job.IJobInput;
import org.eclipse.scout.commons.job.JobExecutionException;
import org.eclipse.scout.commons.job.JobInput;
import org.eclipse.scout.commons.security.SimplePrincipal;
import org.junit.Before;
import org.junit.Test;

public class ExceptionTranslatorTest {

  private IJobInput m_input;

  @Before
  public void before() {
    m_input = JobInput.empty();
  }

  @Test
  public void testTranslate() {
    ProcessingException pe1 = new ProcessingException();
    RuntimeException reWithPe = new RuntimeException(pe1);
    Exception e1 = new Exception();
    RuntimeException re1 = new RuntimeException();

    assertSame(pe1, ExceptionTranslator.translate(pe1));
    assertSame(pe1, ExceptionTranslator.translate(reWithPe));
    assertSame(e1, ExceptionTranslator.translate(e1).getCause());
    assertSame(re1, ExceptionTranslator.translate(re1).getCause());

    UndeclaredThrowableException ute1 = new UndeclaredThrowableException(null);
    assertSame(ute1, ExceptionTranslator.translate(ute1).getCause());

    UndeclaredThrowableException ute2 = new UndeclaredThrowableException(e1);
    assertSame(e1, ExceptionTranslator.translate(ute2).getCause());

    UndeclaredThrowableException ute3 = new UndeclaredThrowableException(re1);
    assertSame(re1, ExceptionTranslator.translate(ute3).getCause());

    UndeclaredThrowableException ute4 = new UndeclaredThrowableException(reWithPe);
    assertSame(reWithPe.getCause(), ExceptionTranslator.translate(ute4));

    InvocationTargetException ite1 = new InvocationTargetException(null);
    assertSame(ite1, ExceptionTranslator.translate(ite1).getCause());

    UndeclaredThrowableException ite2 = new UndeclaredThrowableException(e1);
    assertSame(e1, ExceptionTranslator.translate(ite2).getCause());

    UndeclaredThrowableException ite3 = new UndeclaredThrowableException(re1);
    assertSame(re1, ExceptionTranslator.translate(ite3).getCause());

    UndeclaredThrowableException ite4 = new UndeclaredThrowableException(reWithPe);
    assertSame(reWithPe.getCause(), ExceptionTranslator.translate(ite4));
  }

  @Test
  public void testTranslateInterruptedException() {
    InterruptedException e = new InterruptedException();
    JobExecutionException pe = ExceptionTranslator.translateInterruptedException(e, "job-1");
    assertTrue(pe.isInterruption());
    assertFalse(pe.isCancellation());
    assertFalse(pe.isTimeout());
    assertFalse(pe.isRejection());
  }

  @Test
  public void testTranslateCancellationException() {
    CancellationException e = new CancellationException();
    JobExecutionException pe = ExceptionTranslator.translateCancellationException(e, "job-1");
    assertTrue(pe.isCancellation());
    assertFalse(pe.isInterruption());
    assertFalse(pe.isTimeout());
    assertFalse(pe.isRejection());
  }

  @Test
  public void testTranslateTimeoutException() {
    TimeoutException e = new TimeoutException();
    JobExecutionException pe = ExceptionTranslator.translateTimeoutException(e, 1, TimeUnit.MINUTES, "job-1");
    assertTrue(pe.isTimeout());
    assertFalse(pe.isInterruption());
    assertFalse(pe.isCancellation());
    assertFalse(pe.isRejection());
  }

  @Test
  public void testCallableSuccess() throws Exception {
    @SuppressWarnings("unchecked")
    Callable<String> next = mock(Callable.class);
    when(next.call()).thenReturn("ABC");
    assertEquals("ABC", new ExceptionTranslator<>(next, m_input).call());
  }

  @Test
  public void testCallableContextMessage1() throws Exception {
    m_input.id("7");
    m_input.name("job");

    Callable<?> next = mock(Callable.class);
    doThrow(new RuntimeException()).when(next).call();

    try {
      new ExceptionTranslator<>(next, m_input).call();
      fail();
    }
    catch (Exception e) {
      assertTrue(e instanceof ProcessingException);
      ProcessingException pe = ((ProcessingException) e);
      assertEquals(pe.getStatus().getContextMessages(), CollectionUtility.arrayList("job=7;job", "identity=anonymous"));
    }
  }

  @Test
  public void testCallableContextMessage2() throws Exception {
    m_input.id("7");
    m_input.name("job");

    Subject s = new Subject();
    s.getPrincipals().add(new SimplePrincipal("jack"));
    s.getPrincipals().add(new SimplePrincipal("john"));

    final Holder<Exception> errorHolder = new Holder<>();

    Subject.doAs(s, new PrivilegedAction<Void>() {

      @Override
      public Void run() {
        try {
          Callable<?> next = mock(Callable.class);
          doThrow(new RuntimeException()).when(next).call();
          try {
            new ExceptionTranslator<>(next, m_input).call();
          }
          catch (Exception e) {
            errorHolder.setValue(e);
          }
        }
        catch (Exception e1) {
          // NOOP
        }
        return null;
      }
    });

    Exception e = errorHolder.getValue();
    assertTrue(e instanceof ProcessingException);
    ProcessingException pe = ((ProcessingException) e);
    assertEquals(pe.getStatus().getContextMessages(), CollectionUtility.arrayList("job=7;job", "identity=jack, john"));
  }

  @Test
  public void testCallableException() throws Exception {
    m_input.id("7");
    m_input.name("job");

    Callable<?> next = mock(Callable.class);

    // RuntimeException
    RuntimeException r1 = new RuntimeException();
    doThrow(r1).when(next).call();

    try {
      new ExceptionTranslator<>(next, m_input).call();
      fail();
    }
    catch (Exception e) {
      assertTrue(e instanceof ProcessingException);
      assertSame(r1, e.getCause());

      ProcessingException pe = ((ProcessingException) e);
      assertEquals(pe.getStatus().getContextMessages(), CollectionUtility.arrayList("job=7;job", "identity=anonymous"));
    }

    // Exception
    Exception e1 = new Exception();
    doThrow(e1).when(next).call();

    try {
      new ExceptionTranslator<>(next, m_input).call();
      fail();
    }
    catch (Exception e) {
      assertTrue(e instanceof ProcessingException);
      assertSame(e1, e.getCause());
    }

    // ProcessingException
    ProcessingException p1 = new ProcessingException();
    doThrow(p1).when(next).call();

    try {
      new ExceptionTranslator<>(next, m_input).call();
      fail();
    }
    catch (Exception e) {
      assertSame(p1, e);
    }

    // RuntimeException with ProcessingException cause
    ProcessingException p2 = new ProcessingException();
    RuntimeException r2 = new RuntimeException(p2);

    doThrow(r2).when(next).call();

    try {
      new ExceptionTranslator<>(next, m_input).call();
      fail();
    }
    catch (Exception e) {
      assertTrue(e instanceof ProcessingException);
      assertSame(p2, e);
    }

    // UndeclaredThrowableException 1
    ProcessingException p3 = new ProcessingException();
    doThrow(new UndeclaredThrowableException(p3)).when(next).call();

    try {
      new ExceptionTranslator<>(next, m_input).call();
      fail();
    }
    catch (Exception e) {
      assertSame(p3, e);
    }

    // UndeclaredThrowableException 2
    ProcessingException p4 = new ProcessingException();
    RuntimeException r4 = new RuntimeException(p4);
    doThrow(new UndeclaredThrowableException(r4)).when(next).call();

    try {
      new ExceptionTranslator<>(next, m_input).call();
      fail();
    }
    catch (Exception e) {
      assertSame(p4, e);
    }

    // UndeclaredThrowableException 3
    RuntimeException r5 = new RuntimeException();
    doThrow(new UndeclaredThrowableException(r5)).when(next).call();

    try {
      new ExceptionTranslator<>(next, m_input).call();
      fail();
    }
    catch (Exception e) {
      assertTrue(e instanceof ProcessingException);
      assertSame(r5, e.getCause());
    }

    // UndeclaredThrowableException 4
    Exception r6 = new Exception();
    doThrow(new UndeclaredThrowableException(r6)).when(next).call();

    try {
      new ExceptionTranslator<>(next, m_input).call();
      fail();
    }
    catch (Exception e) {
      assertTrue(e instanceof ProcessingException);
      assertSame(r6, e.getCause());
    }

    // UndeclaredThrowableException 5
    UndeclaredThrowableException ue = new UndeclaredThrowableException(null);
    doThrow(ue).when(next).call();

    try {
      new ExceptionTranslator<>(next, m_input).call();
      fail();
    }
    catch (Exception e) {
      assertTrue(e instanceof ProcessingException);
      assertSame(ue, e.getCause());
    }
  }
}
