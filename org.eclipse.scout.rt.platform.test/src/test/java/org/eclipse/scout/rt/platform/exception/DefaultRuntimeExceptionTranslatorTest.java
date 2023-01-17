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

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.concurrent.ExecutionException;

import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class DefaultRuntimeExceptionTranslatorTest {

  private PlatformException m_platformException;
  private ProcessingException m_processingException;
  private RuntimeException m_runtimeException;
  private IllegalArgumentException m_illegalArgumentException;
  private Exception m_checkedException;
  private InterruptedException m_interruptedException;
  private Throwable m_throwable;
  private Error m_error;

  @Before
  public void before() {
    m_platformException = new ProcessingException("expected JUnit test exception");
    m_processingException = new ProcessingException("expected JUnit test exception");
    m_runtimeException = new RuntimeException("expected JUnit test exception");
    m_illegalArgumentException = new IllegalArgumentException("expected JUnit test exception");
    m_checkedException = new Exception("expected JUnit test exception");
    m_interruptedException = new InterruptedException("expected JUnit test exception");
    m_throwable = new Throwable("expected JUnit test exception");
    m_error = new Error("expected JUnit test exception");
  }

  @Test
  public void testTranslate() {
    testTranslate(throwable -> throwable);
  }

  @Test
  public void testTranslateWithExecutionException() {
    testTranslate(throwable -> new ExecutionException("expected JUnit test exception", throwable));
  }

  @Test
  public void testTranslateWithExecutionException_Nested() {
    testTranslate(throwable -> new ExecutionException("expected JUnit test exception", new ExecutionException("expected JUnit test exception", throwable)));
  }

  @Test
  public void testTranslateWithUndeclaredThrowableException() {
    testTranslate(throwable -> new UndeclaredThrowableException(throwable, "expected JUnit test exception"));
  }

  @Test
  public void testTranslateWithUndeclaredThrowableException_Nested() {
    testTranslate(throwable -> new UndeclaredThrowableException(new UndeclaredThrowableException(throwable, "expected JUnit test exception"), "expected JUnit test exception"));
  }

  @Test
  public void testTranslateWithInvocationTargetException() {
    testTranslate(throwable -> new InvocationTargetException(throwable, "expected JUnit test exception"));
  }

  @Test
  public void testTranslateWithInvocationTargetException_Nested() {
    testTranslate(throwable -> new InvocationTargetException(new InvocationTargetException(throwable, "expected JUnit test exception"), "expected JUnit test exception"));
  }

  private void testTranslate(IThrowableProducer throwableProducer) {
    DefaultRuntimeExceptionTranslator translator = new DefaultRuntimeExceptionTranslator();

    // PlatformException
    assertSame(m_platformException, translator.translate(throwableProducer.produce(m_platformException)));

    // ProcessingException
    assertSame(m_processingException, translator.translate(throwableProducer.produce((m_processingException))));

    // RuntimeException
    assertSame(m_runtimeException, translator.translate(throwableProducer.produce(m_runtimeException)));

    // IllegalArgumentException
    assertSame(m_illegalArgumentException, translator.translate(throwableProducer.produce(m_illegalArgumentException)));

    // Exception
    assertTrue(translator.translate(throwableProducer.produce(m_checkedException)) instanceof PlatformException);
    assertSame(m_checkedException, translator.translate(throwableProducer.produce(m_checkedException)).getCause());
    assertTrue(translator.translate(throwableProducer.produce(m_checkedException)) instanceof PlatformException);
    assertTrue(((PlatformException) translator.translate(throwableProducer.produce(m_checkedException))).getContextInfos().contains("translator=" + DefaultRuntimeExceptionTranslator.class.getName()));

    // InterruptedException
    assertTrue(translator.translate(throwableProducer.produce(m_interruptedException)) instanceof PlatformException);
    assertSame(m_interruptedException, translator.translate(throwableProducer.produce(m_interruptedException)).getCause());
    assertTrue(translator.translate(throwableProducer.produce(m_interruptedException)) instanceof PlatformException);
    assertTrue(((PlatformException) translator.translate(throwableProducer.produce(m_interruptedException))).getContextInfos().contains("translator=" + DefaultRuntimeExceptionTranslator.class.getName()));

    // Throwable
    assertTrue(translator.translate(throwableProducer.produce(m_throwable)) instanceof PlatformException);
    assertSame(m_throwable, translator.translate(throwableProducer.produce(m_throwable)).getCause());
    assertTrue(((PlatformException) translator.translate(throwableProducer.produce(m_throwable))).getContextInfos().contains("translator=" + DefaultRuntimeExceptionTranslator.class.getName()));

    // Error
    try {
      translator.translate(throwableProducer.produce(m_error));
      fail("Error expected");
    }
    catch (Error e) {
      // NOOP
    }
  }

  private static interface IThrowableProducer {
    public Throwable produce(Throwable throwable);
  }
}
