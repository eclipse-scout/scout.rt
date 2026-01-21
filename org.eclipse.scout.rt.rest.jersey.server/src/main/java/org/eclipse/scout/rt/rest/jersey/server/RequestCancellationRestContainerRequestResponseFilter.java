/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.jersey.server;

import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.util.ConnectionErrorDetector;
import org.eclipse.scout.rt.rest.RestHttpHeaders;
import org.eclipse.scout.rt.rest.cancellation.RestRequestCancellationRegistry;
import org.eclipse.scout.rt.rest.container.IRestContainerRequestFilter;
import org.eclipse.scout.rt.rest.container.IRestContainerResponseFilter;
import org.eclipse.scout.rt.security.IAccessControlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link IRestContainerRequestFilter} registering requests with {@code RestHttpHeaders#REQUEST_ID} into {@link RestRequestCancellationRegistry}.
 * {@link IRestContainerResponseFilter} wrapping the response output stream in order to be able to ignore connection errors when closing the stream and de-registering requests in {@link RestRequestCancellationRegistry}.
 */
public class RequestCancellationRestContainerRequestResponseFilter implements IRestContainerRequestFilter, IRestContainerResponseFilter {

  private static final Logger LOG = LoggerFactory.getLogger(RequestCancellationRestContainerRequestResponseFilter.class);

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    registerRunMonitor(requestContext);
  }

  @Override
  public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
    String requestId = requestContext.getHeaderString(RestHttpHeaders.REQUEST_ID);
    OutputStream out = responseContext.getEntityStream();
    responseContext.setEntityStream(new P_WrappingOutputStream(out, requestId));
  }

  protected void registerRunMonitor(ContainerRequestContext request) {
    String requestId = request.getHeaderString(RestHttpHeaders.REQUEST_ID);
    if (requestId == null) {
      LOG.trace("cancellation not supported by this request: HTTP header '" + RestHttpHeaders.REQUEST_ID + "' is missing");
      return;
    }

    RunContext runContext = RunContext.CURRENT.get();
    if (runContext == null) {
      LOG.trace("cancellation not supported by this request: not running within a run context");
      return;
    }

    Object userId = resolveUserId(request);
    getCancellationRegistry().register(requestId, userId, runContext);
  }

  /**
   * Returns the user id of the given request. May be {@code null}.
   */
  protected Object resolveUserId(ContainerRequestContext request) {
    return BEANS.get(IAccessControlService.class).getUserIdOfCurrentSubject();
  }

  /**
   * Returns the cancellation registry that manages requests passing this filter (e.g. if different entry points are
   * managed independently). The default is {@link RestRequestCancellationRegistry}.
   */
  protected RestRequestCancellationRegistry getCancellationRegistry() {
    return BEANS.get(RestRequestCancellationRegistry.class);
  }

  /**
   * {@link OutputStream} implementation wrapping an {@link OutputStream} and ignoring connection errors when closing the stream.
   *
   * @see ConnectionErrorDetector#isConnectionError(Throwable)
   */
  protected static class P_WrappingOutputStream extends OutputStream {

    protected final OutputStream m_out;
    protected final String m_requestId;

    P_WrappingOutputStream(OutputStream out, String requestId) {
      m_out = out;
      m_requestId = requestId;
    }

    @Override
    public void write(int b) throws IOException {
      m_out.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
      m_out.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
      m_out.write(b, off, len);
    }

    @Override
    public void close() throws IOException {
      if (Thread.interrupted()) {
        // even if our application did run interrupted we should return to normal operation now before close (required as application server session manager may need non-interrupted state)
        LOG.debug("Reset interrupted state - {}", m_requestId);
      }

      try {
        m_out.close();
      }
      catch (EOFException e) {
        if (BEANS.get(ConnectionErrorDetector.class).isConnectionError(e)) {
          // Ignore disconnect errors: we do not want to throw an exception, if the client closed the connection.
          LOG.debug("EOFException when closing REST response output stream: ", e);
        }
        else {
          throw e;
        }
      }
      finally {
        // unregister cancellation support after closing output stream (e.g. finished to completely write response)
        if (m_requestId != null) {
          BEANS.get(RestRequestCancellationRegistry.class).unregister(m_requestId);
        }
      }
    }

    @Override
    public void flush() throws IOException {
      m_out.flush();
    }
  }
}
