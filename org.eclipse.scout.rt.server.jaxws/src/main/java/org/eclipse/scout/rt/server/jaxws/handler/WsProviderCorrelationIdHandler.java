package org.eclipse.scout.rt.server.jaxws.handler;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.CorrelationId;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.server.jaxws.MessageContexts;
import org.eclipse.scout.rt.server.jaxws.implementor.JaxWsImplementorSpecifics;

/**
 * Handler used on web service providers for extracting the correlation id from the request parameter named
 * {@link CorrelationId#HTTP_HEADER_NAME}. A new correlation id is generated if the header is missing.
 * <p/>
 * <b>Note:</b> This handler should be put at the beginning of the handler chain.
 *
 * @since 5.2
 */
@ApplicationScoped
public class WsProviderCorrelationIdHandler implements SOAPHandler<SOAPMessageContext> {

  @Override
  public boolean handleMessage(final SOAPMessageContext context) {
    if (MessageContexts.isInboundMessage(context)) {
      String correlationId = readCorrelationId(context);
      if (StringUtility.isNullOrEmpty(correlationId)) {
        correlationId = BEANS.get(CorrelationId.class).newCorrelationId();
      }
      MessageContexts.putCorrelationId(context, correlationId);
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
   * Reads the correlation id on the given message context. This implementation reads the HTTP header named
   * {@link CorrelationId#HTTP_HEADER_NAME}.
   *
   * @return Returns the received correlation id or <code>null</code>, if none is set.
   */
  protected String readCorrelationId(final SOAPMessageContext context) {
    final List<String> cidHeader = BEANS.get(JaxWsImplementorSpecifics.class).getHttpRequestHeader(context, CorrelationId.HTTP_HEADER_NAME);
    return CollectionUtility.firstElement(cidHeader);
  }
}
