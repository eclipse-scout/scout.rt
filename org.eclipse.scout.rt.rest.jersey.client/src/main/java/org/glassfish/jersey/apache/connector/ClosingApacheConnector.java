/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
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
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.HTTP;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.util.BooleanUtility;
import org.eclipse.scout.rt.platform.util.FinalValue;
import org.eclipse.scout.rt.platform.util.IRegistrationHandle;
import org.eclipse.scout.rt.platform.util.concurrent.ICancellable;
import org.eclipse.scout.rt.rest.client.RestClientProperties;
import org.eclipse.scout.rt.rest.jersey.client.RestEnsureHttpHeaderConnectionCloseProperty;
import org.eclipse.scout.rt.rest.jersey.client.ScoutApacheConnector;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.client.RequestEntityProcessing;
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
 * @deprecated will be removed with release 23.0. Use {@link ScoutApacheConnector} instead.
 */
@Deprecated
class ClosingApacheConnector extends ApacheConnector {

  private static final Logger LOGGER = Logger.getLogger(ClosingApacheConnector.class.getName());
  private static final FinalValue<Boolean> WRITE_OUT_BOUND_HEADERS_WITH_CLIENT_REQUEST = new FinalValue<>();

  public ClosingApacheConnector(Client client, Configuration config) {
    super(client, config);
  }

