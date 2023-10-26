/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.http.retry;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.net.ssl.SSLException;

import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.concurrent.CancellableDependency;
import org.apache.hc.core5.http.ConnectionClosedException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.NoHttpResponseException;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.TimeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fix for issue 'NoHttpResponseException: localhost failed to respond' on stale socket channels
 *
 * @since 7.0
 */
public class CustomHttpRequestRetryStrategy extends DefaultHttpRequestRetryStrategy {
  private static final Logger LOG = LoggerFactory.getLogger(CustomHttpRequestRetryStrategy.class);

  private final Set<Class<? extends IOException>> m_nonRetriableClasses;
  private final boolean m_retryOnNoHttpResponseException;
  private final boolean m_retryOnSocketExceptionByConnectionReset;
  private final int m_maxRetries;

  public CustomHttpRequestRetryStrategy(final int maxRetries, final boolean retryOnNoHttpResponseException, final boolean retryOnSocketExceptionByConnectionReset) {
    this(
        maxRetries,
        Arrays.asList(
            InterruptedIOException.class,
            UnknownHostException.class,
            ConnectException.class,
            ConnectionClosedException.class,
            SSLException.class),
        retryOnNoHttpResponseException,
        retryOnSocketExceptionByConnectionReset,
        Arrays.asList(
            HttpStatus.SC_TOO_MANY_REQUESTS,
            HttpStatus.SC_SERVICE_UNAVAILABLE));
  }

  protected CustomHttpRequestRetryStrategy(final int maxRetries,
                                           final Collection<Class<? extends IOException>> clazzes,
                                           final boolean retryOnNoHttpResponseException,
                                           final boolean retryOnSocketExceptionByConnectionReset, List<Integer> codes) {
    super(maxRetries, TimeValue.ofSeconds(1L), clazzes, codes);
    m_nonRetriableClasses = new HashSet<>(clazzes);
    m_retryOnNoHttpResponseException = retryOnNoHttpResponseException;
    m_retryOnSocketExceptionByConnectionReset = retryOnSocketExceptionByConnectionReset;
    m_maxRetries = maxRetries;
  }

  @Override
  public boolean retryRequest(
      final HttpRequest request,
      final IOException exception,
      final int execCount,
      final HttpContext context) {
    // method copied from super-class except to add detectStaleSocketChannel call
    Args.notNull(request, "request");
    Args.notNull(exception, "exception");

    if (execCount > m_maxRetries) {
      // Do not retry if over max retries
      return false;
    }
    if (this.m_nonRetriableClasses.contains(exception.getClass())) {
      return false;
    } else {
      for (final Class<? extends IOException> rejectException : this.m_nonRetriableClasses) {
        if (rejectException.isInstance(exception)) {
          return false;
        }
      }
    }
    if (request instanceof CancellableDependency && ((CancellableDependency) request).isCancelled()) {
      return false;
    }

    // Retry if the request is considered idempotent or for stale socket channel (assumption)
    return handleAsIdempotent(request) || detectStaleSocketChannel(exception, context);
  }

  /**
   * Fix for NoHttpResponseException that can occur even if connection check is done in millisecond interval
   * <p>
   * http://hc.apache.org/httpcomponents-client-ga/tutorial/html/connmgmt.html#d5e659
   */
  protected boolean detectStaleSocketChannel(IOException exception, HttpContext context) {
    boolean retry;
    if (m_retryOnNoHttpResponseException && exception instanceof NoHttpResponseException) {
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
