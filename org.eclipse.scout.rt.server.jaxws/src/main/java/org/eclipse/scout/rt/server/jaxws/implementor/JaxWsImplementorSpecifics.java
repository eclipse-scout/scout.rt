/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.jaxws.implementor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.http.HTTPException;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.server.jaxws.provider.auth.handler.WebServiceRequestRejectedException;

/**
 * This class encapsulates functionality as defined by JAX-WS JSR 224 but may divergence among JAX-WS implementations.
 * <p>
 * Replace this class via {@link Replace} annotation if your provider implementation divergences from the JAX-WS
 * standard.
 */
@ApplicationScoped
public class JaxWsImplementorSpecifics {

  public static final String PROP_HTTP_REQUEST_HEADERS = "org.eclipse.scout.jaxws.http.request.headers";
  public static final String PROP_HTTP_RESPONSE_HEADERS = "org.eclipse.scout.jaxws.http.response.headers";
  public static final String PROP_HTTP_RESPONSE_CODE = "org.eclipse.scout.jaxws.http.response.code";
  public static final String PROP_SOCKET_CONNECT_TIMEOUT = "org.eclipse.scout.jaxws.timeout.connect";
  public static final String PROP_SOCKET_READ_TIMEOUT = "org.eclipse.scout.jaxws.timeout.read";

  protected final Map<String, String> m_implementorContextProperties = new HashMap<>();

  @PostConstruct
  protected void initConfig() {
    m_implementorContextProperties.put(PROP_HTTP_REQUEST_HEADERS, MessageContext.HTTP_REQUEST_HEADERS);
    m_implementorContextProperties.put(PROP_HTTP_RESPONSE_HEADERS, MessageContext.HTTP_RESPONSE_HEADERS);
    m_implementorContextProperties.put(PROP_HTTP_RESPONSE_CODE, MessageContext.HTTP_RESPONSE_CODE);
    m_implementorContextProperties.put(PROP_SOCKET_CONNECT_TIMEOUT, null /* implementor specific */);
    m_implementorContextProperties.put(PROP_SOCKET_READ_TIMEOUT, null /* implementor specific */);
  }

  /**
   * Returns the implementor's name and its version.
   */
  public String getVersionInfo() {
    return "unknown";
  }

  /**
   * Returns the HTTP request header, or an empty <code>List</code> if not found.
   */
  public List<String> getHttpRequestHeader(final Map<String, Object> ctx, final String key) {
    return getHttpHeader(valueOf(PROP_HTTP_REQUEST_HEADERS), ctx, key);
  }

  /**
   * Sets the given HTTP request header.
   */
  public void setHttpRequestHeader(final Map<String, Object> ctx, final String key, final String value) {
    setHttpHeader(valueOf(PROP_HTTP_REQUEST_HEADERS), ctx, key, value);
  }

  /**
   * Returns the HTTP response header, or an empty <code>List</code> if not found.
   */
  public List<String> getHttpResponseHeader(final Map<String, Object> ctx, final String key) {
    return getHttpHeader(valueOf(PROP_HTTP_RESPONSE_HEADERS), ctx, key);
  }

  /**
   * Sets the given HTTP response header.
   */
  public void setHttpResponseHeader(final Map<String, Object> ctx, final String key, final String value) {
    setHttpHeader(valueOf(PROP_HTTP_RESPONSE_HEADERS), ctx, key, value);
  }

  /**
   * Returns the HTTP response code.
   */
  public Integer getHttpResponseCode(final Map<String, Object> ctx) {
    return (Integer) ctx.get(valueOf(PROP_HTTP_RESPONSE_CODE));
  }

  /**
   * Sets the given HTTP status code.
   */
  public void setHttpResponseCode(final Map<String, Object> ctx, final int httpResponseCode) {
    ctx.put(valueOf(PROP_HTTP_RESPONSE_CODE), httpResponseCode);
  }

  /**
   * Clears the HTTP status code.
   */
  public void clearHttpResponseCode(final Map<String, Object> ctx) {
    ctx.remove(valueOf(PROP_HTTP_RESPONSE_CODE));
  }

  /**
   * Sets the given socket connect timeout [ms].
   */
  public void setSocketConnectTimeout(final Map<String, Object> requestContext, final int timeoutMillis) {
    if (timeoutMillis > 0) {
      requestContext.put(valueOf(PROP_SOCKET_CONNECT_TIMEOUT), timeoutMillis);
    }
    else {
      requestContext.remove(valueOf(PROP_SOCKET_CONNECT_TIMEOUT));
    }
  }

  /**
   * Sets the given socket read timeout [ms].
   */
  public void setSocketReadTimeout(final Map<String, Object> requestContext, final int timeoutMillis) {
    if (timeoutMillis > 0) {
      requestContext.put(valueOf(PROP_SOCKET_READ_TIMEOUT), timeoutMillis);
    }
    else {
      requestContext.remove(valueOf(PROP_SOCKET_READ_TIMEOUT));
    }
  }

  /**
   * Closes the Socket for the given port.
   */
  public void closeSocket(final Object port, final String operation) {
    // NOOP
  }

  @SuppressWarnings("unchecked")
  protected void setHttpHeader(final String headerProperty, final Map<String, Object> ctx, final String key, final String value) {
    final Map<String, List<String>> headers = (Map<String, List<String>>) ctx.get(headerProperty);
    ctx.put(headerProperty, CollectionUtility.putObject(headers, key, CollectionUtility.appendList(CollectionUtility.getObject(headers, key), value)));
  }

  @SuppressWarnings("unchecked")
  protected List<String> getHttpHeader(final String headerProperty, final Map<String, Object> ctx, final String key) {
    final Map<String, List<String>> headers = (Map<String, List<String>>) ctx.get(headerProperty);
    return CollectionUtility.arrayList(CollectionUtility.getObject(headers, key));
  }

  /**
   * Returns the implementor specific property name, or throws {@link UnsupportedOperationException} if not supported by
   * the implementor.
   */
  protected String valueOf(final String property) {
    final String implementorContextProperty = m_implementorContextProperties.get(property);
    if (implementorContextProperty == null) {
      throw new UnsupportedOperationException(String.format("Functionality '%s' not supported by the JAX-WS implementor '%s'", property, getClass().getSimpleName()));
    }
    else {
      return implementorContextProperty;
    }
  }

  /**
   * Method invoked by a {@link Handler}, if the call chain should not be continued.
   * <p>
   * If throwing {@link WebServiceRequestRejectedException}, the handler chain is exit by throwing a
   * {@link HTTPException}, or by returning <code>false</code> otherwise.
   * <p>
   * The default implementation does nothing.
   * <p>
   * Some JAX-WS implementors (like METRO v2.2.10) do not exit the call chain if the {@link Handler} returns with
   * <code>false</code>. That happens for one-way communication requests. As a result, the endpoint operation is still
   * invoked.
   */
  public void interceptWebServiceRequestRejected(final MessageContext messageContext, final int httpStatusCode) throws WebServiceRequestRejectedException {
  }
}
