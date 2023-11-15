/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.testing.platform.runner.parameterized.AbstractScoutTestParameter;
import org.eclipse.scout.rt.testing.platform.runner.parameterized.IScoutTestParameter;
import org.eclipse.scout.rt.testing.platform.runner.parameterized.ParameterizedPlatformTestRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;

@RunWith(ParameterizedPlatformTestRunner.class)
public class ConnectionErrorDetectorTest {

  private static final String CONNECTION_RESET_MESSAGE = "connection reset by peer";
  private static final String BROKEN_PIPE_MESSAGE = "broken pipe";

  @Parameters
  public static List<IScoutTestParameter> getParameters() {
    List<IScoutTestParameter> parametersList = new LinkedList<>();
    parametersList.add(new ExceptionTestParameter(null, false));
    parametersList.add(new ExceptionTestParameter(new SocketException(CONNECTION_RESET_MESSAGE), true));
    parametersList.add(new ExceptionTestParameter(new SocketException(BROKEN_PIPE_MESSAGE), true));
    parametersList.add(new ExceptionTestParameter(new SocketException("unknown error"), false));

    parametersList.add(new ExceptionTestParameter(new EofException(CONNECTION_RESET_MESSAGE), true));
    parametersList.add(new ExceptionTestParameter(new EofException(BROKEN_PIPE_MESSAGE), true));
    parametersList.add(new ExceptionTestParameter(new EofException("cancel_stream_error"), true));
    parametersList.add(new ExceptionTestParameter(new EofException("unknown error"), false));

    parametersList.add(new ExceptionTestParameter(new ClientAbortException(CONNECTION_RESET_MESSAGE), true));
    parametersList.add(new ExceptionTestParameter(new ClientAbortException(BROKEN_PIPE_MESSAGE), true));
    parametersList.add(new ExceptionTestParameter(new ClientAbortException("unknown error"), false));

    parametersList.add(new ExceptionTestParameter(new InterruptedIOException(CONNECTION_RESET_MESSAGE), true));
    parametersList.add(new ExceptionTestParameter(new InterruptedIOException(BROKEN_PIPE_MESSAGE), true));
    parametersList.add(new ExceptionTestParameter(new InterruptedIOException("unknown error"), false));

    parametersList.add(new ExceptionTestParameter(new IOException(CONNECTION_RESET_MESSAGE), true));
    parametersList.add(new ExceptionTestParameter(new IOException("Connection reset"), true));
    parametersList.add(new ExceptionTestParameter(new IOException("An established connection was aborted by the software in your host machine"), true));
    parametersList.add(new ExceptionTestParameter(new IOException(BROKEN_PIPE_MESSAGE), true));
    parametersList.add(new ExceptionTestParameter(new IOException("unknown error"), false));

    parametersList.add(new ExceptionTestParameter(new CustomException(CONNECTION_RESET_MESSAGE), false));
    parametersList.add(new ExceptionTestParameter(new CustomException(BROKEN_PIPE_MESSAGE), false));
    parametersList.add(new ExceptionTestParameter(new CustomException("unknown error"), false));

    parametersList.add(new ExceptionTestParameter(createDetectedNestedException(), true));
    parametersList.add(new ExceptionTestParameter(createUndetectedNestedException(), false));

    parametersList.add(new ExceptionTestParameter(createUndetectedCycledCauseException(), false));

    return parametersList;
  }

  private static Exception createDetectedNestedException() {
    IOException detectedIOException = new IOException(BROKEN_PIPE_MESSAGE);
    CustomException customException = new CustomException("custom exception", detectedIOException);
    IOException ioException = new IOException("some IO exception", customException);
    return new Exception("detected nested exception", ioException);
  }

  private static Exception createUndetectedNestedException() {
    IOException undetectedIOException = new IOException("unknown IO exception");
    CustomException customException = new CustomException("custom exception", undetectedIOException);
    IOException ioException = new IOException("some IO exception", customException);
    return new Exception("undetected nested exception", ioException);
  }

  private static CycledCauseException createUndetectedCycledCauseException() {
    CycledCauseException cycledCauseException = new CycledCauseException("cycled cause exception");
    Assert.assertSame(cycledCauseException, cycledCauseException.getCause());

    return cycledCauseException;
  }

  public final ExceptionTestParameter m_testParameter;

  public ConnectionErrorDetectorTest(ExceptionTestParameter testParameter) {
    m_testParameter = testParameter;
  }

  @Test
  public void testIsConnectionError() {
    ConnectionErrorDetector connectionErrorDetector = BEANS.get(ConnectionErrorDetector.class);
    assertEquals(m_testParameter.isDetectable(), connectionErrorDetector.isConnectionError(m_testParameter.getException()));
  }

  private static class ClientAbortException extends IOException {

    private static final long serialVersionUID = 1L;

    public ClientAbortException(String message) {
      super(message);
    }
  }

  private static class EofException extends IOException {

    private static final long serialVersionUID = 1L;

    public EofException(String message) {
      super(message);
    }
  }

  private static class CustomException extends Exception {

    private static final long serialVersionUID = 1L;

    public CustomException(String message) {
      super(message);
    }

    public CustomException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  private static class CycledCauseException extends Exception {

    private static final long serialVersionUID = 1L;

    public CycledCauseException(String message) {
      super(message);
    }

    @Override
    public synchronized Throwable getCause() {
      return ObjectUtility.nvl(super.getCause(), this);
    }
  }

  public static class ExceptionTestParameter extends AbstractScoutTestParameter {

    private Exception m_exception;
    private boolean m_isDetectable;

    public ExceptionTestParameter(Exception exception, boolean isDetectable) {
      super(exception == null ? "null" : exception.getClass().getSimpleName() + ":" + exception.getMessage());
      m_exception = exception;
      m_isDetectable = isDetectable;
    }

    public Exception getException() {
      return m_exception;
    }

    public boolean isDetectable() {
      return m_isDetectable;
    }
  }
}
