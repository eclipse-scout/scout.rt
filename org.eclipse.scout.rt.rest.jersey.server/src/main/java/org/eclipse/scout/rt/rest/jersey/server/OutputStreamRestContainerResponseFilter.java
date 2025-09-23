/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
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
import org.eclipse.scout.rt.platform.util.ConnectionErrorDetector;
import org.eclipse.scout.rt.rest.container.IRestContainerResponseFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link IRestContainerResponseFilter} wrapping the response output stream in order to be able to ignore connection errors when closing the stream.
 */
public class OutputStreamRestContainerResponseFilter implements IRestContainerResponseFilter {

  private static final Logger LOG = LoggerFactory.getLogger(OutputStreamRestContainerResponseFilter.class);

  @Override
  public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
    OutputStream out = responseContext.getEntityStream();
    responseContext.setEntityStream(new P_WrappingOutputStream(out));
  }

  /**
   * {@link OutputStream} implementation wrapping an {@link OutputStream} and ignoring connection errors when closing the stream.
   *
   * @see ConnectionErrorDetector#isConnectionError(Throwable)
   */
  protected static class P_WrappingOutputStream extends OutputStream {

    protected final OutputStream m_out;

    P_WrappingOutputStream(OutputStream out) {
      m_out = out;
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
    }

    @Override
    public void flush() throws IOException {
      m_out.flush();
    }
  }
}
