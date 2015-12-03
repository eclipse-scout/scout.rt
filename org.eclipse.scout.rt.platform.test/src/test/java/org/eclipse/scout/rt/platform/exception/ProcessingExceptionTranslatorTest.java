/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.exception;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.security.PrivilegedAction;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.security.SimplePrincipal;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class ProcessingExceptionTranslatorTest {

  @Test
  public void testTranslate() {
    ProcessingExceptionTranslator exceptionTranslator = new ProcessingExceptionTranslator();

    ProcessingException pe1 = new ProcessingException();
    RuntimeException reWithPe = new RuntimeException(pe1);
    Exception e1 = new Exception();
    RuntimeException re1 = new RuntimeException();

    // test 'normal cases'
    assertSame(pe1, exceptionTranslator.translate(pe1));
    assertSame(pe1, exceptionTranslator.translate(reWithPe));
    assertSame(e1, exceptionTranslator.translate(e1).getCause());
    assertSame(re1, exceptionTranslator.translate(re1).getCause());

    // test 'Error' (1)
    Error error = new Error();
    try {
      exceptionTranslator.translate(error);
      fail("Error's should not be translated but re-throw instead");
    }
    catch (Throwable t) {
      assertSame(error, t);
    }

    // test 'Error' (2)
    AssertionError assertError = new AssertionError();
    try {
      exceptionTranslator.translate(assertError);
      fail("Error's should not be translated but re-throw instead");
    }
    catch (Throwable t) {
      assertSame(assertError, t);

    }

    // test 'UndeclaredThrowableException'
    UndeclaredThrowableException ute1 = new UndeclaredThrowableException(null);
    assertSame(ute1, exceptionTranslator.translate(ute1).getCause());

    UndeclaredThrowableException ute2 = new UndeclaredThrowableException(e1);
    assertSame(e1, exceptionTranslator.translate(ute2).getCause());

    UndeclaredThrowableException ute3 = new UndeclaredThrowableException(re1);
    assertSame(re1, exceptionTranslator.translate(ute3).getCause());

    UndeclaredThrowableException ute4 = new UndeclaredThrowableException(reWithPe);
    assertSame(reWithPe.getCause(), exceptionTranslator.translate(ute4));

    // test 'InvocationTargetException'
    InvocationTargetException ite1 = new InvocationTargetException(null);
    assertSame(ite1, exceptionTranslator.translate(ite1).getCause());

    InvocationTargetException ite2 = new InvocationTargetException(e1);
    assertSame(e1, exceptionTranslator.translate(ite2).getCause());

    InvocationTargetException ite3 = new InvocationTargetException(re1);
    assertSame(re1, exceptionTranslator.translate(ite3).getCause());

    InvocationTargetException ite4 = new InvocationTargetException(reWithPe);
    assertSame(reWithPe.getCause(), exceptionTranslator.translate(ite4));

    // test 'ExecutionException'
    ExecutionException ee1 = new ExecutionException(null);
    assertSame(ee1, exceptionTranslator.translate(ee1).getCause());

    ExecutionException ee2 = new ExecutionException(e1);
    assertSame(e1, exceptionTranslator.translate(ee2).getCause());

    ExecutionException ee3 = new ExecutionException(re1);
    assertSame(re1, exceptionTranslator.translate(ee3).getCause());

    ExecutionException ee4 = new ExecutionException(reWithPe);
    assertSame(reWithPe.getCause(), exceptionTranslator.translate(ee4));
  }

  @Test
  public void testTranslateAndAddContextMessage() {
    ProcessingExceptionTranslator exceptionTranslator = new ProcessingExceptionTranslator();

    ProcessingException pe1 = new ProcessingException();
    assertSame(pe1, exceptionTranslator.translateAndAddContextMessages(pe1, (String[]) null));

    pe1 = new ProcessingException();
    assertSame(pe1, exceptionTranslator.translateAndAddContextMessages(pe1, "message 1"));
    assertTrue(pe1.getStatus().getContextMessages().contains("message 1"));

    pe1 = new ProcessingException();
    assertSame(pe1, exceptionTranslator.translateAndAddContextMessages(pe1, "message 1", "message 2"));
    assertTrue(pe1.getStatus().getContextMessages().contains("message 1"));
    assertTrue(pe1.getStatus().getContextMessages().contains("message 2"));

    Exception e1 = new Exception();
    ProcessingException e1Translated = exceptionTranslator.translateAndAddContextMessages(e1, "message");
    assertTrue(e1Translated instanceof ProcessingException);
    assertTrue(e1Translated.getStatus().getContextMessages().contains("message"));

    Exception e2 = new Exception();
    ProcessingException e2Translated = exceptionTranslator.translateAndAddContextMessages(e2, "message 1", "message 2");
    assertTrue(e2Translated instanceof ProcessingException);
    assertTrue(e2Translated.getStatus().getContextMessages().contains("message 1"));
    assertTrue(e2Translated.getStatus().getContextMessages().contains("message 2"));

  }

  @Test
  public void testJobContextMessage() throws Exception {
    final ProcessingExceptionTranslator exceptionTranslator = new ProcessingExceptionTranslator();

    // test context message with 'no job' and 'no subject'
    IFuture.CURRENT.remove();
    ProcessingException pe = Subject.doAs(null, new PrivilegedAction<ProcessingException>() {

      @Override
      public ProcessingException run() {
        return exceptionTranslator.translate(new ProcessingException());
      }
    });
    assertEquals(CollectionUtility.hashSet("user=anonymous"), new HashSet<>(pe.getStatus().getContextMessages()));

    // test context message with 'anonymous job' and 'no subject'
    IFuture future = mock(IFuture.class);
    IFuture.CURRENT.set(future);
    when(future.getJobInput()).thenReturn(Jobs.newInput().withRunContext(RunContexts.empty()));
    pe = Subject.doAs(null, new PrivilegedAction<ProcessingException>() {

      @Override
      public ProcessingException run() {
        return exceptionTranslator.translate(new ProcessingException());
      }
    });
    assertEquals(CollectionUtility.hashSet("user=anonymous"), new HashSet<>(pe.getStatus().getContextMessages()));

    // test context message with 'job' and 'no subject'
    future = mock(IFuture.class);
    IFuture.CURRENT.set(future);
    when(future.getJobInput()).thenReturn(Jobs.newInput()
        .withRunContext(RunContexts.empty())
        .withName("do-something"));
    pe = Subject.doAs(null, new PrivilegedAction<ProcessingException>() {

      @Override
      public ProcessingException run() {
        return exceptionTranslator.translate(new ProcessingException());
      }
    });
    assertEquals(CollectionUtility.hashSet("job=do-something", "user=anonymous"), new HashSet<>(pe.getStatus().getContextMessages()));

    // test context message with 'job' and 'subject'
    future = mock(IFuture.class);
    IFuture.CURRENT.set(future);
    Subject subject = new Subject();
    subject.getPrincipals().add(new SimplePrincipal("anna"));
    subject.getPrincipals().add(new SimplePrincipal("john"));

    when(future.getJobInput()).thenReturn(Jobs.newInput()
        .withRunContext(RunContexts.empty())
        .withName("do-something"));
    pe = Subject.doAs(subject, new PrivilegedAction<ProcessingException>() {

      @Override
      public ProcessingException run() {
        return exceptionTranslator.translate(new ProcessingException());
      }
    });
    assertEquals(CollectionUtility.hashSet("job=do-something", "user=anna, john"), new HashSet<>(pe.getStatus().getContextMessages()));
  }

  @Test
  public void testJobContextMessageWithSameJob() {
    final ProcessingExceptionTranslator exceptionTranslator = new ProcessingExceptionTranslator();

    // test context message that already contains job
    IFuture future = mock(IFuture.class);
    IFuture.CURRENT.set(future);
    Subject subject = new Subject();
    subject.getPrincipals().add(new SimplePrincipal("anna"));

    when(future.getJobInput()).thenReturn(Jobs.newInput()
        .withRunContext(RunContexts.empty())
        .withName("do-something"));
    ProcessingException actualException = Subject.doAs(subject, new PrivilegedAction<ProcessingException>() {

      @Override
      public ProcessingException run() {
        ProcessingException cause = new ProcessingException();
        cause.addContextMessage("user=anna");
        return exceptionTranslator.translate(cause);
      }
    });

    assertEquals(2, actualException.getStatus().getContextMessages().size()); // not 3 entries
    assertEquals(CollectionUtility.hashSet("job=do-something", "user=anna"), new HashSet<>(actualException.getStatus().getContextMessages()));
  }

  @Test
  public void testJobContextMessageWithSameUser() {
    final ProcessingExceptionTranslator exceptionTranslator = new ProcessingExceptionTranslator();

    // test context message that already contains job
    IFuture future = mock(IFuture.class);
    IFuture.CURRENT.set(future);
    Subject subject = new Subject();
    subject.getPrincipals().add(new SimplePrincipal("anna"));

    when(future.getJobInput()).thenReturn(Jobs.newInput()
        .withRunContext(RunContexts.empty())
        .withName("do-something"));
    ProcessingException actualException = Subject.doAs(subject, new PrivilegedAction<ProcessingException>() {

      @Override
      public ProcessingException run() {
        ProcessingException cause = new ProcessingException();
        cause.addContextMessage("job=do-something");
        return exceptionTranslator.translate(cause);
      }
    });
    assertEquals(2, actualException.getStatus().getContextMessages().size()); // not 3 entries
    assertEquals(CollectionUtility.hashSet("job=do-something", "user=anna"), new HashSet<>(actualException.getStatus().getContextMessages()));
  }
}
