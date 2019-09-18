/*******************************************************************************
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.http.retry;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.net.ssl.SSLException;

import org.apache.http.HttpRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fix for issue 'NoHttpResponseException: localhost failed to respond' on stale socket channels
 *
 * @since 7.0
 */
public class CustomHttpRequestRetryHandler extends DefaultHttpRequestRetryHandler {
  private static final Logger LOG = LoggerFactory.getLogger(CustomHttpRequestRetryHandler.class);

  private final Set<Class<? extends IOException>> m_nonRetriableClasses;
  private final boolean m_retryOnNoHttpResponseException;
  private final boolean m_retryOnSocketExceptionByConnectionReset;

  public CustomHttpRequestRetryHandler(final int retryCount, final boolean requestSentRetryEnabled, final boolean retryOnNoHttpResponseException, final boolean retryOnSocketExceptionByConnectionReset) {
    this(
        retryCount,
        requestSentRetryEnabled, Arrays.asList(
            InterruptedIOException.class,
            UnknownHostException.class,
            ConnectException.class,
            SSLException.class),
        retryOnNoHttpResponseException,
        retryOnSocketExceptionByConnectionReset);
  }

  protected CustomHttpRequestRetryHandler(int retryCount, boolean requestSentRetryEnabled, Collection<Class<? extends IOException>> clazzes, final boolean retryOnNoHttpResponseException,
      final boolean retryOnSocketExceptionByConnectionReset) {
    super(retryCount, requestSentRetryEnabled, clazzes);
    m_nonRetriableClasses = new HashSet<>(clazzes);
    m_retryOnNoHttpResponseException = retryOnNoHttpResponseException;
    m_retryOnSocketExceptionByConnectionReset = retryOnSocketExceptionByConnectionReset;
  }

  @Override
  @SuppressWarnings("deprecation")
  public boolean retryRequest(
      final IOException exception,
      final int executionCount,
      final HttpContext context) {
    Args.notNull(exception, "Exception parameter");
    Args.notNull(context, "HTTP context");
    if (executionCount > getRetryCount()) {
      // Do not retry if over max retry count
      return false;
    }
    if (m_nonRetriableClasses.contains(exception.getClass())) {
      return false;
    }
    else {
      for (final Class<? extends IOException> rejectException : m_nonRetriableClasses) {
        if (rejectException.isInstance(exception)) {
          return false;
        }
      }
    }
    final HttpClientContext clientContext = HttpClientContext.adapt(context);
    final HttpRequest request = clientContext.getRequest();

    if (requestIsAborted(request)) {
      return false;
    }

    if (handleAsIdempotent(request)) {
      // Retry if the request is considered idempotent
      return true;
    }

    if (!clientContext.isRequestSent() || isRequestSentRetryEnabled()) {
      // Retry if the request has not been sent fully or
      // if it's OK to retry methods that have been sent
      return true;
    }

    if (detectStaleSocketChannel(exception, clientContext)) {
      return true;
    }

    // otherwise do not retry
    return false;
  }

  /**
   * Fix for NoHttpResponseException that can occur even if connection check is done in millisecond interval
   * <p>
   * http://hc.apache.org/httpcomponents-client-ga/tutorial/html/connmgmt.html#d5e659
   */
  protected boolean detectStaleSocketChannel(IOException exception, HttpContext context) {
    boolean retry;
    if (m_retryOnNoHttpResponseException && exception instanceof org.apache.http.NoHttpResponseException) {
      String message = "detected a 'NoHttpResponseException', assuming a stale socket channel; retry non-idempotent request";
      if (Thread.currentThread().isInterrupted()) {
        LOG.debug(message);
      }
      else {
        LOG.warn(message);
      }
      retry = true;
    }
    else if (m_retryOnSocketExceptionByConnectionReset && exception instanceof java.net.SocketException && "Connection reset".equals(exception.getMessage())) {
      String message = "detected a 'SocketException: Connection reset', assuming a stale socket channel; retry non-idempotent request";
      if (Thread.currentThread().isInterrupted()) {
        LOG.debug(message);
      }
      else {
        LOG.warn(message);
      }
      retry = true;
    }
    else {
      retry = false;
    }

    if (retry) {
      HttpRequest request = HttpClientContext.adapt(context).getRequest();
      OneTimeRepeatableRequestEntityProxy.installRetry(request);
    }
    return retry;
  }
}
