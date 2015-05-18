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

import java.io.Closeable;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.handler.MessageContext;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

/**
 * JAX-WS implementor specifics of 'JAX-WS CXF implementation'.
 *
 * @since 5.1
 */
public class JaxWsCxfSpecifics extends JaxWsImplementorSpecifics {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JaxWsCxfSpecifics.class);

  @Override
  public void setHttpResponseHeader(final MessageContext context, final String key, final String value) {
    final HttpServletResponse servletResponse = (HttpServletResponse) context.get(MessageContext.SERVLET_RESPONSE);
    servletResponse.setHeader(key, value);
  }

  @Override
  public void setHttpStatusCode(final MessageContext context, final int httpStatusCode) {
    context.put("org.apache.cxf.message.Message.RESPONSE_CODE", httpStatusCode);
  }

  @Override
  public Integer getHttpStatusCode(Map<String, Object> responseContext) {
    return (Integer) responseContext.get("org.apache.cxf.message.Message.RESPONSE_CODE");
  }

  @Override
  public void setSocketConnectTimeout(Map<String, Object> requestContext, int timeoutMillis) {
    if (timeoutMillis > 0) {
      requestContext.put("javax.xml.ws.client.connectionTimeout", timeoutMillis);
    }
    else {
      requestContext.remove("javax.xml.ws.client.connectionTimeout");
    }
  }

  @Override
  public void setSocketReadTimeout(Map<String, Object> requestContext, int timeoutMillis) {
    if (timeoutMillis > 0) {
      requestContext.put("javax.xml.ws.client.receiveTimeout", timeoutMillis);
    }
    else {
      requestContext.remove("javax.xml.ws.client.receiveTimeout");
    }
  }

  @Override
  public void closeSocket(Object port, String operation) {
    try {
      ((Closeable) port).close();
    }
    catch (final Throwable e) {
      LOG.error(String.format("Failed to close Socket for: %s", operation), e);
    }
  }
}
