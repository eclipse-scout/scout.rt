/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.jaxws.handler;

import java.util.Collections;
import java.util.Set;

import javax.xml.namespace.QName;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.handler.soap.SOAPHandler;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.CorrelationId;
import org.eclipse.scout.rt.server.jaxws.MessageContexts;
import org.eclipse.scout.rt.server.jaxws.implementor.JaxWsImplementorSpecifics;

/**
 * Handler used on web service consumers for adding the current contxt's correlation id as HTTP header. The header name
 * is {@link CorrelationId#HTTP_HEADER_NAME}.
 *
 * @since 5.2
 */
@ApplicationScoped
public class WsConsumerCorrelationIdHandler implements SOAPHandler<SOAPMessageContext> {

  @Override
  public boolean handleMessage(final SOAPMessageContext context) {
    if (MessageContexts.isOutboundMessage(context)) {
      final String cid = CorrelationId.CURRENT.get();
      if (cid != null) {
        writeCorrelationId(context, cid);
      }
    }
    return true;
  }

  @Override
  public boolean handleFault(final SOAPMessageContext context) {
    return true;
  }

  @Override
  public void close(final MessageContext context) {
    // NOP
  }

  @Override
  public Set<QName> getHeaders() {
    return Collections.emptySet();
  }

  /**
   * Writes the given correlation id to the message context. This implementation writes it to the HTTP header named
   * {@link CorrelationId#HTTP_HEADER_NAME}.
   */
  protected void writeCorrelationId(final SOAPMessageContext context, final String cid) {
    BEANS.get(JaxWsImplementorSpecifics.class).setHttpRequestHeader(context, CorrelationId.HTTP_HEADER_NAME, cid);
  }
}
