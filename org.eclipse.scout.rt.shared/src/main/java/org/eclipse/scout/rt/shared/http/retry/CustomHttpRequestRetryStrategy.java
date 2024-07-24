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

import org.apache.hc.client5.http.HttpRequestRetryStrategy;
import org.apache.hc.client5.http.classic.ExecChain;
import org.apache.hc.client5.http.classic.ExecChain.Scope;
import org.apache.hc.client5.http.classic.ExecChainHandler;
import org.apache.hc.client5.http.impl.ChainElement;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.concurrent.CancellableDependency;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ConnectionClosedException;
import org.apache.hc.core5.http.HttpException;
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

  public static final String RETRY_ENABLER_EXEC_CHAIN_NAME = OneTimeRetryPrepareExecChainHandler.class.getSimpleName();

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

  /**
   * Register this {@link HttpRequestRetryStrategy} for this builder. For async-clients no
   * {@link OneTimeRepeatableRequestEntityProxy} will be installed (= no one-time retry for stale-socket channels).
   */
  public void enable(HttpAsyncClientBuilder builder) {
    // no AsyncExecChainHandler implementation for OneTimeRetryPrepareExec (and corresponding proxy installed there)
    builder.setRetryStrategy(this);
  }

  /**
   * Register this {@link HttpRequestRetryStrategy} for this builder. Also {@link OneTimeRepeatableRequestEntityProxy}
   * will be used for certain stale-socket channel errors, see {@link #detectStaleSocketChannel(IOException)}
   * (regardless of repeatability of the actual entity).
   */
  public void enable(HttpClientBuilder builder) {
    builder.addExecInterceptorAfter(ChainElement.RETRY.name(), RETRY_ENABLER_EXEC_CHAIN_NAME, new OneTimeRetryPrepareExecChainHandler());
    builder.setRetryStrategy(this);
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
    }
    else {
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
    return handleAsIdempotent(request) || detectStaleSocketChannel(exception);
  }

  /**
   * Fix for NoHttpResponseException that can occur even if connection check is done in millisecond interval
   * <p>
   * http://hc.apache.org/httpcomponents-client-ga/tutorial/html/connmgmt.html#d5e659
   */
  public boolean detectStaleSocketChannel(IOException exception) {
    if (checkRetryNoHttpResponseException(exception)) {
      String message = "detected a 'NoHttpResponseException', assuming a stale socket channel; retry non-idempotent request";
      if (Thread.currentThread().isInterrupted()) {
        LOG.debug(message);
      }
      else {
        LOG.warn(message);
      }
      return true;
    }
    else if (checkRetrySocketException(exception)) {
      String message = "detected a 'SocketException: Connection reset', assuming a stale socket channel; retry non-idempotent request";
      if (Thread.currentThread().isInterrupted()) {
        LOG.debug(message);
      }
      else {
        LOG.warn(message);
      }
      return true;
    }

    return false;
  }

  protected boolean checkRetryNoHttpResponseException(IOException exception) {
    return m_retryOnNoHttpResponseException && exception instanceof NoHttpResponseException;
  }

  protected boolean checkRetrySocketException(IOException exception) {
    return m_retryOnSocketExceptionByConnectionReset && exception instanceof java.net.SocketException && "Connection reset".equals(exception.getMessage());
  }

  public class OneTimeRetryPrepareExecChainHandler implements ExecChainHandler {

    @Override
    public ClassicHttpResponse execute(ClassicHttpRequest request, Scope scope, ExecChain chain) throws IOException, HttpException {
      try {
        return chain.proceed(request, scope);
      }
      catch (IOException e) {
        if (CustomHttpRequestRetryStrategy.this.checkRetryNoHttpResponseException(e) || CustomHttpRequestRetryStrategy.this.checkRetrySocketException(e)) {
          LOG.debug("Installing {} to support retry of non-idempotent request", OneTimeRepeatableRequestEntityProxy.class, e);

          // installing a proxy to ensure isRepeatable = true at least once (keep pre-24.1 behavior)
          // even though it is not even guaranteed that original request is really repeatable
          OneTimeRepeatableRequestEntityProxy.installRetry(request);
        }
        throw e;
      }
    }
  }
}
