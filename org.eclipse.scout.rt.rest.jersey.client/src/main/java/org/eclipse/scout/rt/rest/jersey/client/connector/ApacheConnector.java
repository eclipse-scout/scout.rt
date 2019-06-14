/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package org.eclipse.scout.rt.rest.jersey.client.connector;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.util.concurrent.ICancellable;
import org.eclipse.scout.rt.shared.http.transport.ApacheHttpTransport;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.client.RequestEntityProcessing;
import org.glassfish.jersey.client.spi.AsyncConnectorCallback;
import org.glassfish.jersey.client.spi.Connector;
import org.glassfish.jersey.message.internal.HeaderUtils;
import org.glassfish.jersey.message.internal.OutboundMessageContext;
import org.glassfish.jersey.message.internal.ReaderWriter;
import org.glassfish.jersey.message.internal.Statuses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link Connector} that utilizes the Apache HTTP Client to send and receive HTTP request and responses.
 */
public class ApacheConnector implements Connector {

  private static final Logger LOG = LoggerFactory.getLogger(ApacheConnector.class);

  private final CloseableHttpClient m_client;
  private final RequestConfig m_requestConfig;

  /**
   * Create the new Apache HTTP Client connector.
   *
   * @param client
   *          JAX-RS client instance for which the connector is being created.
   * @param config
   *          client configuration.
   */
  ApacheConnector(final Client client, final Configuration config) {
    m_client = (CloseableHttpClient) ((ApacheHttpTransport) BEANS.get(RestClientHttpTransportManager.class).getHttpTransport()).getHttpClient();
    m_requestConfig = RequestConfig.custom().build();
  }