  @Override
  @SuppressWarnings({"resource", "squid:S1141", "squid:RedundantThrowsDeclarationCheck"})
  public ClientResponse apply(final ClientRequest clientRequest) throws ProcessingException {
    preprocessClientRequest(clientRequest);

    final HttpUriRequest request = getUriHttpRequest(clientRequest);

    // Work around for rare abnormal connection terminations (258238)
    ensureHttpHeaderCloseConnection(clientRequest, request);
    final Map<String, String> clientHeadersSnapshot = writeOutBoundHeaders(clientRequest, request);

    final IRegistrationHandle cancellableHandle = registerCancellable(clientRequest, request);

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
      HeaderUtils.checkHeaderChanges(clientHeadersSnapshot, clientRequest.getHeaders(), this.getClass().getName(), null);

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
   * Enables cookies if enabled by {@link RestClientProperties#ENABLE_COOKIES} and disables chunked transfer encoding if
   * disabled by {@link RestClientProperties#DISABLE_CHUNKED_TRANSFER_ENCODING}.
   * <p>
   * If corresponding properties are already set, their values are not overridden.
   * <p>
   * Must be called before {@link #getUriHttpRequest(ClientRequest)}.
   */
  protected void preprocessClientRequest(ClientRequest clientRequest) {
    if (!isCookiePropertySet(clientRequest) && !BooleanUtility.nvl(clientRequest.resolveProperty(RestClientProperties.ENABLE_COOKIES, Boolean.class))) {
      clientRequest.setProperty(ApacheClientProperties.DISABLE_COOKIES, true);
    }

    if (clientRequest.getProperty(ClientProperties.REQUEST_ENTITY_PROCESSING) == null && BooleanUtility.nvl(clientRequest.resolveProperty(RestClientProperties.DISABLE_CHUNKED_TRANSFER_ENCODING, Boolean.class))) {
      clientRequest.setProperty(ClientProperties.REQUEST_ENTITY_PROCESSING, RequestEntityProcessing.BUFFERED);
    }

    boolean suppressDefaultUserAgent = BooleanUtility.nvl(clientRequest.resolveProperty(RestClientProperties.SUPPRESS_DEFAULT_USER_AGENT, false));
    if (!suppressDefaultUserAgent && !clientRequest.getHeaders().containsKey(HttpHeaders.USER_AGENT)) {
      clientRequest.getHeaders().add(HttpHeaders.USER_AGENT, "Generic");
    }
  }

  protected boolean isCookiePropertySet(ClientRequest clientRequest) {
    if (clientRequest.getProperty(ApacheClientProperties.DISABLE_COOKIES) != null) {
      return true;
    }

    RequestConfig requestConfig = (RequestConfig) clientRequest.getConfiguration().getProperty(ApacheClientProperties.REQUEST_CONFIG);
    if (requestConfig == null) {
      return false; // absent -> default request configuration
    }

    return requestConfig.getCookieSpec() != null; // e.g. standard
  }

  /**
   * Adds the HTTP header {@code Connection: close} if {@code RestClientProperties.CONNECTION_CLOSE} is {@code true} or
   * {@link RestEnsureHttpHeaderConnectionCloseProperty} is {@code true} and the given {@code headers} do not contain
   * the key {@code Connection}.
   */
  protected void ensureHttpHeaderCloseConnection(ClientRequest clientRequest, HttpUriRequest httpRequest) {
    boolean closeConnection = BooleanUtility.nvl(clientRequest.resolveProperty(RestClientProperties.CONNECTION_CLOSE, CONFIG.getPropertyValue(RestEnsureHttpHeaderConnectionCloseProperty.class)), true);
    MultivaluedMap<String, Object> headers = clientRequest.getHeaders();
    if (closeConnection && !headers.containsKey(HTTP.CONN_DIRECTIVE)) {
      LOGGER.finest("Adding HTTP header '" + HTTP.CONN_DIRECTIVE + ": " + HTTP.CONN_CLOSE + "'");
      httpRequest.setHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_CLOSE);
    }
  }

  /**
   * Registers an {@link ICancellable} if this method is invoked in the context of a {@link RunMonitor} (i.e.
   * {@link RunMonitor#CURRENT} is not {@code null}).
   */
  protected IRegistrationHandle registerCancellable(ClientRequest clientRequest, final HttpUriRequest request) {
    final RunMonitor runMonitor = RunMonitor.CURRENT.get();
    if (runMonitor == null) {
      return IRegistrationHandle.NULL_HANDLE;
    }
    ICancellable cancellable;
    Object c = clientRequest.getProperty(RestClientProperties.CANCELLABLE);
    if (c instanceof ICancellable) {
      // use cancellable provided by the client request and ignore the default HTTP connection-aborting strategy
      cancellable = (ICancellable) c;
    }
    else {
      if (c != null) {
        LOGGER.fine("non-null cancellable has unexpected type: " + c.getClass());
      }
      cancellable = new ICancellable() {
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
    }
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
  private static Map<String, String> writeOutBoundHeaders(final ClientRequest clientRequest, final HttpUriRequest request) {
    // Apache connector 2.30.1 uses a different API for this private method
    if (WRITE_OUT_BOUND_HEADERS_WITH_CLIENT_REQUEST.setIfAbsentAndGet(ClosingApacheConnector::createWriteOutboundHeadersFunction)) {
      return invokePrivateMethod(
          "writeOutBoundHeaders",
          new Class[]{ClientRequest.class, HttpUriRequest.class},
          Map.class,
          null,
          new Object[]{clientRequest, request});
    }
    else {
      return invokePrivateMethod(
          "writeOutBoundHeaders",
          new Class[]{MultivaluedMap.class, HttpUriRequest.class},
          Map.class,
          null,
          new Object[]{clientRequest.getHeaders(), request});
    }
  }

  private static boolean createWriteOutboundHeadersFunction() {
    boolean useApiWithClientRequest = false;
    try {
      ApacheConnector.class.getDeclaredMethod("writeOutBoundHeaders", ClientRequest.class, HttpUriRequest.class);
      useApiWithClientRequest = true;
    }
    catch (Exception e) {
      LOGGER.log(Level.FINER, "writeOutBoundHeaders(ClientRequest,class, HttpUriRequest.class) does not exist", e);
    }
    LOGGER.info("using writeOutBoundHeaders with clientRequest: " + useApiWithClientRequest);
    return useApiWithClientRequest;
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
