/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.jaxws;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.security.auth.Subject;
import javax.xml.soap.SOAPException;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;

/**
 * Utility methods to work with JAX-WS {@link MessageContext}.
 *
 * @since 5.1
 */
public final class MessageContexts {

  public static final String PROP_RUNCONTEXT = MessageContexts.class.getName() + ".RunContext";
  public static final String PROP_CORRELATION_ID = MessageContexts.class.getName() + ".CorrelationId";

  private MessageContexts() {
  }

  /**
   * Returns the current SOAP message from the given {@link SOAPMessageContext}.
   */
  public static String getSoapMessage(final SOAPMessageContext context) throws SOAPException, IOException {
    try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
      context.getMessage().writeTo(bos);
      return bos.toString(StandardCharsets.UTF_8.name());
    }
  }

  /**
   * @return <code>true</code> if the current message is an outbound-message, meaning that the request was already
   *         processed.
   */
  public static boolean isOutboundMessage(final MessageContext context) {
    return TypeCastUtility.castValue(context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY), boolean.class);
  }

  /**
   * @return <code>true</code> if the current message is an inbound-message, meaning that the request is not processed
   *         yet.
   */
  public static boolean isInboundMessage(final MessageContext context) {
    return !isOutboundMessage(context);
  }

  /**
   * Puts the given {@link RunContext} on {@link MessageContext} to be used in subsequent handlers and port type.
   */
  public static void putRunContext(final MessageContext context, final RunContext runContext) {
    if (runContext == null) {
      context.remove(PROP_RUNCONTEXT);
    }
    else {
      context.put(PROP_RUNCONTEXT, runContext);
      context.setScope(PROP_RUNCONTEXT, Scope.APPLICATION); // APPLICATION-SCOPE to be accessible in port type.
    }
  }

  /**
   * Returns the {@link RunContext} of the ongoing request, or <code>null</code> if not set.
   */
  public static RunContext getRunContext(final MessageContext messageContext) {
    final Object runContext = messageContext.get(PROP_RUNCONTEXT);
    if (runContext instanceof RunContext) {
      return (RunContext) runContext;
    }
    else if (runContext == null) {
      return null;
    }
    else {
      throw new WebServiceException(String.format("Invalid 'RunContext' on '%s' [actual=%s, expected=%s]", MessageContext.class.getName(), runContext.getClass().getName(), RunContext.class.getName()));
    }
  }

  /**
   * Returns the subject as declared in {@link RunContext} contained in {@link MessageContext}, or the default subject
   * if not set.
   */
  public static Subject getSubject(final MessageContext messageContext, final Subject defaultSubject) {
    final RunContext runContext = getRunContext(messageContext);
    if (runContext != null && runContext.getSubject() != null) {
      return runContext.getSubject();
    }
    else {
      return defaultSubject;
    }
  }

  /**
   * Puts the given correlation id on {@link MessageContext} to be used in subsequent handlers and port type.
   */
  public static void putCorrelationId(final MessageContext context, final String cid) {
    if (cid == null) {
      context.remove(PROP_CORRELATION_ID);
    }
    else {
      context.put(PROP_CORRELATION_ID, cid);
      context.setScope(PROP_CORRELATION_ID, Scope.APPLICATION); // APPLICATION-SCOPE to be accessible in port type.
    }
  }

  /**
   * Returns the correlation id of the ongoing request, or <code>null</code> if not set.
   */
  public static String getCorrelationId(final MessageContext context) {
    Object cid = context.get(PROP_CORRELATION_ID);
    if (cid instanceof String) {
      return (String) cid;
    }
    else if (cid == null) {
      return null;
    }
    else {
      throw new WebServiceException(String.format("Invalid 'CorrelationId' on '%s' [actual=%s, expected=%s]", MessageContext.class.getName(), cid.getClass().getName(), String.class.getName()));
    }
  }
}
