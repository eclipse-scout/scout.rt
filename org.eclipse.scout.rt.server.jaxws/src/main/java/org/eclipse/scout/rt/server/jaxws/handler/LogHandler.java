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
package org.eclipse.scout.rt.server.jaxws.handler;

import java.util.Collections;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.server.jaxws.JaxWsConfigProperties.JaxWsLogHandlerDebugProperty;
import org.eclipse.scout.rt.server.jaxws.MessageContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler to log SOAP messages.
 */
@ApplicationScoped
public class LogHandler implements SOAPHandler<SOAPMessageContext> {

  private static final Logger LOG = LoggerFactory.getLogger(LogHandler.class);

  private boolean m_logDebug;

  public LogHandler() {
    initConfig();
  }

  protected void initConfig() {
    setLogDebug(CONFIG.getPropertyValue(JaxWsLogHandlerDebugProperty.class).booleanValue());
  }

  @Override
  public final boolean handleMessage(final SOAPMessageContext context) {
    try {
      handleLogMessage(context);
    }
    catch (final Exception e) {
      LOG.error("Failed to log SOAP message", e);
    }
    return true;
  }

  @Override
  public final boolean handleFault(final SOAPMessageContext context) {
    try {
      handleLogMessage(context);
    }
    catch (final Exception e) {
      LOG.error("Failed to log SOAP message", e);
    }
    return true;
  }

  @Override
  public final Set<QName> getHeaders() {
    return Collections.emptySet();
  }

  @Override
  public final void close(final MessageContext messageContext) {
    // NOOP
  }

  protected void handleLogMessage(final SOAPMessageContext context) throws Exception {
    if (!LOG.isInfoEnabled() || m_logDebug && !LOG.isDebugEnabled()) {
      return;
    }

    final String soapMessage = MessageContexts.getSoapMessage(context);
    final String direction = (MessageContexts.isInboundMessage(context) ? "IN" : "OUT");
    final QName service = (QName) context.get(SOAPMessageContext.WSDL_SERVICE);
    final QName port = (QName) context.get(SOAPMessageContext.WSDL_PORT);
    final QName operation = (QName) context.get(SOAPMessageContext.WSDL_OPERATION);
    final String correlationId = MessageContexts.getCorrelationId(context);

    if (m_logDebug) {
      LOG.debug("WS SOAP [service={}, port={}, operation={}, direction={}, correlationId={}, message={}]", service, port, operation, direction, correlationId, soapMessage);
    }
    else {
      LOG.info("WS SOAP [service={}, port={}, operation={}, direction={}, correlationId={}, message={}]", service, port, operation, direction, correlationId, soapMessage);
    }
  }

  public void setLogDebug(final boolean logDebug) {
    m_logDebug = logDebug;
  }
}
