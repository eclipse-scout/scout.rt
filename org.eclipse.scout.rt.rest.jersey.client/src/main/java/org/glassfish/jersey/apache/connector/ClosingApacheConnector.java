/*
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.glassfish.jersey.apache.connector;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.AuthCache;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.CloseableHttpClient;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.util.IRegistrationHandle;
import org.eclipse.scout.rt.platform.util.concurrent.ICancellable;
import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.message.internal.HeaderUtils;
import org.glassfish.jersey.message.internal.ReaderWriter;
import org.glassfish.jersey.message.internal.Statuses;

/**
 * The original {@link ApacheConnector} does not close connections properly resulting in resource leaks. This class
 * applies a patch that will be available in version 2.29 which is not yet released.
 * <p>
 * <b>Note:</b> This class must be in the original java package because {@link ApacheConnector} is declared
 * package-private.
 *
 * @see <a href="https://github.com/eclipse-ee4j/jersey/issues/3629">Jersey Issue 3629 - ApacheConnector could throw
 *      ConnectionClosedException when using httpclient: 4.5.1+ with chunked transfer encoding</a>
 * @see <a href="https://github.com/eclipse-ee4j/jersey/pull/3861">GitHub pull request 3861</a>
 */
// TODO [9.0] abr: remove this class as soon as jersey-apache-connector has been updated to 2.29+
class ClosingApacheConnector extends ApacheConnector {

  private static final Logger LOGGER = Logger.getLogger(ClosingApacheConnector.class.getName());

  public ClosingApacheConnector(Client client, Configuration config) {
    super(client, config);
  }

