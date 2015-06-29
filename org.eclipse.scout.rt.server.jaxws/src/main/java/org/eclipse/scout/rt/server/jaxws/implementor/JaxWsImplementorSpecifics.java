/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.jaxws.implementor;

import java.util.List;
import java.util.Map;

import javax.xml.ws.handler.MessageContext;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.annotations.Replace;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.server.jaxws.JaxWsConfigProperties.JaxWsImplementorProperty;

/**
 * This class encapsulates functionality as defined by JAX-WS JSR 224 but may divergence among JAX-WS implementations.<br/>
 * Replace this class via {@link Replace} annotation if your provider implementation divergences from the JAX-WS
 * standard.
 */
@ApplicationScoped
public class JaxWsImplementorSpecifics {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JaxWsImplementorSpecifics.class);

  /**
   * Returns the HTTP request header, or an empty <code>List</code> if not found.
   */
  public List<String> getHttpRequestHeader(final MessageContext context, final String key) {
    @SuppressWarnings("unchecked")
    final Map<String, List<String>> headerMap = (Map<String, List<String>>) context.get(MessageContext.HTTP_REQUEST_HEADERS);
    return CollectionUtility.arrayList(CollectionUtility.getObject(headerMap, key));
  }

  /**
   * Sets the given HTTP request header.
   */
  public void setHttpRequestHeader(final Map<String, Object> context, final String key, final String value) {
    @SuppressWarnings("unchecked")
    final Map<String, List<String>> headerMap = (Map<String, List<String>>) context.get(MessageContext.HTTP_REQUEST_HEADERS);
    context.put(MessageContext.HTTP_REQUEST_HEADERS, CollectionUtility.putObject(headerMap, key, CollectionUtility.appendList(CollectionUtility.getObject(headerMap, key), value)));
  }

  /**
   * Returns the HTTP response header, or an empty <code>List</code> if not found.
   */
  public List<String> getHttpResponseHeader(final Map<String, Object> context, final String key) {
    @SuppressWarnings("unchecked")
    final Map<String, List<String>> headerMap = (Map<String, List<String>>) context.get(MessageContext.HTTP_RESPONSE_HEADERS);
    return CollectionUtility.arrayList(CollectionUtility.getObject(headerMap, key));
  }

  /**
   * Sets the given HTTP response header.
   */
  public void setHttpResponseHeader(final MessageContext context, final String key, final String value) {
    @SuppressWarnings("unchecked")
    final Map<String, List<String>> headerMap = (Map<String, List<String>>) context.get(MessageContext.HTTP_RESPONSE_HEADERS);
    context.put(MessageContext.HTTP_RESPONSE_HEADERS, CollectionUtility.putObject(headerMap, key, CollectionUtility.appendList(CollectionUtility.getObject(headerMap, key), value)));
  }

  /**
   * Returns the HTTP status code.
   */
  public Integer getHttpStatusCode(final Map<String, Object> responseContext) {
    return (Integer) responseContext.get(MessageContext.HTTP_RESPONSE_CODE);
  }

  /**
   * Sets the given HTTP status code.
   */
  public void setHttpStatusCode(final MessageContext context, final int httpStatusCode) {
    context.put(MessageContext.HTTP_RESPONSE_CODE, httpStatusCode);
  }

  /**
   * Sets the given socket connect timeout [ms].
   */
  public void setSocketConnectTimeout(final Map<String, Object> requestContext, final int timeoutMillis) {
    LOG.warn("'socketConnectTimeout' not supported. To support this feature register a '%s' in 'config.properties' using property '%s'.", JaxWsImplementorSpecifics.class.getSimpleName(), BEANS.get(JaxWsImplementorProperty.class).getKey());
  }

  /**
   * Sets the given socket read timeout [ms].
   */
  public void setSocketReadTimeout(final Map<String, Object> requestContext, final int timeoutMillis) {
    LOG.warn("'socketReadTimeout' not supported. To support this feature register a '%s' in 'config.properties' using property '%s'.", JaxWsImplementorSpecifics.class.getSimpleName(), BEANS.get(JaxWsImplementorProperty.class).getKey());
  }

  /**
   * Closes the Socket for the given port.
   */
  public void closeSocket(final Object port, final String operation) {
    LOG.warn("'closeSocket' not supported. To support this feature register a '%s' in 'config.properties' using property '%s'.", JaxWsImplementorSpecifics.class.getSimpleName(), BEANS.get(JaxWsImplementorProperty.class).getKey());
  }
}
