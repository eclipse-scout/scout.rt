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

import static java.util.Collections.singleton;

import java.io.IOException;
import java.util.Set;

import jakarta.annotation.Priority;
import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.ext.WriterInterceptor;
import jakarta.ws.rs.ext.WriterInterceptorContext;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.ConnectionErrorDetector;
import org.eclipse.scout.rt.rest.RestApplication.IRestApplicationSingletonsContributor;
import org.glassfish.jersey.inject.hk2.AbstractBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interceptor to ignore connection errors. This might e.g. happen if the client that initiated the request is disconnected.
 */
@Priority(5)
@Singleton
public class ConnectionErrorInterceptor implements WriterInterceptor {

  private static final Logger LOG = LoggerFactory.getLogger(ConnectionErrorInterceptor.class);

  @Override
  public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
    try {
      context.proceed();
    }
    catch (Exception e) {
      if (BEANS.get(ConnectionErrorDetector.class).isConnectionError(e)) {
        // Ignore disconnected errors: we do not want to throw an exception, if the client closed the connection.
        LOG.debug("Connection error while reading from the request.", e);
      }
      else {
        throw e;
      }
    }
  }

  public static class ConnectionErrorInterceptorContributor implements IRestApplicationSingletonsContributor {
    @Override
    public Set<Object> contribute() {
      return singleton(new AbstractBinder() {
        @Override
        protected void configure() {
          //noinspection unchecked
          bind(ConnectionErrorInterceptor.class)
              .to(WriterInterceptor.class)
              .in(Singleton.class);
        }
      });
    }
  }
}