  @Override
  @SuppressWarnings({"resource", "squid:S1141", "squid:RedundantThrowsDeclarationCheck"})
  public ClientResponse apply(final ClientRequest clientRequest) throws ProcessingException {
    final HttpUriRequest request = getUriHttpRequest(clientRequest);
    final Map<String, String> clientHeadersSnapshot = writeOutBoundHeaders(clientRequest.getHeaders(), request);

    final IRegistrationHandle cancellableHandle = registerCancellable(request);

    try {
      final CloseableHttpResponse response;
      final HttpClientContext context = HttpClientContext.create();
      if (isPreemptiveBasicAuth()) {
        final AuthCache authCache = new BasicAuthCache();
        final BasicScheme basicScheme = new BasicScheme();
        authCache.put(getHost(request), basicScheme);
        context.setAuthCache(authCache);
      }
      response = getClient().execute(getHost(request), request, context);
      HeaderUtils.checkHeaderChanges(clientHeadersSnapshot, clientRequest.getHeaders(), this.getClass().getName());

      final Response.StatusType status = response.getStatusLine().getReasonPhrase() == null
          ? Statuses.from(response.getStatusLine().getStatusCode())
          : Statuses.from(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());

      final ClientResponse responseContext = new ClientResponse(status, clientRequest);
      final List<URI> redirectLocations = context.getRedirectLocations();
      if (redirectLocations != null && !redirectLocations.isEmpty()) {
        responseContext.setResolvedRequestUri(redirectLocations.get(redirectLocations.size() - 1));
      }

      final Header[] respHeaders = response.getAllHeaders();
      final MultivaluedMap<String, String> headers = responseContext.getHeaders();
      for (final Header header : respHeaders) {
        final String headerName = header.getName();
        List<String> list = headers.get(headerName);
        if (list == null) {
          list = new ArrayList<>();
        }
        list.add(header.getValue());
        headers.put(headerName, list);
      }

      final HttpEntity entity = response.getEntity();

      if (entity != null) {
        if (headers.get(HttpHeaders.CONTENT_LENGTH) == null) {
          headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(entity.getContentLength()));
        }

        final Header contentEncoding = entity.getContentEncoding();
        if (headers.get(HttpHeaders.CONTENT_ENCODING) == null && contentEncoding != null) {
          headers.add(HttpHeaders.CONTENT_ENCODING, contentEncoding.getValue());
        }
      }

      try {
        responseContext.setEntityStream(getInputStream(response));
      }
      catch (final IOException e) {
        LOGGER.log(Level.SEVERE, null, e);
      }

      return responseContext;
    }
    catch (final Exception e) {
      throw new ProcessingException(e);
    }
    finally {
      cancellableHandle.dispose();
    }
  }

  /**
   * Registers an {@link ICancellable} if this method is invoked in the context of a {@link RunMonitor} (i.e.
   * {@link RunMonitor#CURRENT} is not {@code null}).
   */
  protected IRegistrationHandle registerCancellable(final HttpUriRequest request) {
    final RunMonitor runMonitor = RunMonitor.CURRENT.get();
    if (runMonitor == null) {
      return IRegistrationHandle.NULL_HANDLE;
    }
    ICancellable cancellable = new ICancellable() {
      @Override
      public boolean isCancelled() {
        return request.isAborted();
      }

      @Override
      public boolean cancel(boolean interruptIfRunning) {
        LOGGER.fine("Aborting HTTP REST request");
        request.abort();
        return true;
      }
    };
    runMonitor.registerCancellable(cancellable);
    return () -> runMonitor.unregisterCancellable(cancellable);
  }

  private static InputStream getInputStream(final CloseableHttpResponse response) throws IOException {

    final InputStream inputStream;

    if (response.getEntity() == null) {
      inputStream = new ByteArrayInputStream(new byte[0]);
    }
    else {
      @SuppressWarnings("resource")
      final InputStream i = response.getEntity().getContent();
      if (i.markSupported()) {
        inputStream = i;
      }
      else {
        inputStream = new BufferedInputStream(i, ReaderWriter.BUFFER_SIZE);
      }
    }

    return new FilterInputStream(inputStream) {
      @Override
      public void close() throws IOException {
        try {
          super.close();
        }
        catch (@SuppressWarnings("squid:S1166") IOException ex) {
          // Ignore
        }
        finally {
          response.close();
        }
      }
    };

  }

  // --- Helper methods for accessing private methods and fields in super class --------------------

  private HttpUriRequest getUriHttpRequest(final ClientRequest clientRequest) {
    return invokePrivateMethod(
        "getUriHttpRequest",
        new Class[]{ClientRequest.class},
        HttpUriRequest.class,
        this,
        new Object[]{clientRequest});
  }

  @SuppressWarnings("unchecked")
  private static Map<String, String> writeOutBoundHeaders(final MultivaluedMap<String, Object> headers, final HttpUriRequest request) {
    return invokePrivateMethod(
        "writeOutBoundHeaders",
        new Class[]{MultivaluedMap.class, HttpUriRequest.class},
        Map.class,
        null,
        new Object[]{headers, request});
  }

  private HttpHost getHost(final HttpUriRequest request) {
    return invokePrivateMethod(
        "getHost",
        new Class[]{HttpUriRequest.class},
        HttpHost.class,
        this,
        new Object[]{request});
  }

  private CloseableHttpClient getClient() {
    return readPrivateField("client", CloseableHttpClient.class);
  }

  private boolean isPreemptiveBasicAuth() {
    return readPrivateField("preemptiveBasicAuth", Boolean.class).booleanValue();
  }

  private static <T> T invokePrivateMethod(String methodName, Class[] parameterTypes, Class<T> returnType, Object instance, Object[] args) {
    try {
      Method method = ApacheConnector.class.getDeclaredMethod(methodName, parameterTypes);
      makeAccessible(method);
      return returnType.cast(method.invoke(instance, args));
    }
    catch (Exception e) {
      throw new PlatformException("Could not invoke super method {}({})", methodName, parameterTypes, e);
    }
  }

  private <T> T readPrivateField(String fieldName, Class<T> fieldType) {
    try {
      Field field = ApacheConnector.class.getDeclaredField(fieldName);
      makeAccessible(field);
      return fieldType.cast(field.get(this));
    }
    catch (Exception e) {
      throw new PlatformException("Could not read field of super class {}", fieldName, e);
    }
  }

  private static void makeAccessible(AccessibleObject o) {
    if (System.getSecurityManager() == null) {
      o.setAccessible(true);
    }
    else {
      AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
        o.setAccessible(true);
        return null;
      });
    }
  }
}