  @Override
  public ClientResponse apply(final ClientRequest clientRequest) {
    final HttpUriRequest request = getUriHttpRequest(clientRequest);
    final Map<String, String> clientHeadersSnapshot = writeOutBoundHeaders(clientRequest.getHeaders(), request);

    registerCancellable(request);

    try {
      final CloseableHttpResponse response;
      final HttpClientContext context = HttpClientContext.create();
//      if (m_preemptiveBasicAuth) {
//        final AuthCache authCache = new BasicAuthCache();
//        final BasicScheme basicScheme = new BasicScheme();
//        authCache.put(getHost(request), basicScheme);
//        context.setAuthCache(authCache);
//      }

      // If a request-specific CredentialsProvider exists, use it instead of the default one
//      CredentialsProvider credentialsProvider =
//          clientRequest.resolveProperty(ApacheClientProperties.CREDENTIALS_PROVIDER, CredentialsProvider.class);
//      if (credentialsProvider != null) {
//        context.setCredentialsProvider(credentialsProvider);
//      }

      response = m_client.execute(getHost(request), request, context);
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
        responseContext.setEntityStream(new HttpClientResponseInputStream(getInputStream(response)));
      }
      catch (final IOException e) {
        LOG.warn("Failed to set response entity stream", e);
      }

      return responseContext;
    }
    catch (final Exception e) {
      throw new ProcessingException(e);
    }
  }

  /**
   * Registers an {@link ICancellable} if this method is invoked in the context of a {@link RunMonitor} (i.e.
   * {@link RunMonitor#CURRENT} is not {@code null}).
   */
  protected void registerCancellable(final HttpUriRequest request) {
    RunMonitor runMonitor = RunMonitor.CURRENT.get();
    if (runMonitor != null) {
      runMonitor.registerCancellable(new ICancellable() {

        @Override
        public boolean isCancelled() {
          return request.isAborted();
        }

        @Override
        public boolean cancel(boolean interruptIfRunning) {
          LOG.info("Aborting HTTP REST request");
          request.abort();
          return true;
        }
      });
    }
  }

  @Override
  public Future<?> apply(final ClientRequest request, final AsyncConnectorCallback callback) {
    try {
      ClientResponse response = apply(request);
      callback.response(response);
      return CompletableFuture.completedFuture(response);
    }
    catch (Throwable t) {
      callback.failure(t);
      CompletableFuture<Object> future = new CompletableFuture<>();
      future.completeExceptionally(t);
      return future;
    }
  }

  @Override
  public String getName() {
    return "Apache HttpClient Jersey Connector";
  }

  @Override
  public void close() {
    try {
      m_client.close(); // FIXME pbz need to close here or rely on httptransportmanager
    }
    catch (final IOException e) {
      throw new ProcessingException("FAILED_TO_STOP_CLIENT", e);
    }
  }

  protected HttpHost getHost(final HttpUriRequest request) {
    return new HttpHost(request.getURI().getHost(), request.getURI().getPort(), request.getURI().getScheme());
  }

  protected HttpUriRequest getUriHttpRequest(final ClientRequest clientRequest) {
    final RequestConfig.Builder requestConfigBuilder = RequestConfig.copy(m_requestConfig);

    final int connectTimeout = clientRequest.resolveProperty(ClientProperties.CONNECT_TIMEOUT, -1);
    final int socketTimeout = clientRequest.resolveProperty(ClientProperties.READ_TIMEOUT, -1);

    if (connectTimeout >= 0) {
      requestConfigBuilder.setConnectTimeout(connectTimeout);
    }
    if (socketTimeout >= 0) {
      requestConfigBuilder.setSocketTimeout(socketTimeout);
    }

    final Boolean redirectsEnabled =
        clientRequest.resolveProperty(ClientProperties.FOLLOW_REDIRECTS, m_requestConfig.isRedirectsEnabled());
    requestConfigBuilder.setRedirectsEnabled(redirectsEnabled);

    final Boolean bufferingEnabled = clientRequest.resolveProperty(ClientProperties.REQUEST_ENTITY_PROCESSING,
        RequestEntityProcessing.class) == RequestEntityProcessing.BUFFERED;
    final HttpEntity entity = getHttpEntity(clientRequest, bufferingEnabled);

    return RequestBuilder
        .create(clientRequest.getMethod())
        .setUri(clientRequest.getUri())
        .setConfig(requestConfigBuilder.build())
        .setEntity(entity)
        .build();
  }

  protected HttpEntity getHttpEntity(final ClientRequest clientRequest, final boolean bufferingEnabled) {
    final Object entity = clientRequest.getEntity();

    if (entity == null) {
      return null;
    }

    final AbstractHttpEntity httpEntity = new AbstractHttpEntity() {
      @Override
      public boolean isRepeatable() {
        return false;
      }

      @Override
      public long getContentLength() {
        return -1;
      }

      @Override
      public InputStream getContent() throws IOException {
        if (bufferingEnabled) {
          final ByteArrayOutputStream buffer = new ByteArrayOutputStream(512);
          writeTo(buffer);
          return new ByteArrayInputStream(buffer.toByteArray());
        }
        else {
          return null;
        }
      }

      @Override
      public void writeTo(final OutputStream outputStream) throws IOException {
        clientRequest.setStreamProvider(new OutboundMessageContext.StreamProvider() {
          @Override
          public OutputStream getOutputStream(final int contentLength) throws IOException {
            return outputStream;
          }
        });
        clientRequest.writeEntity();
      }

      @Override
      public boolean isStreaming() {
        return false;
      }
    };

    if (bufferingEnabled) {
      try {
        return new BufferedHttpEntity(httpEntity);
      }
      catch (final IOException e) {
        throw new ProcessingException("ERROR_BUFFERING_ENTITY", e);
      }
    }
    else {
      return httpEntity;
    }
  }

  protected Map<String, String> writeOutBoundHeaders(final MultivaluedMap<String, Object> headers,
      final HttpUriRequest request) {
    final Map<String, String> stringHeaders = HeaderUtils.asStringHeadersSingleValue(headers);

    for (final Map.Entry<String, String> e : stringHeaders.entrySet()) {
      request.addHeader(e.getKey(), e.getValue());
    }
    return stringHeaders;
  }

  // FIXME pbz unnötiger stream
  private static final class HttpClientResponseInputStream extends FilterInputStream {

    HttpClientResponseInputStream(final InputStream inputStream) throws IOException {
      super(inputStream);
    }
  }

  protected InputStream getInputStream(final CloseableHttpResponse response) throws IOException {

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
}
