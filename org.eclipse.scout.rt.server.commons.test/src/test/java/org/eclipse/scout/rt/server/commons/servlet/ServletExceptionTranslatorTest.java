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
package org.eclipse.scout.rt.server.commons.servlet;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.concurrent.ExecutionException;

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;

import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class ServletExceptionTranslatorTest {

  private ServletException m_servletException;
  private UnavailableException m_unavailableException;
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
    m_servletException = new ServletException();
    m_unavailableException = new UnavailableException("");
    m_platformException = new ProcessingException();
    m_processingException = new ProcessingException();
    m_runtimeException = new RuntimeException();
    m_illegalArgumentException = new IllegalArgumentException();
    m_checkedException = new Exception();
    m_interruptedException = new InterruptedException();
    m_throwable = new Throwable();
    m_error = new Error();
  }

  @Test
  public void testTranslate() {
    testTranslate(new IThrowableProducer() {

      @Override
      public Throwable produce(Throwable throwable) {
        return throwable;
      }
    });
  }

  @Test
  public void testTranslateWithExecutionException() {
    testTranslate(new IThrowableProducer() {

      @Override
      public Throwable produce(Throwable throwable) {
        return new ExecutionException(throwable);
      }
    });
  }

  @Test
  public void testTranslateWithExecutionException_Nested() {
    testTranslate(new IThrowableProducer() {

      @Override
      public Throwable produce(Throwable throwable) {
        return new ExecutionException(
            new ExecutionException(
                throwable));
      }
    });
  }

  @Test
  public void testTranslateWithUndeclaredThrowableException() {
    testTranslate(new IThrowableProducer() {

      @Override
      public Throwable produce(Throwable throwable) {
        return new UndeclaredThrowableException(throwable);
      }
    });
  }

  @Test
  public void testTranslateWithUndeclaredThrowableException_Nested() {
    testTranslate(new IThrowableProducer() {

      @Override
      public Throwable produce(Throwable throwable) {
        return new UndeclaredThrowableException(
            new UndeclaredThrowableException(
                throwable));
      }
    });
  }

  @Test
  public void testTranslateWithInvocationTargetException() {
    testTranslate(new IThrowableProducer() {

      @Override
      public Throwable produce(Throwable throwable) {
        return new InvocationTargetException(throwable);
      }
    });
  }

  @Test
  public void testTranslateWithInvocationTargetException_Nested() {
    testTranslate(new IThrowableProducer() {

      @Override
      public Throwable produce(Throwable throwable) {
        return new InvocationTargetException(
            new InvocationTargetException(
                throwable));
      }
    });
  }

  private void testTranslate(IThrowableProducer throwableProducer) {
    ServletExceptionTranslator translator = new ServletExceptionTranslator();

    // ServletException
    assertSame(m_servletException, translator.translate(throwableProducer.produce(m_servletException)));

    // UnavailableException
    assertSame(m_unavailableException, translator.translate(throwableProducer.produce(m_unavailableException)));

    // PlatformException
    assertTrue(translator.translate(throwableProducer.produce(m_platformException)) instanceof ServletException);
    assertSame(m_platformException, translator.translate(throwableProducer.produce(m_platformException)).getCause());

    // ProcessingException
    assertTrue(translator.translate(throwableProducer.produce(m_processingException)) instanceof ServletException);
    assertSame(m_processingException, translator.translate(throwableProducer.produce(m_processingException)).getCause());

    // RuntimeException
    assertTrue(translator.translate(throwableProducer.produce(m_runtimeException)) instanceof ServletException);
    assertSame(m_runtimeException, translator.translate(throwableProducer.produce(m_runtimeException)).getCause());

    // IllegalArgumentException
    assertTrue(translator.translate(throwableProducer.produce(m_illegalArgumentException)) instanceof ServletException);
    assertSame(m_illegalArgumentException, translator.translate(throwableProducer.produce(m_illegalArgumentException)).getCause());

    // Exception
    assertTrue(translator.translate(throwableProducer.produce(m_checkedException)) instanceof ServletException);
    assertSame(m_checkedException, translator.translate(throwableProducer.produce(m_checkedException)).getCause());

    // InterruptedException
    assertTrue(translator.translate(throwableProducer.produce(m_interruptedException)) instanceof ServletException);
    assertSame(m_interruptedException, translator.translate(throwableProducer.produce(m_interruptedException)).getCause());

    // Throwable
    assertTrue(translator.translate(throwableProducer.produce(m_throwable)) instanceof ServletException);
    assertSame(m_throwable, translator.translate(throwableProducer.produce(m_throwable)).getCause());

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
