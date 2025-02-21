/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.jersey;

import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response.Status;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.resource.MimeType;
import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.client.spi.AsyncConnectorCallback;
import org.glassfish.jersey.client.spi.Connector;
import org.glassfish.jersey.message.internal.HeaderUtils;

/**
 * Mock connector for consumers which do not have a real provider available during unit tests.
 */
@ApplicationScoped
public class TestJerseyMockConnector implements Connector {

  private ExecutorService m_executor = Executors.newSingleThreadExecutor();

  private byte[] m_lastRequest;
  private Map<String, String> m_lastRequestHeaders;
  private Status m_nextResponseStatus = Status.OK;
  private InputStream m_nextResponseEntityStream;

  @Override
  public String getName() {
    return getClass().getName();
  }

  @Override
  public void close() {
    // nop: nothin' to close, this is a mock connector
  }

  @Override
  public Future<?> apply(ClientRequest request, AsyncConnectorCallback callback) {
    return m_executor.submit(() -> callback.response(apply(request)));
  }

  @Override
  public ClientResponse apply(ClientRequest request) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    request.setStreamProvider(contentLength -> baos);
    try {
      request.writeEntity();
    }
    catch (IOException e) {
      throw new ProcessingException("An error occured", e);
    }
    m_lastRequest = baos.toByteArray();
    m_lastRequestHeaders = HeaderUtils.asStringHeadersSingleValue(request.getHeaders(), request.getConfiguration());

    ClientResponse clientResponse = new ClientResponse(m_nextResponseStatus, request);
    assertNotNull(m_nextResponseEntityStream);
    clientResponse.getHeaders().add(HttpHeaders.CONTENT_TYPE, MimeType.JSON.getType());
    clientResponse.setEntityStream(m_nextResponseEntityStream);
    m_nextResponseEntityStream = null;
    return clientResponse;
  }

  /**
   * Get the last request, e.g. for assertions
   */
  public byte[] getLastRequest() {
    return m_lastRequest;
  }

  /**
   * Get the last request headers, e.g. for assertions
   */
  public Map<String, String> getLastRequestHeaders() {
    return Collections.unmodifiableMap(m_lastRequestHeaders);
  }

  /**
   * Set the next response status code
   */
  public void setNextResponseStatus(Status nextResponseStatus) {
    m_nextResponseStatus = nextResponseStatus;
  }

  /**
   * Set the next response
   */
  public void setNextResponseEntityStream(InputStream is) {
    m_nextResponseEntityStream = is;
  }
}
